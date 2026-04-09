# AI Agents Guidelines - ExtroPOS v2

This document outlines the operational protocols and strict rules for AI agents assisting in the development of the **ExtroPOS v2** system.

## 1. Primary Focus
- **Kotlin POS System Development**: All code generation, refactoring, and troubleshooting must prioritize Kotlin-first patterns, Jetpack Compose for UI, and robust offline-first architecture suitable for Point of Sale environments.

## 2. Failure Recovery Protocol
- **Three-Strike Rule**: If the AI fails to fix a specific code issue after **three (3) consecutive attempts**, it must:
    1. Acknowledge the failure.
    2. Utilize integrated search tools to find external solutions (StackOverflow, Kotlin documentation, Android Developer guides, or relevant GitHub issues).
    3. Synthesize the findings into a new solution.

## 3. Documentation & Context Awareness
- **Contextual Documentation**: Every significant change or new feature must be accompanied by documentation updates.
- **Context Awareness**: Before proposing changes, the agent should analyze existing architectural patterns (MVVM/MVI, Dependency Injection, Room database schemas) to ensure consistency.
- **Change Logs**: Maintain a clear record of "Why" a change was made, not just "What" was changed, to assist future agent sessions and human developers.

## 4. Mandatory AI Agent Rules

### Rule 1: Modular Development
- **Modularity is Mandatory**: Code must be organized into logical modules or distinct packages (e.g., `:core`, `:feature:sales`, `:feature:inventory`, `:data:local`).
- **Single Responsibility**: Each class or function must do one thing. If a file exceeds 300 lines, the agent must evaluate if it should be split into smaller components.
- **Dependency Inversion**: High-level modules should not depend on low-level modules. Both should depend on abstractions (interfaces).

### Rule 2: POS Financial Integrity
- **No Floating Point for Money**: Always use `BigDecimal` for currency and calculations.
- **Rounding Strategy**: Explicitly define rounding modes (e.g., `HALF_EVEN`) in all financial operations.

### Rule 3: Fail-Safe Operations
- **Hardware Abstraction**: Create interfaces for hardware (Printers, Scanners) so they can be mocked or swapped without breaking the business logic.
- **Offline Reliability**: Every data write must consider the "offline-first" state. Ensure Room transactions are used for data integrity.

### Rule 4: Documentation Before Execution
- **Architecture Updates**: If a new module or service is added, update the project's README or specific feature documentation before writing the implementation.
- **In-Code Comments**: Explain complex POS logic (like tax calculations or discount stacking) directly in the code.

## 5. Kotlin POS Best Practices
- **Precision**: Ensure currency and decimal handling uses `BigDecimal`.
- **Reliability**: Prioritize error handling for peripheral hardware and network fluctuations.
- **Security**: Follow strict data persistence guidelines for sensitive transaction data.
