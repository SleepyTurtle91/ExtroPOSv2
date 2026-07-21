# Testing Strategy - ExtroPOS v2

## 1. Unit Testing
- **Financial Logic**: All currency calculations (Tax, Rounding, Pricing) must have 100% unit test coverage using JUnit 5.
- **UseCases**: Test business logic in isolation by mocking repositories.
- **ViewModels**: Test UI state transitions using `StateFlow` testing patterns.

## 2. Integration Testing
- **Database Migrations**: Every Room migration must be verified using the `MigrationTestHelper`.
- **Repository Sync**: Verify the offline-first sync engine logic under simulated network failures.

## 3. UI Testing
- **Composables**: Use Compose testing library for critical UI flows (e.g., adding item to cart).
- **Screen Flow**: End-to-end tests for the standard checkout process.

## 4. Hardware Simulation
- Use `MockPrinter` and `MockScale` to test peripheral logic without physical hardware.
