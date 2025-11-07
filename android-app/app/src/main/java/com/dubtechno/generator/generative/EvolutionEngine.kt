package com.dubtechno.generator.generative

import com.dubtechno.generator.model.GenerativeConfig
import com.dubtechno.generator.model.Note
import com.dubtechno.generator.model.Pattern
import kotlin.random.Random

/**
 * Evolution engine for gradually mutating musical patterns
 *
 * Implements genetic algorithm concepts to evolve patterns over time:
 * - Mutation: Random small changes
 * - Selection: Keep changes that maintain musicality
 * - Gradual evolution: Prevents abrupt changes
 */
class EvolutionEngine(private val config: GenerativeConfig) {

    private val random = config.seed?.let { Random(it) } ?: Random.Default
    private var generationCount = 0
    private val mutationHistory = mutableListOf<String>()

    /**
     * Evolve pattern based on mutation rate
     *
     * @param pattern Pattern to evolve
     * @param mutationRate Probability of mutation (0.0-1.0)
     * @return Evolved pattern (may be unchanged)
     */
    fun evolvePattern(pattern: Pattern, mutationRate: Float = 0.3f): Pattern {
        generationCount++

        // Decide whether to mutate based on rate and evolution speed
        val effectiveMutationRate = mutationRate * config.evolutionSpeed
        if (!shouldMutate(effectiveMutationRate)) {
            return pattern
        }

        // Apply random mutation
        val mutated = applyRandomMutation(pattern)

        // Log mutation for analysis
        logMutation("Generation $generationCount: ${getLastMutationType()}")

        return mutated
    }

    /**
     * Decide if mutation should occur
     */
    private fun shouldMutate(mutationRate: Float): Boolean {
        return random.nextFloat() < mutationRate
    }

    /**
     * Apply a random mutation to the pattern
     */
    private fun applyRandomMutation(pattern: Pattern): Pattern {
        val mutationType = random.nextInt(8)

        return when (mutationType) {
            0 -> changeOneNote(pattern)
            1 -> addRest(pattern)
            2 -> removeRest(pattern)
            3 -> shiftPattern(pattern)
            4 -> adjustVelocities(pattern)
            5 -> adjustProbabilities(pattern)
            6 -> swapNotes(pattern)
            7 -> duplicateNote(pattern)
            else -> pattern
        }
    }

    /**
     * Change a single note in the pattern
     */
    private fun changeOneNote(pattern: Pattern): Pattern {
        val noteIndices = pattern.notes.indices.filter { pattern.notes[it] != null }
        if (noteIndices.isEmpty()) return pattern

        val indexToChange = noteIndices.random(random)
        val currentNote = pattern.notes[indexToChange] ?: return pattern

        // Transpose by small interval
        val transposition = when (random.nextInt(5)) {
            0 -> -2 // Down a step
            1 -> -1 // Down a semitone
            2 -> 1  // Up a semitone
            3 -> 2  // Up a step
            else -> 0
        }

        val newNote = currentNote.transpose(transposition)
        val newNotes = pattern.notes.toMutableList()
        newNotes[indexToChange] = newNote

        mutationHistory.add("changeOneNote")
        return Pattern(newNotes, pattern.length, pattern.seed)
    }

    /**
     * Convert a note to a rest
     */
    private fun addRest(pattern: Pattern): Pattern {
        val noteIndices = pattern.notes.indices.filter { pattern.notes[it] != null }
        if (noteIndices.isEmpty()) return pattern

        val indexToRemove = noteIndices.random(random)
        val newNotes = pattern.notes.toMutableList()
        newNotes[indexToRemove] = null

        mutationHistory.add("addRest")
        return Pattern(newNotes, pattern.length, pattern.seed)
    }

    /**
     * Convert a rest to a note
     */
    private fun removeRest(pattern: Pattern): Pattern {
        val restIndices = pattern.notes.indices.filter { pattern.notes[it] == null }
        if (restIndices.isEmpty()) return pattern

        // Find a note to copy
        val existingNote = pattern.notes.firstOrNull { it != null } ?: return pattern

        val indexToFill = restIndices.random(random)
        val newNotes = pattern.notes.toMutableList()

        // Add similar note with possibly different velocity
        val velocityVariation = (random.nextFloat() - 0.5f) * 0.2f
        val newNote = existingNote.copy(
            velocity = (existingNote.velocity + velocityVariation).coerceIn(0.1f, 1.0f)
        )
        newNotes[indexToFill] = newNote

        mutationHistory.add("removeRest")
        return Pattern(newNotes, pattern.length, pattern.seed)
    }

    /**
     * Rotate pattern by one step
     */
    private fun shiftPattern(pattern: Pattern): Pattern {
        val direction = if (random.nextBoolean()) 1 else -1

        mutationHistory.add("shiftPattern")
        return pattern.rotate(direction)
    }

