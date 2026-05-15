# Reporting Module Implementation Plan

This plan outlines the creation of a comprehensive reporting module for ExtroPOS v2. The module will be modular, feature-rich, and optimized for POS environments.

## Proposed Changes

### [Modular Architecture]

Create a new Gradle module `:feature:reporting` to isolate reporting logic from the main app.

#### `:feature:reporting/build.gradle.kts` [NEW]
- Add dependencies for `vico` (charts), `kotlin-csv`, and `hilt`.

---

### [Data Layer]

Enhance the data layer with specialized queries for aggregation.

#### `ReportingDao.kt` (in `:core` or `:feature:reporting`) [NEW]
- Define methods for Sales Summary, Product Performance, etc.
- Use `@Query` with SQL aggregation (`SUM`, `COUNT`, `GROUP BY`).

#### `ReportingRepository.kt` [NEW]
- Abstract data access for reports.
- Support filtering by date range and categories.

---

### [Domain Layer]

Define the business logic and models for various reports.

#### Report Models [NEW]
- `SalesSummaryReport`, `ProductPerformanceReport`, `InventoryReport`, `CommissionReport`.

#### UseCases [NEW]
- `GetSalesSummaryUseCase`, `GetProductPerformanceUseCase`, `ExportReportUseCase`.

---

### [UI Layer (Jetpack Compose)]

Implement modern, interactive reporting screens.

#### `ReportingDashboard.kt` [NEW]
- Overview with Vico charts (Bar/Line).

#### `ReportListScreen.kt` [NEW]
- Scrollable list of detailed reports.

#### `CommissionReportScreen.kt` [NEW]
- Dedicated view for staff earnings based on product settings.

---

### [Export & Sync]

#### `ExportService.kt` [NEW]
- Logic for generating CSV and PDF files.

---

## Verification Plan

### Automated Tests
- Unit tests for `CommissionCalculator` logic.
- DAO tests for aggregation queries.

### Manual Verification
1. Open Reporting dashboard.
2. Filter by different date ranges.
3. Verify chart data against sales history.
4. Export a Sales Summary PDF/CSV and check contents.
5. Verify staff commissions match product settings.
