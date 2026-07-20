# Project Non-Goals

To prevent scope creep and maintain architectural integrity, the following objectives are explicitly **OUT OF SCOPE** for ExtroPOS v2.

## 1. Cloud-Mandatory Operation
ExtroPOS v2 will **NOT** require a persistent internet connection for daily operations. We will not implement architectures that depend on real-time cloud validation for checkout or inventory management.

## 2. Platform Fragmentation
We will **NOT** create separate applications for each industry vertical (e.g., "ExtroRetail", "ExtroF&B"). All industry-specific logic must be handled via the internal Capability System within a single codebase.

## 3. Technology for Tech's Sake
We will **NOT** introduce new libraries or technologies (e.g., KMM, Compose Multiplatform, NoSQL) without a formal architectural review and an update to the `DEVELOPMENT_CONTRACT.md`.

## 4. Replacement of Local Authority
We will **NOT** replace the local Room database with a cloud-first database (e.g., Firestore) as the primary source of truth. The local database remains the absolute authority for the terminal.

## 5. Generic ERP Complexity
ExtroPOS v2 is a Point of Sale platform. It is **NOT** a full-scale ERP or Accounting system. While we support integrations (e.g., AutoCount), we will not build internal General Ledger or complex HR systems.
