package com.extrotarget.extroposv2

import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.lhdn.BuyerInfo
import com.extrotarget.extroposv2.core.data.model.lhdn.LhdnConfig
import com.extrotarget.extroposv2.core.network.api.lhdn.InvoisMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class InvoisMapperTest {

    @Test
    fun `test mapping preserves bigdecimal precision`() {
        val sale = Sale(
            id = "SALE-123",
            timestamp = System.currentTimeMillis(),
            totalAmount = BigDecimal("100.05"),
            taxAmount = BigDecimal("6.00"),
            paymentMethod = "CASH"
        )
        val items = listOf(
            SaleItem(
                id = "ITEM-1",
                saleId = "SALE-123",
                productId = "PROD-1",
                productName = "Product 1",
                quantity = BigDecimal("1.00"),
                unitPrice = BigDecimal("94.05"),
                taxRate = BigDecimal("6.00"),
                taxAmount = BigDecimal("6.00"),
                totalAmount = BigDecimal("100.05")
            )
        )
        val config = LhdnConfig(
            sellerTin = "TIN123",
            sellerBrn = "BRN123",
            msicCode = "12345",
            businessActivityDesc = "Retail"
        )

        val doc = InvoisMapper.mapToDocument(sale, items, config)

        // Verify top level amounts are BigDecimal
        assertTrue(doc["totalPayableAmount"] is BigDecimal)
        assertEquals(BigDecimal("100.05"), doc["totalPayableAmount"])
        
        // Verify item level amounts are BigDecimal
        val docItems = doc["documentItems"] as List<Map<String, Any>>
        val item1 = docItems[0]
        assertTrue(item1["unitPrice"] is BigDecimal)
        assertEquals(BigDecimal("94.05"), item1["unitPrice"])
        assertEquals(BigDecimal("1.00"), item1["quantity"])
    }

    @Test
    fun `test zero tax mapping`() {
        val sale = Sale(
            id = "SALE-124",
            totalAmount = BigDecimal("50.00"),
            paymentMethod = "CASH"
        )
        val items = listOf(
            SaleItem(
                id = "ITEM-2",
                saleId = "SALE-124",
                productId = "PROD-2",
                productName = "Product 2",
                quantity = BigDecimal("1.00"),
                unitPrice = BigDecimal("50.00"),
                taxRate = BigDecimal.ZERO,
                taxAmount = BigDecimal.ZERO,
                totalAmount = BigDecimal("50.00")
            )
        )
        val config = LhdnConfig(sellerTin = "TIN", sellerBrn = "BRN", msicCode = "123", businessActivityDesc = "X")

        val doc = InvoisMapper.mapToDocument(sale, items, config)
        val docItems = doc["documentItems"] as List<Map<String, Any>>
        assertEquals("Z", docItems[0]["taxCategory"])
    }
}
