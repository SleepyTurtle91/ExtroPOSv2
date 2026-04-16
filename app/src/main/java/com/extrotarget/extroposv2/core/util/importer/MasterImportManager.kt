package com.extrotarget.extroposv2.core.util.importer

import android.content.Context
import com.extrotarget.extroposv2.core.data.local.AppDatabase
import com.extrotarget.extroposv2.core.data.local.dao.CategoryDao
import com.extrotarget.extroposv2.core.data.local.dao.loyalty.LoyaltyDao
import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.loyalty.Member
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec

@Singleton
class MasterImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productImportManager: ProductImportManager,
    private val categoryDao: CategoryDao,
    private val loyaltyDao: LoyaltyDao,
    private val db: AppDatabase
) {
    suspend fun importFromZip(inputStream: InputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var totalImported = 0
            ZipInputStream(inputStream).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "products.csv" -> {
                            val result = productImportManager.importFromCsv(zipIn)
                            if (result.isSuccess) {
                                totalImported += result.getOrDefault(0)
                            }
                        }
                        "categories.csv" -> {
                            importCategoriesFromCsv(zipIn)
                        }
                        "members.csv" -> {
                            importMembersFromCsv(zipIn)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
            Result.success(totalImported)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun importCategoriesFromCsv(inputStream: InputStream) {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        reader.readLine() // Skip header
        var line: String? = reader.readLine()
        while (line != null) {
            val parts = parseCsvLine(line)
            if (parts.size >= 2) {
                val category = Category(
                    id = parts[0],
                    name = parts[1],
                    description = parts.getOrNull(2)
                )
                categoryDao.insertCategory(category)
            }
            line = reader.readLine()
        }
    }

    private suspend fun importMembersFromCsv(inputStream: InputStream) {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        reader.readLine() // Skip header
        var line: String? = reader.readLine()
        while (line != null) {
            val parts = parseCsvLine(line)
            if (parts.size >= 3) {
                val member = Member(
                    id = parts[0],
                    name = parts[1],
                    phoneNumber = parts[2],
                    email = parts.getOrNull(3),
                    totalPoints = parts.getOrNull(4)?.toBigDecimal() ?: BigDecimal.ZERO,
                    tier = parts.getOrNull(5) ?: "BRONZE",
                    joinDate = parts.getOrNull(6)?.toLong() ?: System.currentTimeMillis()
                )
                loyaltyDao.insertMember(member)
            }
            line = reader.readLine()
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var curVal = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < line.length && line[i + 1] == '\"') {
                        curVal.append('\"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    curVal.append(ch)
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true
                } else if (ch == ',') {
                    result.add(curVal.toString().trim())
                    curVal = StringBuilder()
                } else {
                    curVal.append(ch)
                }
            }
            i++
        }
        result.add(curVal.toString().trim())
        return result
    }

    suspend fun importEncryptedData(inputStream: InputStream, password: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val encryptedData = inputStream.readBytes()
            val decryptedData = decrypt(encryptedData, password)
            importFromZip(ByteArrayInputStream(decryptedData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decrypt(data: ByteArray, password: String): ByteArray {
        val salt = data.sliceArray(0 until 16)
        val iv = data.sliceArray(16 until 32)
        val encrypted = data.sliceArray(32 until data.size)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        
        return cipher.doFinal(encrypted)
    }
}
