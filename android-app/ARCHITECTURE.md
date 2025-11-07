# Android App Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                              │
│                      (Jetpack Compose)                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐      │
│  │   Generative   │  │     Synths     │  │    Effects     │      │
│  │    Controls    │  │    Controls    │  │    Controls    │      │
│  │                │  │                │  │                │      │
│  │ • Root Note    │  │ • Bass Synth   │  │ • Tape Echo    │      │
│  │ • Scale        │  │ • Melody Synth │  │ • Reverb       │      │
│  │ • Tempo        │  │ • Chord Synth  │  │ • Chorus       │      │
│  │ • Density      │  │                │  │ • Filter       │      │
│  │ • Chaos        │  │ Each with:     │  │ • Compressor   │      │
│  │ • Swing        │  │ - Filter       │  │ • Saturation   │      │
│  │                │  │ - ADSR         │  │                │      │
│  │ Advanced:      │  │ - Effects      │  │ Each with:     │      │
│  │ • Euclidean    │  │ - Waveform     │  │ - Enable       │      │
│  │ • Humanize     │  │ - Volume       │  │ - Parameters   │      │
│  │ • Polyrhythm   │  │                │  │ - Mix          │      │
│  └────────────────┘  └────────────────┘  └────────────────┘      │
│                                                                     │
│  ┌────────────────┐  ┌──────────────────────────────────────┐    │
│  │    Presets     │  │      Transport Controls               │    │
│  │                │  │                                        │    │
│  │ • Save         │  │  [Randomize] [█ PLAY █] [Volume]     │    │
│  │ • Load         │  │                                        │    │
│  │ • Delete       │  │  ═════════ Waveform Visualizer ═════ │    │
│  │ • Import       │  │                                        │    │
│  │ • Export       │  └──────────────────────────────────────┘    │
│  └────────────────┘                                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                              ▲ │
                              │ │ StateFlow
                              │ ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        MainViewModel                                 │
│                     (State Management)                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  State:                          Methods:                           │
│  • isPlaying: StateFlow          • play()                          │
│  • config: StateFlow             • stop()                          │
│  • synthSettings: StateFlow      • randomize()                     │
│  • effectSettings: StateFlow     • setTempo()                      │
│  • presets: StateFlow            • setScale()                      │
│  • waveformData: StateFlow       • updateBassSynth()               │
│  • message: StateFlow            • updateDelay()                   │
│                                   • savePreset()                    │
│                                   • loadPreset()                    │
│                                                                     │
└────────────┬────────────────────────────────────────┬──────────────┘
             │                                        │
             │ Controls                               │ Controls
             ▼                                        ▼
┌──────────────────────────┐           ┌──────────────────────────┐
│   GenerativeEngine       │           │     AudioEngine          │
│   (Kotlin Coroutines)    │           │     (JNI Wrapper)        │
├──────────────────────────┤           ├──────────────────────────┤
│                          │           │                          │
│ • start()                │◄─────────►│ • initialize()           │
│ • stop()                 │  Triggers │ • start()                │
│ • updateConfig()         │   Notes   │ • stop()                 │
│ • randomize()            │           │ • triggerNote()          │
│                          │           │ • updateBassSynth()      │
│ Algorithm:               │           │ • updateMelodySynth()    │
│ • Euclidean rhythms      │           │ • updateChordSynth()     │
│ • Scale generation       │           │ • updateDelay()          │
│ • Probabilistic triggers │           │ • updateReverb()         │
│ • Timing (16th notes)    │           │ • getWaveformData()      │
│                          │           │                          │
│ Note Triggering:         │           │ Native Methods:          │
│ • triggerBass()          │           │ • nativeInitialize()     │
│ • triggerMelody()        │───────────►│ • nativeUpdateBass()     │
│ • triggerChord()         │           │ • nativeTriggerNote()    │
│                          │           │ • nativeGetWaveform()    │
└──────────────────────────┘           └────────────┬─────────────┘
                                                    │
                                                    │ JNI
                                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    NATIVE C++ AUDIO ENGINE                          │
