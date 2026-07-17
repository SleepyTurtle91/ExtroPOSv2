# Implementation Plan - License Activation & Table Management

This plan addresses two critical needs: implementing a 30-day license activation system (HWID-based) and adding comprehensive table management for Restaurant/Cafe modes (Bulk Add, Edit, Delete).

## User Review Required

> [!IMPORTANT]
> **Activation Lock**: After the 30-day trial expires, the app will lock and redirect to the Activation screen. You will need to provide your Device ID to get an activation key.

> [!NOTE]
> **Bulk Table Addition**: How would you like the bulk naming to work? (e.g., prefix "Table" + start number 1 + count 10 = Table 1, Table 2... Table 10).

## Proposed Changes

### 1. License Activation System

#### [MODIFY] [AppConfig.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/core/config/AppConfig.kt)
- Set `TRIAL_DURATION_DAYS = 30`.

#### [MODIFY] [LicenseManager.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/core/license/LicenseManager.kt)
- Update trial logic to 30 days.
- Ensure `getDeviceId()` returns the clean `ANDROID_ID` for display.

#### [NEW] [LicenseActivationScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/settings/license/LicenseActivationScreen.kt)
- UI to display HWID, enter key, and see trial remaining.

#### [MODIFY] [MainScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/navigation/MainScreen.kt)
- Implement full-screen block if license is expired.

---

### 2. Table Management (F&B)

#### [MODIFY] [TableViewModel.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/fnb/viewmodel/TableViewModel.kt)
- Add `bulkAddTables(prefix: String, startNumber: Int, count: Int, capacity: Int, zone: String)`.
- Add `updateTable(table: Table)` and `deleteTable(tableId: String)`.

#### [MODIFY] [StitchTableCard.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/components/stitch/StitchTableCard.kt)
- Add `onLongClick` support to the surface to trigger management actions.

#### [MODIFY] [TableFloorPlanScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/fnb/TableFloorPlanScreen.kt)
- Add a Floating Action Button (FAB) or "Manage" button.
- Implement dialogs for:
    - **Bulk Add Tables**: Input prefix, start number, count, and capacity.
    - **Edit/Delete Table**: Triggered on long-press of a table card.

## Verification Plan

### Automated Tests
- Verify `LicenseManager` 30-day calculation logic.
- Verify `TableViewModel` bulk add generates the correct number of tables.

### Manual Verification
- **Activation**: Expire the trial manually and verify the app locks. Enter a valid HWID key and verify unlocking.
- **Bulk Add**: Add 20 tables at once and verify they appear in the grid.
- **Edit/Delete**: Long-press "Table 5", change its capacity, and verify the update. Delete it and verify it's gone.
