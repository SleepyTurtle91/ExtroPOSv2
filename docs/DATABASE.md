# Database Documentation - ExtroPOS v2

## Entity Ownership Matrix
The following matrix defines which module "owns" the data and lifecycle of specific entities.

| Entity | Primary Owner | Module/Package |
| :--- | :--- | :--- |
| `ProductEntity` | Inventory | `:core:data:local:entities` |
| `SaleEntity` | Sales | `:core:data:local:entities` |
| `StockMovement` | Inventory | `:core:data:local:entities` |
| `StaffEntity` | Authentication| `:core:data:local:entities` |
| `LicenseEntity` | Licensing | `:core:data:local:entities` |

## Persistence Internal Boundary
To maintain the integrity of our architecture, the following boundary rules are enforced:
1. **No External Exposure**: Room entities are internal to the persistence layer.
2. **Domain Mapping**: All data fetched from Room must be mapped to a Domain Model before leaving the Repository.
3. **Internal DAOs**: DAOs are only accessible within the Data layer or via Hilt-provided Repositories.

## Migration Policy
1. **Immutable Migrations**: Never edit existing migration files once they are committed.
2. **Sequential Schema**: Always create a new migration for any schema change.
3. **Audit Trail**: Every migration must be documented in `Docs/DATABASE_MIGRATIONS.md` (legacy: `docs/database/migrations.md`).
4. **Offline Resilience**: Ensure default values are provided for new columns to avoid breaking offline terminals.

## Data Persistence Rules
- **Room First**: All data writes must target Room first to ensure offline integrity.
- **Transactions**: Use `@Transaction` for multi-table writes (e.g., Sale + StockMovement).
- **Relational Integrity**: Use ForeignKeys with appropriate `onDelete` actions (usually `CASCADE` for child items, `RESTRICT` for lookups).
