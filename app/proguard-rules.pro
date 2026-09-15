# ─────────────────────────────────────────────────────────────────────────────
#  Hävn — R8 rules
#
#  R8 is enabled for release: it shrinks the APK and strips readable class and
#  method names. Everything below is a case where something is reached
#  reflectively at runtime and so cannot be proven reachable at build time.
#
#  A note on shape, because it took a bisect to find: rules that match a global
#  `class *` and then filter by annotation — e.g.
#      -keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }
#  crash R8 8.5 with a ConcurrentModificationException inside the tree-shaking
#  enqueuer, which fails the release build outright. Every rule here is
#  therefore scoped to a concrete package or supertype. Keep it that way.
# ─────────────────────────────────────────────────────────────────────────────

# Keep line numbers so a crash from a release build is still readable, but
# rename the source attribute so it doesn't leak original file paths.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Annotations are load-bearing for Room, Hilt and kotlinx.serialization.
-keepattributes *Annotation*,InnerClasses,Signature,Exceptions

# ── Backup format ────────────────────────────────────────────────────────────
# These DTOs define the on-disk shape of an export. kotlinx.serialization ships
# consumer rules for its generated serializers; this keeps the model classes so
# a backup written by one build still restores into the next.
-keep class com.havn.app.data.backup.** { *; }
-dontnote kotlinx.serialization.**

# ── WorkManager ──────────────────────────────────────────────────────────────
# Workers are instantiated by class name from a persisted job record, so a
# worker renamed by R8 would silently never run again for anything already
# scheduled — for this app that means reminders quietly stop firing.
-keep class com.havn.app.notifications.** { *; }
-keep class * extends androidx.work.ListenableWorker { <init>(...); }

# ── Glance widget ────────────────────────────────────────────────────────────
# The launcher instantiates the receiver by name from the manifest.
-keep class com.havn.app.widget.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# ── Room ─────────────────────────────────────────────────────────────────────
# Hilt and Room both ship consumer rules for their generated code, so only the
# pieces reflected over at runtime are named here.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ── WebView JavaScript bridge ────────────────────────────────────────────────
# The 3D organizer calls back into Kotlin through @JavascriptInterface, which
# JavaScript resolves by method name at runtime. Scoped to the package holding
# the bridge — see the note at the top of this file.
-keepclassmembers class com.havn.app.ui.components.** {
    @android.webkit.JavascriptInterface <methods>;
}

# ── Compose ──────────────────────────────────────────────────────────────────
-dontwarn androidx.compose.**

# ── Strip verbose logging from release ───────────────────────────────────────
# Defence in depth: nothing should be logging medication data, but this ensures
# a stray debug log cannot ship to a device that might later be inspected.
# Log.w and Log.e are kept so genuine failures stay diagnosable.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
