# Hardware Integration - ExtroPOS v2

## HAL Strategy (Hardware Abstraction Layer)
Business code must never interact with hardware drivers directly. All hardware interactions are mediated by the HAL.

**Workflow:**
`Business Logic` -> `Hardware Interface (Core)` -> `Vendor Implementation (Plugin/Module)`

**Example (Printer):**
1. Business code calls `printer.print(receiptData)`.
2. The `PrinterInterface` (in Core) routes the command.
3. The specific implementation (e.g., `IMINPrinter`, `SunmiPrinter`, or `GenericEscPos`) handles the byte-level protocol.

This ensures that adding a new hardware vendor requires zero changes to the core business logic.

## Device Support
- **IMIN Focus**: Deep integration with IMIN POS hardware (integrated printers, secondary displays).
- **Cash Drawer**: Triggered via ESC/POS pulse command from the printer or direct GPIO on supported devices.
- **Barcode Scanners**: Handled via HID (Keyboard emulation) or Intent-based scanning for integrated hardware.

## Scale Integration
- Support for weighing scales via Serial/USB.
- Use `ScaleInterface` for abstraction to allow mocking and multi-vendor support.
