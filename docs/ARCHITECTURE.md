# ExtroPOS v2 Architecture

## Overview
ExtroPOS v2 is a **Shared Commerce Platform** built on Clean Architecture principles, designed to support multiple industry domains (Retail, F&B, etc.) on a unified core.

## Core Layers
- **Shared Core**: Contains universal business rules (Tax, Rounding, Pricing) and Hardware Abstractions (Printer HAL).
- **Domains**: Pluggable modules like `domain-retail` and `domain-fnb` that extend the core with industry-specific workflows.
- **Data Layer**: Room-based local persistence with an eventually consistent synchronization engine.

## Key Subsystems
- **Capability System**: Decouples UI features from the core engine.
- **Stock Ledger**: Uses `StockMovement` records (deltas) as the source of truth for inventory.
- **Sync Engine**: Idempotent, offline-first synchronization using an asynchronous queue.
