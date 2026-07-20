# Implementation Plan - License Activation & Advanced Table Management

This plan finalizes the License Activation workflow and adds the missing UI for F&B Table Management (Bulk Add, Edit, Delete).

## User Review Required

> [!IMPORTANT]
> **License Locking**: If the license status is `Expired` (after 30-day trial + 3-day grace period), the app will **ONLY** show the Activation screen. All POS and Dashboard functions will be blocked until a valid key is entered.

> [!NOTE]
> **Bulk Table Prefix**: The bulk add function will use a naming convention like `Prefix + Number` (e.g., `A 1`, `A 2`). Does this meet your requirements?

## Proposed Changes

### 1. License System UI & Wiring

#### [NEW] [LicenseActivationScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/settings/license/LicenseActivationScreen.kt)
- Create a dedicated screen to display the formatted **Device ID** (e.g., `F4A7-92B1-CC10-D332`).
- Input field for the **Activation Key**.
- Status indicators for Trial, Grace Period, and Activated states.
- Instructions for the user on how to obtain a key.

#### [MODIFY] [MainScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/navigation/MainScreen.kt)
- Observe `licenseStatus` from `MainViewModel`.
- Implement a high-priority conditional check: if status is `Expired`, display `LicenseActivationScreen` as the sole UI component.

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/settings/SettingsScreen.kt)
- Add a "Software License" navigation item under "About & Support".

---

### 2. Table Management UI

#### [MODIFY] [TableFloorPlanScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/fnb/TableFloorPlanScreen.kt)
- **Bulk Add Dialog**: Create a dialog with inputs for Prefix, Start Number, Count, and Capacity.
- **Edit/Delete Dialog**: Implement a management dialog triggered by long-pressing a table card.
- **Duplicate Prevention**: Wire the UI to show an error if a user tries to create a table with an existing name.

#### [MODIFY] [TableViewModel.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/fnb/viewmodel/TableViewModel.kt)
- Expose a `UIEvent` or `ErrorMessage` flow to notify the UI when a duplicate name is detected or a bulk operation fails.

## Verification Plan

### Automated Tests
- Verify `LicenseUtils.calculateStatus` correctly handles the 33-day total period (30 trial + 3 grace).
- Verify `TableViewModel.bulkAddTables` skips duplicates correctly.

### Manual Verification
- **Activation Lock**: Set system date forward by 35 days and verify the POS is locked behind the Activation screen.
- **Bulk Table Setup**: Add 10 tables with prefix "T" and capacity 4. Verify they appear as "T 1", "T 2", etc.
- **Edit/Delete**: Long-press "T 1", change name to "VIP 1", and verify the change. Delete it and verify it disappears from the grid.
