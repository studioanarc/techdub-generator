package com.dubtechno.generator.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dubtechno.generator.generative.*
import com.dubtechno.generator.model.*

/**
 * Example Activity demonstrating GenerativeEngine integration
 *
 * This shows how to integrate the generative music system with
 * your audio engine and UI.
 */
class ExampleActivity : AppCompatActivity() {

    private var generativeEngine: GenerativeEngine? = null
    // private var audioEngine: AudioEngine? = null // Your C++ audio engine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize audio engine first
        // audioEngine = AudioEngine()
        // audioEngine?.initialize()

        setupGenerativeEngine()
        setupUI()
    }

    private fun setupGenerativeEngine() {
        // Create configuration
        val config = GenerativeConfig(
            rootNote = 60,                // C
            scale = ScaleType.MINOR,
            tempo = 120f,
            density = 0.5f,
            chaos = 0.3f,
            evolutionSpeed = 0.5f,
            melodyRange = MelodyRange.MID,
            rhythmComplexity = 0.5f,
            bassMovement = 0.3f,
            melodyDensity = 0.4f,
            useEuclideanDrums = true,
            seed = null // Random
        )

        // Create audio callback
        val audioCallback = object : GenerativeEngine.AudioCallback {
            override fun playBassNote(midiNote: Int, velocity: Float) {
                // Trigger bass synth via JNI
                // audioEngine?.triggerBass(midiNote, velocity)

                // Example JNI call:
                // nativePlayBass(midiNote, velocity)

                println("Bass: MIDI $midiNote, vel $velocity")
            }

            override fun playMelodyNote(midiNote: Int, velocity: Float) {
                // Trigger melody synth
                // audioEngine?.triggerMelody(midiNote, velocity)

                println("Melody: MIDI $midiNote, vel $velocity")
            }

            override fun playPad(midiNote: Int, velocity: Float) {
                // Trigger pad/chord synth
                // audioEngine?.triggerPad(midiNote, velocity)

                println("Pad: MIDI $midiNote, vel $velocity")
            }

            override fun triggerKick(velocity: Float) {
                // Trigger kick drum sample
                // audioEngine?.triggerDrum(DrumType.KICK, velocity)

                println("Kick: vel $velocity")
            }

            override fun triggerSnare(velocity: Float) {
                // Trigger snare drum sample
                // audioEngine?.triggerDrum(DrumType.SNARE, velocity)

                println("Snare: vel $velocity")
            }

            override fun triggerHiHat(velocity: Float) {
                // Trigger hi-hat sample
                // audioEngine?.triggerDrum(DrumType.HIHAT, velocity)

                println("HiHat: vel $velocity")
            }

            override fun triggerPercussion(velocity: Float) {
                // Trigger percussion sample
                // audioEngine?.triggerDrum(DrumType.PERCUSSION, velocity)

                println("Perc: vel $velocity")
            }

            override fun onPatternsEvolved(bar: Int) {
                // Update UI or log
                runOnUiThread {
                    println("Patterns evolved at bar $bar")
                    // updateEvolutionIndicator()
                }
            }
        }

        // Create engine
        generativeEngine = GenerativeEngine(config, audioCallback)
    }

    private fun setupUI() {
        // Setup play/stop buttons
        // playButton.setOnClickListener {
        //     generativeEngine?.start()
        // }

        // stopButton.setOnClickListener {
        //     generativeEngine?.stop()
        // }

        // randomizeButton.setOnClickListener {
        //     generativeEngine?.randomize()
        // }

        // Setup parameter sliders
        // densitySlider.setOnSeekBarChangeListener { ... }
        // chaosSlider.setOnSeekBarChangeListener { ... }
    }

    override fun onDestroy() {
        super.onDestroy()
        generativeEngine?.stop()
        // audioEngine?.shutdown()
    }

    // JNI declarations (implement in your C++ audio engine)
    // private external fun nativePlayBass(midiNote: Int, velocity: Float)
    // private external fun nativePlayMelody(midiNote: Int, velocity: Float)
    // private external fun nativePlayPad(midiNote: Int, velocity: Float)
    // private external fun nativeTriggerKick(velocity: Float)
    // private external fun nativeTriggerSnare(velocity: Float)
    // private external fun nativeTriggerHiHat(velocity: Float)
}

/**
 * Example demonstrating manual pattern generation and analysis
 */
class PatternGeneratorExample {

    fun demonstrateEuclideanRhythms() {
        println("=== Euclidean Rhythms ===")

        // Four-on-floor kick
        val kick = EuclideanRhythm.generate(4, 16)
        println("Kick E(4,16): ${kick.map { if (it) 'X' else '.' }.joinToString(" ")}")

        // Cuban tresillo
        val tresillo = EuclideanRhythm.generate(5, 8)
        println("Tresillo E(5,8): ${tresillo.map { if (it) 'X' else '.' }.joinToString(" ")}")

        // Moderate hi-hat
        val hiHat = EuclideanRhythm.generate(7, 16)
        println("HiHat E(7,16): ${hiHat.map { if (it) 'X' else '.' }.joinToString(" ")}")

        println()
    }

    fun demonstrateMarkovChain() {
        println("=== Markov Chain Melody ===")

        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)
        println("Scale: ${scale.map { MusicTheory.getNoteNamee(it) }}")

        val chain = DubTechnoMarkov.createMelodyChain(scale, hypnotic = true)
        val melody = chain.generate(16, seed = listOf(scale[0]), randomSeed = 12345L)

        println("Generated melody:")
        melody.forEach { note ->
            println("  ${MusicTheory.getNoteNamee(note)} (MIDI $note)")
        }

