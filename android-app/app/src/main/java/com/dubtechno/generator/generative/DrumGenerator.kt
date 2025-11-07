package com.dubtechno.generator.generative

import com.dubtechno.generator.model.GenerativeConfig
import com.dubtechno.generator.model.Note
import com.dubtechno.generator.model.Pattern
import kotlin.random.Random

/**
 * Generates drum patterns for dub techno
 *
 * Dub techno drums:
 * - Deep, muffled kick (often half-time)
 * - Sparse, reverberant snare/clap
 * - Crisp hi-hats with probability
 * - Euclidean rhythms for even distribution
 */
class DrumGenerator(private val config: GenerativeConfig) {

    private val random = config.seed?.let { Random(it) } ?: Random.Default

    /**
     * Container for complete drum pattern
     */
    data class DrumPattern(
        val kick: Pattern,
        val snare: Pattern,
        val hiHat: Pattern,
        val percussion: Pattern? = null,
        val openHat: Pattern? = null
    )

    /**
     * Generate complete drum pattern
     */
    fun generatePattern(tempo: Float): DrumPattern {
        val kick = generateKick()
        val snare = generateSnare()
        val hiHat = generateHiHat()
        val percussion = if (config.density > 0.5f) generatePercussion() else null
        val openHat = if (config.rhythmComplexity > 0.6f) generateOpenHat() else null

        return DrumPattern(kick, snare, hiHat, percussion, openHat)
    }

    /**
     * Generate kick drum pattern
     */
    private fun generateKick(): Pattern {
        return if (config.useEuclideanDrums) {
            // Use Euclidean rhythms for even distribution
            when {
                config.rhythmComplexity < 0.3f -> {
                    // Half-time kick (very sparse)
                    EuclideanRhythm.DubPresets.kickHalfTime(0.85f)
                }
                config.rhythmComplexity < 0.6f -> {
                    // Four-on-floor
                    EuclideanRhythm.DubPresets.kickFourOnFloor(0.8f)
                }
                else -> {
                    // More complex
                    EuclideanRhythm.DubPresets.kickSyncopated(0.8f)
                }
            }
        } else {
            // Manual pattern
            generateKickManual()
        }
    }

