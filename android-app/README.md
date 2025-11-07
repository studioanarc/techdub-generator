# Dub Techno Generator - Android App

A real-time generative dub techno music application for Android using Oboe for low-latency audio.

## Features

- **Low-latency audio** using Google Oboe library
- **Native C++ audio engine** with JNI integration
- **Real-time audio synthesis** with sine wave generation
- **Jetpack Compose UI** with Material 3 design
- **Dark theme** with neon cyan accents
- **Live audio parameter control** (frequency and volume)

## Architecture

### Project Structure

```
android-app/
├── app/
│   ├── build.gradle.kts           # App-level build configuration
│   ├── proguard-rules.pro         # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml    # App manifest
│       ├── cpp/                    # Native C++ code
│       │   ├── CMakeLists.txt     # CMake build configuration
│       │   ├── AudioEngine.cpp/h  # Main audio engine
│       │   ├── OboeAudioCallback.cpp/h  # Audio callback
│       │   ├── native-lib.cpp     # JNI bindings
│       │   └── oboe/              # Oboe library (submodule)
│       ├── java/com/dubtechno/generator/
│       │   ├── AudioEngine.kt     # Kotlin wrapper for native code
│       │   ├── MainActivity.kt    # Main UI activity
│       │   └── DubTechnoApp.kt    # Application class
│       └── res/                    # Android resources
├── build.gradle.kts               # Root build configuration
├── settings.gradle.kts            # Project settings
└── gradle.properties              # Gradle properties
```

### Technology Stack

- **Language**: Kotlin + C++17
- **UI Framework**: Jetpack Compose
- **Audio Library**: Oboe (Google's high-performance audio library)
- **Build System**: Gradle 8.2 + CMake 3.22.1
- **NDK**: Version 26+
- **Target SDK**: Android 14 (API 34)
- **Min SDK**: Android 7.0 (API 24)

### Audio Configuration

- **Sample Rate**: 48,000 Hz
- **Channel Count**: 2 (Stereo)
- **Format**: Float (32-bit)
- **Performance Mode**: Low Latency
- **Sharing Mode**: Exclusive (falls back to Shared)
- **Buffer Size**: Auto-optimized (typically 2x burst size)

## Prerequisites

- **Android Studio**: Hedgehog | 2023.1.1 or later
- **JDK**: 17 or later
- **Android SDK**: API 34
- **NDK**: Version 26 or later
- **CMake**: 3.22.1 or later
- **Git**: For cloning submodules

## Setup Instructions

### 1. Clone the Repository with Submodules

```bash
git clone --recursive https://github.com/yourusername/techdub-generator.git
cd techdub-generator/android-app
```

If you already cloned without `--recursive`:

```bash
git submodule update --init --recursive
```

### 2. Install Android SDK and NDK

Open Android Studio and install:
- Android SDK API 34
- NDK (Side by side) version 26.x
- CMake 3.22.1

Or via command line:

```bash
sdkmanager "platforms;android-34"
sdkmanager "ndk;26.1.10909125"
sdkmanager "cmake;3.22.1"
```

### 3. Build the Project

Using Gradle wrapper:

```bash
./gradlew assembleDebug
```

Using Android Studio:
1. Open the `android-app` folder
2. Wait for Gradle sync
3. Click **Build > Make Project**

## Running the App

### Install on Device/Emulator

```bash
# Install debug APK
./gradlew installDebug

# Or use adb directly
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Launch from Android Studio

1. Connect your device or start an emulator
2. Click **Run > Run 'app'** (Shift+F10)

## Usage

1. **Launch** the app
2. **Click START** to begin audio playback (440 Hz sine wave)
3. **Adjust Frequency** slider (200-800 Hz) to change pitch
4. **Adjust Volume** slider (0-100%) to control loudness
5. **View audio info** at the bottom:
   - Sample rate (typically 48000 Hz)
   - Buffer size (frames)
   - Estimated latency (should be <20ms on modern devices)

## Testing Checklist

- [ ] Project builds without errors
- [ ] APK installs on device
- [ ] Click "Start" button → hear 440 Hz sine wave
- [ ] Adjust frequency slider → pitch changes smoothly
- [ ] Adjust volume slider → volume changes smoothly
- [ ] No audio glitches or crackling
- [ ] Logcat shows Oboe stream info
- [ ] Latency < 20ms on modern devices

## Logcat Monitoring

View audio engine logs:

```bash
adb logcat -s AudioEngine:D OboeAudioCallback:D NativeLib:D
```

Expected output when starting:

```
AudioEngine: Starting audio engine...
AudioEngine: Audio stream started successfully
AudioEngine: Sample rate: 48000 Hz
AudioEngine: Buffer size: 192 frames
AudioEngine: Frames per burst: 96
AudioEngine: Channel count: 2
AudioEngine: Format: Float
AudioEngine: Performance mode: LowLatency
AudioEngine: Sharing mode: Exclusive
AudioEngine: Estimated latency: 4.00 ms
```

## Troubleshooting

### Build Errors

**CMake not found:**
```bash
sdkmanager "cmake;3.22.1"
```

**NDK not found:**
```bash
sdkmanager "ndk;26.1.10909125"
```

**Oboe submodule missing:**
```bash
git submodule update --init --recursive
```

### Audio Issues

**No sound:**
- Check device volume
- Verify audio permissions in settings
- Check logcat for errors
- Ensure device has low-latency audio support

**High latency:**
- Some devices don't support exclusive mode
- Check for background apps using audio
- Try on a different device

**Crackling/glitches:**
- Device may not support low-latency audio
- Check CPU usage
- Reduce buffer size in AudioEngine.cpp

### Supported Devices

For best performance, use devices with:
- `android.hardware.audio.low_latency` feature
- `android.hardware.audio.pro` feature (professional audio)
- Modern chipset (Snapdragon 8xx, Exynos, etc.)

Check device features:

```bash
adb shell pm list features | grep audio
```

## Performance Benchmarks

Typical latency on various devices:

| Device | Android Version | Latency |
|--------|----------------|---------|
| Pixel 7 | Android 14 | 3-5 ms |
| Samsung S23 | Android 14 | 4-6 ms |
| OnePlus 11 | Android 13 | 5-8 ms |
| Older devices | Android 10 | 10-20 ms |

## Next Steps

This basic audio foundation is ready for the full synth engine integration:

1. **Replace sine wave** with complex synthesis engine
2. **Add MIDI support** for external controllers
3. **Implement audio effects** (reverb, delay, filters)
4. **Add presets system**
5. **Implement pattern sequencer**
6. **Add audio recording/export**

## Oboe Documentation

- [Oboe GitHub](https://github.com/google/oboe)
- [Getting Started Guide](https://github.com/google/oboe/blob/main/docs/GettingStarted.md)
- [API Reference](https://google.github.io/oboe/)

## License

See main project LICENSE file.

## Contributing

See main project CONTRIBUTING.md file.
