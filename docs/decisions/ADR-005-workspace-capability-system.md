# ADR-005: Workspace Profile & Capability System

## Context
ExtroPOS v2 is a multi-mode platform. A single installation might be a small Retail kiosk, a full F&B restaurant, or a Hotel with add-on services.

## Decision
We use a **Workspace Profile and Capability System** to dynamically control application behavior.
- **Workspace Profile**: Defines the identity and "Mode" of the business.
- **Capabilities**: Fine-grained flags that toggle specific UI and business features (e.g., `TableManagement`, `BarcodeScanning`).

## Consequences
- Prevents "Hardcoding" industry-specific logic into the core.
- Allows the UI to adapt dynamically based on the enabled capabilities.
- AI must not simplify this into a global settings table; it is a foundational decoupled architecture.
