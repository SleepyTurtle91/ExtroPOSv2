# ADR-001: Offline-First Architecture

## Context
POS systems in Malaysian SME environments (Retail, F&B) often face unstable network conditions. Business operations must not stop when the internet is unavailable.

## Decision
ExtroPOS v2 will use an **Offline-First** architecture. The local Room database is the primary source of truth for all business operations (Sales, Inventory, etc.).

## Consequences
- All data MUST be persisted locally before synchronization.
- UI must observe local database states.
- A synchronization engine must handle eventual consistency with the cloud backend.
