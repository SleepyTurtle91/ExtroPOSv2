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

## Phase 3: Business Workflow Completion (CURRENT)
**Goal**: Finalize the core business workflows that staff and managers interact with daily.

### 3A: Advanced Modifier UI (F&B)
- [ ] **Grouping & Validation**: Support `ModifierGroup` logic (`minSelect`, `maxSelect`).
- [ ] **UX Indicators**: Show selection progress (e.g., "Required 0/1") and disable "Add to Cart" until satisfied.
- [ ] **Visual Clarity**: Explicit "*" and "Required" labels for mandatory groups.

### 3B: Kitchen Station Routing (F&B)
- [ ] **Station Tabs**: Replace KDS dropdowns with high-speed tabs `[KITCHEN] [BAR] [DESSERT]`.
- [ ] **Logic**: Filter order items based on the `kitchenStation` field in `MenuItem`.
- [ ] **Badges**: Add "Pending Items" count badges to each tab.

### 3C: Stock Adjustment & Inventory Ledger (Retail/Core)
- [ ] **Reason-Driven Adjustments**: Mandatory "Adjustment Type" and "Reason" fields.
- [ ] **Smart Defaults**: Auto-populate reasons for types like "Damaged" while allowing edits.
- [ ] **Accountability**: Every adjustment linked to a staff session and recorded in `StockMovement`.

### 3D: Security & Auditability (Core)
- [ ] **Granular Permission Enum**: Transition from role-only checks to a `Permission` enum (`VOID_CART`, `PRICE_OVERRIDE`, `OPEN_CASH_DRAWER`, etc.).
- [ ] **Authorization Dialog**: A reusable PIN-based dialog for granting elevated permissions.
- [ ] **Structured Audit Log**: Record business decisions (e.g., Rejected/Approved discounts) with structured action types for reporting.

---

## Phase 4: Operational Controls
**Goal**: Manage the daily cash cycle and staff accountability.
- [ ] **Shift Management**: Opening/Closing balance, shift duration tracking.
- [ ] **Cash Drawer Control**: Triggering drawer opening via hardware abstraction.
- [ ] **Standard Reports**: X-Report (Mid-shift) and Z-Report (End-of-day).

## Phase 5: Resilience & Multi-Branch
**Goal**: Ensure data safety and prepare for multi-site operations.
- [ ] **Backup/Restore**: Local and cloud-based database backups.
- [ ] **Branch Sync**: Conflict-resolution logic for multi-branch environments.
- [ ] **Offline Queue**: Improved handling of pending sync actions.

## Phase 6: Compliance & Advanced Integrations
**Goal**: Meet local regulatory requirements and external system needs.
- [ ] **LHDN (E-Invoicing)**: Full compliance with offline-to-online submission logic.
- [ ] **Third-Party Integrations**: AutoCount, SQL Account, and other accounting exports.

## Phase 7: Analytics & Growth
**Goal**: Provide business owners with actionable insights.
- [ ] **Owner Dashboard**: High-level KPIs (Sales, Top Products, Low Stock).
- [ ] **Business Analytics**: Trend analysis and predictive stock ordering.
