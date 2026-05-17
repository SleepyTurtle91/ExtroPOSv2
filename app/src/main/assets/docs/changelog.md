# ExtroPOS v2 - Changelog

All notable changes to this project will be documented in this file.

## [2.0.0] - 2026-05-15
### Added
- **Hotel & Homestay Room Selection**: Interactive room grid with real-time status tracking and booking management.
- **Booking Dialog**: Streamlined guest registration and check-in workflow for hospitality modes.
- **Mobile Sales Layout**: Optimized UI for handheld devices with horizontal category navigation and barcode scanner shortcuts.
- **Room Configuration**: Fully implemented Room Management screen to add, edit, and configure rooms and units.

### Changed
- **Navigation Sidebar**: Redesigned for better accessibility and fixed button overlapping (Lock/Logout/Settings).
- **Responsive Grids**: Optimized room and product grids for landscape tablet displays.

### Fixed
- Resolved "Unresolved reference: MobileSalesLayout" build error.
- Fixed sidebar layout stability on low-resolution tablet screens.

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
