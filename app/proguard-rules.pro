# 1. Gson & 데이터 클래스
-keep class com.usnine.scheduler.data.Schedule { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-dontwarn com.google.gson.**

# 2. Firebase Remote Config
-keep class com.google.firebase.remoteconfig.** { *; }
-keep class com.google.android.gms.internal.firebase_remote_config.** { *; }

# 3. Hilt & Dagger
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface dagger.hilt.internal.GeneratedComponent { *; }
-keep class dagger.hilt.internal.GeneratedComponentManager { *; }

# 4. Room DB
-keep class com.usnine.scheduler.data.Schedule { *; }
-keep interface com.usnine.scheduler.data.ScheduleDao { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
