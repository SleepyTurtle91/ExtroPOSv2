# Implementation Plan: AI Silent Auditor & Google Drive Integration

This plan details the implementation of a Google-backed "AI Silent Auditor" powered by Gemini 3.5 Flash-Lite, integrated with Google Drive for secure, automated backups.

## User Review Required

- **AI Model Selection**: Using Gemini 3.5 Flash-Lite (via `generativeai` SDK) for high-speed, cost-effective auditing and NLP tasks.
- **Credential Storage**: Google Auth tokens will be securely stored in `EncryptedSharedPreferences`.
- **CSV Data Privacy**: When fixing CSV templates, only the schema and row structure (no customer sensitive data) are sent to the AI for repair.
- **Auto-Backup Frequency**: Should backups trigger on every **Shift Close (Z-Report)** or at a specific time (e.g., 3:00 AM)?

## Proposed Changes

### 1. Security & Authentication Layer

#### [GoogleAuthManager.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/core/auth/GoogleAuthManager.kt) [NEW]
- Implements Google Sign-In using the modern `Credential Manager API`.
- Manages ID tokens and Refresh tokens for Drive and AI services.

---

### 2. Google Drive Backup System

#### [DriveBackupManager.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/core/util/backup/DriveBackupManager.kt) [NEW]
- Uses `google-api-services-drive` to upload/download Room database snapshots.
- Implements "Version Rotational Backup" (keeps last 7 days).

#### [BackupWorker.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/core/work/BackupWorker.kt) [NEW]
- WorkManager task to trigger background sync when the device is charging and on Wi-Fi.

---

### 3. AI Silent Auditor Engine (Gemini Powered)

#### [GeminiProvider.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/core/network/ai/GeminiProvider.kt) [NEW]
- Centralizes the configuration for the `GenerativeModel`.

#### [SilentAuditorEngine.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/core/util/audit/SilentAuditorEngine.kt) [NEW]
- **Case 1: Sales Discrepancy**: Compares `Sales` vs `Shift` data to find missing cash.
- **Case 2: Insight Generation**: Analyzes `RawProductPerformance` to suggest best-sellers and slow items.
- **Case 3: CSV Repair**: Takes corrupted `*.csv` files, identifies schema errors, and generates a corrected version for `ProductImportManager`.
- **Case 4: Search Optimization**: Provides "semantic search" suggestions in the POS search bar.
- **Case 5: Inventory Smart-Audit**: Suggests stock adjustments based on sales velocity.

---

### 4. UI Enhancements

#### [AiAssistantOverlay.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/components/ai/AiAssistantOverlay.kt) [NEW]
- A non-intrusive floating bubble or sidebar for natural language report requests.

#### [ProductManagementScreen.kt](file:///C:/Users/HP/StudioProjects/ExtroPOSv2/app/src/main/java/com/extrotarget/extroposv2/ui/settings/inventory/ProductManagementScreen.kt)
- Add "AI Auto-Fix CSV" button to the import dialog.

---

## Verification Plan

### Automated Tests
- Unit tests for `SilentAuditorEngine` using mocked Gemini responses.
- `DriveBackupManagerTest`: Verifying file metadata creation.

### Manual Verification
- **CSV Repair**: Upload a CSV with a missing comma or incorrect header and verify the AI fixes it without crashing.
- **Natural Language Query**: Ask the AI "Show me card vs cash sales for last month" and verify it parses the `ReportingDao` data correctly.
- **Drive Backup**: Verify that after a shift closure, the backup file appears in the Google Drive "ExtroPOS_Backups" folder.
