package com.extrotarget.extroposv2.core.data.local.converter

import androidx.room.TypeConverter
import com.extrotarget.extroposv2.core.data.model.platform.Capability
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryItem
import com.extrotarget.extroposv2.core.data.model.hardware.PrintJobStatus
import com.extrotarget.extroposv2.core.hardware.printer.PrintCommand
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun fromLaundryItemList(value: List<LaundryItem>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLaundryItemList(value: String): List<LaundryItem> {
        val listType = object : TypeToken<List<LaundryItem>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromCapabilitySet(value: Set<Capability>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCapabilitySet(value: String): Set<Capability> {
        val type = object : TypeToken<Set<Capability>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromPrintJobStatus(value: PrintJobStatus): String {
        return value.name
    }

    @TypeConverter
    fun toPrintJobStatus(value: String): PrintJobStatus {
        return PrintJobStatus.valueOf(value)
    }

    @TypeConverter
    fun fromPrintCommandList(value: List<PrintCommand>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toPrintCommandList(value: String): List<PrintCommand> {
        val listType = object : TypeToken<List<PrintCommand>>() {}.type
        return gson.fromJson(value, listType)
    }
}
