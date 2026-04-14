package com.extrotarget.extroposv2.core.data.local.converter

import androidx.room.TypeConverter
import com.extrotarget.extroposv2.core.data.model.dobi.LaundryItem
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
}
