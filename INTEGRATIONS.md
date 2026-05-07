# ExtroPOS v2 - Integration Specifications

Documentation for developers extending the system or integrating with 3rd party APIs.

## 1. P2P Synchronization Protocol
ExtroPOS v2 uses Ktor WebSockets for real-time synchronization between Master (Backend) and Slave (Counter).

- **Service Discovery**: Uses Network Service Discovery (NSD) with type `_extropos_sync._tcp.`.
- **Default Port**: `8080`
- **Messages**: JSON formatted as `SyncMessage<T>`.
    - `PUSH_SALE`: Slave -> Master (New transaction)
    - `STOCK_UPDATE`: Master -> Slave (Broadcast stock changes)
    - `PRODUCT_SYNC`: Master -> Slave (Full product data refresh)

## 2. Hardware HAL (Hardware Abstraction Layer)
To add new hardware support, implement the following interfaces:

### Printers
Implement `PrinterInterface` in `com.extrotarget.extroposv2.core.hardware.printer`.
- Supports Bluetooth, USB, and Network (TCP).
- Uses ESC/POS encoding.

### Payment Terminals
Implement `TerminalInterface` in `com.extrotarget.extroposv2.core.hardware.terminal`.
- Standard template provided in `GhlTerminal`.

## 3. External API Mappings

### LHDN MyInvois
- **Base URL (Sandbox)**: `https://preprod-api.myinvois.hasil.gov.my/`
- **Base URL (Production)**: `https://api.myinvois.hasil.gov.my/`
- Mapping logic located in `InvoisMapper.kt`.

### AutoCount
- Integration via REST API.
- Configuration managed in `AutoCountSettingsViewModel`.
