# ExtroPOS v2: Multi-Purpose Malaysian POS

ExtroPOS v2 is a high-performance, modular Kotlin-based Android Point of Sale system specifically tailored for Malaysian SMEs. It features deep integration with local regulatory requirements (SST, BNM Rounding) and industry-specific workflows for Retail, F&B, Laundry, and Car Wash sectors.

---

## 🚀 Core Features & Malaysian Compliance

### 1. Financial Precision
- **BigDecimal Engine**: All currency calculations use `java.math.BigDecimal` with `RoundingMode.HALF_EVEN` to ensure zero floating-point errors.
- **BNM 5-Sen Rounding**: Automated rounding of cash transactions to the nearest 5 cents as per Bank Negara Malaysia guidelines.
- **SST Management**: Native support for Malaysian Sales and Service Tax with configurable rates (5%, 6%, 8%, 10%).

### 2. Payment & E-Invoicing
- **DuitNow QR**: Dynamic EMVCo-compatible QR generation with native CRC-16 validation for Malaysian banking apps.
- **LHDN e-Invoicing Ready**: Baseline support for MyInvois QR code generation on receipts.
- **E-Wallets**: Integration support for TNG, GrabPay, and ShopeePay.

### 3. Hardware Abstraction Layer (HAL)
- **Unified Printer System**: Support for Bluetooth (RFCOMM), USB (Host Mode), and Network (IP Port 9100) thermal printers.
- **ESC/POS Encoder**: Custom optimized driver for POSMAC and HPRT brands, including bold text, QR codes, and paper cutting.
- **Cash Drawer**: RJ11 trigger integration via ESC/POS commands.

---

## 🏗️ Industry-Specific Modules

### 🛒 General Retail
- **Inventory Management**: High-speed SKU tracking and barcode scanning integration.
- **Fast Checkout**: Optimized UI for high-volume transactions with quick-pay buttons.
- **Stock Alerts**: Real-time notifications for low-stock items based on configurable thresholds.

### 🚗 Car Wash & Service
- **Staff Commission Engine**: Assignment of staff to specific line-items (e.g., Ali did the Interior, Abu did the Wax).
- **Earnings Logic**: $E_{staff} = \sum (P_i \times C_i) + F_i$ (Price × Rate% + Fixed Fee).
- **Job Queue**: Visual dashboard for tracking vehicle progress.

### ☕ F&B (Cafe / Restaurant)
- **Table Management**: Visual floor plan with real-time order status.
- **Kitchen Printing**: Split routing of orders (Food to Kitchen, Drinks to Bar).
- **Modifiers**: Support for local preferences like "Bungkus" and "Ikat Tepi".
- **Real-time KDS Sync**: Immediate synchronization of orders across all kitchen screens via P2P.

### 🖥️ Kitchen Display System (KDS) & Web Dashboard
- **P2P Synchronization**: Order status updates (e.g., "Mark Ready") sync instantly across all Android terminals and WebApp instances.
- **Responsive Web Dashboard**: Access the KDS from any device on the local network (PC, Tablet, Smart TV) via `http://<device-ip>:8080`.
- **Audio & Visual Alerts**: Real-time notifications for new orders to ensure kitchen efficiency.

### 🧺 Dobi (Laundry)
- **Weight-Based Pricing**: Digital scale integration logic.
- **Order Lifecycle**: Tracking from Received ➔ Processing ➔ Ready ➔ Collected.
- **WhatsApp Alerts**: Automated "Cucian Siap" notifications.

### 🏨 Hotel & Homestay
- **Room Management**: Real-time status tracking (Available, Occupied, Dirty, Maintenance).
- **Booking Lifecycle**: Streamlined flow from reservation and check-in to final bill generation and check-out.
- **Guest Profiles**: Integrated guest database with identity tracking and loyalty tiering (Regular, VIP, etc.).
- **Add-on Services**: Capability to link additional charges like meals, tours, or transport directly to the room bill.
- **Financial Settlement**: Integrated check-out workflow that handles room nights, addons, and deposit reconciliation with the central POS engine.

---

## 🛠️ Technical Stack

- **UI**: Jetpack Compose (Material 3) with "Fat-Finger" touch targets and **Scrollable Sidebar Navigation** for high-density functionality.
- **Architecture**: MVVM with Clean Architecture principles.
- **DI**: Hilt (Dependency Injection).
- **Database**: Room (Offline-first approach).
- **Concurrency**: Kotlin Coroutines & Flow.
- **Scanning**: Google ML Kit for high-speed barcode/QR recognition.

---

## 📖 Development Guidelines

The project follows strict architectural rules defined in `KOTLIN_DEVELOPMENT_RULES.md`:
1. **Immutability**: Prefer `val` and immutable collections.
2. **Offline-First**: All data must be persisted to Room before syncing.
3. **No Floats for Money**: Always use `BigDecimal`.
4. **Modularization**: Package-by-feature structure.

---

## 📈 Project Status

See `PROGRESSION.md` for the latest implementation milestones.
- ✅ Phase 1-9: Core Engine, Hardware, Sales, Industry Modules, UI, Analytics, Security, Data Portability & Advanced P2P Sync.
- ✅ Phase 10: Final Hardening & LHDN Sandbox (LHDN MyInvois V1.1 Integration, Status Polling, and Stock Consistency).
- ✅ Phase 11-13: Enterprise Integration, Customer Loyalty, Tax Genericization, and Production Hardening.
- ✅ **Special Enhancement (May 2026)**: P2P Kitchen Display System (KDS) & Remote Web Dashboard Integration.

---

## 🌍 Localization
Fully supports:
- **English** (Default)
- **Bahasa Melayu** (`values-ms`)
- **Mandarin** (`values-zh-rCN`)
