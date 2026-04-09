package com.extrotarget.extroposv2.ui.inventory.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.Product
import java.math.BigDecimal
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    product: Product? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "0.00") }
    var taxRate by remember { mutableStateOf(product?.taxRate?.toString() ?: "0.00") }
    var minStockLevel by remember { mutableStateOf(product?.minStockLevel?.toString() ?: "0") }
    var printerTag by remember { mutableStateOf(product?.printerTag ?: "KITCHEN") }
    var commissionRate by remember { mutableStateOf(product?.commissionRate?.toString() ?: "0.00") }
    var fixedCommission by remember { mutableStateOf(product?.fixedCommission?.toString() ?: "0.00") }
    var selectedCategoryId by remember { mutableStateOf(product?.categoryId ?: categories.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") }, modifier = Modifier.fillMaxWidth())
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (RM)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = taxRate, onValueChange = { taxRate = it }, label = { Text("SST %") }, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minStockLevel, onValueChange = { minStockLevel = it }, label = { Text("Min Stock Alert") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = printerTag, onValueChange = { printerTag = it }, label = { Text("Printer Tag") }, modifier = Modifier.weight(1f))
                }

                Text("Commission (Car Wash / Service)", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = commissionRate, onValueChange = { commissionRate = it }, label = { Text("Rate %") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = fixedCommission, onValueChange = { fixedCommission = it }, label = { Text("Fixed (RM)") }, modifier = Modifier.weight(1f))
                }

                Text("Category", style = MaterialTheme.typography.labelMedium)
                categories.forEach { category ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = selectedCategoryId == category.id, onClick = { selectedCategoryId = category.id })
                        Text(category.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalProduct = Product(
                        id = product?.id ?: UUID.randomUUID().toString(),
                        name = name,
                        sku = sku,
                        barcode = barcode.ifBlank { null },
                        price = price.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        taxRate = taxRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        stockQuantity = product?.stockQuantity ?: BigDecimal.ZERO,
                        minStockLevel = minStockLevel.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        printerTag = printerTag,
                        commissionRate = commissionRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        fixedCommission = fixedCommission.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        categoryId = selectedCategoryId.ifBlank { null }
                    )
                    onConfirm(finalProduct)
                },
                enabled = name.isNotBlank() && sku.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