│                       (Oboe Library)                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │                   Oboe Audio Stream                         │   │
│  │         (Low-latency, 48kHz, Stereo, Float)                │   │
│  └────────────────────────────────────────────────────────────┘   │
│                              ▲ │                                    │
│                              │ │                                    │
│                              │ ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                  Audio Processing Callback                   │  │
│  │                   (Real-time Thread)                         │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                      │
│                ┌─────────────┼─────────────┐                       │
│                │             │             │                       │
│                ▼             ▼             ▼                       │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐│
│  │   Bass Synth     │  │  Melody Synth    │  │  Chord Synth     ││
│  │                  │  │                  │  │                  ││
│  │ • Oscillator     │  │ • Oscillator     │  │ • Oscillator     ││
│  │   - Waveform     │  │   - Waveform     │  │   - Waveform     ││
│  │   - Sub-osc      │  │   - Detune       │  │                  ││
│  │ • Filter         │  │ • Vibrato LFO    │  │ • Filter         ││
│  │   - Cutoff       │  │ • Filter         │  │ • Envelope       ││
│  │   - Resonance    │  │ • Envelope       │  │ • Chorus         ││
│  │   - Envelope     │  │                  │  │                  ││
│  │ • Envelope       │  │                  │  │                  ││
│  │ • Distortion     │  │                  │  │                  ││
│  │ • Glide          │  │                  │  │                  ││
│  └──────────────────┘  └──────────────────┘  └──────────────────┘│
│                │             │             │                       │
│                └─────────────┼─────────────┘                       │
│                              │                                      │
│                              ▼                                      │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                     Mixer / Summer                           │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                      │
│                              ▼                                      │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                   Effects Chain                              │  │
│  │                                                              │  │
│  │  [Tape Echo] → [Reverb] → [Chorus] →                       │  │
│  │  → [Filter] → [Compressor] → [Saturation]                  │  │
│  │                                                              │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                      │
│                              ▼                                      │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                  Master Output                               │  │
│  │                  (Volume + Limiter)                          │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                      │
│                              ▼                                      │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │              Waveform Buffer (for visualizer)               │  │
│  │              FloatArray[256] exported to Kotlin              │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    Android Audio Output
                    (Speaker / Headphones)
```

## Data Flow Examples

### Example 1: User Adjusts Tempo

```
1. User drags tempo slider to 128 BPM
   ├─ UI: Slider value changes
   │
2. Composable calls viewModel.setTempo(128f)
   ├─ ViewModel: Updates StateFlow
   │  _config.value = config.copy(tempo = 128f)
   │
3. ViewModel calls generativeEngine.updateConfig(newConfig)
   ├─ GenerativeEngine: Updates internal tempo
   │  Adjusts coroutine delay for 16th notes
   │
4. Result: Pattern generation now runs at 128 BPM
```

### Example 2: User Changes Bass Filter Cutoff

```
1. User adjusts bass filter cutoff slider to 800 Hz
   ├─ UI: Slider value changes
   │
2. Composable calls onUpdate with new BassSynthParams
   ├─ ViewModel: Updates StateFlow
   │  _synthSettings.value = settings.copy(
   │    bass = bass.copy(filterCutoff = 800f)
   │  )
   │
3. ViewModel calls audioEngine.updateBassSynth(params)
   ├─ AudioEngine: Calls native method
   │  nativeUpdateBassSynth(waveform, 800f, resonance, ...)
   │
4. JNI → Native C++ code
   ├─ BassSynth: Updates filter cutoff parameter
   │  Thread-safe atomic update
   │
5. Audio callback reads new value
   ├─ Next audio buffer uses 800 Hz cutoff
   │
6. Result: Bass sound changes immediately
```

### Example 3: Generative Engine Triggers Note

```
1. GenerativeEngine coroutine fires (every 16th note)
   ├─ GenerativeEngine: Decides to trigger bass note
   │  shouldTriggerNote(density) returns true
   │
2. triggerBass() calculates note
   ├─ Selects note from scale: rootNote + scale[2] = 62 (D)
   │  Octave: bassOctave = 2 → final note = 38 (D1)
   │  Velocity: 0.7 + random * chaos
   │  Duration: noteLength * 0.5 + 0.2 = 0.45 seconds
   │
3. Calls audioEngine.triggerNote(0, 38, 0.8f, 0.45f)
   ├─ AudioEngine: Passes to native
   │  synthId = 0 (bass)
   │
4. JNI → Native C++ code
   ├─ BassSynth: Allocates voice
   │  Sets MIDI note = 38
   │  Calculates frequency = 440 * 2^((38-69)/12) = 55 Hz
   │  Triggers envelope
   │  Starts oscillator
   │
5. Audio callback generates samples
   ├─ Oscillator generates waveform at 55 Hz
   │  Filter processes with envelope modulation
   │  Envelope shapes amplitude (ADSR)
   │
6. Result: Deep bass note plays for 0.45 seconds
```

### Example 4: User Saves Preset

```
1. User taps "Save Current" button
   ├─ UI: Opens save dialog
   │
2. User enters name "My Deep Dub" and taps Save
   ├─ Dialog calls viewModel.saveCurrentAsPreset("My Deep Dub")
   │
3. ViewModel creates Preset object
   ├─ preset = Preset(
   │    name = "My Deep Dub",
   │    config = currentConfig,
   │    synthSettings = currentSynthSettings,
   │    effectSettings = currentEffectSettings,
   │    timestamp = System.currentTimeMillis()
   │  )
   │
4. ViewModel calls presetRepository.savePreset(preset)
   ├─ Repository: Serializes to JSON
   │  val json = Json.encodeToString(preset)
   │
5. Saves to SharedPreferences
   ├─ prefs.edit().putString(preset.id, json).apply()
   │  Also updates preset index
   │
