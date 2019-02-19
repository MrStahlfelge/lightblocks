# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Savegames
-keep class * extends de.golfgl.lightblocks.model.GameModel
-keep class  de.golfgl.lightblocks.model.Tetromino {
   **[] $VALUES;
    *;
}
-keep class  de.golfgl.lightblocks.model.TetrominoDrawyer {
   **[] $VALUES;
    *;
}
-keep class  de.golfgl.lightblocks.model.GameScore {
   **[] $VALUES;
    *;
}
-keep class  de.golfgl.lightblocks.model.Gameboard {
   **[] $VALUES;
    *;
}

# Overall score
-keep class  de.golfgl.lightblocks.state.BestScore {
   **[] $VALUES;
    *;
}
-keep class  de.golfgl.lightblocks.state.TotalScore {
   **[] $VALUES;
    *;
}
-keep class  de.golfgl.lightblocks.state.CloudGameState {
   **[] $VALUES;
    *;
}

# Multiplayer communication
-keep class  de.golfgl.lightblocks.multiplayer.MultiPlayerObjects* {
   **[] $VALUES;
    *;
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-verbose

-dontwarn android.support.**
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.physics.box2d.utils.Box2DBuild
-dontwarn com.badlogic.gdx.jnigen.BuildTarget*

-keepclassmembers class com.badlogic.gdx.backends.android.AndroidInput* {
   <init>(com.badlogic.gdx.Application, android.content.Context, java.lang.Object, com.badlogic.gdx.backends.android.AndroidApplicationConfiguration);
}

-keep public class com.badlogic.gdx.**
-keep class com.badlogic.gdx.controllers.android.AndroidControllers

-keep class com.badlogic.**{
   **[] $VALUES;
    *;
}

#kryo
-dontwarn sun.reflect.**
-dontwarn java.beans.**
-keep,allowshrinking class com.esotericsoftware.** {
   <fields>;
   <methods>;
}
-keep,allowshrinking class java.beans.** { *; }
-keep,allowshrinking class sun.reflect.** { *; }
-keep class com.esotericsoftware.kryo.** { *; }
-keep,allowshrinking class com.esotericsoftware.kryo.io.** { *; }
-keep,allowshrinking class sun.nio.ch.** { *; }
-dontwarn sun.nio.ch.**
-dontwarn sun.misc.**

#IAP
-keep class com.android.vending.billing.**
-dontwarn com.amazon.**
-keep class com.amazon.** {*;}
-keepattributes *Annotation*
-optimizations !code/allocation/variable

# GPGS
-keep class com.google.android.gms.games.multiplayer.InvitationEntity
