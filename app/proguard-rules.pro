-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class * {
    @ dagger.* *;
    @ javax.inject.* *;
}
-keepclasseswithmembers class * {
    @ dagger.internal.DaggerGenerated <methods>;
}

-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.internal.DaggerGenerated { *; }

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keep,allowobfuscation,allowshrinking class * extends androidx.room.RoomDatabase
-keep,allowobfuscation,allowshrinking class * extends androidx.work.Worker
-keep,allowobfuscation,allowshrinking interface com.financemanager.** { *; }
