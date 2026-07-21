# ADR-003: Hardware Abstraction Layer (HAL)

## Context
ExtroPOS v2 needs to support multiple hardware brands (IMIN, Sunmi, Star, etc.) and connection types (USB, Bluetooth, Network).

## Decision
Implement a **Hardware Abstraction Layer (HAL)** using interfaces. Business logic interacts with generic interfaces (e.g., `PrinterInterface`) rather than vendor-specific implementations.

## Consequences
- Decouples business logic from peripheral drivers.
- Simplifies support for new hardware.
- Allows for easy mocking of hardware during testing.
