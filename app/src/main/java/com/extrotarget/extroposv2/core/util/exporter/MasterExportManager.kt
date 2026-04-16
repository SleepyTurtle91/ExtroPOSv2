package com.extrotarget.extroposv2.core.util.exporter

import android.content.Context
import com.extrotarget.extroposv2.core.data.local.dao.CategoryDao
import com.extrotarget.extroposv2.core.data.local.dao.ProductDao
import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.local.dao.loyalty.LoyaltyDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.security.SecureRandom

@Singleton
class MasterExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productExportManager: ProductExportManager,
    private val sstReportManager: SstReportManager,
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val saleDao: SaleDao,
    private val loyaltyDao: LoyaltyDao
) {
    suspend fun exportAllData(outputStream: OutputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            ZipOutputStream(outputStream).use { zipOut ->
                // Export Products
                zipOut.putNextEntry(ZipEntry("products.csv"))
                productExportManager.exportToCsv(zipOut)
                zipOut.closeEntry()

                // Export Categories
                zipOut.putNextEntry(ZipEntry("categories.csv"))
                exportCategoriesToCsv(zipOut)
                zipOut.closeEntry()

                // Export Members
                zipOut.putNextEntry(ZipEntry("members.csv"))
                exportMembersToCsv(zipOut)
                zipOut.closeEntry()

                // Export SST Report (Last 90 days as default for master export)
                val endDate = System.currentTimeMillis()
                val startDate = endDate - (90L * 24 * 60 * 60 * 1000)
                zipOut.putNextEntry(ZipEntry("sst_report_90d.csv"))
                sstReportManager.generateSstCsvReport(startDate, endDate, zipOut)
                zipOut.closeEntry()

                // Add more exports as needed
            }
            Result.success(1)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun exportCategoriesToCsv(outputStream: OutputStream) {
        val categories = categoryDao.getAllCategories().first()
        val writer = BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8))
        writer.write("Id,Name,Description")
        writer.newLine()
        categories.forEach {
            writer.write("${it.id},${escapeCsv(it.name)},${escapeCsv(it.description ?: "")}")
            writer.newLine()
        }
        writer.flush()
    }

    private suspend fun exportMembersToCsv(outputStream: OutputStream) {
        val members = loyaltyDao.getAllMembers().first()
        val writer = BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8))
        writer.write("Id,Name,Phone,Email,Points,Tier,JoinDate")
        writer.newLine()
        members.forEach {
            writer.write("${it.id},${escapeCsv(it.name)},${it.phoneNumber},${escapeCsv(it.email ?: "")},${it.totalPoints},${it.tier},${it.joinDate}")
            writer.newLine()
        }
        writer.flush()
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }

    suspend fun exportEncryptedData(outputStream: OutputStream, password: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val baos = ByteArrayOutputStream()
            exportAllData(baos)
            val data = baos.toByteArray()

            val encryptedData = encrypt(data, password)
            outputStream.write(encryptedData)
            Result.success(1)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun encrypt(data: ByteArray, password: String): ByteArray {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        
        val encrypted = cipher.doFinal(data)
        
        // Combine salt + iv + encrypted data
        return salt + iv + encrypted
    }
}
