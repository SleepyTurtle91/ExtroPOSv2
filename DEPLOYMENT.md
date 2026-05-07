# ExtroPOS v2 - Deployment Guide

This document provides instructions for deploying ExtroPOS v2 to live merchants.

## 1. Release Build
To generate a production APK:
1. Ensure `isMinifyEnabled = true` in `app/build.gradle.kts`.
2. Run `./gradlew assembleRelease`.
3. The APK will be located in `app/build/outputs/apk/release/`.

## 2. Licensing
ExtroPOS v2 uses device-bound licensing tied to the Android ID (SSAID).
- **Key Generation**: Use the SHA-256 algorithm with the salt `EXTRO_SALT_2024` as defined in `LicenseManager.kt`.
- **Trial**: The app automatically starts a 14-day trial if no license is found.

## 3. LHDN Production Transition
To move from Sandbox to Production:
1. Go to **Settings > LHDN MyInvois**.
2. Uncheck **Enable Sandbox**.
3. Enter the merchant's **Production Client ID** and **Client Secret**.
4. Use the **Test LHDN Connection** button to verify.

## 4. Payment Terminal Integration
To link a live GHL/IPAY88 terminal:
1. Set the terminal type to `GHL_JSON` in code or via the future configuration UI.
2. Ensure the terminal is on the same network.
3. Configure the static IP and port (default 7000) of the terminal.

## 5. P2P Multi-Outlet Deployment
1. Designate the primary station as **Backend Mode (Master)**.
2. Connect all **Counter Mode (Slave)** devices to the same Local Area Network.
3. Verify discovery via **Settings > Multi-Terminal Sync**.