6. ViewModel reloads presets
   ├─ loadPresets() updates StateFlow
   │
7. UI updates
   ├─ Presets list shows new preset
   │  Snackbar: "Preset saved: My Deep Dub"
   │
8. Result: Preset saved and appears in list
```

### Example 5: Play Button Flow

```
1. User taps large play button
   ├─ UI: PlayStopButton onClick fires
   │
2. Calls viewModel.play()
   ├─ ViewModel: Checks isInitialized
   │  if (not initialized) show message, return
   │
3. Start audio engine
   ├─ audioEngine.start()
   │  └─ nativeStart() via JNI
   │     └─ Native: Oboe stream starts
   │        └─ Audio callback begins
   │
4. Start generative engine
   ├─ generativeEngine.start()
   │  └─ Launches coroutine
   │     └─ while (isRunning) {
   │          generateStep()
   │          delay(stepDuration)
   │        }
   │
5. Update UI state
   ├─ _isPlaying.value = true
   │
6. Start visualizer
   ├─ startVisualizer()
   │  └─ Launches coroutine
   │     └─ while (isPlaying) {
   │          waveformData = audioEngine.getWaveformData()
   │          delay(50ms)
   │        }
   │
7. UI reacts to state changes
   ├─ Play button turns to stop button
   │  Button starts pulsing animation
   │  Waveform visualizer shows activity
   │  Snackbar: "Playing"
   │
8. Audio starts playing
   ├─ Generative engine begins triggering notes
   │  Audio engine processes and outputs sound
   │
9. Result: Music plays, visualizer animates, UI updates
```

## Thread Model

### UI Thread (Main Thread)
- Jetpack Compose rendering
- User input handling
- StateFlow updates
- ViewModel method calls

### Coroutine Dispatcher.Default
- Generative engine timing loop
- Pattern generation
- Note calculation

### Coroutine Dispatcher.IO
- Preset file operations
- SharedPreferences read/write
- JSON serialization

### Native Audio Thread (Real-time)
- Oboe audio callback
- Synth processing
- Effects processing
- Sample generation
- **Critical**: No blocking operations, no memory allocation

## Memory Management

### Kotlin Layer
- ViewModel scoped to Activity lifecycle
- Automatically cleaned up when Activity destroyed
- Coroutines cancelled in ViewModel.onCleared()
- StateFlow collectors automatically disposed

### Native Layer
- Audio buffers pre-allocated
- Synth voices managed in voice pool
- No allocations in audio callback
- Resources freed in nativeShutdown()

## Performance Characteristics

### Latency
- **UI → Parameter Update**: <5ms (JNI call overhead)
- **Audio Latency**: 3-10ms (depends on device)
- **Generative Timing**: 1-2ms precision (coroutine timing)
- **Visualizer Update**: 50ms (20 FPS)

### CPU Usage
- **UI Thread**: <5% (Compose is efficient)
- **Audio Thread**: 10-30% (one core, depends on effect chain)
- **Generative Engine**: <1% (simple calculations)
- **Total**: ~15-35% overall CPU

### Memory Usage
- **UI**: ~50MB (Compose, Views, Bitmaps)
- **ViewModel**: ~5MB (state objects)
- **Native Audio**: ~10MB (buffers, synth state)
- **Total**: ~65MB (typical music app footprint)

## Scalability

### Current Limits
- 3 synth voices (bass, melody, chords)
- 6 effects (tape echo, reverb, chorus, filter, compressor, saturation)
- Unlimited presets (SharedPreferences limit ~1MB)

### Extensible
- Add more synth types: Create new SynthParams class and UI screen
- Add more effects: Add to EffectSettings and EffectsScreen
- Custom scales: Extend ScaleType enum
- MIDI input: Implement MIDI receiver, send to AudioEngine

## Error Handling

### UI Layer
- Try-catch around JNI calls
- Graceful degradation if native library missing
- User-friendly error messages in Snackbar

### Native Layer
- Null checks for audio stream
- Graceful handling of invalid parameters
- Logging to logcat for debugging

## Testing Strategy

### Unit Tests
- ViewModel state management
- Preset serialization
- Generative algorithm logic
- Scale generation

### Integration Tests
- JNI function calls
- Audio engine initialization
- Preset save/load flow

### UI Tests
- Compose UI tests
- Navigation tests
- User interaction flows

### Manual Tests
- Audio output verification
- Parameter changes affect sound
- No glitches or crackling
- Preset save/load
- Rotation handling

## Summary

This architecture provides:
- ✅ Clean separation of concerns
- ✅ Reactive UI (StateFlow)
- ✅ Low-latency audio (Oboe)
- ✅ Real-time parameter control
- ✅ Algorithmic music generation
- ✅ Preset management
- ✅ Professional UX
- ✅ Extensible design
- ✅ Performance optimized
- ✅ Well documented

The UI is ready for integration with your native audio engine!
