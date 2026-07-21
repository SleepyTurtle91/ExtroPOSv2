# Engineering Development Contract & Governance Model

## 0. Entry Point
AI agents should begin by reading **Docs/AI_CONTEXT.md** for a compressed project overview before applying the rules in this contract.

## 1. Supreme Authority Statement
This document constitutes the **Supreme Authority** for the ExtroPOS v2 project.
 All technical decisions, code reviews, and architectural evolutions must comply with the rules established herein. Conflicts between this contract and implementation code must be resolved in favor of the contract.

---

## 2. Architecture Pillars (The "Iron Rules")

### Rule A: Dependency Direction
- **One-Way Flow**: Features -> Core only.
- **Strict Isolation**: 
    - `Core` must NEVER depend on any `Feature`.
    - `Feature A` must NEVER depend on `Feature B`.
    - Features communicate only via the `Core` API or Shared UseCases.

### Rule B: Logic Ownership (The Boundary Matrix)
- **UI (Compose)**: Pure display and user interaction logic. Zero business decisions.
- **ViewModel**: State orchestration and navigation. Bridges UI to UseCases.
- **UseCase**: The **only** place for business decisions and rules.
- **Repository**: Abstraction of data sources. No business logic.
- **Data Source (Room/API)**: Implementation of persistence and networking.

---

## 3. Financial Integrity Governance
Financial accuracy is non-negotiable.

- **Mandatory Types**: All currency, quantities, and rates MUST use `java.math.BigDecimal`. `Double` and `Float` are strictly forbidden for financial calculations.
- **Calculation Order Pipeline**:
    `Base Price` -> `Discount` -> `Taxable Amount` -> `SST/Tax` -> `Rounding` -> `Final Total`.
- **Intermediate Rounding**: Prohibited. Rounding MUST only occur at the final stage of the pipeline or when explicitly required by law (e.g., BNM 5-sen rounding).
- **Immutability**: Once a transaction is finalized, all associated financial values must be immutable.

---

## 4. Persistence Governance
- **Primary Authority**: Room is the single source of truth for the terminal.
- **Offline-First**: Every write must succeed locally before being queued for synchronization.
- **Immutable Migrations**: Database schemas are append-only. Existing migrations must never be modified.
- **Model Integrity**: Room entities are internal to the data layer. They must be mapped to **Domain Models** before crossing into the UseCase or UI layers.

---

## 5. Security & Hardware Pillars
- **Security**: 
    - Use Android Keystore for encryption keys.
    - Zero secrets in source control (Git).
    - Adhere to the "Never Store" list in `implementation/SECURITY.md`.
- **Hardware (HAL)**:
    - Business logic must interact with generic interfaces (e.g., `PrinterInterface`).
    - Vendor-specific implementations (IMIN, Sunmi, Star) must be isolated behind the HAL.

---

## 6. Interface Ownership Rule
Interfaces MUST be owned by the layer that defines the required behavior (the consumer), not the layer that provides the implementation (the provider).
- **Correct**: `Domain` defines `PaymentProcessor` interface -> `Infrastructure` implements it.
- **Incorrect**: `Payment SDK` defines the interface -> `Domain` imports it.

---

## 7. Compliance & Enforcement
All code contributions must include a "Compliance Check" against this contract. Failure to adhere to these pillars is grounds for immediate rejection of the change.

---

## 7. Documentation Synchronization Rule
Documentation MUST evolve with architecture changes. Any change that affects modules, database entities, business rules, hardware support, security models, or feature capabilities MUST update the relevant documentation in `/Docs` and `/features`.
