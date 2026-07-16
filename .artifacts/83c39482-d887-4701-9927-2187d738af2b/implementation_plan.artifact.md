# Implementation Plan - POS Final Wiring & Optimization

This plan completes the optimization of ExtroPOS v2 for small tablets and wires the remaining placeholder buttons to real actions.

## User Review Required

> [!NOTE]
> **Global Action Wiring**: I am wiring the "Open Drawer" and "Search" buttons in the shared `StitchTopBar` so they work globally.

> [!NOTE]
> **Cart Actions Menu**: The `MoreVert` button in the cart will now show a menu for actions like "Clear Cart", "Reprint Last", and "Cart Discount".

## Proposed Changes

### 1. Shared UI Finalization

#### [MODIFY] [StitchTopBar.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/components/stitch/StitchTopBar.kt)
- Ensure all callback parameters are used.
- Add "Search" focus trigger state.

#### [MODIFY] [MainScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/navigation/MainScreen.kt)
- Wire `StitchTopBar` buttons to execute drawer and search actions.

#### [MODIFY] [StitchCartSidebar.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/components/stitch/StitchCartSidebar.kt)
- Implement a `DropdownMenu` for the `MoreVert` button to expose cart-wide actions.

### 2. Logic Wiring

#### [MODIFY] [SalesViewModel.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/sales/viewmodel/SalesViewModel.kt)
- Add state to `SalesUiState` to request UI focus on the search field.
- Implement basic handlers/placeholders for `TRANSFER_TABLE` and `SPLIT_BILL` to avoid silent clicks.

### 3. Dashboard Cleanup

#### [MODIFY] [WorkspaceDashboardScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/navigation/WorkspaceDashboardScreen.kt)
- Wire the Notifications button to a simple "No notifications" Snackbar or Placeholder dialog.

## Verification Plan

### Automated Tests
- Build verification: `gradle :app:assembleDebug`.

### Manual Verification
- **TopBar Actions**: Verify "Open Drawer" in TopBar works across screens.
- **Cart Menu**: Verify `MoreVert` menu opens and actions (Clear Cart, Reprint) work correctly.
- **Search Focus**: Verify clicking the Search function key focuses the product search bar.
- **F&B Actions**: Verify `Transfer Table` and `Split Bill` trigger a "Coming Soon" or placeholder dialog instead of doing nothing.
