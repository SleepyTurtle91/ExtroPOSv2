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
- [x] **Experimental API Opt-In**: Standardized `@OptIn(ExperimentalMaterial3Api::class)` across Sales, Inventory, and Dialog components to resolve Compose Material 3 stability warnings.
- [x] **Dark Mode**: Standardizing themes for night-time operations.

## Phase 6: Analytics & Inventory
- [x] **Inventory Management**: Manual stock adjustments and barcode tracking with CSV Import/Export.
- [x] **Low Stock Alerts**: Real-time visual warnings for threshold-breached items.
- [x] **Business Analytics**: Gross sales, transaction counts, and period filtering.
- [x] **SST Reporting**: Automated calculation of 6%/8%/10% tax for Malaysian filing with CSV Export.
- [x] **Backup & Restore**: Secure database export/import via Storage Access Framework (SAF) with automatic checkpointing and app restart.

## Phase 7: Security, Licensing & Multi-User
- [x] **Software Licensing**: Device-bound encrypted license activation and hardware ID (SSAID) binding.
- [x] **Trial Management**: Automated 14-day trial logic with offline grace periods.
- [x] **POS Screen Lock**: PIN-based high-speed user switching for shared terminals.
- [x] **Biometric Authentication**: Fingerprint/Face unlock for high-speed terminal switching.
- [x] **Role-Based Access Control (RBAC)**: Permission enforcement for Voids, Discounts, and Settings.

## Phase 8: Genius POS UI/UX Refactor
- [x] **Professional Aesthetic**: Slate 800/900 theme with primary blue accents.
- [x] **High-Density Layouts**: Optimized sidebar, header, and cart sidebar for 10-inch tablets.
- [x] **Performance Tracking**: New Staff Earnings dashboard with formula visualization.
- [x] **Inventory Overhaul**: Streamlined product list with status chips and fat-finger actions.
- [x] **Z-Report (EOD)**: Tablet-optimized "Shift Closeout" screen with real-time cash reconciliation (Expected vs. Actual) and Malaysian rounding/SST summaries.
- [x] **Onboarding Wizard**: 4-step animated setup flow (Business Type -> Store -> Admin -> License) for new enterprise deployments with Slate 800/900 Material 3 design.
- [x] **Business Mode Isolation**: Dynamic UI adaptation (Retail, F&B, Car Wash, Dobi) with persistent settings and automated module filtering (e.g., hiding Table Management in Retail mode).

## Phase 9: Standalone Excellence & Advanced Sync
- [x] **Excel/CSV Master Export**: Comprehensive export of all transaction and tax data for external accounting with AES-256 encryption.
- [x] **Local Network Sync (P2P)**: Real-time sync between terminals (Master/Slave) using Ktor WebSockets and NSD.
- [x] **Scheduled Auto-Backup**: Automated database protection using WorkManager.
- [x] **Conflict Management**: "Dirty Sync" detection for data integrity.

## Phase 10: Final Hardening & LHDN Sandbox
- [x] **LHDN MyInvois Sandbox**: Real-time API submission of e-Invoices to LHDN sandbox environment with encrypted credential storage and consolidated invoice support.
- [x] **Edge Case Hardening**: Implemented safer database sync with backup/restore and exponential backoff for P2P WebSocket connections.
- [x] **Production Readiness**: Code obfuscation (ProGuard/R8) and final security audit.

## Phase 11: Enterprise & Cloud Ecosystem
- [x] **AutoCount Accounting Integration**: Automated sync of daily sales and payments to AutoCount via API.
- [ ] **Cloud Management Portal**: Remote dashboard for multi-outlet inventory and sales tracking.
- [x] **Customer Loyalty System**: Points-based rewards with Tiered Rewards (Bronze/Silver/Gold) and detailed member purchase/transaction history.
- [ ] **Branch-to-Branch Sync**: Stock transfer and centralized membership data.

---
*Last Updated: Completed Phase 9 and initiated Phase 10: Final Hardening & LHDN Sandbox.*
