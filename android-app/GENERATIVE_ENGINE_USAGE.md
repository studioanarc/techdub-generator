# Generative Music Engine - Usage Guide

## Overview

The Generative Music Engine is a sophisticated algorithmic composition system for dub techno music. It uses advanced techniques including Euclidean rhythms, Markov chains, and genetic algorithms to create evolving, musical patterns.

## Quick Start

### Basic Setup

```kotlin
// 1. Create configuration
val config = GenerativeConfig.classic() // Or .minimal() or .experimental()

// 2. Implement audio callback
val audioCallback = object : GenerativeEngine.AudioCallback {
    override fun playBassNote(midiNote: Int, velocity: Float) {
        // Trigger your bass synth
        audioEngine.playBass(midiNote, velocity)
    }

    override fun playMelodyNote(midiNote: Int, velocity: Float) {
        // Trigger your melody synth
        audioEngine.playMelody(midiNote, velocity)
    }

    override fun playPad(midiNote: Int, velocity: Float) {
        // Trigger pad/chord
        audioEngine.playPad(midiNote, velocity)
    }

    override fun triggerKick(velocity: Float) {
        audioEngine.triggerDrum(DrumType.KICK, velocity)
    }

    override fun triggerSnare(velocity: Float) {
        audioEngine.triggerDrum(DrumType.SNARE, velocity)
    }

    override fun triggerHiHat(velocity: Float) {
        audioEngine.triggerDrum(DrumType.HIHAT, velocity)
    }

    override fun triggerPercussion(velocity: Float) {
        audioEngine.triggerDrum(DrumType.PERCUSSION, velocity)
    }

    override fun onPatternsEvolved(bar: Int) {
        // Optional: Update UI or log evolution
        println("Patterns evolved at bar $bar")
    }
}

// 3. Create and start engine
val engine = GenerativeEngine(config, audioCallback)
engine.start()

// 4. Stop when done
engine.stop()
```

### Using the Builder Pattern

```kotlin
val engine = GenerativeEngineBuilder()
    .setRootNote(60) // Middle C
    .setTempo(120f)
    .setDensity(0.5f)
    .setChaos(0.3f)
    .setAudioCallback(audioCallback)
    .build()

engine.start()
```

## Configuration Options

### Preset Configurations

```kotlin
// Minimal/ambient dub techno
val minimal = GenerativeConfig.minimal()
// - Very sparse patterns
// - Slow evolution
// - Hypnotic repetition

// Classic dub techno
val classic = GenerativeConfig.classic()
// - Balanced density
// - Moderate evolution
// - Traditional dub sound

// Experimental/chaotic
val experimental = GenerativeConfig.experimental()
// - Dense patterns
// - Fast evolution
// - More variation
```

### Custom Configuration

```kotlin
val config = GenerativeConfig(
    rootNote = 60,              // MIDI note (60 = C, 57 = A, etc.)
    scale = ScaleType.MINOR,    // MINOR, DORIAN, PHRYGIAN, etc.
    tempo = 120f,               // BPM (typical: 110-130)
    density = 0.5f,             // 0.0 = sparse, 1.0 = busy
    chaos = 0.3f,               // 0.0 = stable, 1.0 = chaotic
    evolutionSpeed = 0.5f,      // How fast patterns evolve
    melodyRange = MelodyRange.MID, // LOW, MID, HIGH
    rhythmComplexity = 0.5f,    // Drum complexity
    swing = 0.0f,               // Swing amount
    bassMovement = 0.3f,        // Bass activity level
    melodyDensity = 0.4f,       // Melody note frequency
    useEuclideanDrums = true,   // Use Euclidean rhythms
    seed = 12345L               // For reproducibility
)
```

## Musical Parameters Explained

### Root Note
- **60** = Middle C (common)
- **57** = A (warm, deep)
- **62** = D (bright)
- **69** = A (higher pitch)

### Scale Types
- **MINOR**: Dark, melancholic (most common in dub)
- **DORIAN**: Jazzy, sophisticated
- **PHRYGIAN**: Dark, exotic
- **MINOR_PENTATONIC**: Safe, bluesy
- **WHOLE_TONE**: Dreamy, ambiguous

### Density (0.0-1.0)
- **0.0-0.3**: Minimal, ambient, lots of space
- **0.3-0.6**: Classic dub techno
- **0.6-1.0**: Busy, dense, techno-leaning

### Chaos (0.0-1.0)
- **0.0-0.2**: Predictable, stable patterns
- **0.2-0.5**: Balanced variation
- **0.5-1.0**: Unpredictable, experimental

### Evolution Speed (0.0-1.0)
- **0.0-0.3**: Very slow evolution (static)
- **0.3-0.7**: Gradual changes
- **0.7-1.0**: Rapid evolution

## Example Generated Patterns

### Bass Pattern (Classic Config)
```
Step:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
Note:  C  -  -  -  C  -  -  -  C  -  -  -  G  -  -  -
       |           |           |           |
       Root        Root        Root        Fifth
```

### Kick Drum (Half-Time)
```
Step:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
Hit:   X  .  .  .  .  .  .  .  X  .  .  .  .  .  .  .
       |                       |
       Beat 1                  Beat 3
```

### Hi-Hat (Euclidean E(7,16))
```
Step:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
Hit:   X  .  .  X  .  X  .  X  .  X  .  X  .  .  X  .
```

### Melody Pattern (Minimal)
```
Step:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
Note:  -  -  -  -  C  -  -  -  -  -  -  -  D  -  -  -
                   |                       |
                   Sparse, spacious notes
```

