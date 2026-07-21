# Feature Matrix by Business Vertical

This document maps ExtroPOS v2 capabilities to specific industry verticals.

| Feature | Retail | F&B | Hotel | Dobi | Car Wash |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Inventory** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Barcode Scanning** | ✅ | Optional | ❌ | ❌ | ❌ |
| **Table Management** | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Room Booking** | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Kitchen Sync** | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Weight-Based Pricing** | ❌ | ❌ | ❌ | ✅ | ❌ |
| **Staff Commission** | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Loyalty Points** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **SST Compliance** | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## Capability Toggling
Features are enabled dynamically via the `CapabilitySystem` based on the active `WorkspaceEntity` profile.
