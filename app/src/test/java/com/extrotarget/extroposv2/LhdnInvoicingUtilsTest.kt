package com.extrotarget.extroposv2

import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.util.lhdn.LhdnInvoicingUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class LhdnInvoicingUtilsTest {

    @Test
    fun `test groupItemsByTax handles BigDecimal correctly`() {
        val items = listOf(
            createSaleItem("ITEM-1", BigDecimal("6.00")),
            createSaleItem("ITEM-2", BigDecimal("6.00")),
            createSaleItem("ITEM-3", BigDecimal("0.00")),
            createSaleItem("ITEM-4", BigDecimal("8.00"))
        )

        val groups = LhdnInvoicingUtils.groupItemsByTax(items)

        assertEquals(3, groups.size)
        assertTrue(groups.containsKey(BigDecimal("6.00")))
        assertTrue(groups.containsKey(BigDecimal("0.00")))
        assertTrue(groups.containsKey(BigDecimal("8.00")))
        
        assertEquals(2, groups[BigDecimal("6.00")]?.size)
    }

    private fun createSaleItem(id: String, taxRate: BigDecimal): SaleItem {
        return SaleItem(
            id = id,
            saleId = "SALE-1",
            productId = "PROD-1",
            productName = "Product",
            quantity = BigDecimal.ONE,
            unitPrice = BigDecimal.TEN,
            taxRate = taxRate,
            taxAmount = BigDecimal.ZERO,
            totalAmount = BigDecimal.TEN
        )
    }
}
