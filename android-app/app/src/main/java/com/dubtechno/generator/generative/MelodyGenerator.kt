package com.dubtechno.generator.generative

import com.dubtechno.generator.model.GenerativeConfig
import com.dubtechno.generator.model.MelodyRange
import com.dubtechno.generator.model.Note
import com.dubtechno.generator.model.Pattern
import kotlin.random.Random

/**
 * Generates melodic patterns for dub techno
 *
 * Uses Markov chains for coherent melodic movement
 * Characteristics:
 * - Sparse (lots of space between notes)
 * - Stays within scale
 * - Develops short motifs
 * - Hypnotic repetition with variation
 */
class MelodyGenerator(
    private val markovChain: MarkovChain,
    private val config: GenerativeConfig
) {

    private val random = config.seed?.let { Random(it) } ?: Random.Default

    /**
     * Generate melodic pattern
     */
    fun generatePattern(
        rootNote: Int,
        scale: List<Int>,
        length: Int = 32
    ): Pattern {
        // Adjust root note by melody range
        val adjustedRoot = rootNote + (config.melodyRange.octaveOffset * 12)

        // Generate note sequence using Markov chain
        val noteCount = (length * config.melodyDensity).toInt().coerceAtLeast(1)
        val markovNotes = try {
            markovChain.generate(noteCount, seed = listOf(scale[0]), randomSeed = config.seed)
        } catch (e: Exception) {
            // Fallback if Markov chain fails
            List(noteCount) { scale[0] }
        }

        // Distribute notes across the pattern length
        val notes = mutableListOf<Note?>()

        // Calculate step positions for notes
        val stepInterval = if (noteCount > 1) length / noteCount else length
        var currentNoteIndex = 0

        for (i in 0 until length) {
            val shouldPlaceNote = if (noteCount == 1) {
                i == 0
            } else {
                i % stepInterval == 0 && currentNoteIndex < markovNotes.size
            }

            if (shouldPlaceNote) {
                val scaleNote = markovNotes[currentNoteIndex]
                val midiNote = adjustedRoot + scaleNote

                // Velocity variation for humanization
                val baseVelocity = 0.5f + (config.density * 0.2f)
                val variation = (random.nextFloat() - 0.5f) * 0.15f
                val velocity = (baseVelocity + variation).coerceIn(0.3f, 0.7f)

                // Duration based on density
                val duration = if (config.density < 0.5f) 0.5f else 0.25f

                // Add probability for sparse feel
                val probability = 0.7f + (config.chaos * 0.3f)

                notes.add(Note(midiNote, velocity, duration, probability))
                currentNoteIndex++
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, length, config.seed)
    }

    /**
     * Generate short melodic motif (2-4 notes)
     */
    fun generateMotif(scale: List<Int>, length: Int = 4): List<Int> {
        val motif = mutableListOf<Int>()
        val root = scale[0]

        // Start on root or nearby note
        motif.add(root)

        // Add stepwise motion
        for (i in 1 until length) {
            val lastNote = motif.last()
            val scaleIndex = scale.indexOf(lastNote)

            // Move up or down by step
            val direction = if (random.nextBoolean()) 1 else -1
            val newIndex = (scaleIndex + direction).coerceIn(0, scale.size - 1)

            motif.add(scale[newIndex])
        }

        return motif
    }

    /**
     * Vary an existing motif
     */
    fun varyMotif(motif: List<Int>, variation: MotifVariation, scale: List<Int>): List<Int> {
        return when (variation) {
            MotifVariation.TRANSPOSE -> {
                // Transpose up or down by a scale degree
                val offset = scale[1] - scale[0]
                motif.map { note ->
                    val transposed = note + offset
                    // Quantize to scale
                    scale.minByOrNull { Math.abs(it - transposed) } ?: note
                }
            }

            MotifVariation.RETROGRADE -> {
                // Play backwards
                motif.reversed()
            }

            MotifVariation.INVERT -> {
                // Invert intervals around the first note
                val pivot = motif.firstOrNull() ?: return motif
                motif.map { note ->
                    val interval = note - pivot
                    val inverted = pivot - interval
                    // Quantize to scale
                    scale.minByOrNull { Math.abs(it - inverted) } ?: note
                }
            }

            MotifVariation.AUGMENT -> {
                // Double the rhythmic values (extend motif)
                motif.flatMap { listOf(it, it) }
            }

            MotifVariation.DIMINISH -> {
                // Half the rhythmic values (compress motif)
                motif.filterIndexed { index, _ -> index % 2 == 0 }
            }
        }
    }

    /**
     * Generate pattern from motif with repetition and variation
     */
    fun generateFromMotif(
        rootNote: Int,
        scale: List<Int>,
        motif: List<Int>,
        length: Int = 32
    ): Pattern {
        val adjustedRoot = rootNote + (config.melodyRange.octaveOffset * 12)
        val notes = mutableListOf<Note?>()

        var currentMotif = motif
        var motifPosition = 0

        for (i in 0 until length) {
            // Place notes sparsely
            val shouldPlaceNote = i % 4 == 0 || (i % 2 == 0 && config.density > 0.6f)

            if (shouldPlaceNote && motifPosition < currentMotif.size) {
                val scaleNote = currentMotif[motifPosition]
                val midiNote = adjustedRoot + scaleNote

                val velocity = 0.5f + (random.nextFloat() * 0.2f)
                notes.add(Note(midiNote, velocity, 0.25f))

                motifPosition++

                // When motif completes, maybe vary it
                if (motifPosition >= currentMotif.size) {
                    motifPosition = 0

                    // Sometimes vary the motif
                    if (random.nextFloat() < config.chaos) {
                        val variations = MotifVariation.values()
                        val randomVariation = variations[random.nextInt(variations.size)]
                        currentMotif = varyMotif(currentMotif, randomVariation, scale)
                    }
                }
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, length, config.seed)
    }

    /**
     * Generate arpeggio pattern
     */
    fun generateArpeggio(
        rootNote: Int,
        scale: List<Int>,
        length: Int = 16
    ): Pattern {
        val adjustedRoot = rootNote + (config.melodyRange.octaveOffset * 12)
        val notes = mutableListOf<Note?>()

        // Simple up-down arpeggio through scale
        val arpeggioPattern = listOf(0, 2, 4, 2) // I - III - V - III
        var arpeggioIndex = 0

        for (i in 0 until length) {
            if (i % 2 == 0) { // 8th notes
                val scaleIndex = arpeggioPattern[arpeggioIndex % arpeggioPattern.size]
                val note = if (scaleIndex < scale.size) {
                    adjustedRoot + scale[scaleIndex]
                } else {
                    adjustedRoot + scale[0]
                }

                val velocity = 0.5f + (random.nextFloat() * 0.15f)
                notes.add(Note(note, velocity, 0.25f))

                arpeggioIndex++
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, length, config.seed)
    }
}

/**
 * Motif variation techniques
 */
enum class MotifVariation {
    TRANSPOSE,  // Move up or down by interval
    RETROGRADE, // Play backwards
    INVERT,     // Flip intervals
    AUGMENT,    // Lengthen rhythm
    DIMINISH    // Shorten rhythm
}
