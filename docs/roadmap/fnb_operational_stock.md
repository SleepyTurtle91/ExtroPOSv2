# Future Feature Concept: F&B Operational Stock Management

## Problem Observed
Small F&B businesses often avoid ingredient-based inventory because:
- Recipe setup is time-consuming.
- Staff must maintain accurate ingredient usage.
- Food wastage is difficult to track.
- Owners feel the system becomes too complicated for daily operations.

Most small restaurants only need visibility of **important kitchen supplies**, not full ingredient costing.

---

## Proposed Solution: Simple Kitchen Stock Tracking
Instead of automatic ingredient deduction based on recipes:
`Sales Item -> Recipe Engine -> Ingredient Deduction`

Use a manual **Kitchen Supply Tracking** model:
`Kitchen Supply -> Manual Usage Tracking -> Stock Monitoring`

### Core Workflow
Kitchen staff simply "consume" items as they are opened or used:
1. Staff: "Cooking oil finished."
2. Action: Taps `[ Take 1 ]` on the tablet.
3. Result: Inventory is decremented, owner is notified if low.

---

## Target Items (Kitchen Consumables)
✅ Cooking Oil (Bottle/Tin)  
✅ Gas Cylinder  
✅ Rice Bag  
✅ Flour  
✅ Sauce Bottle  
✅ Packaging Box / Plastic Bag  
✅ Tissue Roll  
✅ Gloves / Cleaning Supplies  

---

## User Experience (Staff Interface)
A simplified "Kitchen Dashboard" for stock consumption:
```
Cooking Oil
[ Take 1 ] [ Take 5 ] [ Custom ]
```
**Philosophy**: Don't force businesses to follow software; make software follow the natural workflow of the business.

---

## Owner Analytics
Instead of complex recipe-costing reports, the owner gets operational usage data:
```
Monthly Kitchen Usage (Example: Cooking Oil)
- Purchased: 50 bottles
- Used: 42 bottles
- Remaining: 8 bottles
- Estimated usage rate: 1.4 bottles/day
```

---

## Implementation Notes
- **Capability**: This should be a toggleable capability under the F&B module.
- **Simplicity**: No complex recipe linking required.
- **Auditability**: Every "Take" action must be recorded in the `StockMovement` ledger with the staff ID.
