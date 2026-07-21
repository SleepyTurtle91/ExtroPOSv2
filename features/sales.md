# Feature: Sales Reference

## Overview
The Sales feature is the primary interface for processing customer transactions. It supports multiple business modes (Retail, F&B, Service).

## Key Components
- **SalesViewModel**: Orchestrates the cart, payment selection, and finalization.
- **CartEngine**: Handles item additions, quantity updates, and discount applications.
- **PaymentProcessor**: Pluggable interface for Cash, Card, and E-wallet payments.

## Workflows
1. **Basket Building**: Scanning barcodes or selecting from the catalog.
2. **Modifier Selection**: (F&B) Customizing items with add-ons.
3. **Checkout**: Choosing payment method and applying rounding.
4. **Post-Sale**: Printing receipts and triggering cloud sync.

---
## Compliance Section
This feature must follow `Docs/DEVELOPMENT_CONTRACT.md`, `Docs/product/BUSINESS_RULES.md`, and `Docs/implementation/SECURITY.md`.
