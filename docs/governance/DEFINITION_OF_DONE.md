# Definition of Done (DoD)

All feature development and code modifications in ExtroPOS v2 must pass through these four mandatory gates before being considered "Done."

## 1. Code Gate
- [ ] **Module Ownership**: Code is placed in the correct module or package.
- [ ] **Architecture Compliance**: Follows `UI -> VM -> UseCase -> Repo -> Source`.
- [ ] **No Duplication**: Existing UseCases, Repositories, or Utility classes were reused where possible.
- [ ] **Forbidden Dependencies**: No illegal imports (e.g., Feature-to-Feature or Core-to-Feature).
- [ ] **Clean Code**: Meaningful names, no magic numbers, and single responsibility functions.

## 2. Business Gate
- [ ] **Business Rules**: Logic complies with `Docs/BUSINESS_RULES.md` (e.g., BigDecimal for money).
- [ ] **Edge Cases**: Empty states, error states, and invalid inputs are handled.
- [ ] **Offline Behavior**: Feature works correctly without a network connection.
- [ ] **Financial Integrity**: Calculation order and rounding policies are strictly followed.

## 3. Delivery Gate
- [ ] **Tests**: Unit tests added for critical business/financial logic.
- [ ] **Database**: Room migrations are tested and follow the immutability policy.
- [ ] **Documentation**: Feature docs in `/features/` or technical docs in `/Docs/` are updated.
- [ ] **AI Context**: `Docs/AI_CONTEXT.md` updated if the architecture or high-level status changed.

## 4. Regression Gate
- [ ] **Core Flow**: Existing sales checkout and payment flows remain operational.
- [ ] **Data Integrity**: Existing inventory deduction and stock ledger logic are unaffected.
- [ ] **Reporting**: Historical data and report aggregation remain accurate.
- [ ] **Compatibility**: No breaking changes to existing hardware or sync protocols.
