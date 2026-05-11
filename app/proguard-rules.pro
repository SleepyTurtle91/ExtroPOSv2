# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\User\AppData\Local\Android\Sdk\tools\proguard\proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# --- Room ---
-keepclassmembers class * extends androidx.room.RoomDatabase {
   public <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-keep class com.extrotarget.extroposv2.core.data.local.dao.** { *; }
-keep class com.extrotarget.extroposv2.core.data.model.** { *; }
-keepclassmembers class com.extrotarget.extroposv2.core.data.model.** { *; }

# --- Hilt ---
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep @dagger.hilt.InstallIn class *
-keep class * extends androidx.lifecycle.ViewModel

# --- Retrofit / OkHttp ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations, AnnotationDefault
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# --- GSON ---
-keep class com.google.gson.** { *; }
-keep class com.extrotarget.extroposv2.core.network.api.** { *; }

# --- Ktor ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.serialization.** { *; }

# --- Netty (Ktor Backend) ---
-dontwarn io.netty.handler.ssl.**
-dontwarn io.netty.util.internal.logging.**
-dontwarn io.netty.util.internal.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.eclipse.jetty.npn.**
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn reactor.blockhound.integration.BlockHoundIntegration

# --- Timber (Keep only high-level logging in release) ---
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
}

# --- General ---
-keep class com.extrotarget.extroposv2.core.util.security.SecurityManager { *; }
-keep class com.extrotarget.extroposv2.core.util.lhdn.** { *; }

# --- Printer Drivers & Hardware ---
-keep class com.extrotarget.extroposv2.core.hardware.** { *; }
-keep interface com.extrotarget.extroposv2.core.hardware.** { *; }
-keepclassmembers class com.extrotarget.extroposv2.core.hardware.** { *; }

# --- Financial Integrity ---
-keep class java.math.BigDecimal { *; }

# --- Build Config ---
-keep class com.extrotarget.extroposv2.BuildConfig { *; }
