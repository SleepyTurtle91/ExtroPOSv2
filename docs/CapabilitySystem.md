# Capability System - ExtroPOS v2

The Capability System is the core mechanism that allows ExtroPOS v2 to adapt to different business types (Retail, F&B, Hotel, Laundry, Car Wash) without code duplication.

## 1. What is a Capability?

A Capability represents a distinct feature or business rule that can be toggled or configured for a specific branch or workspace.

### Core Capabilities:
- `RETAIL`: Enables SKU/Barcode management, inventory alerts, and supplier integration.
- `FNB`: Enables floor plans, table ordering, kitchen stations, and menu modifiers.
- `SERVICES`: Enables job tracking (e.g., Car Wash, Laundry) with staff commission logic.
- `HOSPITALITY`: Enables room scheduling, guest management, and stay durations.

## 2. Implementation

Capabilities are managed via the `WorkspaceEntity` and the `WorkspaceDao`.

### Dynamic UI
The UI layer uses the active `BusinessMode` and enabled capabilities to adjust layouts:
- `PosContentGrid` filters products based on the business mode.
- `SalesScreen` switches between a Retail-style search and an F&B-style category navigation.

### Domain Separation
While the `Product` entity is shared for common fields (ID, Name, Price), domain-specific logic is encapsulated in:
- `RetailProduct` for inventory and supply chain.
- `MenuItem` for F&B routing and modifiers.
- `CarWashJob` for service-specific workflows.

## 3. Benefits
- **Single codebase**: Maintain one app for multiple industries.
- **Modularity**: New industries (e.g., Pharmacy) can be added by implementing new capabilities.
- **Scalability**: Multi-branch support allows each branch to have different active capabilities.
