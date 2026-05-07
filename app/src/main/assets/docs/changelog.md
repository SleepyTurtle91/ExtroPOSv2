# ExtroPOS v2 - Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-05-06
### Added
- **Production Ready Release**: Core POS engine for Retail, F&B, Car Wash, and Laundry.
- **LHDN MyInvois Integration**: Real-time e-Invoicing compliance for Malaysia.
- **P2P Multi-Terminal Sync**: Local network synchronization without cloud dependency.
- **Integrated Payments HAL**: Support for GHL/IPAY88 standard JSON-over-TCP terminals.
- **Onboarding Wizard**: Streamlined setup for new merchants.
- **In-App Help & Documentation**: Integrated user manual.
- **Software Versioning**: In-app changelog and version display.

### Changed
- Refactored P2P networking for type-safety and modularity.
- Optimized UI sidebar for tablet high-density layouts.
- Decoupled tax logic for generic globalization (VAT/GST support).

### Fixed
- Resolved double-sidebar UI bug on POS screens.
- Fixed database WAL checkpointing during P2P export.
