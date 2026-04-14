# --- ExtroPOS v2 ProGuard Rules ---

# Preserve Line Numbers for Debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Room Persistence ---
-keep class * extends androidx.room.RoomDatabase
-keep class * { @androidx.room.Dao *; }
-keep class * { @androidx.room.Entity *; }
-keep class * { @androidx.room.PrimaryKey *; }

# --- Hilt / Dagger ---
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ComponentManager
-keep @dagger.hilt.android.EntryPoint class *
-keep @dagger.hilt.InstallIn class *

# --- Gson / Retrofit / OkHttp ---
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Preserve LHDN and DuitNow Models for JSON Mapping
-keep class com.extrotarget.extroposv2.core.data.model.lhdn.** { *; }
-keep class com.extrotarget.extroposv2.core.network.api.lhdn.** { *; }
-keep class com.extrotarget.extroposv2.core.data.model.settings.DuitNowConfig { *; }

# --- Ktor ---
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.json.** { *; }

# --- Security & Crypto ---
-keep class androidx.security.crypto.** { *; }

# --- Android Hardware & ML Kit ---
-keep class com.google.mlkit.** { *; }
-keep class androidx.camera.** { *; }
