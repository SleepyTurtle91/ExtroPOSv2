# Security & Access Control

## Role-Based Access Control (RBAC)
The system defines four primary roles:
1. **Owner**: Full system access, including financial settings and staff management.
2. **Manager**: Access to reports, inventory adjustments, and voiding sales.
3. **Cashier**: Restricted to sales operations, opening/closing shifts.
4. **Tech**: System-level maintenance, hardware configuration, and debugging.

Permissions are defined in `com.extrotarget.extroposv2.core.security.Permission`.

## Workspace Identity
- **WorkspaceEntity**: Defines the business identity.
- **Terminal ID**: Each device has a unique identity linked to the workspace.
- **Device Binding**: Licenses are bound to the hardware serial number.

## "Never Store" List (Strict Prohibition)
The following must **NEVER** be stored in cleartext or checked into source control:
- **License Secrets**: Must not be hardcoded.
- **API Keys**: Must be stored in `local.properties` (not Git) or retrieved via secure vault.
- **Encryption Keys**: Never store as string constants. Use **Android Keystore**.
- **Customer PII**: (Personally Identifiable Information) Must be encrypted at rest if stored locally.

## Cryptography Standards
- **Key Management**: All encryption keys must be generated and stored within the **Android Keystore System**.
- **At-Rest Encryption**: Use `EncryptedSharedPreferences` for small tokens and SQLCipher (or similar) for sensitive database fields.
