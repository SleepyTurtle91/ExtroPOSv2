# ExtroPOS v2 AI Context

**Version**: v2.2  
**Last Updated**: 2026-07-20  
**Governance Status**: Frozen Foundation (Mandatory Rules)

This document is the primary onboarding entry point for AI agents. It provides a compressed overview of ExtroPOS v2's identity, technology stack, and mandatory rules to ensure architectural consistency.

## AI Role
The AI agent acts as a senior software engineer contributing to an existing production system.
- **Goal**: Preserve established patterns, understand the architecture before modifying, and make minimal, correct changes.
- **Constraint**: Do not redesign existing systems or introduce new architectural patterns unless explicitly requested.

## Project Identity
ExtroPOS v2 is a modular, high-performance, offline-first Android POS platform built for Malaysian SMEs. It supports multiple industry domains (Retail, F&B, Hotel, Laundry, Car Wash) on a unified core engine with deep regulatory compliance.

## Technology Stack
- **Language**: Kotlin (1.9.24)
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture (MVVM/MVI)
- **DI**: Hilt (2.50)
- **Database**: Room (2.6.1) - Primary Authority
- **Compliance**: Malaysian SST, BNM 5-sen Rounding, LHDN e-Invoicing

## Module Ownership
- **`core/`**: Shared infrastructure (Data, Utils, Hardware HAL). No business features.
- **`domain/`**: Business models, Interfaces, and UseCases.
- **`feature-*/`**: Business capabilities (Sales, Inventory, etc.).
- **`app/`**: Application composition and entry point.

## Capability Matrix
| Capability | Retail | F&B | Hotel |
| :--- | :---: | :---: | :---: |
| Product Management | ✅ | ✅ | ✅ |
| Barcode Scanner | ✅ | Optional | ❌ |
| Table Management | ❌ | ✅ | ❌ |
| Room Management | ❌ | ❌ | ✅ |
| Kitchen Display | ❌ | ✅ | ❌ |
| Inventory | ✅ | ✅ | ✅ |
| Reports | ✅ | ✅ | ✅ |

## Governance & Execution Gates
1. **[AI_CONTEXT.md](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/Docs/AI_CONTEXT.md)**: Project orientation.
2. **[DEVELOPMENT_CONTRACT.md](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/Docs/DEVELOPMENT_CONTRACT.md)**: Supreme authority and iron rules.
3. **[DEFINITION_OF_DONE.md](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/Docs/DEFINITION_OF_DONE.md)**: Mandatory completion gate.
4. **[RELEASE_CHECKLIST.md](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/Docs/RELEASE_CHECKLIST.md)**: Production readiness gate.

## Project Map (Simplified)
- **`Docs/`**: Engineering Governance, Contracts, and Operational Controls.
  - `decisions/`: Architecture Decision Records (ADRs).
  - `AI_SESSIONS.md`: Handover log for AI development.
- **`features/`**: Feature-level technical references.
- **`app/src/main/java/com/extrotarget/extroposv2/`**:
  - `ui/`: Compose-based screens.
  - `core/`: Shared data, hardware, and utils.
  - `domain/`: Business brain (UseCases).
  - `feature/`: Domain-specific business logic.

## Search Strategy
1. Search the relevant **feature module**.
2. Search for existing **UseCases** in `domain/`.
3. Search **Repository** interfaces.
4. Search **Domain Models**.
5. Search existing **Tests** for logic examples.

## Context Loading Priority
For agents with limited context windows, load in this order:
1. **Priority 1**: `Docs/AI_CONTEXT.md`, `Docs/DEVELOPMENT_CONTRACT.md`
2. **Priority 2**: `Docs/ARCHITECTURE.md`, `Docs/DEFINITION_OF_DONE.md`
3. **Priority 3**: Relevant ADRs in `Docs/decisions/`
4. **Priority 4**: Relevant Feature Docs in `features/`

## AI Mistake Prevention
- **DO NOT** create a new `Service` class if a `UseCase` already exists.
- **DO NOT** use `Double`/`Float` for money.
- **DO NOT** bypass the `Repository` layer.
- **DO NOT** modify released Room migrations.
- **DO NOT** introduce feature-to-feature dependencies.