    /**
     * Adjust velocities of all notes
     */
    private fun adjustVelocities(pattern: Pattern): Pattern {
        val adjustment = (random.nextFloat() - 0.5f) * 0.3f // ±15%

        val newNotes = pattern.notes.map { note ->
            note?.let {
                val newVelocity = (it.velocity + adjustment).coerceIn(0.1f, 1.0f)
                it.copy(velocity = newVelocity)
            }
        }

        mutationHistory.add("adjustVelocities")
        return Pattern(newNotes, pattern.length, pattern.seed)
    }

    /**
     * Adjust note trigger probabilities
     */
    private fun adjustProbabilities(pattern: Pattern): Pattern {
        val adjustment = (random.nextFloat() - 0.5f) * 0.4f

        val newNotes = pattern.notes.map { note ->
            note?.let {
                val newProbability = (it.probability + adjustment).coerceIn(0.3f, 1.0f)
                it.copy(probability = newProbability)
            }
        }

        mutationHistory.add("adjustProbabilities")
        return Pattern(newNotes, pattern.length, pattern.seed)
    }

    /**
     * Swap two notes in the pattern
     */
    private fun swapNotes(pattern: Pattern): Pattern {
        val noteIndices = pattern.notes.indices.filter { pattern.notes[it] != null }
        if (noteIndices.size < 2) return pattern

        val index1 = noteIndices.random(random)
        val index2 = noteIndices.filter { it != index1 }.randomOrNull(random) ?: return pattern

        val newNotes = pattern.notes.toMutableList()
        val temp = newNotes[index1]
        newNotes[index1] = newNotes[index2]
        newNotes[index2] = temp

        mutationHistory.add("swapNotes")
        return Pattern(newNotes, pattern.length, pattern.seed)
    }

    /**
     * Duplicate a note to a nearby position
     */
    private fun duplicateNote(pattern: Pattern): Pattern {
        val noteIndices = pattern.notes.indices.filter { pattern.notes[it] != null }
        if (noteIndices.isEmpty()) return pattern

        val sourcIndex = noteIndices.random(random)
        val sourceNote = pattern.notes[sourcIndex] ?: return pattern

        // Find nearby rest position
        val nearbyRests = pattern.notes.indices.filter {
            pattern.notes[it] == null && Math.abs(it - sourcIndex) <= 4
        }

        if (nearbyRests.isEmpty()) return pattern

        val targetIndex = nearbyRests.random(random)
        val newNotes = pattern.notes.toMutableList()
        newNotes[targetIndex] = sourceNote.copy()

        mutationHistory.add("duplicateNote")
        return Pattern(newNotes, pattern.length, pattern.seed)
    }

    /**
     * Get the last mutation type applied
     */
    private fun getLastMutationType(): String {
        return mutationHistory.lastOrNull() ?: "none"
    }

    /**
     * Evolve pattern with constraints (maintain musicality)
     */
    fun evolveMusical(pattern: Pattern, scale: List<Int>, mutationRate: Float = 0.3f): Pattern {
        val evolved = evolvePattern(pattern, mutationRate)

        // Quantize notes to scale to maintain tonality
        val quantized = quantizeToScale(evolved, scale)

        return quantized
    }

    /**
     * Quantize all notes in pattern to scale
     */
    private fun quantizeToScale(pattern: Pattern, scale: List<Int>): Pattern {
        val newNotes = pattern.notes.map { note ->
            note?.let {
                // Find closest note in scale
                val quantizedMidi = MusicTheory.quantizeToScale(it.midiNote, scale)
                if (quantizedMidi != it.midiNote) {
                    it.copy(midiNote = quantizedMidi)
                } else {
                    it
                }
            }
        }

        return Pattern(newNotes, pattern.length, pattern.seed)
    }

    /**
     * Crossover two patterns (genetic algorithm)
     */
    fun crossover(pattern1: Pattern, pattern2: Pattern): Pattern {
        require(pattern1.length == pattern2.length) {
            "Patterns must be same length for crossover"
        }

        val crossoverPoint = random.nextInt(pattern1.length)

        val newNotes = pattern1.notes.take(crossoverPoint) +
                pattern2.notes.drop(crossoverPoint)

        return Pattern(newNotes, pattern1.length)
    }

    /**
     * Get evolution statistics
     */
    fun getStatistics(): EvolutionStats {
        val mutationCounts = mutationHistory.groupingBy { it }.eachCount()

        return EvolutionStats(
            generationCount = generationCount,
            mutationCounts = mutationCounts,
            totalMutations = mutationHistory.size
        )
    }

    /**
     * Reset evolution state
     */
    fun reset() {
        generationCount = 0
        mutationHistory.clear()
    }

    data class EvolutionStats(
        val generationCount: Int,
        val mutationCounts: Map<String, Int>,
        val totalMutations: Int
    )
}
