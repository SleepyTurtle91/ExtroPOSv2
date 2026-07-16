# ExtroPOS v2 - Changelog

All notable changes to this project will be documented in this file.

## [2.1.0] - 2026-07-16
### Added
- **Tablet-First UI Optimization**: Implemented `BoxWithConstraints` for an adaptive checkout sidebar (320dp/360dp/420dp) and compact product cards for 8.7-inch screens.
- **Adaptive Function Grid**: New mode-aware F-key grid at the bottom of the product area for muscle memory, supporting Retail and Restaurant workflows.
- **Member Selection**: Integrated a new `MemberSelectionDialog` to easily assign customers to sales.
- **Laundry Module Wiring**: Fully wired the Quick Check-in sidebar with live scale integration and customer management.
- **Cart Actions Menu**: Added a `MoreVert` menu to the cart for clearing orders (with confirmation), reprinting, and discounts.

### Changed
- **Scrollable Dashboard**: Redesigned the rigid dashboard layout to support vertical scrolling and optimized padding for tablet usage.
- **Global Wiring**: Connected "Open Cash Drawer" and "Search Focus" to the global TopBar for faster accessibility.
- **UI Cleanup**: Removed redundant buttons from the sidebar to provide more order visibility.

### Fixed
- Resolved placeholder behavior in F&B table management (Transfer/Merge now provide feedback).
- Improved information density across Dashboard and Sales screens for smaller tablets.

## [2.0.1] - 2026-05-17
### Fixed
- **Code Quality Cleanup**: Addressed multiple static analysis warnings across core modules.
- **Improved Syntax**: Enforced trailing commas and named arguments for better code maintainability.
- **Logic Clarification**: Added parentheses to complex boolean expressions in Sync, LHDN, and UI logic to prevent ambiguity.
- **Import Optimization**: Removed unused imports in `SalesScreen.kt` and `SyncClient.kt`.

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
