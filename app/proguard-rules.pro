# Retrofit rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, AnnotationDefault

# GSON rules
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Keep your data models
-keep class com.sobhaty.model.** { *; }

# Firebase Rules
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keepattributes *Annotation*

# Serialization rules for DataStore and Moshi/Gson if needed
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# AndroidX Navigation/Lifecycle rules
-keepnames class androidx.lifecycle.ViewModel
