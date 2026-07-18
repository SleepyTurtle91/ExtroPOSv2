package com.extrotarget.extroposv2.core.util.backup

data class BackupMetadata(
    val backupVersion: Int = 1,
    val branchId: String,
    val dbVersion: Int,
    val deviceInfo: String,
    val timestamp: Long = System.currentTimeMillis()
)
