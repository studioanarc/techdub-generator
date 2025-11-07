/**
 * Markov Chain Melody Generator for Dub Techno
 *
 * Generates musical melodies using probabilistic state transitions.
 * Dub techno characteristics:
 * - Sparse melodies (lots of rests)
 * - Repetitive motifs (high probability of staying on same note)
 * - Stepwise motion (prefer small intervals)
 * - Occasional jumps for interest
 * - Gravitates toward root note
 */

import { getScaleNotes, getInterval } from './scales.js';

/**
 * Weighted random selection
 * @param {Array} options - Array of {value, weight} objects
 * @returns {*} - Randomly selected value
 */
function weightedRandom(options) {
    const totalWeight = options.reduce((sum, opt) => sum + opt.weight, 0);
    let random = Math.random() * totalWeight;

    for (let option of options) {
        random -= option.weight;
        if (random <= 0) {
            return option.value;
        }
    }

    return options[options.length - 1].value;
}

/**
 * Markov Melody Generator Class
 */
export class MarkovMelodyGenerator {
    constructor(options = {}) {
        // Musical parameters
        this.root = options.root || 'C';
        this.scale = options.scale || 'minor';
        this.octave = options.octave || 3;
        this.octaves = options.octaves || 2;

        // Behavior parameters
        this.density = options.density || 0.6;           // 0-1: How many notes vs rests
        this.repetition = options.repetition || 0.5;     // 0-1: Stay on same note
        this.stepwise = options.stepwise || 0.7;         // 0-1: Prefer small intervals
        this.rootGravity = options.rootGravity || 0.3;   // 0-1: Pull toward root
        this.jumpiness = options.jumpiness || 0.1;       // 0-1: Large interval jumps

        // Get scale notes
        this.scaleNotes = getScaleNotes(this.root, this.scale, this.octave, this.octaves);
        this.rootNote = this.scaleNotes[0];

        // State
        this.currentNote = this.rootNote;
        this.previousNote = null;
        this.motif = []; // Store generated motifs for repetition
    }

    /**
     * Generate a single note (or rest) based on current state
     * @returns {Object|null} - Note object or null for rest
     */
    generateNextNote() {
        // First decide: note or rest?
        if (Math.random() > this.density) {
            this.previousNote = this.currentNote;
            return null; // Rest
        }

        // Should we repeat a motif?
        if (this.motif.length > 0 && Math.random() < 0.3) {
            const motifNote = this.motif[Math.floor(Math.random() * this.motif.length)];
            const note = this.scaleNotes.find(n => n.midi === motifNote.midi);
            if (note) {
                this.previousNote = this.currentNote;
                this.currentNote = note;
                return note;
            }
        }

        // Build weighted options for next note
        const options = [];

        for (let note of this.scaleNotes) {
            let weight = 1.0; // Base weight

            // Stay on same note (dub loves repetition)
            if (note.midi === this.currentNote.midi) {
                weight *= (1 + this.repetition * 10);
            }

            // Root gravity (pull toward root note)
            if (note.midi === this.rootNote.midi) {
                weight *= (1 + this.rootGravity * 5);
            }

            // Stepwise motion (prefer small intervals)
            const interval = Math.abs(note.midi - this.currentNote.midi);

            if (interval === 0) {
                // Same note - already handled above
            } else if (interval <= 2) {
                // Minor 2nd or major 2nd (stepwise)
                weight *= (1 + this.stepwise * 8);
            } else if (interval <= 4) {
                // Minor 3rd or major 3rd
                weight *= (1 + this.stepwise * 4);
            } else if (interval <= 7) {
                // 4th, 5th (consonant)
                weight *= (1 + this.stepwise * 2);
            } else {
                // Large jumps (rare in dub, but add interest)
                weight *= (1 + this.jumpiness * 3);
                weight *= 0.3; // Generally reduce large jumps
            }

            // Penalize notes too far from root octave
            const octaveDiff = Math.abs(note.midi - this.rootNote.midi);
            if (octaveDiff > 12) {
                weight *= 0.5;
            }

            // Avoid same note as previous (adds variety)
            if (this.previousNote && note.midi === this.previousNote.midi) {
                weight *= 0.3;
            }

            options.push({ value: note, weight: Math.max(weight, 0.01) });
        }

        // Select note based on weights
        const selectedNote = weightedRandom(options);

        // Update state
        this.previousNote = this.currentNote;
        this.currentNote = selectedNote;

        // Add to motif bank (limited size)
        if (this.motif.length < 8) {
            this.motif.push(selectedNote);
        }

        return selectedNote;
    }

    /**
     * Generate a complete melody sequence
     * @param {number} length - Number of steps to generate
     * @param {Object} options - Additional options
     * @returns {Array} - Array of note objects (or null for rests)
     */
    generateMelody(length, options = {}) {
        // Override parameters if provided
        if (options.density !== undefined) this.density = options.density;
        if (options.repetition !== undefined) this.repetition = options.repetition;
        if (options.stepwise !== undefined) this.stepwise = options.stepwise;
        if (options.rootGravity !== undefined) this.rootGravity = options.rootGravity;

        // Reset state
        if (options.resetState !== false) {
            this.currentNote = this.rootNote;
            this.previousNote = null;
            this.motif = [];
        }

        const melody = [];

        for (let i = 0; i < length; i++) {
            melody.push(this.generateNextNote());
        }

        // Ensure melody ends on root or consonant interval (optional)
        if (options.endOnRoot && melody.length > 0) {
            const lastNoteIndex = melody.length - 1;
            melody[lastNoteIndex] = this.rootNote;
        }

        return melody;
    }

