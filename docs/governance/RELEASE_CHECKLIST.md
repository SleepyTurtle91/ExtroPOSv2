# Release Readiness Checklist

This checklist must be completed and verified before any production release or terminal deployment of ExtroPOS v2.

## 1. Data & Persistence
- [ ] **Migration Check**: All Room migrations are tested on physical devices.
- [ ] **Schema Integrity**: Verify no existing columns were renamed or deleted (append-only).
- [ ] **Local Backup**: Verify that local database export/backup functionality is working.

## 2. Financial Verification
- [ ] **SST Calculation**: Verify tax calculations against standard test vectors.
- [ ] **BNM Rounding**: Verify 5-sen rounding logic for various cash totals.
- [ ] **Grand Total**: Verify that Subtotal + Tax + Rounding = Final Payment across multiple test carts.

## 3. Hardware Stability
- [ ] **ESC/POS Printing**: Verify receipt layout on target IMIN/Sunmi hardware.
- [ ] **USB/BT Connectivity**: Test peripheral reconnection after disconnect/reconnect cycles.
- [ ] **Cash Drawer**: Verify trigger command works correctly after a cash sale.

## 4. Security & Compliance
- [ ] **License Activation**: Verify that the license check flow works for new and existing installations.
- [ ] **Permission Matrix**: Verify that "Cashier" cannot access "Manager" or "Technician" settings.
- [ ] **Secrets**: Confirm no API keys or secrets are visible in logs or plain text.

## 5. Offline & Sync
- [ ] **Offline Mode**: Verify full checkout flow with Airplane Mode enabled.
- [ ] **Sync Recovery**: Verify that offline records sync successfully once the network is restored.
- [ ] **P2P KDS**: Verify order routing between Master and Kitchen terminals.

## 6. Performance
- [ ] **Database Size**: Check database performance with 10,000+ stock records.
- [ ] **UI Lag**: Verify 60fps scrolling on heavy product lists (LazyColumn optimization).
