# Architecture Evolution Log

A high-level record of major architectural decisions, their reasoning, and their impact on ExtroPOS v2. This is NOT a commit log; it is a "Reasoning History."

---

## 2026-07: Governance & Capability Phase

### Formal Engineering Governance
- **Decision**: Introduced the `DEVELOPMENT_CONTRACT.md` and `AI_CONTEXT.md`.
- **Reason**: To prevent architectural drift during rapid feature expansion and ensure AI-assisted development remains high-quality.
- **Impact**: All contributions now follow a strict Clean Architecture and Financial Integrity protocol.

### Workspace & Capability System
- **Decision**: Implemented a dynamic "Capability" flag system (ADR-005).
- **Reason**: To support multiple industries (Retail, F&B, Hotel) from a single APK without forking codebases.
- **Impact**: Domain-specific UI and logic are toggled based on the business profile.

---

## 2026-05: P2P Sync & KDS

### Peer-to-Peer Kitchen Sync
- **Decision**: Introduced Ktor-based local network synchronization.
- **Reason**: Real-time kitchen updates are critical for F&B but should not depend on external cloud latency.
- **Impact**: Kitchen Display System (KDS) terminals can sync instantly with the Master station.

---

## 2024-01: Foundation Phase

### Offline-First & Room Authority
- **Decision**: Chose Room as the primary source of truth (ADR-001).
- **Reason**: Mission-critical POS availability in the Malaysian market.
- **Impact**: Full checkout capability remains available during internet outages.