    /**
     * Generate melody with rhythm pattern
     * @param {boolean[]} rhythmPattern - Euclidean rhythm pattern
     * @returns {Array} - Melody notes aligned with rhythm
     */
    generateMelodyWithRhythm(rhythmPattern) {
        const melody = [];

        for (let hit of rhythmPattern) {
            if (hit) {
                melody.push(this.generateNextNote());
            } else {
                melody.push(null); // Rest
            }
        }

        return melody;
    }

    /**
     * Mutate an existing melody slightly
     * @param {Array} melody - Original melody
     * @param {number} mutationRate - 0-1: How many notes to change
     * @returns {Array} - Mutated melody
     */
    mutateMelody(melody, mutationRate = 0.2) {
        return melody.map((note, i) => {
            if (Math.random() < mutationRate) {
                // Mutate this note
                if (note === null) {
                    // Rest -> maybe add a note
                    return Math.random() < 0.5 ? this.generateNextNote() : null;
                } else {
                    // Note -> maybe rest or different note
                    const action = Math.random();
                    if (action < 0.2) {
                        return null; // Turn into rest
                    } else {
                        // Change to nearby note
                        this.currentNote = note;
                        return this.generateNextNote();
                    }
                }
            }
            return note;
        });
    }

    /**
     * Create a call-and-response pattern
     * @param {number} length - Total length
     * @returns {Object} - {call: Array, response: Array}
     */
    generateCallAndResponse(length) {
        const callLength = Math.floor(length / 2);
        const responseLength = length - callLength;

        // Generate call (more active)
        const originalDensity = this.density;
        this.density = Math.min(0.8, this.density * 1.3);
        const call = this.generateMelody(callLength);

        // Generate response (can be rest or variation)
        this.density = originalDensity * 0.5; // Sparser
        const response = this.generateMelody(responseLength);

        this.density = originalDensity; // Restore

        return { call, response };
    }

    /**
     * Get preset configurations for different dub styles
     * @param {string} style - 'minimal', 'standard', 'busy', 'ambient'
     * @returns {Object} - Configuration object
     */
    static getPreset(style) {
        const presets = {
            minimal: {
                density: 0.3,
                repetition: 0.8,
                stepwise: 0.9,
                rootGravity: 0.6,
                jumpiness: 0.05
            },
            standard: {
                density: 0.6,
                repetition: 0.5,
                stepwise: 0.7,
                rootGravity: 0.3,
                jumpiness: 0.1
            },
            busy: {
                density: 0.8,
                repetition: 0.3,
                stepwise: 0.5,
                rootGravity: 0.2,
                jumpiness: 0.2
            },
            ambient: {
                density: 0.4,
                repetition: 0.7,
                stepwise: 0.8,
                rootGravity: 0.5,
                jumpiness: 0.15
            }
        };

        return presets[style] || presets.standard;
    }

    /**
     * Update generator parameters
     * @param {Object} params - New parameters
     */
    updateParameters(params) {
        if (params.density !== undefined) this.density = params.density;
        if (params.repetition !== undefined) this.repetition = params.repetition;
        if (params.stepwise !== undefined) this.stepwise = params.stepwise;
        if (params.rootGravity !== undefined) this.rootGravity = params.rootGravity;
        if (params.jumpiness !== undefined) this.jumpiness = params.jumpiness;

        // Update scale if root or scale changed
        if (params.root || params.scale) {
            this.root = params.root || this.root;
            this.scale = params.scale || this.scale;
            this.scaleNotes = getScaleNotes(this.root, this.scale, this.octave, this.octaves);
            this.rootNote = this.scaleNotes[0];
            this.currentNote = this.rootNote;
        }
    }

    /**
     * Analyze a melody for statistics
     * @param {Array} melody - Melody to analyze
     * @returns {Object} - Statistics
     */
    analyzeMelody(melody) {
        const notes = melody.filter(n => n !== null);
        const rests = melody.filter(n => n === null);

        let intervals = [];
        for (let i = 1; i < melody.length; i++) {
            if (melody[i] && melody[i - 1]) {
                intervals.push(Math.abs(melody[i].midi - melody[i - 1].midi));
            }
        }

        const avgInterval = intervals.length > 0
            ? intervals.reduce((a, b) => a + b, 0) / intervals.length
            : 0;

        return {
            totalSteps: melody.length,
            noteCount: notes.length,
            restCount: rests.length,
            density: notes.length / melody.length,
            averageInterval: avgInterval,
            maxInterval: intervals.length > 0 ? Math.max(...intervals) : 0,
            uniqueNotes: new Set(notes.map(n => n.midi)).size
        };
    }
}

/**
 * Simple helper function to generate a melody (non-class usage)
 * @param {number} length - Melody length
 * @param {string} scale - Scale name
 * @param {string} rootNote - Root note
 * @param {Object} options - Generation options
 * @returns {Array} - Generated melody
 */
export function generateMelody(length, scale, rootNote, options = {}) {
    const generator = new MarkovMelodyGenerator({
        root: rootNote,
        scale: scale,
        ...options
    });

    return generator.generateMelody(length, options);
}

// For browser usage without modules
if (typeof window !== 'undefined') {
    window.MarkovMelody = {
        MarkovMelodyGenerator,
        generateMelody
    };
}
