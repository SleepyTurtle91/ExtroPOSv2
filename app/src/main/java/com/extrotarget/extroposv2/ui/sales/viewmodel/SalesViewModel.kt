package com.extrotarget.extroposv2.ui.sales.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.carwash.CommissionRecord
import com.extrotarget.extroposv2.core.data.model.lhdn.BuyerInfo
import com.extrotarget.extroposv2.core.data.model.loyalty.Member
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import com.extrotarget.extroposv2.core.data.repository.fnb.TableRepository
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import com.extrotarget.extroposv2.core.data.repository.settings.DuitNowRepository
import com.extrotarget.extroposv2.core.domain.usecase.CartUseCase
import com.extrotarget.extroposv2.core.domain.usecase.PrintReceiptUseCase
import com.extrotarget.extroposv2.core.domain.usecase.ProcessSaleUseCase
import com.extrotarget.extroposv2.core.util.audit.AuditManager
import com.extrotarget.extroposv2.R
import com.extrotarget.extroposv2.core.data.model.SaleWithItems
import com.extrotarget.extroposv2.ui.sales.AdminAuthAction
import com.extrotarget.extroposv2.ui.sales.BusinessMode
import com.extrotarget.extroposv2.ui.sales.CartItem
import com.extrotarget.extroposv2.ui.sales.SalesUiState
import com.extrotarget.extroposv2.core.data.repository.settings.SettingsRepository
import com.extrotarget.extroposv2.core.network.SyncClient
import com.extrotarget.extroposv2.core.data.repository.hardware.TerminalRepository
import com.extrotarget.extroposv2.core.data.repository.ShiftRepository
import com.extrotarget.extroposv2.core.hardware.terminal.TerminalResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val saleRepository: SaleRepository,
    private val staffRepository: StaffRepository,
    private val tableRepository: TableRepository,
    private val auditManager: AuditManager,
    private val lhdnRepository: LhdnRepository,
    private val terminalRepository: TerminalRepository,
    private val duitNowRepository: DuitNowRepository,
    private val securityManager: com.extrotarget.extroposv2.core.util.security.SecurityManager,
    private val syncClient: SyncClient,
    private val branchSyncManager: com.extrotarget.extroposv2.core.network.BranchSyncManager,
    private val processSaleUseCase: ProcessSaleUseCase,
    private val printReceiptUseCase: PrintReceiptUseCase,
    private val cartUseCase: CartUseCase,
    private val loyaltyRepository: com.extrotarget.extroposv2.core.data.repository.loyalty.LoyaltyRepository,
    private val settingsRepository: SettingsRepository,
    private val taxRepository: com.extrotarget.extroposv2.core.data.repository.settings.TaxRepository,
    private val shiftRepository: ShiftRepository,
    private val modifierRepository: com.extrotarget.extroposv2.core.data.repository.fnb.ModifierRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        viewModelScope.launch {
            staffRepository.getAllActiveStaff().collect { staff ->
                _uiState.update { it.copy(staffList = staff) }
            }
        }
        viewModelScope.launch {
            tableRepository.allTables.collect { tables ->
                _uiState.update { it.copy(tables = tables) }
            }
        }
        viewModelScope.launch {
            syncClient.syncStatus.collect { syncStatus ->
                _uiState.update { it.copy(syncStatus = syncStatus) }
            }
        }
        viewModelScope.launch {
            settingsRepository.activeBusinessMode.collect { activeMode ->
                _uiState.update { it.copy(activeMode = activeMode) }
            }
        }
        viewModelScope.launch {
            taxRepository.getTaxConfig().collect { taxConfig ->
                _uiState.update { it.copy(taxConfig = taxConfig) }
            }
        }
        viewModelScope.launch {
            loyaltyRepository.getConfig().collect { config ->
                _uiState.update { it.copy(loyaltyConfig = config) }
            }
        }
        viewModelScope.launch {
            shiftRepository.getActiveShift().collect { shift ->
                if (shift == null) {
                    _uiState.update { it.copy(isLocked = true, terminalStatus = "SHIFT_CLOSED") }
                }
            }
        }
    }

    fun unlock(pin: String) {
        viewModelScope.launch {
            val staff = staffRepository.getStaffByPin(pin)
            if (staff != null) {
                _uiState.update { it.copy(isLocked = false, adminAuthError = null) }
                auditManager.logAction("LOGIN", "Staff ${staff.name} logged in", "AUTH")
            } else {
                _uiState.update { it.copy(adminAuthError = context.getString(R.string.invalid_pin)) }
            }
        }
    }

    fun lock() {
        _uiState.update { it.copy(isLocked = true) }
    }

    fun setActiveTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearCartWithConfirm() {
        if (_uiState.value.cartItems.isNotEmpty()) {
            _uiState.update { it.copy(showConfirmClearCart = true) }
        }
    }

    fun executeClearCart() {
        _uiState.update { it.copy(cartItems = emptyList(), showConfirmClearCart = false, selectedTable = null) }
        viewModelScope.launch {
            auditManager.logAction("CLEAR_CART", "Cart cleared by user", "SALES")
        }
    }

    fun cancelClearCart() {
        _uiState.update { it.copy(showConfirmClearCart = false) }
    }

    fun setBusinessMode(mode: BusinessMode) {
        viewModelScope.launch {
            settingsRepository.updateBusinessMode(mode)
            _uiState.update { 
                it.copy(
                    cartItems = emptyList(), 
                    selectedCategoryId = null,
                    selectedTable = null,
                    activeTab = "pos"
                )
            }
            auditManager.logAction("SETTINGS", "Changed business mode to ${mode.displayName}", "SYSTEM")
        }
    }

    fun toggleSettingsModal(show: Boolean) {
        _uiState.update { it.copy(showSettingsModal = show) }
    }

    fun selectTable(table: com.extrotarget.extroposv2.core.data.model.fnb.Table) {
        _uiState.update { it.copy(selectedTable = table) }
        if (table.status == com.extrotarget.extroposv2.core.data.model.fnb.TableStatus.OCCUPIED || 
            table.status == com.extrotarget.extroposv2.core.data.model.fnb.TableStatus.BILLING) {
            loadPendingSaleForTable(table.id)
        } else {
            _uiState.update { it.copy(cartItems = emptyList()) }
        }
    }

    private fun loadPendingSaleForTable(tableId: String) {
        viewModelScope.launch {
            saleRepository.getPendingSaleWithItemsForTable(tableId).collect { saleWithItems ->
                if (saleWithItems != null) {
                    val cartItems = saleWithItems.items.map { item ->
                        CartItem(
                            id = item.id,
                            product = productRepository.getProductById(item.productId) ?: Product(
                                id = item.productId,
                                name = item.productName,
                                sku = "",
                                barcode = null,
                                price = item.unitPrice,
                                taxRate = item.taxRate,
                                stockQuantity = BigDecimal.ZERO,
                                categoryId = ""
                            ),
                            quantity = item.quantity,
                            unitPrice = item.unitPrice,
                            taxRate = item.taxRate,
                            assignedStaffId = item.assignedStaffId,
                            assignedStaffName = item.assignedStaffName,
                            selectedModifiers = emptyList(), // Need to load modifiers from DB if persistent
                            discount = item.discountLabel?.let { label -> 
                                com.extrotarget.extroposv2.ui.sales.Discount(
                                    type = com.extrotarget.extroposv2.ui.sales.DiscountType.FIXED,
                                    value = item.discountAmount,
                                    label = label
                                )
                            },
                            isSentToKitchen = item.status == "SENT"
                        )
                    }
                    _uiState.update { it.copy(cartItems = cartItems) }
                } else {
                    _uiState.update { it.copy(cartItems = emptyList()) }
                }
            }
        }
    }

    fun sendToKitchen() {
        val currentState = _uiState.value
        val table = currentState.selectedTable ?: return
        if (currentState.cartItems.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isCheckingOut = true) }
                
                // If the table was already occupied, it should have a currentSaleId
                val saleId = table.currentSaleId ?: currentState.lastSaleId ?: UUID.randomUUID().toString()
                
                val sale = com.extrotarget.extroposv2.core.data.model.Sale(
                    id = saleId,
                    subtotal = currentState.subtotal,
                    totalAmount = currentState.totalAmount,
                    taxAmount = currentState.totalTax,
                    serviceChargeAmount = currentState.totalServiceCharge,
                    discountAmount = currentState.totalDiscount,
                    roundingAdjustment = currentState.roundingAdjustment,
                    paymentMethod = "PENDING",
                    status = "PENDING",
                    tableId = table.id,
                    timestamp = System.currentTimeMillis()
                )

                val allSaleItems = currentState.cartItems.map { cartItem ->
                    val scAmount = currentState.taxConfig?.let { cartItem.calculateServiceCharge(it.serviceChargeRate) } ?: BigDecimal.ZERO
                    com.extrotarget.extroposv2.core.data.model.SaleItem(
                        id = cartItem.id,
                        saleId = saleId,
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.unitPrice,
                        taxRate = cartItem.taxRate,
                        taxAmount = cartItem.taxAmount,
                        serviceChargeRate = currentState.taxConfig?.serviceChargeRate ?: BigDecimal.ZERO,
                        serviceChargeAmount = scAmount,
                        discountAmount = cartItem.discountAmount,
                        discountLabel = cartItem.discount?.label,
                        totalAmount = cartItem.totalPrice.add(cartItem.taxAmount).add(scAmount),
                        modifiers = cartItem.selectedModifiers.joinToString(", ") { "${it.name}${if (it.priceAdjustment > BigDecimal.ZERO) " (+RM${it.priceAdjustment})" else ""}" }.takeIf { it.isNotEmpty() },
                        printerTag = cartItem.product.printerTag ?: "KITCHEN",
                        status = "SENT"
                    )
                }

                saleRepository.completeSale(sale, allSaleItems)

                val unsentItems = currentState.cartItems.filter { !it.isSentToKitchen }
                if (unsentItems.isNotEmpty()) {
                    val itemsToPrint = allSaleItems.filter { item -> 
                        unsentItems.any { it.id == item.id }
                    }
                    try {
                        // Batch print by printer tag to avoid multiple connections if possible, 
                        // though PrintReceiptUseCase handles grouping.
                        printReceiptUseCase.printOrderSlip(saleId, table.name, itemsToPrint)
                    } catch (e: Exception) {
                        android.util.Log.e("SalesViewModel", "Kitchen printing failed", e)
                    }
                }

                tableRepository.updateTable(table.copy(
                    status = com.extrotarget.extroposv2.core.data.model.fnb.TableStatus.OCCUPIED, 
                    currentSaleId = saleId,
                    hasUnsentItems = false
                ))

                // After saving, we stay on the table but mark items as sent
                _uiState.update { 
                    it.copy(
                        isCheckingOut = false, 
                        cartItems = currentState.cartItems.map { it.copy(isSentToKitchen = true) },
                        lastSaleId = saleId 
                    ) 
                }
                
                auditManager.logAction("SAVE_TABLE", "Saved order to ${table.name}", "SALES")
            } catch (e: Exception) {
                _uiState.update { it.copy(isCheckingOut = false, terminalStatus = "Error saving: ${e.message}") }
            }
        }
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val product = productRepository.getProductByBarcode(barcode)
            product?.let { addToCart(it) }
        }
    }

    fun addToCart(product: Product) {
        if (product.isWeightBased) {
            _uiState.update { it.copy(showWeightInput = true, productAwaitingWeight = product) }
            return
        }

        if (product.commissionRate > BigDecimal.ZERO || product.fixedCommission > BigDecimal.ZERO) {
            _uiState.update { it.copy(showStaffSelection = true, itemAwaitingStaff = CartItem(
                product = product,
                quantity = BigDecimal.ONE,
                unitPrice = product.price,
                taxRate = product.taxRate
            )) }
            return
        }
        
        _uiState.update { state ->
            val updatedItems = cartUseCase.addItem(state.cartItems, product)
            
            state.selectedTable?.let { table ->
                viewModelScope.launch {
                    tableRepository.updateTable(table.copy(hasUnsentItems = true, status = com.extrotarget.extroposv2.core.data.model.fnb.TableStatus.OCCUPIED))
                }
            }
            
            state.copy(
                cartItems = updatedItems, 
                selectedTable = state.selectedTable?.copy(
                    hasUnsentItems = true, 
                    status = com.extrotarget.extroposv2.core.data.model.fnb.TableStatus.OCCUPIED
                )
            )
        }
    }

    fun saveOrder() {
        sendToKitchen()
    }

    fun addWeightBasedItem(weight: BigDecimal) {
        val product = _uiState.value.productAwaitingWeight ?: return
        _uiState.update { state ->
            val updatedItems = cartUseCase.addWeightBasedItem(state.cartItems, product, weight)
            state.copy(cartItems = updatedItems, showWeightInput = false, productAwaitingWeight = null)
        }
    }

    fun cancelWeightInput() {
        _uiState.update { it.copy(showWeightInput = false, productAwaitingWeight = null) }
    }

    fun assignStaffToItem(staff: com.extrotarget.extroposv2.core.data.model.carwash.Staff) {
        val item = _uiState.value.itemAwaitingStaff ?: return
        _uiState.update { state ->
            val updatedItems = cartUseCase.addItem(state.cartItems, item.product, staff.id, staff.name)
            state.copy(cartItems = updatedItems, showStaffSelection = false, itemAwaitingStaff = null)
        }
    }

    fun cancelStaffSelection() {
        _uiState.update { it.copy(showStaffSelection = false, itemAwaitingStaff = null) }
    }

    fun removeFromCart(cartItem: CartItem) {
        if (!staffRepository.isCurrentUserAdmin()) {
            _uiState.update { it.copy(showAdminAuthDialog = true, adminAuthAction = AdminAuthAction.RemoveItem(cartItem)) }
            return
        }
        executeRemoveFromCart(cartItem)
    }

    private fun executeRemoveFromCart(cartItem: CartItem) {
        _uiState.update { state -> 
            val updatedItems = cartUseCase.removeItem(state.cartItems, cartItem)
            state.copy(cartItems = updatedItems) 
        }
        viewModelScope.launch {
            auditManager.logAction("REMOVE_ITEM", "Removed ${cartItem.product.name} (Qty: ${cartItem.quantity})", "SALES", "WARNING")
        }
    }

    fun showModifierSelection(item: CartItem) {
        viewModelScope.launch {
            val availableModifiers = modifierRepository.getModifiersForProduct(
                item.product.id, 
                item.product.categoryId
            )
            _uiState.update { it.copy(
                itemAwaitingModifiers = item,
                availableModifiers = availableModifiers // We'll need to add this to SalesUiState
            ) }
        }
    }

    fun dismissModifierSelection() {
        _uiState.update { it.copy(itemAwaitingModifiers = null) }
    }

    fun toggleModifier(modifier: com.extrotarget.extroposv2.core.data.model.Modifier) {
        val item = _uiState.value.itemAwaitingModifiers ?: return
        _uiState.update { state ->
            val updatedItems = cartUseCase.toggleModifier(state.cartItems, item, modifier)
            val updatedItem = updatedItems.find { it.id == item.id }
            state.copy(cartItems = updatedItems, itemAwaitingModifiers = updatedItem)
        }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: BigDecimal) {
        _uiState.update { state ->
            val updatedItems = cartUseCase.updateQuantity(state.cartItems, cartItem, newQuantity)
            state.copy(cartItems = updatedItems)
        }
    }

    fun showCartDiscountDialog() {
        _uiState.update { it.copy(showDiscountDialog = true, itemAwaitingDiscount = null) }
    }

    fun showItemDiscountDialog(item: CartItem) {
        _uiState.update { it.copy(showDiscountDialog = true, itemAwaitingDiscount = item) }
    }

    fun dismissDiscountDialog() {
        _uiState.update { it.copy(showDiscountDialog = false, itemAwaitingDiscount = null) }
    }

    fun applyDiscount(discount: com.extrotarget.extroposv2.ui.sales.Discount?) {
        if (!staffRepository.isCurrentUserAdmin()) {
            _uiState.update { it.copy(showAdminAuthDialog = true, adminAuthAction = AdminAuthAction.ApplyDiscount(_uiState.value.itemAwaitingDiscount, discount)) }
            return
        }
        executeApplyDiscount(discount)
    }

    private fun executeApplyDiscount(discount: com.extrotarget.extroposv2.ui.sales.Discount?) {
        val itemToDiscount = _uiState.value.itemAwaitingDiscount
        if (itemToDiscount != null) {
            _uiState.update { state ->
                val updatedItems = cartUseCase.applyItemDiscount(state.cartItems, itemToDiscount, discount)
                state.copy(cartItems = updatedItems, showDiscountDialog = false, itemAwaitingDiscount = null)
            }
            viewModelScope.launch {
                auditManager.logAction("ITEM_DISCOUNT", "Applied ${discount?.label ?: "None"} to ${itemToDiscount.product.name}", "SALES")
            }
        } else {
            _uiState.update { it.copy(cartDiscount = discount, showDiscountDialog = false) }
            viewModelScope.launch {
                auditManager.logAction("CART_DISCOUNT", "Applied ${discount?.label ?: "None"} to entire cart", "SALES")
            }
        }
    }

    fun authenticateAdmin(pin: String) {
        viewModelScope.launch {
            val admin = staffRepository.getStaffByPin(pin)
            if (admin != null && (admin.role == "ADMIN" || admin.role == "SUPERVISOR")) {
                val action = _uiState.value.adminAuthAction
                _uiState.update { it.copy(showAdminAuthDialog = false, adminAuthAction = null, adminAuthError = null) }
                
                when (action) {
                    is AdminAuthAction.RemoveItem -> executeRemoveFromCart(action.item)
                    is AdminAuthAction.ApplyDiscount -> {
                        _uiState.update { it.copy(itemAwaitingDiscount = action.item) }
                        executeApplyDiscount(action.discount)
                    }
                    is AdminAuthAction.OpenDrawer -> executeOpenDrawer()
                    else -> {}
                }
            } else {
                _uiState.update { it.copy(adminAuthError = context.getString(R.string.invalid_admin_pin)) }
            }
        }
    }

    fun dismissAdminAuth() {
        _uiState.update { it.copy(showAdminAuthDialog = false, adminAuthAction = null, adminAuthError = null) }
    }

    fun completeSale(paymentMethod: String) {
        val currentState = _uiState.value
        if (currentState.cartItems.isEmpty()) return

        if (paymentMethod == "OPEN_DIALOG") {
            _uiState.update { it.copy(showPaymentMethodDialog = true) }
            return
        }

        _uiState.update { it.copy(showPaymentMethodDialog = false) }

        if (paymentMethod == "CLOSE_DIALOG") return

        viewModelScope.launch {
            val saleId = UUID.randomUUID().toString()

            when (paymentMethod) {
                "CASH" -> {
                    _uiState.update { it.copy(showCashReceivedDialog = true) }
                }
                "CARD" -> {
                    _uiState.update { it.copy(showTerminalProgress = true, terminalStatus = "Awaiting Terminal Response...") }
                    val response = terminalRepository.processPayment(currentState.totalAmount, saleId)
                    _uiState.update { it.copy(showTerminalProgress = false) }

                    when (response) {
                        is TerminalResponse.Success -> finalizeSale(paymentMethod, saleId, response)
                        is TerminalResponse.Error -> {
                            _uiState.update { it.copy(terminalStatus = "Terminal Error: ${response.message}") }
                            return@launch
                        }
                        TerminalResponse.Cancelled -> {
                            _uiState.update { it.copy(terminalStatus = "Payment Cancelled") }
                            return@launch
                        }
                    }
                }
                else -> {
                    finalizeSale(paymentMethod, saleId)
                }
            }
        }
    }

    fun confirmCashReceived(amount: BigDecimal) {
        val saleId = UUID.randomUUID().toString()
        _uiState.update { it.copy(showCashReceivedDialog = false, cashReceived = amount) }
        viewModelScope.launch {
            finalizeSale("CASH", saleId)
        }
    }

    fun dismissCashReceived() {
        _uiState.update { it.copy(showCashReceivedDialog = false) }
    }

    private suspend fun finalizeSale(
        paymentMethod: String,
        saleId: String,
        terminalResponse: TerminalResponse.Success? = null
    ) {
        val currentState = _uiState.value
        _uiState.update { it.copy(isCheckingOut = true) }
        
        val finalTotal = if (paymentMethod == "CASH") currentState.totalAmountCash else currentState.totalAmount
        val finalRounding = if (paymentMethod == "CASH") currentState.roundingAdjustment else BigDecimal.ZERO

        val sale = Sale(
            id = saleId,
            totalAmount = finalTotal,
            taxAmount = currentState.totalTax,
            discountAmount = currentState.totalDiscount,
            discountLabel = currentState.cartDiscount?.label,
            roundingAdjustment = finalRounding,
            paymentMethod = paymentMethod,
            cardType = terminalResponse?.cardType,
            maskedPan = terminalResponse?.maskedPan,
            approvalCode = terminalResponse?.approvalCode
        )

        val saleItems = currentState.cartItems.map { cartItem ->
            SaleItem(
                id = UUID.randomUUID().toString(),
                saleId = saleId,
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                quantity = cartItem.quantity,
                unitPrice = cartItem.unitPrice,
                taxRate = cartItem.taxRate,
                taxAmount = cartItem.taxAmount,
                discountAmount = cartItem.discountAmount,
                discountLabel = cartItem.discount?.label,
                totalAmount = cartItem.totalPrice.add(cartItem.taxAmount),
                modifiers = cartItem.selectedModifiers.joinToString(", ") { "${it.name}${if (it.priceAdjustment > BigDecimal.ZERO) " (+RM${it.priceAdjustment})" else ""}" }.takeIf { it.isNotEmpty() },
                assignedStaffId = cartItem.assignedStaffId,
                assignedStaffName = cartItem.assignedStaffName,
                printerTag = cartItem.product.printerTag ?: "RECEIPT"
            )
        }

        val commissionRecords = currentState.cartItems.filter { it.assignedStaffId != null }.map { cartItem ->
            val calculatedCommission = com.extrotarget.extroposv2.core.util.CommissionCalculator.calculate(
                price = cartItem.unitPrice,
                ratePercent = cartItem.product.commissionRate,
                fixedAllowance = cartItem.product.fixedCommission,
                quantity = cartItem.quantity
            )

            CommissionRecord(
                id = UUID.randomUUID().toString(),
                staffId = cartItem.assignedStaffId!!,
                saleId = saleId,
                serviceName = cartItem.product.name,
                servicePrice = cartItem.unitPrice,
                commissionRate = cartItem.product.commissionRate,
                fixedAllowance = cartItem.product.fixedCommission,
                calculatedCommission = calculatedCommission
            )
        }

        processSaleUseCase(
            sale = sale,
            saleItems = saleItems,
            commissionRecords = commissionRecords,
            selectedTableId = currentState.selectedTable?.id,
            buyerInfo = currentState.selectedMember?.let { BuyerInfo(name = it.name, contact = it.phoneNumber) }
        )

        // Branch Sync: Push sale to HQ asynchronously
        viewModelScope.launch {
            branchSyncManager.pushSaleToHQ(SaleWithItems(sale, saleItems))
        }

        currentState.selectedMember?.let { member ->
            val loyaltyConfig = loyaltyRepository.getConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.loyalty.LoyaltyConfig()
            if (loyaltyConfig.isEnabled) {
                if (currentState.redeemedPoints > BigDecimal.ZERO) {
                    loyaltyRepository.redeemPoints(member.id, currentState.redeemedPoints, saleId, "Points redeemed for Sale $saleId")
                }
                val earnedPoints = currentState.totalAmount.multiply(loyaltyConfig.pointsPerCurrencyUnit)
                    .setScale(0, java.math.RoundingMode.DOWN)
                if (earnedPoints > BigDecimal.ZERO) {
                    loyaltyRepository.addPoints(member.id, earnedPoints, saleId, "Points earned from Sale $saleId")
                }
            }
        }

        val qrContent = if (paymentMethod == "QR" || paymentMethod == "DUITNOW") {
            val duitNowConfig = duitNowRepository.getConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.settings.DuitNowConfig()
            if (duitNowConfig.isEnabled) {
                val merchantId = securityManager.getString(com.extrotarget.extroposv2.core.util.security.SecurityManager.KEY_DUITNOW_MERCHANT_ID) ?: duitNowConfig.merchantId
                com.extrotarget.extroposv2.core.util.payment.DuitNowQrGenerator.generateDynamicQr(
                    merchantId = merchantId,
                    amount = currentState.totalAmount,
                    merchantName = duitNowConfig.merchantName,
                    city = duitNowConfig.city
                )
            } else null
        } else null

        printReceiptUseCase(sale, saleItems, currentState.selectedTable?.name)
        
        _uiState.update { it.copy(
            cartItems = emptyList(), 
            isCheckingOut = false,
            showPaymentSuccess = true,
            lastSaleId = saleId,
            lastSaleQrContent = qrContent,
            lastPaymentMethod = paymentMethod,
            terminalStatus = null
        ) }
    }

    fun dismissPaymentSuccess() {
        _uiState.update { it.copy(showPaymentSuccess = false, lastSaleQrContent = null, lastSaleId = null) }
    }

    fun dismissTerminalError() {
        _uiState.update { it.copy(terminalStatus = null, showTerminalProgress = false) }
    }

    fun reprintLastReceipt() {
        val saleId = _uiState.value.lastSaleId ?: return
        viewModelScope.launch {
            val sale = saleRepository.getSaleById(saleId) ?: return@launch
            val items = saleRepository.getItemsBySaleId(saleId)
            printReceiptUseCase(sale, items, _uiState.value.selectedTable?.name)
        }
    }

    fun openDrawer() {
        _uiState.update { it.copy(showAdminAuthDialog = true, adminAuthAction = AdminAuthAction.OpenDrawer) }
    }

    private fun executeOpenDrawer() {
        viewModelScope.launch {
            val currentShift = shiftRepository.getActiveShift().firstOrNull()
            val staffName = currentShift?.staffName ?: "Unknown Staff"
            
            printReceiptUseCase.openCashDrawer()
            auditManager.logAction("OPEN_DRAWER", "Manual drawer opening by $staffName", "SECURITY", "WARNING")
        }
    }

    fun selectMember(member: Member?) {
        _uiState.update { it.copy(selectedMember = member, redeemedPoints = BigDecimal.ZERO, showMemberSelection = false) }
    }

    fun setRedeemedPoints(points: BigDecimal) {
        _uiState.update { it.copy(redeemedPoints = points) }
    }

    fun setShowMemberSelection(show: Boolean) {
        _uiState.update { it.copy(showMemberSelection = show) }
    }
}
