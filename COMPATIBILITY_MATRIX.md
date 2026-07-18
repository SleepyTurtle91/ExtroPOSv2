# Hardware Compatibility Matrix - ExtroPOS v2.0.0 Stable

The following hardware has been verified for production use with ExtroPOS v2.

## 1. POS Tablets / Terminals
| Manufacturer | Model | OS Version | Notes |
| --- | --- | --- | --- |
| **Imin** | Swift 1 / 2 | Android 11+ | Built-in printer fully supported. |
| **Imin** | Swan 1 | Android 11 | 15.6" Desktop POS. |
| **Sunmi** | V2 / V2s | Android 11 | Handheld POS. |
| **Samsung** | Galaxy Tab A8/A9 | Android 13 | Tablet mode recommended. |

## 2. Receipt Printers (ESC/POS)
| Connection | Brand/Type | Resolution | Status |
| --- | --- | --- | --- |
| **Bluetooth** | Xprinter / Rongta | 58mm / 80mm | Verified |
| **USB** | Epson TM-T88VI | 80mm | Verified |
| **Network** | Star Micronics | 80mm | Verified |
| **Internal** | Imin/Sunmi Built-in | 58mm / 80mm | Native Driver |

## 3. Barcode Scanners
| Connection | Type | 1D/2D | Status |
| --- | --- | --- | --- |
| **Built-in** | Camera | Both | Verified (ML Kit) |
| **USB** | HID Keyboard Mode | Both | Plug & Play |
| **Bluetooth** | Netum / Zebra | Both | Verified |

## 4. Payment Terminals (Planned)
- [ ] **GHL** (API Integration)
- [ ] **Pine Labs**
- [ ] **Adyen**
