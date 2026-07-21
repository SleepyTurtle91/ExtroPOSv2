# ExtroPOS v2 Architecture

## Overview
ExtroPOS v2 is a **Shared Commerce Platform** built on Clean Architecture principles, designed to support multiple industry domains (Retail, F&B, etc.) on a unified core.

## Clean Architecture Layers
- **UI (Jetpack Compose)**: Declarative UI layer. Views are passive and observe StateFlow from ViewModels.
- **ViewModel**: Manages UI state and handles user interactions by calling UseCases.
- **UseCase (Domain)**: Contains business logic. Each UseCase represents a single user action (e.g., `ProcessSaleUseCase`).
- **Repository (Data)**: Orchestrates data from different sources (Room, Network, Settings). Single source of truth.
- **Local Persistence (Room)**: Offline-first storage for all business entities.

## Module Rules & Governance (Strict Compliance)

### Rule A: Dependency Direction
**Required:**
- `Feature -> Core`
- `Feature -> Domain Models`

**Forbidden:**
- `Core -> Feature` (Circular dependency)
- `Feature A -> Feature B` (Tight coupling)
- `Room Entity -> UI` (Leakage of persistence details)

### Rule B: Logic Ownership
| Layer | Responsibility | Forbidden Actions |
| :--- | :--- | :--- |
| **UI** | Display State, Capture Input | Perform calculations, call Repositories |
| **ViewModel** | State Mapping, UI Logic | Business logic, direct DB access |
| **UseCase** | **Business Decisions**, Policy | UI state management, SQL queries |
| **Repository** | Data Coordination | Business logic |
| **Data Source**| Room/API Implementation | Business logic |

## Model Governance
- **Internal Entities**: Room `@Entity` classes are private to the Data layer. They must NOT leak across module boundaries.
- **Domain Models**: Use POJO/Data classes for business logic. Every Entity must have a mapper to/from a Domain Model.

## Key Subsystems
- **Capability System**: Decouples UI features from the core engine, allowing dynamic activation of features based on workspace configuration.
- **Sync Engine**: Idempotent, offline-first synchronization using an asynchronous queue. Ensures eventual consistency across terminals and the cloud.
- **Hardware HAL**: An abstraction layer for printers, scanners, and terminals to ensure hardware-agnostic business logic.
- **Stock Ledger**: Uses `StockMovement` records (deltas) as the source of truth for inventory auditability.

## Implementation Details
- **Dependency Injection**: Hilt is used globally. Field injection is avoided.
- **Concurrency**: Kotlin Coroutines for asynchronous tasks. Flow/StateFlow for reactive data streams.
- **UI Architecture**: MVVM/MVI patterns with StateHoisting for component reusability.

## Change Impact Map
When modifying these core areas, be aware of the following impacts:
- **Sales Calculation**: Affects `../product/BUSINESS_RULES.md`, reporting engine, receipt generation, and tax exports.
- **Product Entity**: Affects `../implementation/DATABASE.md`, inventory management, sales workflow, and Room migrations.
- **Hardware Interface**: Affects `../implementation/HARDWARE.md` and all industry-specific modules (Retail, F&B, etc.).
- **Workspace/Profile**: Affects `../implementation/SECURITY.md`, capability system, and initial onboarding.
