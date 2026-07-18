# ExtroPOS v2 - Strategic Product Roadmap

This document outlines the strategic evolution of ExtroPOS v2, transitioning from architectural foundation to operational maturity.

## ExtroPOS v2 Philosophy
ExtroPOS v2 is not a single POS application. It is a **Shared Commerce Platform** built around reusable business domains.

**Core principles:**
- **Shared Core first**: Logic once, use everywhere.
- **Capability-driven architecture**: Modular features (Inventory, Loyalty, Tax).
- **Offline-first**: Reliability in all environments.
- **Malaysia-first compliance**: LHDN and local business needs.
- **Domain isolation**: Clean separation between Retail, F&B, etc.
- **Production-grade maintainability**: Code built for the long term.
- **Long-term extensibility**: Ready for what comes next.

**Current Active Domains:**
- ✅ Retail
- ✅ F&B

**Future Domains:**
- ⏳ Laundry
- ⏳ Car Wash
- ⏳ Hotel / Homestay
- ⏳ Workshop
- ⏳ Pharmacy

---

## Strategic Milestones
- [x] **Milestone A**: Architecture Complete (Core, Events, Domain Separation)
- [ ] **Milestone B**: Retail Production Ready
- [ ] **Milestone C**: Restaurant Production Ready
- [ ] **Milestone D**: Commercial Release
- [ ] **Milestone E**: Cloud Sync & Multi-Branch
- [ ] **Milestone F**: Analytics Platform

---

## Phase 3: Business Workflow Completion (DONE)
**Goal**: Finalize the core business workflows that staff and managers interact with daily.

### 3A: Advanced Modifier UI (F&B)
- [x] **Grouping & Validation**: Support `ModifierGroup` logic (`minSelect`, `maxSelect`).
- [x] **UX Indicators**: Show selection progress (e.g., "Required 0/1") and disable "Add to Cart" until satisfied.
- [x] **Visual Clarity**: Explicit "*" and "Required" labels for mandatory groups.

### 3B: Kitchen Station Routing (F&B)
- [x] **Station Tabs**: Replace KDS dropdowns with high-speed tabs `[KITCHEN] [BAR] [DESSERT]`.
- [x] **Logic**: Filter order items based on the `kitchenStation` field in `MenuItem`.
- [x] **Badges**: Add "Pending Items" count badges to each tab.

### 3C: Stock Adjustment & Inventory Ledger (Retail/Core)
- [x] **Reason-Driven Adjustments**: Mandatory "Adjustment Type" and "Reason" fields.
- [x] **Smart Defaults**: Auto-populate reasons for types like "Damaged" while allowing edits.
- [x] **Accountability**: Every adjustment linked to a staff session and recorded in `StockMovement`.

### 3D: Security & Auditability (Core)
- [x] **Granular Permission Enum**: Transition from role-only checks to a `Permission` enum (`VOID_CART`, `PRICE_OVERRIDE`, `OPEN_CASH_DRAWER`, etc.).
- [x] **Authorization Dialog**: A reusable PIN-based dialog for granting elevated permissions.
- [x] **Structured Audit Log**: Record business decisions (e.g., Rejected/Approved discounts) with structured action types for reporting.

---

## Phase 3 Completion Criteria
The platform is considered Phase 3 complete when:
- [x] ✅ F&B modifiers validated at domain level.
- [x] ✅ Kitchen stations configurable via entities.
- [x] ✅ Inventory changes fully auditable with old/new values.
- [x] ✅ Sensitive actions permission protected (PIN-gate).
- [x] ✅ Business actions recorded in structured AuditLog.

---

## Phase 4: Operational Controls (DONE)
**Goal**: Manage the daily cash cycle and staff accountability.
- [x] **Shift Management**: Opening/Closing balance, shift duration tracking.
- [x] **Cash Drawer Control**: Triggering drawer opening via hardware abstraction.
- [x] **Standard Reports**: X-Report (Mid-shift) and Z-Report (End-of-day).

## Phase 4 Completion Criteria
The platform is considered operationally ready when:
- [x] ✅ Staff shifts supported with cash reconciliation.
- [x] ✅ Cash drawer lifecycle (Open/Drop/Close) supported.
- [x] ✅ End-of-day (Z-Report) reporting supported.
- [x] ✅ Staff can open/close shifts and cash differences are calculated.
- [x] ✅ Sensitive actions audited and inventory adjustments accountable.
- [x] ✅ Kitchen workflow fully operational with configurable stations.

The system is ready for pilot deployment.

---

