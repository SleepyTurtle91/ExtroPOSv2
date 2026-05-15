# Architecture Overview - ExtroPOS v2

ExtroPOS v2 follows a **Clean Architecture** and **Modular** approach, designed for high reliability in offline-first POS environments.

## 1. Project Structure

- `:app`: The main Android application module. It contains the UI and integrates all features.
- `:core`: (Planned/Internal) Contains shared business logic, data models, and utilities used across all modules.
- `:feature:reporting`: Isolated module for business analytics, tax reports, and performance charts.
- `:feature:hotel`: Isolated module for Hospitality management (Rooms, Bookings, Guests).

## 2. Layers

### Data Layer
- **Room/SQLite**: The primary source of truth. All operations are performed locally first to ensure 100% offline uptime.
- **Repositories**: Abstract the data source (Local/Remote) from the rest of the application.
- **DAOs**: Located in `com.extrotarget.extroposv2.core.data.local.dao`.

### Domain Layer
- **Models**: Pure Kotlin data classes. Located in `com.extrotarget.extroposv2.core.data.model` (Entities) and `com.extrotarget.extroposv2.core.domain.model`.
- **UseCases**: Encapsulate specific business rules (e.g., `GetSalesSummaryUseCase`).

### UI Layer (Jetpack Compose)
- **MVVM Pattern**: Using Hilt for dependency injection.
- **ViewModels**: Manage UI state and interact with the Domain layer.
- **Navigation**: Centralized in `NavGraph.kt` using Type-safe routes.

## 3. Key Principles

- **BigDecimal for Money**: All financial calculations MUST use `BigDecimal` with `HALF_EVEN` rounding.
- **Offline-First**: Every write operation must target the local DB. Syncing with MongoDB/Appwrite is handled via `WorkManager` or background services.
- **Dependency Injection**: Hilt is used throughout for providing DAOs, Repositories, and ViewModels.
