# Project Progression Tracker - ExtroPOS v2

This file tracks the implementation status of features defined in the development plan.

## Phase 1: Core Engine & Compliance (Malaysian Standards)
- [x] **SST Logic**: Multi-rate support (6%, 8%, etc.) with accurate `BigDecimal` math.
- [x] **BNM 5-Sen Rounding**: Automated cash rounding for MYR transactions.
- [x] **Currency Utilities**: Centralized formatting and calculation engine.
- [x] **LHDN e-Invoicing (Baseline)**: QR code generation for receipts ready for MyInvois.
- [x] **LHDN e-Invoicing (Compliance)**: SHA-256 Digital Signature and UUID tracking on receipt footers.

## Phase 2: Hardware & Infrastructure
- [x] **Printer HAL**: Abstract layer for Bluetooth, USB, and Network (IP) printers.
- [x] **ESC/POS Encoding**: Custom encoder for POSMAC/HPRT (Bold, Alignment, QR, Cutting).
- [x] **Cash Drawer**: RJ11 trigger via ESC/POS command `0x1B, 0x70...`
- [x] **Barcode Scanning**: ML Kit integrated for high-speed camera-based scanning.
- [x] **Local Persistence**: Room v10 with encrypted migrations (Custom Payments, Discount & Rounding Audit).

## Phase 3: Sales & Payments
- [x] **Cart Management**: Real-time tax, subtotal, and discount calculations.
- [x] **Dynamic DuitNow QR**: EMVCo compliant QR generation with dynamic amounts.
- [x] **Checkout Flow**: Post-sale success dialog with on-screen QR and Reprint options.
- [x] **Discount Engine**: Real-time item-level and cart-level discounts (Fixed/Percentage).
- [x] **Card Terminal Link**: Integrated Pax/Ingenico HAL with Simulated Terminal for development.
- [x] **E-Wallet Direct Integration**: TNG/GrabPay via DuitNow dynamic QR.

## Phase 4: Industry-Specific Modules

### 🚗 Car Wash / Service Module
- [x] **Staff Commission Engine**: assignment per line-item and earnings calculation.
- [x] **Earnings Dashboard**: Real-time staff performance tracking.
- [x] **Job Queue**: Visual dashboard for vehicle progress (Wash -> Polish -> Ready) with Staff Assignment.
- [x] **Integration**: Automatic job creation upon sales checkout for car wash services.

### ☕ F&B Module (Cafe / Restaurant)
- [x] **Table Management**: Visual floor plan with order status.
- [x] **Kitchen Printing**: Split routing (Food to Kitchen, Drinks to Bar).
- [x] **Order Modifiers**: Support for "Bungkus", "Ikat Tepi", and custom add-ons.

### 🧺 Dobi / Laundry Module
- [x] **Order Tracking**: Received -> Processing -> Ready -> Collected (Data Layer).
- [x] **Weight-Based Pricing**: Digital scale integration (Mock & Interface).
- [x] **WhatsApp Alerts**: Automated "Siap" notifications via WhatsApp API.

## Phase 5: UI & Localization
- [x] **Multi-Language Support**: English (EN), Bahasa Melayu (BM), Mandarin (ZH).
- [x] **Material 3 Design**: "Fat-Finger" touch targets for high-speed POS environments.
- [x] **Dark Mode**: Standardizing themes for night-time operations.

## Phase 6: Analytics & Inventory
- [x] **Inventory Management**: Manual stock adjustments and barcode tracking.
- [x] **Low Stock Alerts**: Real-time visual warnings for threshold-breached items.
- [x] **Business Analytics**: Gross sales, transaction counts, and period filtering.
- [x] **SST Reporting**: Automated calculation of 6%/8% tax for Malaysian filing.
- [x] **Backup & Restore**: Manual database export/import for offline data safety.

## Phase 7: Cloud & Multi-Terminal (Coming Soon)
- [ ] **Cloud Sync**: WorkManager-based background sync with centralized dashboard (Postponed).
- [ ] **Multi-Outlet Support**: Data isolation and aggregation across multiple branches (Postponed).

---
*Last Updated: Integrated Card Terminal HAL (Phase 3) and finalized Car Wash staff assignment workflow.*