        println()
    }

    fun demonstrateBassGeneration() {
        println("=== Bass Generation ===")

        val config = GenerativeConfig.classic()
        val generator = BassGenerator(config)
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)

        // Generate different bass styles
        val minimal = generator.generateDrone(36, scale)
        val classic = generator.generatePattern(36, scale, 16)
        val walking = generator.generateWalkingBass(36, scale, 16)

        println("Drone bass hits: ${minimal.countHits()}")
        println("Classic bass hits: ${classic.countHits()}")
        println("Walking bass hits: ${walking.countHits()}")

        // Show classic pattern
        println("\nClassic bass pattern:")
        for (i in 0 until classic.length) {
            val note = classic.getNoteAt(i)
            if (note != null) {
                println("  Step $i: ${MusicTheory.getNoteNamee(note.midiNote)}")
            }
        }

        println()
    }

    fun demonstrateEvolution() {
        println("=== Pattern Evolution ===")

        val config = GenerativeConfig(chaos = 0.5f, seed = 12345L)
        val evolution = EvolutionEngine(config)
        val scale = MusicTheory.getScaleNotes(60, ScaleType.MINOR)

        // Create initial pattern
        val bassGen = BassGenerator(config)
        var pattern = bassGen.generatePattern(36, scale, 16)

        println("Initial pattern density: ${pattern.getDensity()}")

        // Evolve 10 times
        repeat(10) { generation ->
            pattern = evolution.evolveMusical(pattern, scale, 0.3f)
            println("Generation $generation density: ${pattern.getDensity()}")
        }

        val stats = evolution.getStatistics()
        println("\nEvolution statistics:")
        println("  Generations: ${stats.generationCount}")
        println("  Total mutations: ${stats.totalMutations}")
        println("  Mutation types: ${stats.mutationCounts}")

        println()
    }

    fun demonstrateCompleteSystem() {
        println("=== Complete Generative System ===")

        val config = GenerativeConfig(
            rootNote = 60,
            scale = ScaleType.MINOR,
            tempo = 120f,
            density = 0.5f,
            chaos = 0.3f,
            seed = 12345L
        )

        println("Config:")
        println("  Root: ${MusicTheory.getNoteNamee(config.rootNote)}")
        println("  Scale: ${config.scale}")
        println("  Tempo: ${config.tempo} BPM")
        println("  Ms per 16th: ${config.getMsPerSixteenth()} ms")

        // Generate all patterns
        val scale = MusicTheory.getScaleNotes(config.rootNote, config.scale)

        val bassGen = BassGenerator(config)
        val melodyGen = MelodyGenerator(
            DubTechnoMarkov.createMelodyChain(scale),
            config
        )
        val drumGen = DrumGenerator(config)

        val bass = bassGen.generatePattern(36, scale, 16)
        val melody = melodyGen.generatePattern(60, scale, 32)
        val drums = drumGen.generatePattern(config.tempo)

        println("\nGenerated patterns:")
        println("  Bass: ${bass.countHits()} hits over ${bass.length} steps")
        println("  Melody: ${melody.countHits()} hits over ${melody.length} steps")
        println("  Kick: ${drums.kick.countHits()} hits")
        println("  Snare: ${drums.snare.countHits()} hits")
        println("  HiHat: ${drums.hiHat.countHits()} hits")

        println()
    }

    fun runAllDemos() {
        demonstrateEuclideanRhythms()
        demonstrateMarkovChain()
        demonstrateBassGeneration()
        demonstrateEvolution()
        demonstrateCompleteSystem()
    }
}

/**
 * Example showing different musical presets
 */
object PresetExamples {

    fun minimalAmbient(): GenerativeConfig {
        return GenerativeConfig(
            rootNote = 57,                    // A (warm, deep)
            scale = ScaleType.MINOR,
            tempo = 115f,
            density = 0.2f,                   // Very sparse
            chaos = 0.1f,                     // Very stable
            evolutionSpeed = 0.2f,            // Slow changes
            melodyRange = MelodyRange.LOW,
            rhythmComplexity = 0.3f,
            bassMovement = 0.1f,              // Almost static bass
            melodyDensity = 0.2f,             // Rare melody notes
            useEuclideanDrums = true
        )
    }

    fun classicDubTechno(): GenerativeConfig {
        return GenerativeConfig(
            rootNote = 60,                    // C
            scale = ScaleType.DORIAN,
            tempo = 124f,
            density = 0.5f,                   // Balanced
            chaos = 0.3f,                     // Moderate variation
            evolutionSpeed = 0.5f,
            melodyRange = MelodyRange.MID,
            rhythmComplexity = 0.5f,
            bassMovement = 0.3f,
            melodyDensity = 0.4f,
            useEuclideanDrums = true
        )
    }

    fun experimentalTechno(): GenerativeConfig {
        return GenerativeConfig(
            rootNote = 62,                    // D (brighter)
            scale = ScaleType.PHRYGIAN,
            tempo = 132f,
            density = 0.7f,                   // Busy
            chaos = 0.7f,                     // Chaotic
            evolutionSpeed = 0.8f,            // Rapid evolution
            melodyRange = MelodyRange.HIGH,
            rhythmComplexity = 0.8f,
            bassMovement = 0.6f,              // Active bassline
            melodyDensity = 0.6f,             // Dense melody
            useEuclideanDrums = true
        )
    }

    fun deepDrone(): GenerativeConfig {
        return GenerativeConfig(
            rootNote = 48,                    // Very low C
            scale = ScaleType.MINOR_PENTATONIC,
            tempo = 110f,
            density = 0.3f,
            chaos = 0.15f,
            evolutionSpeed = 0.3f,
            melodyRange = MelodyRange.LOW,
            rhythmComplexity = 0.4f,
            bassMovement = 0.05f,             // Almost no movement
            melodyDensity = 0.25f,
            useEuclideanDrums = true
        )
    }
}
