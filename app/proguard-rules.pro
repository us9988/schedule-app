# Hilt & Dagger
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface dagger.hilt.internal.GeneratedComponent { *; }
-keep class dagger.hilt.internal.GeneratedComponentManager { *; }

# Room DB
-keep class com.usnine.scheduler.data.local.ScheduleEntity { *; }
-keep interface com.usnine.scheduler.data.local.ScheduleDao { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
