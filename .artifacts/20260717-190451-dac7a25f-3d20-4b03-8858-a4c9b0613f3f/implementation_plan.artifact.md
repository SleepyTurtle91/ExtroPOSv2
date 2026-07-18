# Implementation Plan - ExtroPOS v2.0.0 Stable

This plan shifts focus from rapid architectural growth to operational excellence and technical maturity, culminating in the **v2.0.0 Stable** release.

## Architectural Principles
- **Stabilization First**: No new major features until the platform is rock-solid.
- **Workflow Correctness**: Transition from single-action features to state-driven business processes.
- **Measurable Quality**: Target specific performance benchmarks for industrial hardware.
- **Architecture Freeze**: Lock the core platform foundations after Phase B.

## Proposed Changes

### Phase A: Platform Stabilization (Highest Priority)

#### A1: Performance Optimization
- **Room Indexing**: Add `@Index` to `Sale(timestamp)`, `StockMovement(productId)`, `Product(sku, barcode)`.
- **Target Latency**: Cold start < 2s, search < 100ms.
- **List Virtualization**: Optimize 10k+ product grid scrolling in Compose.

#### A2: Reliability & Resilience
- **Hardware HAL**: Auto-reconnect with exponential backoff and "Retry Print" buffering.
- **Maintenance**: Periodic `VACUUM` and `integrity_check` implementation.

#### A3: UI/UX Consistency
- **Standardized UI**: Shared component library for loaders, modals, and error states in `core:ui`.
- **Tablet Mastery**: Dedicated high-density 3-column layouts for 10-inch devices.

---

### Phase B: Production Readiness

#### B1: Purchase Orders (Retail Domain)
- **PO lifecycle**: `Draft` -> `Submitted` -> `Approved` -> `Ordered` -> `Received` -> `Closed`.
- **Auto-Stock**: Automatic `StockMovement` creation upon receiving items.

#### B2: Advanced Refund Flow (Core Domain)
- **Workflow**: `Requested` -> `Approved` (PIN) -> `Processing` -> `Completed`.
- **Restock Decision**: Mandatory selection to return items to inventory.

#### B3: Reporting Domain (`feature-reporting`)
- **Isolation**: Structured sub-modules for Sales, Inventory, Finance, Staff, Tax, and Purchase.

---

### Phase C: Comprehensive Documentation & Polish
- Create `docs/` folder: `Architecture.md`, `CapabilitySystem.md`, `SyncEngine.md`, `Reporting.md`.
- **PRODUCTION_CHECKLIST.md**: Deployment verification steps (Release gate).
- **COMPATIBILITY_MATRIX.md**: Verified hardware list.

---

## Verification Plan

### Performance Benchmarking
- **100k Challenge**: Verify smooth scrolling with 100k sales and instant search with 10k products.
- **Latency Audit**: Measure cold start and scan-to-cart speed on target tablets.

### Manual Verification
- **Resilience Loop**: Continuous printing with forced Bluetooth/USB disconnects.
- **Full Supply Chain**: Supplier -> PO lifecycle -> Receiving -> Sale -> Refund -> Restock.
- **Audit Consistency**: Verify `AuditLog` captures state changes across all new workflows.
