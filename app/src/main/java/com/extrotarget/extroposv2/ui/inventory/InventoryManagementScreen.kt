package com.extrotarget.extroposv2.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.extrotarget.extroposv2.core.data.model.Category
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.Modifier as PosModifier
import com.extrotarget.extroposv2.core.data.model.ModifierTargetType
import com.extrotarget.extroposv2.ui.inventory.components.AddEditProductDialog
import com.extrotarget.extroposv2.ui.inventory.viewmodel.InventoryViewModel
import java.math.BigDecimal
import java.util.UUID

@Composable
fun InventoryManagementScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    var activeTab by remember { mutableStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()
    val modifiers by viewModel.modifiers.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    "Catalog Builder",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )
                Text(
                    "Manage your menus, products, and available modifiers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
                
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search categories, products or modifiers...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = Color(0xFF3B82F6)
                        )
                    }
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Categories", fontWeight = if(activeTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Products", fontWeight = if(activeTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Modifiers", fontWeight = if(activeTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            when (activeTab) {
                0 -> CategoryListTab(
                    categories = uiState.categories.filter { it.name.contains(uiState.searchQuery, ignoreCase = true) },
                    allModifiers = modifiers,
                    viewModel = viewModel
                )
                1 -> ProductListTab(
                    products = uiState.filteredProducts,
                    categories = uiState.categories,
                    modifiers = modifiers,
                    viewModel = viewModel
                )
                2 -> ModifierListTab(
                    modifiers = modifiers.filter { it.name.contains(uiState.searchQuery, ignoreCase = true) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun CategoryListTab(
    categories: List<Category>,
    allModifiers: List<PosModifier>,
    viewModel: InventoryViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var showLinkDialog by remember { mutableStateOf<Category?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = if (category.isAvailable) Color(0xFF6366F1) else Color.Gray
                            )
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                category.name,
                                fontWeight = FontWeight.Bold,
                                color = if (category.isAvailable) Color(0xFF1E293B) else Color.Gray
                            )
                            if (!category.description.isNullOrBlank()) {
                                Text(category.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        IconButton(onClick = { showLinkDialog = category }) {
                            Icon(Icons.Default.Link, contentDescription = "Bind Modifiers", tint = Color(0xFF3B82F6))
                        }

                        IconButton(onClick = { editingCategory = category }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }

                        Switch(
                            checked = category.isAvailable,
                            onCheckedChange = { viewModel.updateCategory(category.copy(isAvailable = it)) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp),
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White
        ) { Icon(Icons.Default.Add, contentDescription = "Add Category") }
    }

    if (showAddDialog || editingCategory != null) {
        val cat = editingCategory
        var name by remember { mutableStateOf(cat?.name ?: "") }
        var description by remember { mutableStateOf(cat?.description ?: "") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false; editingCategory = null },
            title = { Text(if (cat == null) "Add Category" else "Edit Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Category Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (Optional)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (cat == null) {
                        viewModel.addCategory(name, description)
                    } else {
                        viewModel.updateCategory(cat.copy(name = name, description = description))
                    }
                    showAddDialog = false
                    editingCategory = null
                }) { Text("SAVE") }
            }
        )
    }

    if (showLinkDialog != null) {
        ModifierBindingDialog(
            targetId = showLinkDialog!!.id,
            targetName = showLinkDialog!!.name,
            targetType = ModifierTargetType.CATEGORY,
            allModifiers = allModifiers,
            viewModel = viewModel,
            onDismiss = { showLinkDialog = null }
        )
    }
}

@Composable
fun ProductListTab(
    products: List<Product>,
    categories: List<Category>,
    modifiers: List<PosModifier>,
    viewModel: InventoryViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showLinkDialog by remember { mutableStateOf<Product?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                product.name,
                                fontWeight = FontWeight.Bold,
                                color = if (product.isAvailable) Color(0xFF1E293B) else Color.Gray
                            )
                            Text(
                                "SKU: ${product.sku} | RM ${product.price}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            val catName = categories.find { it.id == product.categoryId }?.name ?: "No Category"
                            Text("Category: $catName", style = MaterialTheme.typography.labelSmall, color = Color(0xFF3B82F6))
                        }

                        IconButton(onClick = { showLinkDialog = product }) {
                            Icon(Icons.Default.Link, contentDescription = "Bind Modifiers", tint = Color(0xFF3B82F6))
                        }

                        IconButton(onClick = { editingProduct = product }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }

                        Switch(
                            checked = product.isAvailable,
                            onCheckedChange = { viewModel.toggleProductAvailability(product) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp),
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White
        ) { Icon(Icons.Default.Add, contentDescription = "Add Product") }
    }

    if (showAddDialog || editingProduct != null) {
        AddEditProductDialog(
            product = editingProduct,
            categories = categories,
            onDismiss = { showAddDialog = false; editingProduct = null },
            onConfirm = {
                viewModel.upsertProduct(it)
                showAddDialog = false
                editingProduct = null
            }
        )
    }

    if (showLinkDialog != null) {
        ModifierBindingDialog(
            targetId = showLinkDialog!!.id,
            targetName = showLinkDialog!!.name,
            targetType = ModifierTargetType.PRODUCT,
            allModifiers = modifiers,
            viewModel = viewModel,
            onDismiss = { showLinkDialog = null }
        )
    }
}

@Composable
fun ModifierListTab(
    modifiers: List<PosModifier>,
    viewModel: InventoryViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(modifiers) { modifier ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                modifier.name,
                                fontWeight = FontWeight.Bold,
                                color = if (modifier.isAvailable) Color(0xFF1E293B) else Color.Gray
                            )
                            if (modifier.priceAdjustment > BigDecimal.ZERO) {
                                Text("+ RM ${modifier.priceAdjustment}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF59E0B))
                            }
                        }
                        Switch(
                            checked = modifier.isAvailable,
                            onCheckedChange = { viewModel.toggleModifierAvailability(modifier) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp),
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White
        ) { Icon(Icons.Default.Add, contentDescription = "Add Modifier") }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Modifier") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Modifier Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Extra Charge (RM)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addModifier(name, price.toBigDecimalOrNull() ?: BigDecimal.ZERO)
                    name = ""; price = ""; showAddDialog = false
                }) { Text("SAVE") }
            }
        )
    }
}

@Composable
fun ModifierBindingDialog(
    targetId: String,
    targetName: String,
    targetType: ModifierTargetType,
    allModifiers: List<PosModifier>,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val selectedIds = remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(targetId) {
        selectedIds.value = viewModel.getSelectedModifierIds(targetId, targetType).toSet()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifiers for $targetName", fontWeight = FontWeight.Black) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(allModifiers) { modifier ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedIds.value = if (selectedIds.value.contains(modifier.id)) selectedIds.value - modifier.id else selectedIds.value + modifier.id
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = selectedIds.value.contains(modifier.id), onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text(modifier.name)
                        if (modifier.priceAdjustment > BigDecimal.ZERO) {
                            Text(" (+RM ${modifier.priceAdjustment})", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (targetType == ModifierTargetType.PRODUCT) {
                    viewModel.linkModifiersToProduct(targetId, selectedIds.value.toList())
                } else {
                    viewModel.linkModifiersToCategory(targetId, selectedIds.value.toList())
                }
                onDismiss()
            }) { Text("SAVE BINDING") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
