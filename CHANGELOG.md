# Changelog - ExtroPOS v2

All notable changes to this project will be documented in this file.

## [v2.0.0] - 2026-07-18
### Added
- **Shared Commerce Platform**: Unified core for multi-industry support.
- **Capability System**: Decoupled UI features from business logic.
- **Retail Domain**: Full support for retail workflows including products and stock.
- **F&B Domain**: Table management, modifiers, and Kitchen Display System (KDS).
- **Stock Ledger Architecture**: Transaction-safe inventory based on `StockMovement` deltas.
- **Supply Chain Management**: Professional Purchase Order (PO) lifecycle (Draft to Closed).
- **Advanced Refund Workflow**: State-driven multi-step refund process with mandatory PIN authorization.
- **Modular Reporting**: Isolated modules for Sales, Inventory, Finance, and Tax insights.
- **Resilient Offline Queue**: Persistent sync queue with idempotency tracking.
- **Security & Audit**: Granular permission system and structured state-change logging.
- **Diagnostics Dashboard**: Real-time terminal health, database integrity, and storage monitoring.

### Improved
- **Database Performance**: Added Room indexes for 100k+ sale record scale.
- **UI Responsiveness**: Implemented stable keys and list virtualization for smooth 10k+ item product grids.
- **Hardware Resilience**: Hardened Printer HAL with automatic reconnection and exponential backoff.
- **Maintenance**: Automated daily database `VACUUM` and integrity verification.

### Technical Details
- **Room Database Version**: 38
- **Minimum Android SDK**: 24
- **Target Hardware**: Verified for IMIN Falcon, Sunmi V2, and standard 10-inch tablets.

---
*ExtroPOS v2.0.0 Stable is now frozen and ready for pilot deployment.*
