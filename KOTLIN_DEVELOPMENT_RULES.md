# Kotlin Development Rules - ExtroPOS v2

This document defines the coding standards and architectural rules for all Kotlin development within the ExtroPOS v2 project.

## 1. Code Style & Language Features
- **Immutability First**: Always prefer `val` over `var`. Use immutable collections (`List`, `Map`, `Set`) by default.
- **Expression-style Functions**: Use single-expression functions (`fun add(a: Int, b: Int) = a + b`) where it improves readability for simple logic.
- **Null Safety**: Avoid using `!!`. Use safe calls `?.`, the Elvis operator `?:`, or `requireNotNull()` / `checkNotNull()` for explicit contract enforcement.
- **Explicit Visibility**: Always define visibility modifiers (`private`, `internal`, `public`).
- **Data Classes**: Use `data class` for all models and UI state holders. Ensure they are immutable.

## 2. Architecture & Dependency Injection
- **MVVM Pattern**: Strictly follow the Model-View-ViewModel pattern.
    - **View**: Jetpack Compose only. No logic allowed in Composables.
    - **ViewModel**: Manage UI state using `StateFlow`. Use `viewModelScope` for coroutines.
    - **Repository**: Single source of truth for data logic.
- **Hilt for DI**: All dependencies must be provided via Hilt. Field injection is prohibited except in Android components where constructor injection is impossible.
- **Modularization**: Package by feature (e.g., `com.extrotarget.extroposv2.feature.sales`).

## 3. Concurrency & Data Flow
- **Coroutines**: Use `kotlinx.coroutines`. Avoid `GlobalScope`.
- **Flow**: Use `Flow` for cold streams (database queries) and `StateFlow` for hot streams (UI state).
- **Dispatchers**: Always specify dispatchers. Use `Dispatchers.IO` for disk/network, `Dispatchers.Default` for CPU-intensive tasks (complex calculations), and `Dispatchers.Main` for UI updates.

## 4. POS Specific Integrity (STRICT)
- **Financial Precision**: 
    - **NEVER** use `Float` or `Double` for currency.
    - **ALWAYS** use `BigDecimal`.
    - Always specify `RoundingMode` (default: `HALF_EVEN`).
- **Offline-First**: All data must be persisted to Room before attempting network synchronization. The UI must observe the local database.
- **Unit Formatting**: Use `CurrencyUtils` for formatting. Do not format currency strings in ViewModels or Composables.

## 5. Jetpack Compose Rules
- **State Hoisting**: Hoist state to the highest relevant caller to keep components reusable and testable.
- **Stability**: Ensure UI models are `@Immutable` or `@Stable`.
- **Previews**: Every reusable Composable must have a `@Preview` function.
- **Theme**: Use `MaterialTheme` color scheme and typography. No hardcoded colors or text sizes.

## 6. Error Handling
- **Result Pattern**: Use a `Result` or `Resource` wrapper for repository returns to handle Success/Error states explicitly.
- **Fail Fast**: Use `require()` and `check()` for internal state validation.
