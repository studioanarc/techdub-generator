# Android UI & Integration Guide

Complete guide for the Jetpack Compose UI and integration with the audio engine.

## Overview

The Android UI is built with **Jetpack Compose** and **Material 3**, featuring:
- Professional dark theme with neon cyan accents
- Four main screens (Generative, Synths, Effects, Presets)
- Real-time parameter control
- Waveform visualizer
- Preset management system
- Smooth animations and glass morphism effects

## Architecture Overview

```
┌─────────────────────────────────────────────────┐
│                  MainActivity                    │
│  (Handles permissions, lifecycle)               │
└───────────────────┬─────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────┐
│               MainViewModel                      │
│  - Manages app state (StateFlow)                │
│  - Coordinates AudioEngine & GenerativeEngine   │
│  - Handles preset operations                    │
└───────┬───────────────────────────┬─────────────┘
        │                           │
┌───────▼──────────┐      ┌─────────▼──────────┐
│  AudioEngine     │      │ GenerativeEngine    │
│  (JNI Wrapper)   │      │ (Kotlin/Coroutines) │
│  - Native calls  │◄─────┤ - Pattern generation│
│  - Synth params  │      │ - Note triggering   │
│  - Effects       │      └─────────────────────┘
└──────────────────┘
        │
        ▼
┌──────────────────┐
│  Native C++ Code │
│  - Oboe audio    │
│  - DSP processing│
└──────────────────┘
```

## File Structure

### UI Layer (`/ui`)

**MainActivity.kt**
- Entry point
- Permissions handling
- Lifecycle management
- Sets up Jetpack Compose

**MainScreen.kt**
- Main layout with tabs
- Transport controls (Play, Stop, Randomize)
- Waveform visualizer
- Top app bar

**GenerativeControlsScreen.kt**
- Simple mode: Basic controls (Root Note, Scale, Tempo, Density, Chaos, Swing)
- Advanced mode: Euclidean density, humanization, polyrhythm
- Real-time updates to GenerativeEngine

**SynthControlsScreen.kt**
- Tabs for Bass, Melody, Chord synths
- Waveform selection
- Filter controls (cutoff, resonance, envelope amount)
- ADSR envelope
- Synth-specific parameters (distortion, vibrato, chorus)

**EffectsScreen.kt**
- Enable/disable switches for each effect
- Tape Echo: Time, feedback, wow & flutter, filter
- Reverb: Size, decay, damping, predelay, mix
- Chorus: Rate, depth, mix
- Master Filter: Type, cutoff, resonance
- Compressor: Threshold, ratio, attack, release
- Saturation: Drive, mix

**PresetsScreen.kt**
- List of saved presets
- Save current configuration
- Load preset
- Delete preset (with confirmation)
- Import/Export as JSON
- Factory presets included

### UI Components (`/ui/components`)

