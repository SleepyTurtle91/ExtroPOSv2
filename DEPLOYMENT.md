# Deployment Log - ExtroPOS v2

This file tracks production releases and client deployments.

## Release History

### v2.0.0 (2024-05-07)
- **Status**: Release Candidate 1
- **Version Code**: 2
- **Key Changes**:
    - Transitioned to Production build configuration.
    - Enabled R8 obfuscation and resource shrinking.
    - Hardened financial integrity with `BigDecimal` keep rules.
    - Broadened hardware ProGuard rules for peripherals.
    - Automated environment-aware LHDN switching (`isSandbox` based on `BuildConfig.DEBUG`).
    - Implemented secure signing configuration (credentials in `local.properties`).

## Client Deployment Checklist

| Client Name | Version | Date | Status |
| ----------- | ------- | ---- | ------ |
| (Template)  | v2.0.0  |      | PENDING|

## Build Artifacts (CI/CD)
- **APK Path**: `app/build/outputs/apk/release/app-release.apk`
- **Mapping File**: `app/build/outputs/mapping/release/mapping.txt` (Required for de-obfuscating crash logs)
