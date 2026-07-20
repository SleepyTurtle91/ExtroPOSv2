# ADR-002: BigDecimal for Financial Calculations

## Context
Floating-point arithmetic (`Double`, `Float`) introduces rounding errors that are unacceptable in POS systems and financial reporting.

## Decision
All currency, quantities, and rates MUST use `java.math.BigDecimal` with a defined `RoundingMode` (typically `HALF_EVEN`).

## Consequences
- Strict prohibition of `Double`/`Float` for money.
- Centralized formatting and calculation logic in `CurrencyUtils`.
- Compliance with Malaysian regulatory requirements for financial precision.