**UIComponents.kt**
- `LabeledSlider`: Slider with label and value display
- `NoteSelector`: Dropdown for selecting root note (C, C#, D, etc.)
- `ScaleSelector`: Dropdown for selecting scale type
- `WaveformSelector`: Chip group for waveform selection
- `EffectCard`: Collapsible card with enable/disable switch
- `GlassContainer`: Glass morphism effect container
- `PulsingIndicator`: Animated indicator for active state
- `LoadingIndicator`: Loading spinner with message

### Theme (`/ui/theme`)

**Color.kt**
```kotlin
Background = #0a0a0a (deep black)
Surface = #1a1a1a (dark gray)
Primary = #00ff88 (neon cyan)
Secondary = #00ccff (bright blue)
TextPrimary = #ffffff (white)
TextSecondary = #b0b0b0 (light gray)
```

**Theme.kt**
- Material 3 dark color scheme
- Status bar and navigation bar styling
- Always-dark theme (no light mode)

**Type.kt**
- Typography scale following Material 3
- Font sizes from 11sp (labelSmall) to 57sp (displayLarge)

### ViewModel Layer (`/viewmodel`)

**MainViewModel.kt**

Key responsibilities:
1. **State Management**
   - `isPlaying: StateFlow<Boolean>`
   - `config: StateFlow<GenerativeConfig>`
   - `synthSettings: StateFlow<SynthSettings>`
   - `effectSettings: StateFlow<EffectSettings>`
   - `presets: StateFlow<List<Preset>>`
   - `waveformData: StateFlow<FloatArray>`
   - `message: StateFlow<String?>`

2. **Audio Control**
   - `play()`: Start audio and generative engine
   - `stop()`: Stop playback
   - `randomize()`: Randomize generative parameters

3. **Parameter Updates**
   - `setTempo(bpm: Float)`
   - `setRootNote(note: Int)`
   - `setScale(scale: ScaleType)`
   - `setDensity(density: Float)`
   - `setChaos(chaos: Float)`
   - `updateBassSynth(params: BassSynthParams)`
   - `updateDelay(params: DelayParams)`
   - ... and more

4. **Preset Management**
   - `loadPresets()`
   - `saveCurrentAsPreset(name: String)`
   - `loadPreset(preset: Preset)`
   - `deletePreset(preset: Preset)`
   - `exportPreset(preset: Preset): String`
   - `importPreset(json: String)`

### Data Layer (`/data`)

**Preset.kt**

Data classes for all configurable parameters:

```kotlin
@Serializable
data class Preset(
    val id: String,
    val name: String,
    val timestamp: Long,
    val config: GenerativeConfig,
    val synthSettings: SynthSettings,
    val effectSettings: EffectSettings
)

@Serializable
data class GenerativeConfig(
    val tempo: Float,
    val rootNote: Int,
    val scale: ScaleType,
    val density: Float,
    val chaos: Float,
    val swing: Float,
    // ... more parameters
)

@Serializable
data class BassSynthParams(
    val waveform: Waveform,
    val filterCutoff: Float,
    val filterResonance: Float,
    val attack: Float,
    val decay: Float,
    val sustain: Float,
    val release: Float,
    // ... more parameters
)

// Similar for MelodySynthParams, ChordSynthParams
// DelayParams, ReverbParams, etc.
```

**PresetRepository.kt**

Manages preset persistence using SharedPreferences:

```kotlin
class PresetRepository(context: Context) {
    suspend fun savePreset(preset: Preset): Result<Unit>
    suspend fun loadPreset(id: String): Result<Preset?>
    suspend fun getAllPresets(): Result<List<Preset>>
    suspend fun deletePreset(id: String): Result<Unit>
    suspend fun initializeFactoryPresets(): Result<Unit>
    fun exportPreset(preset: Preset): String
    suspend fun importPreset(json: String): Result<Preset>
}
```

Factory presets:
- **Deep Dub**: 120 BPM, minor scale, heavy delay/reverb
- **Minimal Techno**: 128 BPM, dorian mode, sparse, high resonance
- **Ambient Dub**: 110 BPM, phrygian, lots of reverb, slow attack

### Audio Integration (`/audio`)

**AudioEngine.kt**

JNI wrapper for native C++ audio engine:

```kotlin
class AudioEngine private constructor() {
    // Initialization
    external fun nativeInitialize(sampleRate: Int, bufferSize: Int): Boolean
    external fun nativeStart()
    external fun nativeStop()
    external fun nativeShutdown()

    // Synth parameter updates (calls native code)
    external fun nativeUpdateBassSynth(
        waveform: Int,
        filterCutoff: Float,
        filterResonance: Float,
        filterEnvAmount: Float,
        attack: Float,
        decay: Float,
        sustain: Float,
        release: Float,
        distortion: Float,
        subOscLevel: Float,
        glide: Float,
        volume: Float
    )

    // Similar for melody, chords, effects...

    // Note triggering
    external fun triggerNote(
        synthId: Int,  // 0=bass, 1=melody, 2=chords
        midiNote: Int,
        velocity: Float,
        duration: Float
    )

    external fun stopNote(synthId: Int, midiNote: Int)

    // Visualizer data
    external fun nativeGetWaveformData(): FloatArray

    companion object {
        init {
            System.loadLibrary("dubtechno")
        }
    }
}
```

**Key Integration Points:**

1. **Parameter Updates**: When UI sliders change, ViewModel calls AudioEngine methods
2. **Note Triggering**: GenerativeEngine calls `triggerNote()` to play notes
3. **Waveform Data**: Retrieved every 50ms for visualizer
4. **Thread Safety**: All native calls wrapped in try-catch for safety

### Generative Engine (`/generative`)

**GenerativeEngine.kt**

Pure Kotlin implementation of algorithmic composition:

```kotlin
class GenerativeEngine(
    private val audioEngine: AudioEngine,
    private var config: GenerativeConfig
) {
    fun start() // Start generation loop
    fun stop()  // Stop generation
    fun updateConfig(newConfig: GenerativeConfig) // Update in real-time
    fun randomize() // Randomize parameters
    fun getConfig(): GenerativeConfig

    private fun generateStep() {
        // Called every 16th note
        // Decides whether to trigger bass, melody, chords
        // Uses Euclidean rhythms, probability, scales
    }

    private fun triggerBass() {
        // Select note from scale
        // Apply chaos for out-of-scale notes
        // Trigger on AudioEngine
    }

    private fun triggerMelody()
    private fun triggerChord()
}
```

**Algorithm Details:**

1. **Timing**: Coroutine-based loop at 16th note resolution
2. **Bass**: Triggers on downbeats with some variation
3. **Melody**: Probabilistic based on density parameter
4. **Chords**: Trigger every 16 steps (4 bars)
5. **Scales**: Proper scale degree selection
6. **Chaos**: Probability of chromatic notes
7. **Swing**: Timing offset for groove

## Integration Checklist

### 1. Native Audio Engine Setup

Implement these JNI functions in C++:

```cpp
// In native-lib.cpp or dubtechno-jni.cpp

extern "C" JNIEXPORT jboolean JNICALL
Java_com_dubtechno_generator_audio_AudioEngine_nativeInitialize(
    JNIEnv* env, jobject thiz, jint sampleRate, jint bufferSize)
{
    // Initialize Oboe stream
    // Create synth instances
    // Setup effect chain
    return true;
}

extern "C" JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_AudioEngine_nativeUpdateBassSynth(
    JNIEnv* env, jobject thiz,
    jint waveform, jfloat filterCutoff, jfloat filterResonance,
    jfloat filterEnvAmount, jfloat attack, jfloat decay,
    jfloat sustain, jfloat release, jfloat distortion,
    jfloat subOscLevel, jfloat glide, jfloat volume)
{
    // Update bass synth parameters
    // Thread-safe parameter updates
}

extern "C" JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_AudioEngine_triggerNote(
    JNIEnv* env, jobject thiz,
    jint synthId, jint midiNote, jfloat velocity, jfloat duration)
{
    // Trigger note on specified synth
    // Add to voice allocator
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_dubtechno_generator_audio_AudioEngine_nativeGetWaveformData(
    JNIEnv* env, jobject thiz)
{
    // Return 256 samples for visualizer
    jfloatArray result = env->NewFloatArray(256);
    // Fill with audio buffer data
    return result;
}
```

### 2. CMakeLists.txt

```cmake
cmake_minimum_required(VERSION 3.22.1)
project("dubtechno")

set(CMAKE_CXX_STANDARD 17)

# Add Oboe
add_subdirectory(oboe)

# Add your sources
add_library(dubtechno SHARED
    native-lib.cpp
    AudioEngine.cpp
    Synth.cpp
    BassSynth.cpp
    MelodySynth.cpp
    ChordSynth.cpp
    DelayEffect.cpp
    ReverbEffect.cpp
    # ... more files
)

target_link_libraries(dubtechno
    log
    android
    oboe
)
```

### 3. Build & Test

```bash
# Build
./gradlew assembleDebug

# Install
./gradlew installDebug

# Run
adb shell am start -n com.dubtechno.generator/.ui.MainActivity

# Monitor logs
adb logcat -s DubTechno:D AudioEngine:D
```

## UI Features in Detail

### 1. Transport Controls

Located at bottom of screen:

- **Large Play/Stop Button** (96dp)
  - Pulsing animation when playing
  - Green gradient when active
  - Icon changes: PlayArrow ↔ Stop

- **Randomize Button** (64dp)
  - Shuffle icon
  - Randomizes all generative parameters

- **Waveform Visualizer** (80dp height)
  - 256-sample buffer
  - Neon cyan color (#00ff88)
  - Updates at 20 FPS
  - Flat line when stopped

### 2. Tab Navigation

Four tabs with icons:
1. **Generative** - Build icon
2. **Synths** - Create icon
3. **Effects** - Star icon
4. **Presets** - List icon

### 3. Generative Controls

**Simple Mode (default):**
- Note selector (C-B dropdown)
- Scale selector (dropdown with 9 scales)
- Tempo slider (100-140 BPM)
- Density slider (Sparse/Medium/Busy labels)
- Strangeness slider (Normal/Interesting/Weird)
- Swing slider (0-100%)

**Advanced Mode (expandable):**
- Pattern lengths (bass, melody, chords)
- Euclidean density slider
- Note length slider
- Velocity variation slider
- Humanization slider
- Polyrhythm toggle and ratio
- Octave settings

### 4. Synth Controls

Separate tabs for each synth:

**Bass Synth:**
- Oscillator section: Waveform, sub-osc level, glide
- Filter section: Cutoff, resonance, envelope amount
- Envelope: Attack, decay, sustain, release (sliders)
- Effects: Distortion, volume

**Melody Synth:**
- Oscillator: Waveform, detune
- Vibrato: Rate, depth
- Filter & envelope (compact view)
- Volume

**Chord Synth:**
- Waveform
- Filter (cutoff, resonance)
- Envelope (compact display: A/D/S/R values)
- Chorus depth
- Volume

### 5. Effects

Each effect in a collapsible card:

**Enable/Disable Switch**: Top right of each card

**Tape Echo:**
- Time (10-2000ms)
- Feedback (0-90%)
- Wow & Flutter (0-50%)
- Filter cutoff (500-8000Hz)
- Mix (0-100%)

**Reverb:**
- Size (Small Room / Medium Hall / Large Space)
- Decay (0-100%)
- Damping (0-100%)
- Pre-delay (0-100ms)
- Mix (0-100%)

**Chorus:**
- Rate (0.1-5Hz)
- Depth (0-100%)
- Mix (0-100%)

**Master Filter:**
- Type chips (Lowpass, Highpass, Bandpass, Notch)
- Cutoff (20-20000Hz)
- Resonance (0-100%)

**Compressor:**
- Threshold (-40 to 0 dB)
- Ratio (1:1 to 20:1)
- Attack/Release (compact display)

**Saturation:**
- Drive (0-100%)
- Mix (0-100%)

### 6. Presets

**Top Section:**
- "Save Current" button
- "Import" button

**Current Preset Indicator:**
- Shows loaded preset name
- Highlighted in primary container color

**Preset List:**
- Card for each preset
- Name, timestamp, tempo, scale
- Tap to load
- Menu button with:
  - Load
  - Export
  - Delete (with confirmation dialog)

**Save Dialog:**
- Text field for preset name
- Save/Cancel buttons

**Import Dialog:**
- Text area for JSON
- Import/Cancel buttons

## Performance Optimization

### 1. Composable Optimization

```kotlin
@Composable
fun LabeledSlider(/* params */) {
    // Use remember to cache expensive computations
    val formattedValue = remember(value) {
        formatValue(value)
    }

    // Avoid recreating lambdas
    val onValueChangeCallback = remember { { newValue: Float ->
        onValueChange(newValue)
    }}
}
```

### 2. Debouncing Slider Updates

```kotlin
private var sliderUpdateJob: Job? = null

fun setFilterCutoff(value: Float) {
    _synthSettings.value = _synthSettings.value.copy(
        bass = _synthSettings.value.bass.copy(filterCutoff = value)
    )

    // Debounce native updates
    sliderUpdateJob?.cancel()
    sliderUpdateJob = viewModelScope.launch {
        delay(50) // Update audio every 50ms max
        audioEngine.updateBassSynth(_synthSettings.value.bass)
    }
}
```

### 3. Waveform Visualizer Optimization

```kotlin
private fun startVisualizer() {
    visualizerJob = viewModelScope.launch {
        while (_isPlaying.value) {
            val data = audioEngine.getWaveformData()
            _waveformData.value = data
            delay(50) // 20 FPS
        }
    }
}
```

## Testing

### Manual Testing

1. **Launch app**
   - [ ] No crashes on startup
   - [ ] Permission dialog appears

2. **Generative tab**
   - [ ] All sliders move smoothly
   - [ ] Dropdowns open and select correctly
   - [ ] Advanced section expands/collapses

3. **Play button**
   - [ ] Press play → button animates
   - [ ] Audio starts (verify with logcat if native not ready)
   - [ ] Waveform shows activity
   - [ ] Press stop → audio stops

4. **Synth controls**
   - [ ] Tab switching works
   - [ ] All sliders update
   - [ ] Waveform selector changes

5. **Effects**
   - [ ] Toggle switches work
   - [ ] Cards expand/collapse
   - [ ] Sliders update when enabled

6. **Presets**
   - [ ] Factory presets load
   - [ ] Save dialog works
   - [ ] Delete confirmation appears
   - [ ] Load preset updates all parameters

7. **Rotation**
   - [ ] App doesn't crash
   - [ ] State preserved
   - [ ] Audio continues playing

### Unit Tests

```kotlin
@Test
fun testPresetSerialization() {
    val preset = PresetFactory.createDeepDubPreset()
    val json = Json.encodeToString(preset)
    val decoded = Json.decodeFromString<Preset>(json)
    assertEquals(preset, decoded)
}

@Test
fun testGenerativeEngineScales() {
    val config = GenerativeConfig(scale = ScaleType.MINOR)
    val engine = GenerativeEngine(mockAudioEngine, config)
    // Test scale note generation
}
```

## Troubleshooting

### UI Issues

**Sliders not smooth:**
- Reduce update frequency
- Check for heavy operations in onValueChange

**Tabs not switching:**
- Verify state management
- Check for crashes in tab content

**Waveform not showing:**
- Verify native getWaveformData() returns valid array
- Check coroutine is running
- Verify isPlaying state

### Integration Issues

**Native methods not found:**
- Check library name in loadLibrary()
- Verify CMakeLists.txt includes all sources
- Clean and rebuild

**Parameters not affecting audio:**
- Add logging to native side
- Verify JNI function signatures match
- Check thread safety

## Future Enhancements

- [ ] Gesture controls (swipe, pinch)
- [ ] Haptic feedback
- [ ] Landscape layout optimization
- [ ] Tablet UI
- [ ] Settings screen
- [ ] Tutorial/onboarding
- [ ] MIDI export
- [ ] Audio recording
- [ ] Cloud preset sync

## Summary

The Android UI is production-ready and designed for seamless integration with your native audio engine. All parameter types are defined, JNI signatures are specified, and the architecture supports real-time, low-latency audio control.

**Next Steps:**
1. Implement native JNI functions
2. Connect synth/effect parameter updates
3. Implement note triggering
4. Add waveform data export
5. Test and optimize

The UI will work immediately once the native code implements the expected JNI interface!
