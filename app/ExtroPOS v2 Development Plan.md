ExtroPOS v2 Development Plan
ExtroPOS v2: Multi-Purpose Android POS (Malaysian Edition)

A high-performance, modular Kotlin-based Android POS designed for the unique workflows of Malaysian SMEs.

1. Local Compliance & Core Logic (Priority)

To succeed in the Malaysian market, the core engine must handle local tax and regulatory requirements natively.

LHDN e-Invoicing (2025/2026 Ready):

Integration with MyInvois API for real-time validation.

Support for Consolidated e-Invoices for B2C transactions.

Automatic QR code generation on receipts for customer verification.

SST Management:

Configurable Sales Tax (5%/10%) and Service Tax (6%/8%).

Rounding adjustment to the nearest 5 sen ($0.05$) as per Bank Negara Malaysia guidelines.

Malaysian Payment Integration:

DuitNow QR: Static and Dynamic QR generation.

Local E-Wallets: Integration with Touch 'n Go, GrabPay, and ShopeePay (via IPAY88 or Razer Merchant Services).

Terminal Link: Bluetooth/USB connection to card terminals (Pax, Ingenico).

2. Industry-Specific Modules

🏪 Retail Module (Kedai Runcit / Boutique)

Inventory Control: Low-stock alerts and SKU management.

Barcode Mastery: Support for built-in camera scanning and external Bluetooth scanners.

Promotion Engine: PWP (Purchase with Purchase), "Buy 1 Free 1", and membership-based discounts.

☕ F&B Module (Cafe / Restaurant)

Floor Plan: Visual table management with "Order-to-Pay" status.

Kitchen Flow: Split printing (e.g., Food to Kitchen, Drinks to Bar).

Order Modifiers: "Bungkus/Ikat Tepi" options and custom add-ons.

QR Ordering: Customer-self-order via QR table stickers.

🧺 Dobi Module (Laundry / Dry Clean)

Weight-Based Pricing: Integration with digital scales (price per KG).

Order Lifecycle: Track status from Received ➔ Processing ➔ Ready ➔ Collected.

WhatsApp Notifications: Auto-send "Cucian Siap" alerts to customers via WhatsApp API.

Pre-paid Packages: Support for "Top-up Credits" or "Laundry Tokens".

🚗 Car Wash Module (Commission-Based)

Service Tiers: Body Wash, Interior, Coating, and Detailing.

Staff Commission Engine:

Assign staff to specific jobs (e.g., Ali did the Interior, Abu did the Wax).

Automatic calculation: Total Commission = (Service Price × Rate%) + Fixed Fee.

Job Queue: Real-time dashboard showing vehicles currently being washed.

3. Technical Stack (Android Kotlin)

UI/UX: Jetpack Compose (Material 3) with a focus on "Fat-Finger" touch targets for high-speed environments.

Database: Room DB with Encryption for offline data security.

Printing: ESC/POS library for Thermal Bluetooth/USB/Network printers.

Background Sync: WorkManager to sync data once internet is restored (Offline-first).

Scanning: ML Kit for high-speed barcode and QR recognition.

4. Development Roadmap

Phase 1: The Engine (Months 1-2)

Base Android project setup (Hilt, Room, Compose).

Malaysian Tax (SST) & Rounding Logic.

Receipt Template Designer (ESC/POS).

Phase 2: Retail & F&B (Months 3-4)

Barcode & Inventory management.

Table management & Kitchen printing.

Initial DuitNow QR integration.

Phase 3: Service Modules (Months 5-6)

Dobi: Weight logic & WhatsApp alerts.

Car Wash: Commission engine & Staff assignment UI.

LHDN e-Invoicing: Integration testing with MyInvois Sandbox.

Phase 4: Malaysian Market Localization (Months 7)

UI translation (English, BM, Mandarin).

Integration with local hardware (Sunmi, IMIN, Landi handhelds).

Phase 5: Cloud Dashboard (Coming Soon)

Centralized multi-outlet management.

Real-time analytics for owners.

5. Staff Commission Formula (Logic Implementation)

For the Car Wash module, the logic for staff earnings per transaction is defined as:

$$E_{staff} = \sum_{i=1}^{n} (P_i \times C_i) + F_i$$

Where:

$P_i$: Price of service $i$.

$C_i$: Commission percentage for that service.

$F_i$: Fixed allowance per job.