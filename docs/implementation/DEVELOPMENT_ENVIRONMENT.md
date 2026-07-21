# Development Environment Standards

This document defines the required environment for building and testing ExtroPOS v2 to ensure consistency across all contributors.

## 1. Version Locking
- **Kotlin**: `1.9.24`
- **Android Gradle Plugin (AGP)**: `8.5.1`
- **Gradle**: `8.x` (See `gradle-wrapper.properties`)
- **JDK**: `17` (Mandatory for build reproducibility)
- **Hilt**: `2.50`
- **Room**: `2.6.1`

## 2. Supported Platforms
- **OS**: Fedora Linux, Ubuntu LTS, or Windows 11.
- **IDE**: Android Studio Jellyfish or newer.
- **Physical Hardware**: IMIN or Sunmi POS devices (Target targets).
- **Emulators**: API 31+ with Play Store (for development only).

## 3. Standard Commands

### Build
```bash
./gradlew clean assembleDebug
```

### Test
```bash
./gradlew test
```

### Static Analysis
```bash
./gradlew lint
```

## 4. Coding Standards
- **Line Length**: 120 characters maximum.
- **Encoding**: UTF-8.
- **Indentation**: 4 spaces.
- **File Limits**: Files should not exceed 300 lines (See `ARCHITECTURE.md` Rule B).
