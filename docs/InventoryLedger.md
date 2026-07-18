# Inventory Ledger Philosophy

## Source of Truth
Unlike traditional systems that only store a `stockQuantity` field, ExtroPOS v2 treats the **StockMovement** ledger as the source of truth.

## Why a Ledger?
- **Auditability**: Every stock change is linked to a reason (Sale, Restock, Damage, Refund) and a staff member.
- **Resilience**: In a multi-branch environment, we sync movements (deltas) instead of absolute states, which automatically resolves many synchronization conflicts.
- **Traceability**: Owners can see exactly why and when inventory changed.

## Workflow Integration
- **Sales**: Automatic `SALE` movement.
- **Refunds**: Mandatory decision to restock triggers a `RETURN` movement.
- **Purchase Orders**: Receiving a PO triggers a `RESTOCK` movement.