## Phase 4.5: Production Hardening (DONE)
**Goal**: Prepare single-device stability and diagnostics before distributed synchronization.
- [x] **Diagnostics**: Application health dashboard and database integrity checker.
- [x] **Device Identity**: Unique Installation ID and Device Fingerprint tracking.
- [x] **Error Resilience**: Automated corrupted database recovery and structured logging.
- [x] **Crash Reporting**: Integrated crash tracking for offline environments.

---

## Phase 4.5 Completion Criteria
The platform is considered production-hardened when:
- [x] ✅ Every transaction is tagged with a unique Device Identity.
- [x] ✅ Application health can be verified via a diagnostics dashboard.
- [x] ✅ Database integrity is verified on startup with auto-recovery options.

---

## Phase 5: Resilience & Multi-Branch (DONE)
**Goal**: Ensure data safety and prepare for multi-site operations.
- [x] **Backup/Restore**: Local and cloud-based database backups.
- [x] **Branch Sync**: Conflict-resolution logic for multi-branch environments.
- [x] **Offline Queue**: Improved handling of pending sync actions.

---

## Phase 5 Completion Criteria
The platform is considered Phase 5 complete when:
- [x] ✅ Automated daily database backups (Local/Cloud).
- [x] ✅ Multi-branch stock transfers and visibility supported.
- [x] ✅ Conflict resolution logic implemented for data sync.
- [x] ✅ Resilient offline queue with retry logic and state tracking.

The system is now a pilot-ready distributed commerce platform.

---

## Phase A: Platform Stabilization (DONE)
**Goal**: Transition from feature growth to industrial reliability for **v2.0.0 Stable**.

### A1: Performance Optimization
- [x] **Room Indexes**: `@Index` on `Sale(timestamp)`, `StockMovement(productId)`, `Product(sku)`.
- [x] **Target Latency**: Cold start < 2s, search < 100ms.
- [x] **List Virtualization**: Optimize 10k+ product grid scrolling.

### A2: Reliability & Resilience
- [x] **Printer Recovery**: Auto-reconnect Bluetooth/USB with exponential backoff.
- [x] **Sync Robustness**: Hardened conflict resolution for multi-outlet data.
- [x] **Database Maintenance**: Periodic `VACUUM` and integrity checks.

### A3: UI/UX Consistency
- [x] **Tablet Mastery**: Dedicated high-density layouts for 10-inch devices.
- [x] **Standardized UI**: Shared component library for loaders, modals, and error states.

---

## Phase B: Production Readiness (Retail & F&B) (DONE)
**Goal**: Finalize end-to-end business workflows.

### B1: Supply Chain Workflow (Retail)
- [x] **PO Lifecycle**: Draft -> Submitted -> Approved -> Ordered -> Received -> Closed.
- [x] **Supplier Portal**: Full CRUD and purchase history.

### B2: Advanced Refund Workflow (Core)
- [x] **States**: Requested -> Manager Approved -> Processing -> Completed.
- [x] **Restock Choice**: Mandatory decision to restore stock upon refund.

### B3: Reporting Domain (`feature-reporting`)
- [x] Isolated sub-modules: Sales, Inventory, Finance, Staff, Tax, Purchase.

---

## Milestone: Architecture Freeze (COMPLETED)
Architecture is considered frozen when:
- Shared Commerce Platform and Capability System are finalized.
- `CommerceTransaction` model and Pricing Engine API are stable.
- Event Bus contracts and Domain boundaries are locked.

---

## Phase C: Comprehensive Documentation & Polish (DONE)
- [x] **PRODUCTION_CHECKLIST.md**: Deployment verification steps (Required for Stable tag).
- [x] **COMPATIBILITY_MATRIX.md**: Supported hardware and peripherals.
- [x] **Technical Docs**: `Architecture.md`, `WorkflowGuides.md`, and `Contributing.md`.

---

## Performance Targets
| Metric | Target |
| --- | --- |
| Cold Start | < 2.0 s |
| Product Search | < 100 ms |
| Barcode → Cart | < 150 ms |
| Checkout Finalize | < 1.0 s |
| Receipt Print | < 2.0 s |
| 100k Sales DB | Smooth Scroll |
| 10k Products | Smooth Grid |

---

## Phase 6: Compliance & Advanced Integrations
**Goal**: Meet local regulatory requirements and external system needs.
- [ ] **LHDN (E-Invoicing)**: Full compliance with offline-to-online submission logic.
- [ ] **Third-Party Integrations**: AutoCount, SQL Account, and other accounting exports.

## Phase 7: Analytics & Growth
**Goal**: Provide business owners with actionable insights.
- [ ] **Owner Dashboard**: High-level KPIs (Sales, Top Products, Low Stock).
- [ ] **Business Analytics**: Trend analysis and predictive stock ordering.
