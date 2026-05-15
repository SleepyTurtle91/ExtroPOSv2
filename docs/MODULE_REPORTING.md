# Reporting & Analytics Module

The Reporting module provides deep business insights and tax compliance data.

## 1. Components

- **ReportingDao**: Contains aggregated SQL queries for high-performance data retrieval.
- **ReportingViewModel**: Manages date range filters and data streams.
- **Vico Charts**: Used for visual sales trends and category breakdowns.

## 2. Key Reports

### Sales Summary
- Aggregates Gross Sales, Net Sales, Tax, and Discounts.
- Calculated via `getSalesSummary` query using SQL `SUM` and `AVG`.

### Product Performance
- Ranks products by quantity sold and revenue generated.
- Supports category-level filtering.

### Tax Compliance
- Detailed breakdown by tax rate (6%, 8%, etc.).
- Ready for export to CSV for tax filing.

## 3. Data Integration

The module queries the `sales` and `sale_items` tables directly, ensuring that reports are always in sync with actual transactions.
