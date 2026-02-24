# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ==================== CRITICAL: Generic Type Signatures ====================
# Prevent R8 from optimizing generic hierarchies used by reflection
-keep class * {
    *;
}
# Preserve generic type information needed by Gson and Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses,EnclosingMethod

# ==================== Gson ====================
-keep class * implements com.google.gson.TypeAdapter { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }

# Prevent R8 from stripping interface information needed for Gson
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# ==================== Retrofit ====================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ==================== OkHttp ====================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ==================== App Data Models ====================
# Keep repositories and generic wrappers
-keep class com.afrivest.app.data.repository.** { *; }
-keep class com.afrivest.app.core.** { *; }
# Keep ALL classes in data packages — covers any new models added in the future
-keep class com.afrivest.app.data.model.** { *; }
-keep class com.afrivest.app.data.api.** { *; }
-keep class com.afrivest.app.data.local.** { *; }

# ==================== Kotlin ====================
# TEMP: disable optimization to confirm R8 cause
-dontoptimize
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ==================== Parcelize ====================
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ==================== Coroutines ====================
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }
# ==================== Google Tink (transitive Firebase dependency) ====================
-dontwarn com.google.api.client.http.**
-dontwarn com.google.api.client.http.javanet.**
-dontwarn org.joda.time.**
-dontwarn com.google.crypto.tink.util.KeysDownloader