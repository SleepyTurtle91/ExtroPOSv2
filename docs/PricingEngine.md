# Pricing & Compliance Engine

## Calculation Flow
The engine calculates totals in the following strict order to ensure Malaysian regulatory compliance:

1. **Base Price**: The unit price of the item.
2. **Modifiers**: Additive costs from selected modifier options.
3. **Discounts**: Item-level discounts (Fixed or Percentage).
4. **Service Charge**: Calculated on the discounted total (if applicable).
5. **Tax (SST)**: Calculated on the total including service charge but before rounding.
6. **Rounding (BNM)**: Final 5-sen rounding for cash transactions.

## Precision
All calculations use `BigDecimal` with `HALF_EVEN` rounding to prevent floating-point errors in financial totals.
