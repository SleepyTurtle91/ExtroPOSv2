# AI Task Initiation Template

Use this template when starting a new task with an AI agent to ensure consistent context and adherence to the Engineering Development Contract.

---

## 1. Task Definition
- **Task**: [Brief description of the work]
- **Goal**: [What is the desired outcome? e.g., "Implement cash payment with BNM rounding"]
- **Affected Module**: [e.g., `:feature:sales` or `:core:data`]

## 2. Context & Search
- **Relevant Docs**: [e.g., `Docs/BUSINESS_RULES.md`, `features/sales.md`]
- **Existing Implementation Checked**: [ ] Yes (Briefly list what was found to avoid duplication)

## 3. Constraints (Mandatory)
- **BigDecimal Only**: All money calculations must use `BigDecimal`.
- **Logic Ownership**: Business logic must reside in a `UseCase`.
- **Dependency Rules**: No forbidden imports.
- **Persistence**: Follow Room migration policy.

## 4. Expected Output
- [ ] Code implementation.
- [ ] Unit tests for business logic.
- [ ] Updated documentation (if architecture/rules changed).
- [ ] Compliance check against `Docs/DEVELOPMENT_CONTRACT.md`.

## 5. Definition of Success
"This task is complete when [describe the verifiable result]."
