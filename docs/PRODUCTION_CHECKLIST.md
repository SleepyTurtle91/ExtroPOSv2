# Production Release Checklist

A release cannot be tagged as **Stable** until every item in this checklist is verified.

## 1. Data Integrity
- [ ] Room migration tested and verified for new entities.
- [ ] Database backup package includes checksum and metadata.
- [ ] Restore process verified on a fresh installation.

## 2. Hardware Resilience
- [ ] Bluetooth printer automatically reconnects after power cycle.
- [ ] USB printer identified and ready after device reboot.
- [ ] Cash drawer triggers successfully via ESC/POS commands.

## 3. Compliance & Financials
- [ ] 5-sen rounding applies correctly to cash totals.
- [ ] SST (6%/8%) calculated before rounding.
- [ ] Refunds generate an entry in the Audit Log with old vs. new values.

## 4. Distributed Operations
- [ ] Offline sales appear in the queue and sync once connected.
- [ ] Duplicate event IDs are ignored by the server (Idempotency).
- [ ] Ledger-based stock sync resolves multi-branch conflicts.
