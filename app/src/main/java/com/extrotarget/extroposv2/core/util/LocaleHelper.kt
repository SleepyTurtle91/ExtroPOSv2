package com.extrotarget.extroposv2.core.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {
    fun applyLocale(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as LocaleManager
            localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
        } else {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    fun getDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "ms" -> "Bahasa Melayu"
            "zh" -> "简体中文 (Mandarin)"
            else -> "English"
        }
    }
}
