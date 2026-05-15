# Enhance KeyGen and Mobile Variants

This plan outlines the enhancements for the `keygen` and `mobile` product flavors of the ExtroPOS v2 application. The focus is on improving utility functionality in the KeyGen tool and hardware integration/UI efficiency in the Mobile POS variant.

## Proposed Changes

### KeyGen Flavor Enhancements
Focus on making the key generation tool more user-friendly and providing scannable output.

#### [KeyGenScreen.kt](file:///C:/Users/abber/StudioProjects/ExtroPOSv2/app/src/keygen/java/com/extrotarget/extroposv2/ui/keygen/KeyGenScreen.kt)
- Integrate `QrCodeView` to display the generated activation key.
- Improve the layout using `Card` components for input and results.
- Add a "Clear" button to reset the state.

---

### Mobile Flavor Enhancements
Focus on hardware integration for handheld devices and optimizing the sales UI for smaller screens.

#### [IminPrinter.kt](file:///C:/Users/abber/StudioProjects/ExtroPOSv2/app/src/mobile/java/com/extrotarget/extroposv2/core/hardware/printer/IminPrinter.kt)
- Implement `printReceipt` using the `iMin` SDK's `PrinterHelper`.
- Map `PrintCommand` types to SDK-specific calls (text, QR, feed, etc.).

#### [MobileSalesLayout.kt](file:///C:/Users/abber/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/sales/MobileSalesLayout.kt)
- Add a horizontal scrollable category bar for faster product navigation.
- Add a camera scanner icon/button in the search bar area for quick barcode scanning.
- Improve bottom bar summary visibility.

---

## Verification Plan

### Automated Tests
- Not applicable for UI layout changes, but I will ensure the project builds for both flavors.

### Manual Verification
- **KeyGen Variant**:
    - Build and run the `keygen` flavor.
    - Enter a Device ID and verify the activation key is generated.
    - Verify the QR code is displayed and correctly represents the key.
- **Mobile Variant**:
    - Build the `mobile` flavor.
    - Inspect the `MobileSalesLayout` using Compose Preview (if possible) or by reviewing the code logic.
    - Verify the `IminPrinter` code correctly handles different `PrintCommand` types.
