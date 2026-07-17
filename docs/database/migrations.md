# Database Migrations - ExtroPOS v2

This document tracks the schema evolution of the ExtroPOS v2 Room database.

## Migration History

### Version 27 -> 28
- **Date**: 2026-07-16
- **Changes**: 
    - Added `code` (TEXT) to `fnb_tables`.
    - Added `displayOrder` (INTEGER) to `fnb_tables`.
- **Reason**: Enable internal shorthand IDs and manual reordering for restaurant tables.

### Version 28 -> 29
- **Date**: 2026-07-16
- **Changes**:
    - Created `retail_products` table.
    - Created `fnb_menu_items` table.
- **Reason**: Decouple shared `Product` entity from industry-specific fields (Shared Commerce Platform refactor).

### Version 29 -> 30
- **Date**: 2026-07-16
- **Changes**:
    - Created `fnb_modifier_groups` table.
    - Created `fnb_modifier_options` table.
- **Reason**: Support advanced F&B workflows (Required/Optional modifiers).

### Version 30 -> 31
- **Date**: 2026-07-16
- **Changes**:
    - Refactored `stock_movements` table for better audit trail.
    - Added `createdBy` (TEXT) for accountability.
    - Added `reason` (TEXT) for stock adjustments.
    - Added `referenceId` (TEXT) to link to Sales or Purchase Orders.
    - Switched `type` to use `StockMovementType` enum (SALE, RESTOCK, ADJUSTMENT, RETURN, TRANSFER).
- **Reason**: Transition to an audit ledger system for enterprise-grade inventory integrity.

### Version 31 -> 32
- **Date**: 2026-07-16
- **Changes**:
    - Created `suppliers` table.
    - Created `purchase_orders` table.
    - Created `purchase_order_items` table.
- **Reason**: Implement Retail-specific supply chain and restock workflows.

### Version 32 -> 33
- **Date**: 2026-07-16
- **Changes**:
    - Created `fnb_orders` table.
    - Created `fnb_order_items` table.
- **Reason**: Support F&B order lifecycle (Open -> Sent -> Paid) separate from payment transaction.
