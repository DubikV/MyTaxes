# Kotlin Coroutines
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class kotlinx.coroutines.Dispatchers { *; }
-keep class kotlinx.coroutines.internal.** { *; }
-dontwarn kotlinx.coroutines.**

# Kotlinx Serialization
-keepattributes *Annotation*,Signature,InnerClasses
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class * implements kotlinx.serialization.SerializationStrategy { *; }
-keep class * implements kotlinx.serialization.DeserializationStrategy { *; }
-keep class * implements kotlinx.serialization.KSerializer { *; }

# Keep all @Serializable models in our package with their members for JSON parsing/serialization
-keep @kotlinx.serialization.Serializable class com.gmail.vanyadubik.mytaxes.** {
    *;
}
-keepclassmembers class com.gmail.vanyadubik.mytaxes.** {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.SerialName <fields>;
}

# Koin DI
-keep class org.koin.** { *; }
-dontwarn org.koin.**
-keep class com.gmail.vanyadubik.mytaxes.** {
    @org.koin.core.annotation.* <fields>;
    @org.koin.core.annotation.* <methods>;
}

# Ktor & Ktor Client Engines
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keepattributes Signature, HTTPException, NullPointerException

# OkHttp & Okio (used by Ktor)
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature, InnerClasses, EnclosingMethod

# Jetpack DataStore
-dontwarn androidx.datastore.**
-keep class androidx.datastore.** { *; }

# AndroidX & Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Android Entry Points & Application classes
-keep public class com.gmail.vanyadubik.mytaxes.MainActivity
-keep public class com.gmail.vanyadubik.mytaxes.TaxApp

# Parcelable classes (if any)
-keepclassmembers class com.gmail.vanyadubik.mytaxes.** implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
