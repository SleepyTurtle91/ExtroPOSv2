package com.extrotarget.extroposv2.core.util.exporter

import com.extrotarget.extroposv2.core.data.local.dao.SaleDao
import com.extrotarget.extroposv2.core.data.model.Sale
import kotlinx.coroutines.flow.first
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SstReportManager @Inject constructor(
    private val saleDao: SaleDao
) {
    suspend fun generateSstCsvReport(startDate: Long, endDate: Long, outputStream: OutputStream): Result<Int> {
        return try {
            val salesWithItems = saleDao.getSalesWithItemsInRange(startDate, endDate).first()
            val writer = BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8))
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            // Header
            writer.write("Date,Sale ID,Payment Method,Net Amount,Rounding,Tax Amount (6%),Tax Amount (8%),Tax Amount (10%),Total Amount")
            writer.newLine()
            
            var count = 0
            var totalNet = BigDecimal.ZERO
            var totalRounding = BigDecimal.ZERO
            var totalTax6 = BigDecimal.ZERO
            var totalTax8 = BigDecimal.ZERO
            var totalTax10 = BigDecimal.ZERO
            var totalGross = BigDecimal.ZERO

            salesWithItems.forEach { saleWithItems ->
                val sale = saleWithItems.sale
                val items = saleWithItems.items
                
                // Group tax by rate
                val tax6 = items.filter { it.taxRate.compareTo(BigDecimal("6.00")) == 0 }
                    .fold(BigDecimal.ZERO) { acc, item -> acc.add(item.taxAmount) }
                val tax8 = items.filter { it.taxRate.compareTo(BigDecimal("8.00")) == 0 }
                    .fold(BigDecimal.ZERO) { acc, item -> acc.add(item.taxAmount) }
                val tax10 = items.filter { it.taxRate.compareTo(BigDecimal("10.00")) == 0 }
                    .fold(BigDecimal.ZERO) { acc, item -> acc.add(item.taxAmount) }
                
                val netAmount = sale.totalAmount.subtract(sale.taxAmount).subtract(sale.roundingAdjustment)

                // Accumulate totals
                totalNet = totalNet.add(netAmount)
                totalRounding = totalRounding.add(sale.roundingAdjustment)
                totalTax6 = totalTax6.add(tax6)
                totalTax8 = totalTax8.add(tax8)
                totalTax10 = totalTax10.add(tax10)
                totalGross = totalGross.add(sale.totalAmount)

                val line = StringBuilder()
                line.append(dateFormat.format(Date(sale.timestamp))).append(",")
                line.append(sale.id).append(",")
                line.append(sale.paymentMethod).append(",")
                line.append(netAmount.toPlainString()).append(",")
                line.append(sale.roundingAdjustment.toPlainString()).append(",")
                line.append(tax6.toPlainString()).append(",")
                line.append(tax8.toPlainString()).append(",")
                line.append(tax10.toPlainString()).append(",")
                line.append(sale.totalAmount.toPlainString())
                
                writer.write(line.toString())
                writer.newLine()
                count++
            }
            
            // Summary Row
            writer.newLine()
            val summary = StringBuilder()
            summary.append("TOTAL,,,")
            summary.append(totalNet.toPlainString()).append(",")
            summary.append(totalRounding.toPlainString()).append(",")
            summary.append(totalTax6.toPlainString()).append(",")
            summary.append(totalTax8.toPlainString()).append(",")
            summary.append(totalTax10.toPlainString()).append(",")
            summary.append(totalGross.toPlainString())
            writer.write(summary.toString())
            
            writer.flush()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
