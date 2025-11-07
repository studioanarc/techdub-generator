package com.dubtechno.generator.generative

import com.dubtechno.generator.model.GenerativeConfig
import com.dubtechno.generator.model.Pattern
import kotlinx.coroutines.*
import kotlin.math.roundToLong

/**
 * Master generative music engine
 *
 * Coordinates all generators and handles real-time playback scheduling.
 * This is the main entry point for the generative music system.
 */
class GenerativeEngine(
    private val config: GenerativeConfig,
    private val audioCallback: AudioCallback
) {
    // Generators
    private val bassGenerator = BassGenerator(config)
    private val markovChain = DubTechnoMarkov.createMelodyChain(
        scale = MusicTheory.getScaleNotes(config.rootNote, config.scale),
        hypnotic = config.density < 0.6f
    )
    private val melodyGenerator = MelodyGenerator(markovChain, config)
    private val drumGenerator = DrumGenerator(config)
    private val harmonyGenerator = HarmonyGenerator(config)
    private val evolutionEngine = EvolutionEngine(config)

    // Current patterns
    private var currentBassPattern: Pattern? = null
    private var currentMelodyPattern: Pattern? = null
    private var currentDrumPattern: DrumGenerator.DrumPattern? = null
    private var currentHarmonyPattern: Pattern? = null

    // Playback state
    private var isPlaying = false
    private var stepCounter = 0
    private val patternLength = 16 // 16th notes per pattern
    private var playbackJob: Job? = null

    // Timing
    private var lastStepTime = 0L
    private val msPerSixteenth = config.getMsPerSixteenth()

    // Evolution tracking
    private var barsPlayed = 0
    private val evolutionIntervalBars = 32 // Evolve every 32 bars

    /**
     * Start the generative engine
     */
    fun start() {
        if (isPlaying) return

        isPlaying = true
        generateNewPatterns()
        startScheduler()
    }

    /**
     * Stop the generative engine
     */
    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        stepCounter = 0
        barsPlayed = 0
    }

    /**
     * Randomize all patterns
     */
    fun randomize() {
        generateNewPatterns()
    }

    /**
     * Generate new patterns for all instruments
     */
    private fun generateNewPatterns() {
        val scale = MusicTheory.getScaleNotes(config.rootNote, config.scale, octaves = 2)

        // Generate bass
        currentBassPattern = bassGenerator.generateAdaptive(
            rootNote = config.rootNote - 24, // Two octaves down
            scale = scale,
            length = patternLength
        )

        // Generate melody
        currentMelodyPattern = melodyGenerator.generatePattern(
            rootNote = config.rootNote,
            scale = scale,
            length = 32 // Longer pattern for melody
        )

        // Generate drums
        currentDrumPattern = drumGenerator.generateAdaptive(config.tempo)

        // Generate harmony (if density is high enough)
        if (config.density > 0.4f) {
            val progression = harmonyGenerator.generateProgression(
                rootNote = config.rootNote,
                scale = scale,
                length = 4
            )
            currentHarmonyPattern = if (progression.isNotEmpty()) {
                harmonyGenerator.generatePad(
                    chord = progression[0],
                    rootNote = config.rootNote,
                    length = patternLength
                )
            } else {
                null
            }
        }
    }

    /**
     * Start the timing scheduler
     */
    private fun startScheduler() {
        playbackJob = CoroutineScope(Dispatchers.Default).launch {
            lastStepTime = System.currentTimeMillis()

            while (isPlaying) {
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - lastStepTime

                // Check if it's time for next step
                if (elapsed >= msPerSixteenth) {
                    // Compensate for drift
                    val drift = elapsed - msPerSixteenth
                    lastStepTime = currentTime - drift

                    // Process step
                    onStep()

                    stepCounter++
                }

                // Sleep for a short time to prevent busy-waiting
                delay(1L)
            }
        }
    }

    /**
     * Process a single step (16th note)
     */
    private fun onStep() {
        val step = stepCounter % patternLength
        val melodyStep = stepCounter % 32 // Melody has longer pattern

        // Trigger bass
        currentBassPattern?.getNoteAt(step)?.let { note ->
            if (shouldTrigger(note.probability)) {
                audioCallback.playBassNote(note.midiNote, note.velocity)
            }
        }

        // Trigger melody
        currentMelodyPattern?.getNoteAt(melodyStep)?.let { note ->
            if (shouldTrigger(note.probability)) {
                audioCallback.playMelodyNote(note.midiNote, note.velocity)
            }
        }

        // Trigger drums
        currentDrumPattern?.let { drums ->
            // Kick
            drums.kick.getNoteAt(step)?.let { note ->
                if (shouldTrigger(note.probability)) {
                    audioCallback.triggerKick(note.velocity)
                }
            }

            // Snare
            drums.snare.getNoteAt(step)?.let { note ->
                if (shouldTrigger(note.probability)) {
                    audioCallback.triggerSnare(note.velocity)
                }
            }

            // Hi-hat
            drums.hiHat.getNoteAt(step)?.let { note ->
                if (shouldTrigger(note.probability)) {
                    audioCallback.triggerHiHat(note.velocity)
                }
            }

            // Percussion (if present)
            drums.percussion?.getNoteAt(step)?.let { note ->
                if (shouldTrigger(note.probability)) {
                    audioCallback.triggerPercussion(note.velocity)
                }
            }
        }

        // Trigger harmony
        if (step == 0) { // Re-trigger pad at start of pattern
            currentHarmonyPattern?.getNoteAt(0)?.let { note ->
                audioCallback.playPad(note.midiNote, note.velocity)
            }
        }

        // Check for pattern evolution
        if (step == 0) {
            barsPlayed++

            if (barsPlayed % evolutionIntervalBars == 0) {
                evolvePatterns()
            }
        }
    }

    /**
     * Determine if a note should trigger based on probability
     */
    private fun shouldTrigger(probability: Float): Boolean {
        return Math.random() < probability
    }

    /**
     * Evolve patterns gradually
     */
    private fun evolvePatterns() {
        val scale = MusicTheory.getScaleNotes(config.rootNote, config.scale, octaves = 2)
        val mutationRate = config.chaos

        // Evolve bass
        currentBassPattern?.let { bass ->
            currentBassPattern = evolutionEngine.evolveMusical(bass, scale, mutationRate)
        }

        // Evolve melody
        currentMelodyPattern?.let { melody ->
            currentMelodyPattern = evolutionEngine.evolveMusical(melody, scale, mutationRate)
        }

        // Evolve drums (less frequently)
        if (barsPlayed % (evolutionIntervalBars * 2) == 0) {
            currentDrumPattern = drumGenerator.generateAdaptive(config.tempo)
        }

        // Notify callback about evolution
        audioCallback.onPatternsEvolved(barsPlayed)
    }

    /**
     * Get current playback state
     */
    fun getPlaybackState(): PlaybackState {
        return PlaybackState(
            isPlaying = isPlaying,
            currentStep = stepCounter,
            currentBar = barsPlayed,
            tempo = config.tempo,
            evolutionStats = evolutionEngine.getStatistics()
        )
    }

    /**
     * Update configuration on the fly
     */
    fun updateConfig(newConfig: GenerativeConfig) {
        // Note: This would require recreating the engine
        // For now, just note that it's possible
        // In production, you'd want to smoothly transition
    }

    /**
     * Get current patterns for visualization
     */
    fun getCurrentPatterns(): CurrentPatterns {
        return CurrentPatterns(
            bass = currentBassPattern,
            melody = currentMelodyPattern,
            kick = currentDrumPattern?.kick,
            snare = currentDrumPattern?.snare,
            hiHat = currentDrumPattern?.hiHat,
            harmony = currentHarmonyPattern
        )
    }

    data class PlaybackState(
        val isPlaying: Boolean,
        val currentStep: Int,
        val currentBar: Int,
        val tempo: Float,
        val evolutionStats: EvolutionEngine.EvolutionStats
    )

    data class CurrentPatterns(
        val bass: Pattern?,
        val melody: Pattern?,
        val kick: Pattern?,
        val snare: Pattern?,
        val hiHat: Pattern?,
        val harmony: Pattern?
    )

    /**
     * Callback interface for audio engine integration
     */
    interface AudioCallback {
        fun playBassNote(midiNote: Int, velocity: Float)
        fun playMelodyNote(midiNote: Int, velocity: Float)
        fun playPad(midiNote: Int, velocity: Float)
        fun triggerKick(velocity: Float)
        fun triggerSnare(velocity: Float)
        fun triggerHiHat(velocity: Float)
        fun triggerPercussion(velocity: Float)
        fun onPatternsEvolved(bar: Int)
    }
}

/**
 * Builder for easy GenerativeEngine construction
 */
class GenerativeEngineBuilder {
    private var config: GenerativeConfig = GenerativeConfig.classic()
    private var audioCallback: GenerativeEngine.AudioCallback? = null

    fun setConfig(config: GenerativeConfig) = apply {
        this.config = config
    }

    fun setAudioCallback(callback: GenerativeEngine.AudioCallback) = apply {
        this.audioCallback = callback
    }

    fun setRootNote(note: Int) = apply {
        config = config.copy(rootNote = note)
    }

    fun setTempo(tempo: Float) = apply {
        config = config.copy(tempo = tempo)
    }

    fun setDensity(density: Float) = apply {
        config = config.copy(density = density)
    }

    fun setChaos(chaos: Float) = apply {
        config = config.copy(chaos = chaos)
    }

    fun build(): GenerativeEngine {
        requireNotNull(audioCallback) { "AudioCallback must be set" }
        return GenerativeEngine(config, audioCallback!!)
    }
}
