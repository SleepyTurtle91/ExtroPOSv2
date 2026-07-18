# Production Deployment Checklist - ExtroPOS v2.0.0 Stable

This checklist must be completed and signed off before tagging a release as **Stable**.

## 1. Safety & Stability (Critical)
- [ ] **Zero Crash**: Zero unhandled exceptions in core workflows (Sales, Refund, Print).
- [ ] **Data Integrity**: Atomic transactions for all financial operations.
- [ ] **Persistence**: Verify data persists after hard reboot during a transaction.
- [ ] **Migrations**: Successful schema migration from all previous internal builds.

## 2. Technical Performance
- [ ] **Cold Start**: Application must be usable within 2.0 seconds.
- [ ] **Search Latency**: Product search must return results in < 100ms for 10k products.
- [ ] **Grid Performance**: Zero jank while scrolling 1k+ product grid.
- [ ] **Print Buffering**: Verify failed prints go to buffer and can be retried.

## 3. Business Logic Accuracy
- [ ] **Financials**: `BigDecimal` used for all money, `HALF_EVEN` rounding verified.
- [ ] **Tax Compliance**: LHDN logic correctly calculates and formats values.
- [ ] **Inventory**: Every stock change recorded in `StockMovement` with a reason.
- [ ] **Refunds**: PIN-gate authorization prevents unauthorized refunds.

## 4. Hardware Verification
- [ ] **Bluetooth**: Auto-reconnect tested with ESC/POS standard printers.
- [ ] **USB**: OTG connection verified on target Android 11+ tablets.
- [ ] **Built-in**: Imin and Sunmi internal printers verified.

## 5. Security
- [ ] **PIN Gate**: All restricted actions (Void, Refund, Price Override) are protected.
- [ ] **Proguard/R8**: Obfuscation and shrinking enabled for release build.
- [ ] **API Security**: No sensitive keys committed to repo.
