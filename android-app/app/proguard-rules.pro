# Add project specific ProGuard rules here.

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep AudioEngine class
-keep class com.dubtechno.generator.AudioEngine {
    public *;
    native <methods>;
}

# Keep MainActivity and Application
-keep class com.dubtechno.generator.MainActivity { *; }
-keep class com.dubtechno.generator.DubTechnoApp { *; }
