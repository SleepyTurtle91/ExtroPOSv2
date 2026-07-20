# ADR-004: Clean Architecture & Modular Boundaries

## Context
As ExtroPOS v2 scales to support multiple business domains (Retail, F&B, Hotel), there is a high risk of tight coupling between features and logic leakage into the UI.

## Decision
We strictly enforce **Clean Architecture** with hard modular boundaries.
- **Features** are isolated.
- **Core** contains shared infrastructure.
- **Domain** contains business logic (UseCases) and models.
- **Dependency Flow**: Feature -> Domain -> Core.

## Consequences
- No feature-to-feature dependencies allowed.
- Business logic MUST reside in UseCases, never in ViewModels or UI.
- Use of Domain Models is mandatory to isolate features from persistence details (Room Entities).
