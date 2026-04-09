package com.extrotarget.extroposv2.ui.sales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extrotarget.extroposv2.core.data.local.dao.PrinterDao
import com.extrotarget.extroposv2.core.data.model.Product
import com.extrotarget.extroposv2.core.data.model.Sale
import com.extrotarget.extroposv2.core.data.model.SaleItem
import com.extrotarget.extroposv2.core.data.model.carwash.CommissionRecord
import com.extrotarget.extroposv2.core.data.repository.CategoryRepository
import com.extrotarget.extroposv2.core.data.repository.ProductRepository
import com.extrotarget.extroposv2.core.data.repository.SaleRepository
import com.extrotarget.extroposv2.core.data.repository.carwash.StaffRepository
import com.extrotarget.extroposv2.core.data.repository.fnb.TableRepository
import com.extrotarget.extroposv2.core.hardware.printer.*
import com.extrotarget.extroposv2.core.util.printer.ReceiptGenerator
import com.extrotarget.extroposv2.ui.sales.CartItem
import com.extrotarget.extroposv2.ui.sales.SalesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import android.content.Context
import android.hardware.usb.UsbManager

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val saleRepository: SaleRepository,
    private val staffRepository: StaffRepository,
    private val tableRepository: TableRepository,
    private val printerDao: PrinterDao,
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
            _uiState.update { it.copy(isCheckingOut = true) }
            
            val saleId = UUID.randomUUID().toString()
            val sale = Sale(
                id = saleId,
                totalAmount = currentState.totalAmount,
                taxAmount = currentState.totalTax,
                discountAmount = currentState.totalDiscount,
                discountLabel = currentState.cartDiscount?.label,
                roundingAdjustment = currentState.roundingAdjustment,
                paymentMethod = paymentMethod
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
                    assignedStaffName = cartItem.assignedStaffName
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

            // F&B: Release Table if associated
            currentState.selectedTable?.let { table ->
                tableRepository.releaseTable(table.id)
            }
            
            // Generate QR for DuitNow if payment method is QR
            val qrContent = if (paymentMethod == "QR") {
                com.extrotarget.extroposv2.core.util.payment.DuitNowQrGenerator.generateDynamicQr(
                    merchantId = "12345678", // Sample Merchant ID
                    amount = currentState.totalAmount,
                    merchantName = "EXTROPOS DEMO"
                )
            } else null

            // Print Receipt and Kick Drawer
            printReceiptAndKickDrawer(sale, saleItems)
            
            _uiState.update { it.copy(
                cartItems = emptyList(), 
                isCheckingOut = false,
                showPaymentSuccess = true,
                lastSaleId = saleId,
                lastSaleQrContent = qrContent
            ) }
        }
    }

    fun dismissPaymentSuccess() {
        _uiState.update { it.copy(showPaymentSuccess = false, lastSaleQrContent = null, lastSaleId = null) }
    }

    private fun printReceiptAndKickDrawer(sale: Sale, items: List<SaleItem>) {
        viewModelScope.launch {
            printerDao.getDefaultPrinter().firstOrNull()?.let { config ->
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
                        val receiptContent = ReceiptGenerator.generateSaleReceipt(sale, items)
                        
                        // Drawer Kick Command (standard ESC/POS: ESC p 0 25 250)
                        val drawerKick = listOf(
                            PrintCommand.Raw(byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte()))
                        )
                        
                        it.printReceipt(drawerKick + receiptContent)
                        it.disconnect()
                    }
                }
            }
        }
    }

    fun reprintLastReceipt(sale: Sale, items: List<SaleItem>) {
        printReceiptAndKickDrawer(sale, items)
    }
}
