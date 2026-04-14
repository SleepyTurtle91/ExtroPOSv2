# Project Context & Implementation Status - ExtroPOS v2

This document provides a technical summary of implemented features and architectural decisions for AI agent context awareness.

## 1. Core Financial Engine
- **Precision**: Mandatory use of `java.math.BigDecimal` for all currency.
- **Rounding**: `RoundingMode.HALF_EVEN` used globally.
- **Malaysian Compliance**:
    - **SST**: Configurable rates (5%, 6%, 8%, 10%) implemented in `CurrencyUtils.kt`.
    - **BNM Rounding**: Automated 5-sen cash rounding logic (e.g., RM 1.02 -> RM 1.00, RM 1.03 -> RM 1.05).

## 2. Hardware Abstraction Layer (HAL)
- **Printer System**:
    - **Interface**: `PrinterInterface` abstracts `connect()`, `disconnect()`, and `printReceipt()`.
    - **Drivers**:
        - `BluetoothPrinter`: RFCOMM/SPP for mobile thermal printers.
        - `UsbPrinter`: Native Android USB Host for desktop POS printers (Serial-over-USB).
        - `NetworkPrinter`: Socket-based (Port 9100) for high-speed IP printers.
    - **Encoding**: `EscPosEncoder` optimized for **POSMAC** and **HPRT** brands. Includes support for bold, alignment, paper cutting, and QR codes.
- **Drawer Kick**: Standard ESC/POS trigger (`ESC p`) integrated into the post-checkout print stream.

## 3. Commission & Staff Management
- **Logic**: Staff assignment per line-item for services (e.g., Car Wash).
- **Formula**: $E_{staff} = \sum (P_i \times C_i) + F_i$ (Price × Rate% + Fixed Fee).
- **UI**: Reactive earnings dashboard in `StaffManagementScreen` using `flatMapLatest` to combine staff info with real-time Room transaction totals.

## 4. Inventory & Scanning
- **Scanning**: ML Kit integrated into `SalesScreen` (quick-add) and `InventoryScreen` (filtering).
- **Integrity**: Atomic stock adjustments (`StockMovement` + `Product.stockQuantity`) wrapped in `db.withTransaction`.

## 5. Payment Experience
- **DuitNow QR**: 
    - **Dynamic Generation**: EMVCo-compatible QR strings generated on-the-fly.
    - **Data Integrity**: Native CRC-16 (CCITT-FALSE) implementation for Malaysian bank app validation.
    - **On-Screen Display**: Post-checkout dialog with `QrCodeView` (ZXing-powered).

## 6. Localization
- **Languages**: Full resource-based support for **English (Default)**, **Bahasa Melayu (values-ms)**, and **Mandarin (values-zh-rCN)**.
- **Terminology**: Standardized Malaysian POS terms (e.g., "Bungkus", "Pembundaran").

## 7. Analytics & Inventory
- **Inventory**: Atomic stock adjustments (`StockMovement` + `Product.stockQuantity`) wrapped in `db.withTransaction`. Includes real-time **Low Stock Alerts** based on `minStockLevel`.
- **Analytics**: Date-range based sales and tax reporting.
- **SST Compliance**: Dedicated reporting for 6%/8% SST collection for Malaysian tax filing.

## 8. Current Architecture
- **Tech Stack**: Compose (M3), Hilt DI, Room, Coroutines/Flow.
- **Navigation**: Typed `Screen` sealed class with `NavGraph` integration.
- **Database**: Room v7 includes `printer_configs`, `staff`, `commission_records`, `products`, `sales`, `laundry_orders`, and `car_wash_jobs`.

## 9. Next Priority: AutoCount Accounting Integration
- **Objective**: Automate sales data synchronization to AutoCount Accounting software via REST API.
- **Scope**: Mapping `Sale` and `SaleItem` to AutoCount `CashSale` or `SalesInvoice` objects.
- **Authentication**: OAuth2 or API Key based authentication.

## 10. Security & Multi-User (Planned)
- **Licensing**: Implementation of SSAID-bound license keys for device activation.
- **User Management**: PIN-based screen lock and role-based permissions (RBAC).

---
*Last Updated: Phase 7 Initialization (Security & Licensing Focus)*
