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
            writer.write("Date,Sale ID,Table,Payment Method,Subtotal,Discount,Service Charge,Tax Amount,Rounding,Total Amount")
            writer.newLine()
            
            var count = 0
            var totalSubtotal = BigDecimal.ZERO
            var totalDiscount = BigDecimal.ZERO
            var totalServiceCharge = BigDecimal.ZERO
            var totalTax = BigDecimal.ZERO
            var totalRounding = BigDecimal.ZERO
            var totalGross = BigDecimal.ZERO
            val taxBreakdown = mutableMapOf<BigDecimal, BigDecimal>() // Rate -> Tax Amount

            salesWithItems.forEach { saleWithItems ->
                val sale = saleWithItems.sale
                val items = saleWithItems.items
                
                // Accumulate totals
                totalSubtotal = totalSubtotal.add(sale.subtotal)
                totalDiscount = totalDiscount.add(sale.discountAmount)
                totalServiceCharge = totalServiceCharge.add(sale.serviceChargeAmount)
                totalTax = totalTax.add(sale.taxAmount)
                totalRounding = totalRounding.add(sale.roundingAdjustment)
                totalGross = totalGross.add(sale.totalAmount)

                // Accumulate tax breakdown
                items.forEach { item ->
                    val currentTax = taxBreakdown.getOrDefault(item.taxRate, BigDecimal.ZERO)
                    taxBreakdown[item.taxRate] = currentTax.add(item.taxAmount)
                }

                val line = StringBuilder()
                line.append(dateFormat.format(Date(sale.timestamp))).append(",")
                line.append(sale.id).append(",")
                line.append(sale.tableId ?: "").append(",")
                line.append(sale.paymentMethod).append(",")
                line.append(sale.subtotal.toPlainString()).append(",")
                line.append(sale.discountAmount.toPlainString()).append(",")
                line.append(sale.serviceChargeAmount.toPlainString()).append(",")
                line.append(sale.taxAmount.toPlainString()).append(",")
                line.append(sale.roundingAdjustment.toPlainString()).append(",")
                line.append(sale.totalAmount.toPlainString())
                
                writer.write(line.toString())
                writer.newLine()
                count++
            }
            
            // Summary Section
            writer.newLine()
            writer.write("SUMMARY BY TAX RATE")
            writer.newLine()
            writer.write("Tax Rate,Tax Amount")
            writer.newLine()
            taxBreakdown.forEach { (rate, amount) ->
                writer.write("${rate.toPlainString()}%,${amount.toPlainString()}")
                writer.newLine()
            }

            writer.newLine()
            val summary = StringBuilder()
            summary.append("TOTAL,,,")
            summary.append(",").append(totalSubtotal.toPlainString())
            summary.append(",").append(totalDiscount.toPlainString())
            summary.append(",").append(totalServiceCharge.toPlainString())
            summary.append(",").append(totalTax.toPlainString())
            summary.append(",").append(totalRounding.toPlainString())
            summary.append(",").append(totalGross.toPlainString())
            writer.write(summary.toString())
            
            writer.flush()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