    /**
     * Generate kick manually (non-Euclidean)
     */
    private fun generateKickManual(): Pattern {
        val notes = mutableListOf<Note?>()
        val kickNote = 36 // MIDI note for kick

        for (i in 0 until 16) {
            val placeKick = when {
                // Always on beat 1
                i == 0 -> true
                // Beat 3 for half-time feel
                i == 8 && config.rhythmComplexity < 0.5f -> true
                // Beat 2 and 4 for four-on-floor
                (i == 4 || i == 12) && config.rhythmComplexity >= 0.5f -> true
                // Occasional syncopation
                else -> random.nextFloat() < config.chaos * 0.2f
            }

            if (placeKick) {
                val velocity = 0.75f + (random.nextFloat() * 0.15f)
                notes.add(Note(kickNote, velocity, 0.25f))
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, 16, config.seed)
    }

    /**
     * Generate snare/clap pattern
     */
    private fun generateSnare(): Pattern {
        return if (config.useEuclideanDrums) {
            // Euclidean snare patterns
            when {
                config.density < 0.4f -> {
                    // Very sparse
                    EuclideanRhythm.DubPresets.snareSparse(0.6f)
                }
                else -> {
                    // Backbeat (2 and 4)
                    EuclideanRhythm.DubPresets.snareBackbeat(0.65f)
                }
            }
        } else {
            generateSnareManual()
        }
    }

    /**
     * Generate snare manually
     */
    private fun generateSnareManual(): Pattern {
        val notes = mutableListOf<Note?>()
        val snareNote = 38 // MIDI note for snare

        for (i in 0 until 16) {
            val placeSnare = when {
                // Backbeat (beats 2 and 4)
                i == 4 || i == 12 -> config.density > 0.3f
                // Occasional ghost notes
                else -> random.nextFloat() < config.density * 0.15f
            }

            if (placeSnare) {
                // Ghost notes have lower velocity
                val isGhostNote = i != 4 && i != 12
                val velocity = if (isGhostNote) {
                    0.3f + (random.nextFloat() * 0.15f)
                } else {
                    0.6f + (random.nextFloat() * 0.15f)
                }
                notes.add(Note(snareNote, velocity, 0.25f))
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, 16, config.seed)
    }

    /**
     * Generate hi-hat pattern
     */
    private fun generateHiHat(): Pattern {
        return if (config.useEuclideanDrums) {
            // Euclidean hi-hat with probability
            when {
                config.density < 0.3f -> {
                    EuclideanRhythm.generateWithProbability(
                        hits = 5,
                        steps = 16,
                        note = 42,
                        velocity = 0.4f,
                        probability = 0.75f
                    )
                }
                config.density < 0.7f -> {
                    EuclideanRhythm.generateWithProbability(
                        hits = 7,
                        steps = 16,
                        note = 42,
                        velocity = 0.45f,
                        probability = 0.8f
                    )
                }
                else -> {
                    EuclideanRhythm.generateWithProbability(
                        hits = 11,
                        steps = 16,
                        note = 42,
                        velocity = 0.5f,
                        probability = 0.85f
                    )
                }
            }
        } else {
            generateHiHatManual()
        }
    }

    /**
     * Generate hi-hat manually with velocity accents
     */
    private fun generateHiHatManual(): Pattern {
        val notes = mutableListOf<Note?>()
        val hiHatNote = 42 // Closed hi-hat

        for (i in 0 until 16) {
            // Place hi-hats based on density
            val probability = when {
                i % 4 == 0 -> 0.9f // Strong on-beat
                i % 2 == 0 -> 0.7f // Medium on 8th notes
                else -> config.density * 0.6f // Off-beat depends on density
            }

            if (random.nextFloat() < probability) {
                // Accent on-beat hi-hats
                val velocity = if (i % 4 == 0) {
                    0.5f + (random.nextFloat() * 0.1f)
                } else {
                    0.35f + (random.nextFloat() * 0.1f)
                }

                notes.add(Note(hiHatNote, velocity, 0.25f))
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, 16, config.seed)
    }

    /**
     * Generate open hi-hat pattern (occasional)
     */
    private fun generateOpenHat(): Pattern {
        val notes = mutableListOf<Note?>()
        val openHatNote = 46 // Open hi-hat

        for (i in 0 until 16) {
            // Open hat occasionally on off-beats
            val placeOpenHat = (i % 4 == 2) && random.nextFloat() < 0.3f

            if (placeOpenHat) {
                val velocity = 0.4f + (random.nextFloat() * 0.15f)
                notes.add(Note(openHatNote, velocity, 0.5f)) // Longer decay
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, 16, config.seed)
    }

    /**
     * Generate percussion (congas, shakers, etc.)
     */
    private fun generatePercussion(): Pattern {
        return if (config.useEuclideanDrums) {
            // Euclidean conga pattern
            EuclideanRhythm.DubPresets.percussion(0.35f)
        } else {
            generatePercussionManual()
        }
    }

    /**
     * Generate percussion manually
     */
    private fun generatePercussionManual(): Pattern {
        val notes = mutableListOf<Note?>()
        val percNote = 45 // Low tom or conga

        for (i in 0 until 16) {
            // Sparse percussion fills
            val placePerc = random.nextFloat() < config.density * 0.25f

            if (placePerc) {
                val velocity = 0.3f + (random.nextFloat() * 0.2f)
                notes.add(Note(percNote, velocity, 0.25f))
            } else {
                notes.add(null)
            }
        }

        return Pattern(notes, 16, config.seed)
    }

    /**
     * Add swing timing to pattern
     *
     * Delays every other 16th note by a small amount
     * This creates a "shuffle" or "groovy" feel
     */
    fun addSwing(pattern: Pattern, amount: Float = 0.5f): Pattern {
        // Swing is typically applied in the timing/scheduling
        // For now, we adjust velocities to simulate accent patterns
        val swingAmount = amount.coerceIn(0f, 1f)

        val notes = pattern.notes.mapIndexed { index, note ->
            note?.let {
                // Every other 16th note gets slightly lower velocity
                if (index % 2 == 1) {
                    val velocityReduction = swingAmount * 0.15f
                    it.copy(velocity = (it.velocity - velocityReduction).coerceAtLeast(0.1f))
                } else {
                    it
                }
            }
        }

        return Pattern(notes, pattern.length, pattern.seed)
    }

    /**
     * Humanize drum pattern with velocity and timing variations
     */
    fun humanize(pattern: Pattern, amount: Float = 0.5f): Pattern {
        val humanizeAmount = amount.coerceIn(0f, 1f)

        val notes = pattern.notes.map { note ->
            note?.let {
                // Add random velocity variation
                val velocityVariation = (random.nextFloat() - 0.5f) * humanizeAmount * 0.2f
                val newVelocity = (it.velocity + velocityVariation).coerceIn(0.1f, 1.0f)

                it.copy(velocity = newVelocity)
            }
        }

        return Pattern(notes, pattern.length, pattern.seed)
    }

    /**
     * Generate adaptive drum pattern based on config
     */
    fun generateAdaptive(tempo: Float): DrumPattern {
        val basePattern = generatePattern(tempo)

        // Apply swing if configured
        val swungKick = if (config.swing > 0f) {
            addSwing(basePattern.kick, config.swing)
        } else {
            basePattern.kick
        }

        val swungHiHat = if (config.swing > 0f) {
            addSwing(basePattern.hiHat, config.swing)
        } else {
            basePattern.hiHat
        }

        // Humanize if chaos is enabled
        val humanizedSnare = if (config.chaos > 0.3f) {
            humanize(basePattern.snare, config.chaos)
        } else {
            basePattern.snare
        }

        return DrumPattern(
            kick = swungKick,
            snare = humanizedSnare,
            hiHat = swungHiHat,
            percussion = basePattern.percussion,
            openHat = basePattern.openHat
        )
    }
}
