ExtroPOS v2 Development Plan
ExtroPOS v2: Multi-Purpose Android POS (Malaysian Edition)

A high-performance, modular Kotlin-based Android POS designed for the unique workflows of Malaysian SMEs.

1. Local Compliance & Core Logic (Priority)

To succeed in the Malaysian market, the core engine must handle local tax and regulatory requirements natively.

LHDN e-Invoicing (2025/2026 Ready):
- Integration with MyInvois API for real-time validation.
- Support for Consolidated e-Invoices for B2C transactions.
- Automatic QR code generation on receipts for customer verification.

SST Management:
- Configurable Sales Tax (5%/10%) and Service Tax (6%/8%).
- Rounding adjustment to the nearest 5 sen ($0.05$) as per Bank Negara Malaysia guidelines.

Malaysian Payment Integration:
- DuitNow QR: Static and Dynamic QR generation.
- Local E-Wallets: Integration with Touch 'n Go, GrabPay, and ShopeePay (via IPAY88 or Razer Merchant Services).
- Terminal Link: Bluetooth/USB connection to card terminals (Pax, Ingenico).

Software Licensing & Security:
- Device-Bound Licensing: Encrypted license keys tied to Android Hardware ID (SSAID) with offline validation.
- Trial Management: 14-day automated trial period with grace-period logic.
- POS Screen Lock: Multi-user PIN-based access with high-speed "Switch User" functionality for shared terminals.
- Role-Based Access Control (RBAC): Distinct permissions for Cashiers, Supervisors, and Admins (e.g., only Admin can perform voids).

2. Industry-Specific Modules

🏪 Retail Module (Kedai Runcit / Boutique)
- Inventory Control: Low-stock alerts and SKU management.
- Barcode Mastery: Support for built-in camera scanning and external Bluetooth scanners.
- Promotion Engine: PWP (Purchase with Purchase), "Buy 1 Free 1", and membership-based discounts.

☕ F&B Module (Cafe / Restaurant)
- Floor Plan: Visual table management with "Order-to-Pay" status.
- Kitchen Flow: Split printing (e.g., Food to Kitchen, Drinks to Bar).
- Order Modifiers: "Bungkus/Ikat Tepi" options and custom add-ons.
- QR Ordering: Customer-self-order via QR table stickers.

🧺 Dobi Module (Laundry / Dry Clean)
- Weight-Based Pricing: Integration with digital scales (price per KG).
- Order Lifecycle: Track status from Received ➔ Processing ➔ Ready ➔ Collected.
- WhatsApp Notifications: Auto-send "Cucian Siap" alerts to customers via WhatsApp API.
- Pre-paid Packages: Support for "Top-up Credits" or "Laundry Tokens".

🚗 Car Wash Module (Commission-Based)
- Service Tiers: Body Wash, Interior, Coating, and Detailing.
- Staff Commission Engine:
    - Assign staff to specific jobs (e.g., Ali did the Interior, Abu did the Wax).
    - Automatic calculation: Total Commission = (Service Price × Rate%) + Fixed Fee.
- Job Queue: Real-time dashboard showing vehicles currently being washed.

3. Technical Stack (Android Kotlin)
- UI/UX: Jetpack Compose (Material 3) with a focus on "Fat-Finger" touch targets for high-speed environments.
- Database: Room DB with Encryption for offline data security.
- Printing: ESC/POS library for Thermal Bluetooth/USB/Network printers.
- Background Sync: WorkManager to sync data once internet is restored (Offline-first).
- Scanning: ML Kit for high-speed barcode and QR recognition.

4. Development Roadmap

Phase 1: Core Engine & Compliance (COMPLETE)
Phase 2: Hardware & Infrastructure (COMPLETE)
Phase 3: Sales & Payments (COMPLETE)
Phase 4: Industry-Specific Modules (COMPLETE)
Phase 5: UI, Localization & UX (COMPLETE)
Phase 6: Analytics, Inventory & Backup (COMPLETE)
Phase 7: Security, Licensing & Multi-User (COMPLETE)
Phase 8: Genius POS UI/UX Refactor (COMPLETE)
Phase 9: Standalone Excellence & Advanced Sync (COMPLETE)
   - Goal: Enhance data portability and enable multi-terminal setups without cloud dependency.
   - Local Network Sync (P2P): Real-time synchronization between "Main POS" and "Waiter/KDS" terminals via local Ktor server and NSD.
   - Scheduled Auto-Backup: Automated database protection using WorkManager for daily external exports.
   - Advanced Data Portability: Comprehensive ZIP exports for external accounting and tax filing with AES-256 encryption.

Phase 10: Final Hardening & LHDN Sandbox (COMPLETE)
   - Goal: Finalize LHDN e-Invoicing integration and ensure system-wide stability.
   - LHDN MyInvois Sandbox: Real-time API submission, status polling, and validation of e-Invoices.
   - Staff Commission Engine: Implemented core logic using the defined formula ($E = \sum (P \times C) + F$) for automated earnings calculation.
   - Payment Versatility: Integrated multi-method payment selection dialog (Cash, Card, DuitNow, E-Wallet) at checkout for compliance and UX.
   - Multi-terminal Stock Consistency: Real-time inventory synchronization across P2P terminals to prevent overselling.
   - Security Audit: Verified encryption for sensitive LHDN credentials and data exports.

*Project is now focused on final compliance testing and system hardening.*

5. Staff Commission Formula (Logic Implementation)

For the Car Wash module, the logic for staff earnings per transaction is defined as:

$$E_{staff} = \sum_{i=1}^{n} (P_i \times C_i) + F_i$$

Where:
- $P_i$: Price of service $i$.
- $C_i$: Commission percentage for that service.
- $F_i$: Fixed allowance per job.
