# Business Modes & Modular Features

ExtroPOS v2 is a multi-purpose POS that adapts its UI and features based on the selected `BusinessMode`.

## 1. Supported Modes

The active mode is managed in `BusinessMode.kt`.

| Mode | Target Industry | Key Features |
| --- | --- | --- |
| `RETAIL` | Shops, Marts | Inventory, Barcode scanning. |
| `FNB` | Cafes, Restaurants | Table management, KDS, Modifiers. |
| `CARWASH` | Service Centers | Service tracking, Staff commissions. |
| `LAUNDRY` | Dobi | Weight-based pricing, Order lifecycle. |
| `HOTEL` | Hotels, Resorts | Room status, Bookings, Guest profiles. |
| `HOMESTAY`| Short-term Rentals | Simplified booking, Property management. |

## 2. Feature Flags

Each mode can enable specific system features via properties in the `BusinessMode` enum:
- `hasTables`: Enables Floor Plan / Table layout.
- `hasStaffAssignment`: Enables per-item or per-service staff tracking.
- `hasWeightSupport`: Enables integration with weighing scales.

## 3. Mode Selection

Users can switch modes via **Settings > Switch Business Mode**. This reconfigures the UI navigation and available management tools in the settings menu.