## How Evolution Works

The engine evolves patterns every 32 bars through mutation:

### Mutation Types
1. **changeOneNote**: Transpose a note by 1-2 semitones
2. **addRest**: Remove a note (make pattern sparser)
3. **removeRest**: Add a note (make pattern denser)
4. **shiftPattern**: Rotate pattern by one step
5. **adjustVelocities**: Change note intensities
6. **adjustProbabilities**: Change trigger probabilities
7. **swapNotes**: Exchange two note positions
8. **duplicateNote**: Copy a note to nearby position

### Evolution Timeline
```
Bar 0:    Initial patterns generated
Bar 32:   First evolution (subtle changes)
Bar 64:   Second evolution
Bar 96:   Third evolution
...
```

### Example Evolution Sequence

**Bar 0 (Initial):**
```
Bass: C - - - C - - - C - - - C - - -
```

**Bar 32 (After evolution):**
```
Bass: C - - - C - - - C - - - G - - -  (Fifth introduced)
```

**Bar 64 (After evolution):**
```
Bass: C - - - C - - C - - - - G - - -  (Rhythm shifted)
```

## Musical Characteristics

### What Makes It Sound Like Dub Techno?

1. **Deep Bass**: Root-focused, minimal movement
2. **Sparse Melodies**: Space between notes, hypnotic repetition
3. **Half-Time Drums**: Kick on beats 1 and 3 (not 4-on-floor)
4. **Euclidean Rhythms**: Mathematically even distribution
5. **Static Harmony**: Long-held chords or root drone
6. **Gradual Evolution**: Patterns change slowly over time
7. **Probabilistic Triggering**: Notes have chance to trigger
8. **Suspended Chords**: Sus2/Sus4 for ambiguity

### Algorithms Used

- **Euclidean Rhythms (Björklund's Algorithm)**: For drum patterns
- **Markov Chains**: For melodic coherence
- **Genetic Algorithms**: For pattern evolution
- **Music Theory**: Scale quantization, chord voicing

## Performance Considerations

### Timing Precision

The engine uses drift compensation:

```kotlin
val currentTime = System.currentTimeMillis()
val elapsed = currentTime - lastStepTime

if (elapsed >= msPerSixteenth) {
    val drift = elapsed - msPerSixteenth
    lastStepTime = currentTime - drift
    // Process step...
}
```

### Timing at Different Tempos

| Tempo | Ms per 16th | Steps per Second |
|-------|-------------|------------------|
| 110 BPM | 136 ms | 7.35 |
| 120 BPM | 125 ms | 8.0 |
| 130 BPM | 115 ms | 8.7 |

## Advanced Usage

### Accessing Current Patterns

```kotlin
val patterns = engine.getCurrentPatterns()

println("Bass density: ${patterns.bass?.getDensity()}")
println("Melody length: ${patterns.melody?.length}")
println("Kick hits: ${patterns.kick?.countHits()}")
```

### Monitoring Playback State

```kotlin
val state = engine.getPlaybackState()

println("Current bar: ${state.currentBar}")
println("Tempo: ${state.tempo} BPM")
println("Evolution stats: ${state.evolutionStats}")
```

### Manual Pattern Generation

```kotlin
// Generate custom bass pattern
val bassGen = BassGenerator(config)
val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)
val customBass = bassGen.generateWalkingBass(36, scale, 16)

// Generate Euclidean drum pattern
val customKick = EuclideanRhythm.generatePattern(
    hits = 5,
    steps = 16,
    note = 36,
    velocity = 0.8f,
    rotation = 2
)
```

## Testing Your Integration

### Verify Pattern Generation

```kotlin
@Test
fun testPatternGeneration() {
    val config = GenerativeConfig.classic()
    val bassGen = BassGenerator(config)
    val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)

    val pattern = bassGen.generatePattern(36, scale, 16)

    // Pattern should have correct length
    assertEquals(16, pattern.length)

    // Should have some notes
    assertTrue(pattern.countHits() > 0)

    // Notes should be in valid range
    pattern.notes.filterNotNull().forEach { note ->
        assertTrue(note.midiNote in 0..127)
        assertTrue(note.velocity in 0f..1f)
    }
}
```

### Test Reproducibility

```kotlin
@Test
fun testReproducibility() {
    val config = GenerativeConfig(seed = 12345L)

    val gen1 = BassGenerator(config)
    val gen2 = BassGenerator(config)

    val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)

    val pattern1 = gen1.generatePattern(36, scale, 16)
    val pattern2 = gen2.generatePattern(36, scale, 16)

    // Same seed should produce same pattern
    assertEquals(pattern1.notes, pattern2.notes)
}
```

## Troubleshooting

### No Sound
- Check audio callback is implemented correctly
- Verify MIDI notes are being triggered
- Check audio engine is initialized

### Timing Issues
- Ensure coroutines are running on appropriate dispatcher
- Check for blocking operations in audio callback
- Verify tempo calculation is correct

### Patterns Not Evolving
- Check `evolutionSpeed` is > 0
- Verify enough bars have passed (evolution every 32 bars)
- Check `chaos` parameter affects mutation rate

### Not Musical Enough
- Lower `chaos` for more predictable patterns
- Increase `density` for fuller sound
- Try `ScaleType.MINOR` or `DORIAN` for better tonality
- Lower `bassMovement` for more static bass

## License

```
Copyright 2025 Dub Techno Generator
```
