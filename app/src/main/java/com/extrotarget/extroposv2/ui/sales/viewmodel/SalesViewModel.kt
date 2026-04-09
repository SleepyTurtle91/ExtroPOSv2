package com.extrotarget.extroposv2.ui.sales.viewmodel

import android.content.Context
import android.hardware.usb.UsbManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.local.dao.settings.ReceiptDao
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.hardware.PrinterConfig
import com.extrotarget.extroposv2.core.data.model.carwash.CommissionRecord
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import com.extrotarget.extroposv2.core.data.repository.fnb.TableRepository
import com.extrotarget.extroposv2.core.data.repository.lhdn.LhdnRepository
import com.extrotarget.extroposv2.core.data.repository.settings.DuitNowRepository
import com.extrotarget.extroposv2.core.hardware.printer.*
import com.extrotarget.extroposv2.core.util.printer.ReceiptGenerator
import com.extrotarget.extroposv2.core.work.EInvoiceSubmissionWorker
import com.extrotarget.extroposv2.ui.sales.CartItem
import com.extrotarget.extroposv2.ui.sales.SalesUiState
import com.extrotarget.extroposv2.core.data.repository.carwash.CarWashRepository
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashJob
import com.extrotarget.extroposv2.core.data.model.carwash.CarWashStatus
import com.extrotarget.extroposv2.core.data.repository.hardware.TerminalRepository
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
    private val lhdnRepository: LhdnRepository,
    private val carWashRepository: CarWashRepository,
    private val terminalRepository: TerminalRepository,
    private val printerDao: PrinterDao,
    private val receiptDao: ReceiptDao,
    private val duitNowRepository: DuitNowRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            combine(
                productRepository.getAllProducts(),
                categoryRepository.getAllCategories(),
                staffRepository.getAllActiveStaff()
            ) { products, categories, staff ->
                _uiState.update { 
                    it.copy(
                        products = products, 
                        categories = categories,
                        staffList = staff
                    ) 
                }
            }.collect()
        }
    }

    fun selectCategory(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun selectTable(table: com.extrotarget.extroposv2.core.data.model.fnb.Table) {
        _uiState.update { it.copy(selectedTable = table) }
        loadPendingSaleForTable(table.id)
    }

    private fun loadPendingSaleForTable(tableId: String) {
        viewModelScope.launch {
            saleRepository.getPendingSaleWithItemsForTable(tableId).collect { saleWithItems ->
                if (saleWithItems != null) {
                    val cartItems = saleWithItems.items.map { item ->
                        CartItem(
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
                            modifiers = item.modifiers?.split(", ") ?: emptyList(),
                            discount = item.discountLabel?.let { label -> 
                                com.extrotarget.extroposv2.ui.sales.Discount(
                                    type = com.extrotarget.extroposv2.ui.sales.DiscountType.FIXED,
                                    value = item.discountAmount,
                                    label = label
                                )
                            }
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
            _uiState.update { it.copy(isCheckingOut = true) }
            
            // 1. Create or update PENDING sale
            val existingSale = saleRepository.getPendingSaleForTable(table.id)
            val saleId = existingSale?.id ?: UUID.randomUUID().toString()
            
            val sale = Sale(
                id = saleId,
                totalAmount = currentState.totalAmount,
                taxAmount = currentState.totalTax,
                discountAmount = currentState.totalDiscount,
                paymentMethod = "PENDING",
                status = "PENDING",
                tableId = table.id
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
                    modifiers = cartItem.modifiers.joinToString(", ").takeIf { it.isNotEmpty() },
                    printerTag = cartItem.product.printerTag ?: "KITCHEN"
                )
            }

            if (existingSale != null) {
                saleRepository.updateSale(sale)
                // For simplicity, we'll replace items. In a full system, we'd track new vs sent items.
                // saleRepository.replaceItems(saleId, saleItems) 
            } else {
                saleRepository.completeSale(sale, saleItems)
                tableRepository.updateTable(table.copy(status = com.extrotarget.extroposv2.core.data.model.fnb.TableStatus.OCCUPIED, currentSaleId = saleId))
            }

            // 2. Print Order Slips
            val itemsByTag = saleItems.groupBy { it.printerTag }
            val allPrinters = printerDao.getAllPrinters().firstOrNull() ?: emptyList()
            
            allPrinters.filter { it.printerTag != null && it.printerTag != "RECEIPT" }.forEach { printerConfig ->
                val itemsForThisPrinter = itemsByTag[printerConfig.printerTag] ?: emptyList()
                if (itemsForThisPrinter.isNotEmpty()) {
                    printToPrinter(printerConfig) {
                        ReceiptGenerator.generateOrderSlip(
                            saleId = saleId,
                            tableName = table.name,
                            items = itemsForThisPrinter,
                            tag = printerConfig.printerTag ?: "ORDER"
                        )
                    }
                }
            }

            _uiState.update { it.copy(isCheckingOut = false, cartItems = emptyList(), selectedTable = null) }
        }
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val product = productRepository.getProductByBarcode(barcode)
            product?.let { addToCart(it) }
        }
    }

    fun addToCart(product: Product) {
        // If it's a car wash service (has commission potential), show staff selection
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
            val existingItem = state.cartItems.find { it.product.id == product.id && it.assignedStaffId == null }
            val updatedItems = if (existingItem != null) {
                state.cartItems.map {
                    if (it.product.id == product.id && it.assignedStaffId == null) {
                        it.copy(quantity = it.quantity.add(BigDecimal.ONE))
                    } else it
                }
            } else {
                state.cartItems + CartItem(
                    product = product,
                    quantity = BigDecimal.ONE,
                    unitPrice = product.price,
                    taxRate = product.taxRate
                )
            }
            state.copy(cartItems = updatedItems)
        }
    }

    fun assignStaffToItem(staff: com.extrotarget.extroposv2.core.data.model.carwash.Staff) {
        val item = _uiState.value.itemAwaitingStaff ?: return
        _uiState.update { state ->
            val updatedItem = item.copy(
                assignedStaffId = staff.id,
                assignedStaffName = staff.name
            )
            state.copy(
                cartItems = state.cartItems + updatedItem,
                showStaffSelection = false,
                itemAwaitingStaff = null
            )
        }
    }

    fun cancelStaffSelection() {
        _uiState.update { it.copy(showStaffSelection = false, itemAwaitingStaff = null) }
    }

    fun removeFromCart(cartItem: CartItem) {
        _uiState.update { state ->
            val updatedItems = state.cartItems.filter { it.product.id != cartItem.product.id }
            state.copy(cartItems = updatedItems)
        }
    }

    fun showModifierSelection(item: CartItem) {
        _uiState.update { it.copy(itemAwaitingModifiers = item) }
    }

    fun dismissModifierSelection() {
        _uiState.update { it.copy(itemAwaitingModifiers = null) }
    }

    fun toggleModifier(modifier: String) {
        val item = _uiState.value.itemAwaitingModifiers ?: return
        val currentModifiers = item.modifiers
        val newModifiers = if (currentModifiers.contains(modifier)) {
            currentModifiers - modifier
        } else {
            currentModifiers + modifier
        }
        
        val updatedItem = item.copy(modifiers = newModifiers)
        _uiState.update { currentState ->
            val newCartItems = currentState.cartItems.map { 
                if (it.product.id == item.product.id) updatedItem else it 
            }
            currentState.copy(
                cartItems = newCartItems,
                itemAwaitingModifiers = updatedItem
            )
        }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: BigDecimal) {
        if (newQuantity <= BigDecimal.ZERO) {
            removeFromCart(cartItem)
            return
        }
        _uiState.update { state ->
            val updatedItems = state.cartItems.map {
                if (it.product.id == cartItem.product.id) {
                    it.copy(quantity = newQuantity)
                } else it
            }
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
        val itemToDiscount = _uiState.value.itemAwaitingDiscount
        if (itemToDiscount != null) {
            _uiState.update { state ->
                val updatedItems = state.cartItems.map {
                    if (it == itemToDiscount) {
                        it.copy(discount = discount)
                    } else it
                }
                state.copy(cartItems = updatedItems, showDiscountDialog = false, itemAwaitingDiscount = null)
            }
        } else {
            _uiState.update { it.copy(cartDiscount = discount, showDiscountDialog = false) }
        }
    }

    fun completeSale(paymentMethod: String) {
        val currentState = _uiState.value
        if (currentState.cartItems.isEmpty()) return

        viewModelScope.launch {
            val saleId = UUID.randomUUID().toString()

            // Handle Card Terminal Payment
            if (paymentMethod == "CARD") {
                _uiState.update { it.copy(showTerminalProgress = true, terminalStatus = "Awaiting Terminal Response...") }
                val response = terminalRepository.processPayment(currentState.totalAmount, saleId)
                _uiState.update { it.copy(showTerminalProgress = false) }

                when (response) {
                    is TerminalResponse.Success -> {
                        // Continue to finalize sale with card details
                        finalizeSale(paymentMethod, saleId, response)
                    }
                    is TerminalResponse.Error -> {
                        // Show error and don't complete sale
                        _uiState.update { it.copy(terminalStatus = "Terminal Error: ${response.message}") }
                        return@launch
                    }
                    TerminalResponse.Cancelled -> {
                        _uiState.update { it.copy(terminalStatus = "Payment Cancelled") }
                        return@launch
                    }
                }
            } else {
                finalizeSale(paymentMethod, saleId)
            }
        }
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
                modifiers = cartItem.modifiers.joinToString(", ").takeIf { it.isNotEmpty() },
                assignedStaffId = cartItem.assignedStaffId,
                assignedStaffName = cartItem.assignedStaffName,
                printerTag = cartItem.product.printerTag ?: "RECEIPT"
            )
        }

        // Calculate commissions for Car Wash services
        val commissionRecords = currentState.cartItems.filter { it.assignedStaffId != null }.map { cartItem ->
            val p = cartItem.unitPrice
            val c = cartItem.product.commissionRate
            val f = cartItem.product.fixedCommission
            
            // Formula: (Pi * Ci) + Fi
            val calculatedCommission = p.multiply(c)
                .divide(BigDecimal("100"), 2, java.math.RoundingMode.HALF_EVEN)
                .add(f)
                .multiply(cartItem.quantity)

            CommissionRecord(
                id = UUID.randomUUID().toString(),
                staffId = cartItem.assignedStaffId!!,
                saleId = saleId,
                serviceName = cartItem.product.name,
                servicePrice = p,
                commissionRate = c,
                fixedAllowance = f,
                calculatedCommission = calculatedCommission
            )
        }

        saleRepository.completeSale(sale, saleItems)
        if (commissionRecords.isNotEmpty()) {
            staffRepository.addCommissionRecords(commissionRecords)
        }

        // Trigger LHDN e-Invoice Submission if configured
        val lhdnConfig = lhdnRepository.getConfig().firstOrNull()
        if (lhdnConfig != null && lhdnConfig.isEnabled) {
            enqueueEInvoiceSubmission(saleId)
        }

        // Car Wash: Create jobs for any car wash items
        currentState.cartItems.filter { it.product.commissionRate > BigDecimal.ZERO || it.product.fixedCommission > BigDecimal.ZERO }.forEach { item ->
            // Note: In a real app, we'd prompt for plate number. 
            // For now, we'll use a placeholder or check if one was provided in modifiers/notes.
            val plateNumber = item.modifiers.find { it.startsWith("Plate:") }?.removePrefix("Plate:") ?: "WALK-IN"
            
            carWashRepository.createJob(CarWashJob(
                id = UUID.randomUUID().toString(),
                plateNumber = plateNumber,
                serviceName = item.product.name,
                price = item.totalPrice,
                assignedStaffId = item.assignedStaffId,
                assignedStaffName = item.assignedStaffName,
                status = CarWashStatus.QUEUED
            ))
        }

        // F&B: Release Table if associated
        currentState.selectedTable?.let { table ->
            tableRepository.releaseTable(table.id)
        }
        
        // Generate QR for DuitNow if payment method is QR
        val qrContent = if (paymentMethod == "QR" || paymentMethod == "DUITNOW") {
            val duitNowConfig = duitNowRepository.getConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.settings.DuitNowConfig()
            if (duitNowConfig.isEnabled) {
                com.extrotarget.extroposv2.core.util.payment.DuitNowQrGenerator.generateDynamicQr(
                    merchantId = duitNowConfig.merchantId,
                    amount = currentState.totalAmount,
                    merchantName = duitNowConfig.merchantName,
                    city = duitNowConfig.city
                )
            } else null
        } else null

        // Print Receipt and Kick Drawer
        printReceiptAndKickDrawer(sale, saleItems)
        
        _uiState.update { it.copy(
            cartItems = emptyList(), 
            isCheckingOut = false,
            showPaymentSuccess = true,
            lastSaleId = saleId,
            lastSaleQrContent = qrContent,
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
            printReceiptAndKickDrawer(sale, items)
        }
    }

    private fun printReceiptAndKickDrawer(sale: Sale, items: List<SaleItem>) {
        viewModelScope.launch {
            val receiptConfig = receiptDao.getReceiptConfig().firstOrNull() ?: com.extrotarget.extroposv2.core.data.model.settings.ReceiptConfig()
            val lhdnSubmission = lhdnRepository.getSubmission(sale.id)
            val lhdnConfig = lhdnRepository.getConfig().firstOrNull()
            
            val allPrinters = printerDao.getAllPrinters().firstOrNull() ?: emptyList()
            val defaultPrinter = allPrinters.find { it.isDefault } ?: allPrinters.firstOrNull()

            // 1. Print Main Receipt
            defaultPrinter?.let { config ->
                printToPrinter(config) {
                    ReceiptGenerator.generateSaleReceipt(
                        sale = sale,
                        items = items,
                        config = receiptConfig,
                        lhdnSubmission = lhdnSubmission,
                        isSandbox = lhdnConfig?.isSandbox ?: true
                    )
                }
            }

            // 2. Print Order Slips (Kitchen/Bar/etc) grouped by printerTag
            val itemsByTag = items.groupBy { it.printerTag }
            
            allPrinters.filter { it.printerTag != null && it.printerTag != "RECEIPT" }.forEach { printerConfig ->
                val itemsForThisPrinter = itemsByTag[printerConfig.printerTag] ?: emptyList()
                if (itemsForThisPrinter.isNotEmpty()) {
                    printToPrinter(printerConfig) {
                        ReceiptGenerator.generateOrderSlip(
                            saleId = sale.id,
                            tableName = _uiState.value.selectedTable?.name,
                            items = itemsForThisPrinter,
                            tag = printerConfig.printerTag ?: "ORDER"
                        )
                    }
                }
            }
        }
    }

    private suspend fun printToPrinter(config: PrinterConfig, generateCommands: () -> List<PrintCommand>) {
        val printer: PrinterInterface? = when (config.connectionType) {
            "BLUETOOTH" -> BluetoothPrinter(config.address)
            "NETWORK" -> NetworkPrinter(config.address, config.port)
            "USB" -> {
                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                val device = usbManager.deviceList.values.find { it.deviceName == config.address }
                device?.let { UsbPrinter(context, it) }
            }
            else -> null
        }

        printer?.let {
            if (it.connect()) {
                val commands = generateCommands()
                if (config.id == "default_printer" || config.printerTag == "RECEIPT") {
                    it.printReceipt(listOf(PrintCommand.DrawerKick) + commands)
                } else {
                    it.printReceipt(commands)
                }
                it.disconnect()
            }
        }
    }

    fun reprintLastReceipt(sale: Sale, items: List<SaleItem>) {
        printReceiptAndKickDrawer(sale, items)
    }

    private fun enqueueEInvoiceSubmission(saleId: String) {
        val workRequest = OneTimeWorkRequestBuilder<EInvoiceSubmissionWorker>()
            .setInputData(workDataOf(EInvoiceSubmissionWorker.KEY_SALE_ID to saleId))
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
