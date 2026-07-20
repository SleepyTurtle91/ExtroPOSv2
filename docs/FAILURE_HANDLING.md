# Failure Handling Policy

In a Point of Sale (POS) environment, reliability is paramount. The system must assume that infrastructure (hardware, network, or power) will fail and ensure that business state is never corrupted.

## 1. Reliability Principle
**A temporary infrastructure failure must not corrupt the business state.** Business data (Sales, Inventory, Audit Logs) takes priority over hardware availability.

## 2. Data Failure
- **Atomicity**: Use Room `@Transaction` for multi-step operations (e.g., Sale + Stock Movement). No partial transactions allowed.
- **Rollback**: If a database write fails, the entire operation must be rolled back safely, leaving the system in a consistent state.
- **Retry**: Allow the user to retry the specific operation without losing their current input (e.g., the shopping cart).

## 3. Hardware Failure
- **Non-Fatal Peripherals**: A failure in a printer, cash drawer, or scanner must NOT cancel a completed sale.
- **Retry Queue**: If a receipt fails to print, the sale remains "Completed." The receipt print job should be moved to a retryable queue.
- **Manual Override**: Provide UI options for "Skip Printing" or "Try Again" to prevent blocking the checkout line.

## 4. Network Failure
- **Offline Continuity**: The system must operate normally without an internet connection.
- **Sync Queue**: Outbound data (Sync records) must be queued locally. The user should see a "Pending Sync" status, but operations remain unblocked.
- **Conflict Resolution**: The system uses a "last write wins" or explicit timestamp-based resolution for eventual consistency.

## 5. User Recovery Rules
When an operation fails, the user must be informed with:
1. **Context**: What exactly happened (e.g., "Printer disconnected").
2. **Status**: Whether their data was saved (e.g., "Sale saved successfully").
3. **Action**: Clear next steps (e.g., "Reconnect printer and tap Retry").
*Avoid cryptic system codes like "Error 500."*
