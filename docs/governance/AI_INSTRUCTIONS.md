# AI Development Instructions - ExtroPOS v2

## 1. Pre-Coding Workflow (Mandatory)
Before writing any code, the AI agent MUST follow this sequence:
1. **Read AI Context**: Consult `Docs/AI_CONTEXT.md` for a high-level project overview, AI role, and capability matrix.
2. **Read Contract**: Consult `Docs/DEVELOPMENT_CONTRACT.md` for governing architectural and financial rules.
3. **Check ADRs**: Review relevant Architecture Decision Records in `Docs/architecture/decisions/` to understand the "Why" behind the design.
4. **Identify Module**: Determine if the change belongs in `:core` or a specific feature using the **Module Ownership** rules.
5. **Search Strategy**: Follow the search sequence defined in `Docs/AI_CONTEXT.md` to find existing logic.
6. **Implement**: Apply changes following Clean Architecture and the ** iron rules** of the contract.
7. **Update Docs**: Reflect any architectural or rule changes in `/Docs`.

## 2. AI Change Classification
Changes are categorized by risk and scope:

| Class | Scope | Requirements |
| :--- | :--- | :--- |
| **Small** | Bug fixes, UI tweaks | Direct implementation, minimal doc update. |
| **Medium**| New UI component, new API | Requires unit tests, update feature docs. |
| **Large** | Schema change, new Feature | Requires migration, full doc update, compliance check. |

## 2. Kotlin Development Rules
- **Immutability**: Prefer `val` and immutable collections. UI state must be an immutable `data class`.
- **Null Safety**: Avoid `!!`. Use safe calls or Elvis operator.
- **Concurrency**: Use Coroutines and Flow. Always specify `Dispatchers.IO` for database/network.
- **DI**: Use Hilt for all dependency management.

## 3. POS Integrity Rules
- **Money**: Use `BigDecimal` with `RoundingMode.HALF_EVEN`.
- **Offline-First**: Observe the local database. Ensure Room transactions are used for data integrity.
- **Hardware**: Code against interfaces in `core/hardware`. Do not hardcode peripheral logic.

## 4. Operational Protocol
- **Documentation**: Update `/Docs` for any architectural changes.
- **Change Logs**: Maintain a record of "Why" changes were made.
- **Failure Recovery**: If a fix fails three times, search external documentation (Android/Kotlin guides) before retrying.
