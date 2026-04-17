# ExtroPOS v2 - Production Documentation (2026 Readiness)

This document outlines the production architecture, compliance standards, and security protocols for ExtroPOS v2, specifically tailored for the Malaysian market 2026 requirements.

## 1. Compliance & Regulatory (Malaysia 2026)

### 1.1 e-Invoicing (LHDN MyInvois)
*   **Mandatory Threshold**: Automated enforcement of the RM 10,000 rule. Transactions ≥ RM 10,000 are forced to "Individual e-Invoice" status.
*   **Consolidation**: Support for B2C consolidation for transactions < RM 10,000. Automated `LhdnConsolidationWorker` runs every 24h to submit aggregated Document Type 11.
*   **API Environments**:
    *   Sandbox: `https://preprod-api.myinvois.hasil.gov.my/`
    *   Production: `https://api.myinvois.hasil.gov.my/`
*   **Hashing**: SHA-256 canonicalization implemented for document integrity as per LHDN SDK 1.0.

### 1.2 SST & Financials
*   **Multi-Rate Support**: Pre-configured for 2026 rates: 6% (Service/Rental), 8% (Standard), and 5% (Luxury/Import).
*   **Rounding**: BNM-compliant 5-sen rounding logic implemented for Cash payments.
*   **Reporting**: SST reports provide granular breakdowns by tax rate for easier filing.
*   **F&B Modifiers**: Hierarchical modifier system (Category-linked with Product-level override/bypass) for precise kitchen instructions. Includes support for local preferences (e.g., "Kurang Manis", "Ikat Tepi") with automated price adjustments.

### 1.3 F&B Operations Workflow (All-in-One)
*   **Table Management**: Integrated digital floor plan with real-time status (Available, Occupied, Billing).
*   **Order Routing**: Automated splitting of "Dapur" (Kitchen) and "Bar" (Drinks) tickets with high-visibility formatting.
*   **Mid-Meal "Tambah Order"**: Smart-sync logic ensures only newly added items are re-printed to kitchen stations.
*   **Inventory Depletion**: Real-time deduction of stock items and automated "Out of Stock" grey-out on front-end screens.
*   **DuitNow QR**: EMVCo-compatible dynamic QR generation with support for cross-border interoperability (PayNet).

## 2. Technical Architecture

### 2.1 Branch-to-Branch Sync (Cloud-less)
*   **Architecture**: "HQ/Master Relay" P2P sync.
*   **Protocol**: Ktor-based REST API and WebSockets.
*   **Security**: `X-Sync-Token` mandatory for all inter-branch traffic.
*   **Data Consistency**:
    *   *Members*: Pull-on-Search ensures points are accurate across branches.
    *   *Sales*: Push-on-Sale centralizes records at HQ.
    *   *Stock*: Inter-branch transfer (IT) records for stock movement tracking.

### 2.2 Security Hardening
*   **Credential Storage**: AES-256 Encrypted SharedPreferences (Android Keystore) for API Keys and Secrets.
*   **Obfuscation**: R8/Proguard rules configured to protect business logic.
*   **Logging**: `ReleaseTree` implemented in Timber to suppress sensitive data logs in production.

## 3. Build & Deployment

### 3.1 Build Variants
*   **Debug**: `applicationIdSuffix = ".debug"`. Used for testing and sandbox e-invoicing.
*   **Release**: Minified and shrinked for production.

### 3.2 Database (Room)
*   **Current Version**: 26
*   **Key Entities**: `Product`, `Category`, `Modifier`, `ModifierLink`, `Sale`, `Member`, `Branch`, `StockTransfer`, `LhdnConfig`, `Shift`, `EndOfDay`.

## 4. Maintenance & Operations

### 4.1 Backup & Closeout Strategy
*   **Auto-Backup**: Scheduled via WorkManager every 24 hours (off-peak).
*   **End of Day (EOD)**: Mandatory multi-shift consolidation logic implemented. Generates a daily `EndOfDay` record summarizing Gross/Net sales, Tax, and Payment breakdowns.
*   **Manual Export**: Support for CSV/SQL export for SST auditing (via `SstReportManager`).

### 4.2 Local Persistence
*   The app is **Offline-First**. All sales and SST calculations function without internet; LHDN/Sync tasks are queued and retried automatically.

---
*Last Updated: April 2026 - Production v1.0.0 Readiness*
