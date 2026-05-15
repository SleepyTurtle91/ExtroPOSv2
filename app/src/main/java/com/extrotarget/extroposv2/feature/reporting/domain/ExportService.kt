package com.extrotarget.extroposv2.feature.reporting.domain

import com.extrotarget.extroposv2.core.domain.model.reporting.TaxBreakdownItem
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportService @Inject constructor() {

    fun exportTaxComplianceToCsv(outputStream: OutputStream, data: List<TaxBreakdownItem>) {
        csvWriter().open(outputStream) {
            writeRow(listOf("Tax Rate (%)", "Net Sales", "Tax Collected"))
            data.forEach { item ->
                writeRow(listOf(
                    item.taxRate.toString(),
                    item.netSales.toString(),
                    item.taxAmount.toString()
                ))
            }
        }
    }
}
