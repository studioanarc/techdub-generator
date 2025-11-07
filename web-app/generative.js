/**
 * Dub Techno Generative Music System
 * Master controller integrating Euclidean rhythms, Markov melodies, and Tone.js
 *
 * Features:
 * - Real-time pattern generation and evolution
 * - Euclidean rhythms for drums
 * - Markov chain melodies
 * - Bass patterns with occasional movement
 * - Gradual evolution and mutation
 * - Swing and groove
 */

import { generateEuclideanRhythm, getDubRhythmPreset, variatePattern } from './euclidean.js';
import { MarkovMelodyGenerator } from './markov.js';
import {
    getScaleNotes,
    getDubBassMovements,
    getChordNotes,
    createSwingFunction
} from './scales.js';

/**
 * Main Dub Techno Generator Class
 */
export class DubGenerator {
    constructor(options = {}) {
        // Musical parameters
        this.tempo = options.tempo || 122;
        this.rootNote = options.rootNote || 'C';
        this.scale = options.scale || 'minor';
        this.octave = options.octave || 2;

        // Generation parameters
        this.density = options.density || 0.6;           // Overall note density
        this.chaos = options.chaos || 0.2;               // Evolution intensity
        this.evolutionBars = options.evolutionBars || 8; // Bars between evolution
        this.swingAmount = options.swingAmount || 0.3;   // Groove swing

        // Pattern lengths (in 16th notes)
        this.patternLength = options.patternLength || 16;

        // State
        this.isPlaying = false;
        this.currentBar = 0;
        this.patterns = {};
        this.sequences = {};
        this.synths = null; // Will be set externally

        // Initialize generators
        this.melodyGenerator = new MarkovMelodyGenerator({
            root: this.rootNote,
            scale: this.scale,
            octave: this.octave + 1, // Melody octave higher than bass
            octaves: 2,
            density: this.density,
            repetition: 0.5,
            stepwise: 0.7,
            rootGravity: 0.3
        });

        // Generate initial patterns
        this.regenerateAllPatterns();

        // Create swing function
        this.swingFunction = createSwingFunction(this.swingAmount);
    }

    /**
     * Generate all musical patterns
     */
    regenerateAllPatterns() {
        this.patterns = {
            kick: this.generateKickPattern(),
            snare: this.generateSnarePattern(),
            hihat: this.generateHihatPattern(),
            perc: this.generatePercPattern(),
            bass: this.generateBassPattern(),
            melody: this.generateMelodyPattern(),
            chords: this.generateChordPattern()
        };

        console.log('🎵 Generated new patterns:', {
            kick: this.patterns.kick.rhythm.filter(Boolean).length + ' hits',
            snare: this.patterns.snare.rhythm.filter(Boolean).length + ' hits',
            hihat: this.patterns.hihat.rhythm.filter(Boolean).length + ' hits',
            bassNotes: this.patterns.bass.notes.filter(Boolean).length,
            melodyNotes: this.patterns.melody.notes.filter(Boolean).length
        });
    }

    /**
     * Generate kick drum pattern
     */
    generateKickPattern() {
        const feel = this.chaos < 0.3 ? 'minimal' : 'standard';
        const preset = getDubRhythmPreset('kick', feel);
        const rhythm = generateEuclideanRhythm(preset.hits, this.patternLength, preset.rotation);

        return {
            rhythm,
            velocity: rhythm.map(hit => hit ? 0.9 + Math.random() * 0.1 : 0),
            type: 'kick'
        };
    }

    /**
     * Generate snare/clap pattern
     */
    generateSnarePattern() {
        const feel = this.chaos < 0.3 ? 'minimal' : 'standard';
        const preset = getDubRhythmPreset('snare', feel);
        const rhythm = generateEuclideanRhythm(preset.hits, this.patternLength, preset.rotation);

        return {
            rhythm,
            velocity: rhythm.map(hit => hit ? 0.6 + Math.random() * 0.2 : 0),
            type: 'snare'
        };
    }

    /**
     * Generate hi-hat pattern
     */
    generateHihatPattern() {
        const feel = this.density < 0.4 ? 'minimal' : this.density > 0.7 ? 'busy' : 'standard';
        const preset = getDubRhythmPreset('hihat', feel);
        let rhythm = generateEuclideanRhythm(preset.hits, this.patternLength, preset.rotation);

        // Add probability-based triggering for variation
        rhythm = rhythm.map(hit => hit && Math.random() < 0.85);

        return {
            rhythm,
            velocity: rhythm.map(hit => hit ? 0.3 + Math.random() * 0.3 : 0),
            type: 'hihat'
        };
    }

    /**
     * Generate percussion pattern
     */
    generatePercPattern() {
        const preset = getDubRhythmPreset('perc', 'standard');
        const rhythm = generateEuclideanRhythm(preset.hits, preset.steps || this.patternLength, preset.rotation);

        // Extend or trim to pattern length
        const normalized = new Array(this.patternLength).fill(false);
        for (let i = 0; i < this.patternLength; i++) {
            normalized[i] = rhythm[i % rhythm.length];
        }

        return {
            rhythm: normalized,
            velocity: normalized.map(hit => hit ? 0.4 + Math.random() * 0.3 : 0),
            type: 'perc'
        };
    }

    /**
     * Generate bass pattern
     * Mostly stays on root, occasionally moves to 4th or 5th
     */
    generateBassPattern() {
        const scaleNotes = getScaleNotes(this.rootNote, this.scale, this.octave, 1);
        const rootNote = scaleNotes[0];
        const bassMovements = getDubBassMovements(rootNote.midi, this.scale);

        const notes = [];
        const rhythm = generateEuclideanRhythm(4, this.patternLength); // Simple bass rhythm

        for (let i = 0; i < this.patternLength; i++) {
            if (rhythm[i]) {
                // Decide: root, 4th, or 5th?
                const roll = Math.random();

                if (roll < 0.7) {
                    // Stay on root (most common in dub)
                    notes.push(scaleNotes.find(n => n.midi === bassMovements.root));
                } else if (roll < 0.85) {
                    // Move to 4th
                    notes.push(scaleNotes.find(n => n.midi === bassMovements.fourth) || rootNote);
                } else {
                    // Move to 5th
                    notes.push(scaleNotes.find(n => n.midi === bassMovements.fifth) || rootNote);
                }
            } else {
                notes.push(null); // Rest
            }
        }

        return {
            notes,
            rhythm,
            velocity: rhythm.map(hit => hit ? 0.7 + Math.random() * 0.2 : 0),
            type: 'bass'
        };
    }

    /**
     * Generate melody pattern using Markov chain
     */
    generateMelodyPattern() {
        // Create sparse rhythm for melody
        const melodyHits = Math.floor(this.patternLength * this.density * 0.5); // Melody sparser than density
        const rhythm = generateEuclideanRhythm(melodyHits, this.patternLength);

        // Generate melody notes aligned with rhythm
        const notes = this.melodyGenerator.generateMelodyWithRhythm(rhythm);

        return {
            notes,
            rhythm,
            velocity: notes.map(note => note ? 0.5 + Math.random() * 0.3 : 0),
            type: 'melody'
        };
    }

    /**
     * Generate chord pattern (for pad/chord layer)
     */
    generateChordPattern() {
        const chordTypes = ['sus2', 'sus4', 'minor7'];
        const chordType = chordTypes[Math.floor(Math.random() * chordTypes.length)];

        const scaleNotes = getScaleNotes(this.rootNote, this.scale, this.octave + 1, 1);
        const rootNote = scaleNotes[0];

        // Chords change slowly in dub
        const chordRhythm = generateEuclideanRhythm(2, this.patternLength); // Very sparse

        const chords = [];
        for (let i = 0; i < this.patternLength; i++) {
            if (chordRhythm[i]) {
                const chordNotes = getChordNotes(rootNote.name, chordType, this.scale);
                chords.push(chordNotes);
            } else {
                chords.push(null);
            }
        }

        return {
            chords,
            rhythm: chordRhythm,
            velocity: chordRhythm.map(hit => hit ? 0.4 : 0),
            type: 'chord'
        };
    }

    /**
     * Evolve patterns gradually (called every N bars)
     */
    evolve() {
        console.log(`🧬 Evolving patterns (chaos: ${this.chaos})`);

        const mutationRate = this.chaos;

        // Evolve drums: shift, add/remove hits
        if (Math.random() < mutationRate) {
            // Mutate kick
            this.patterns.kick.rhythm = this.mutateRhythm(this.patterns.kick.rhythm, mutationRate * 0.5);
        }

        if (Math.random() < mutationRate) {
            // Mutate snare
            this.patterns.snare.rhythm = this.mutateRhythm(this.patterns.snare.rhythm, mutationRate * 0.5);
        }

        if (Math.random() < mutationRate * 1.5) {
            // Mutate hihat (more frequent)
            this.patterns.hihat.rhythm = this.mutateRhythm(this.patterns.hihat.rhythm, mutationRate);
        }

        // Evolve melody
        if (Math.random() < mutationRate) {
            this.patterns.melody.notes = this.melodyGenerator.mutateMelody(
                this.patterns.melody.notes,
                mutationRate * 0.3
            );
        }

        // Evolve bass (rarely)
        if (Math.random() < mutationRate * 0.5) {
            this.patterns.bass = this.generateBassPattern();
        }

        console.log('✨ Evolution complete');
    }

    /**
     * Mutate a rhythm pattern
     */
    mutateRhythm(rhythm, rate) {
        return rhythm.map(hit => {
            if (Math.random() < rate) {
                return !hit; // Flip hit/rest
            }
            return hit;
        });
    }

    /**
     * Completely randomize all patterns
     */
    randomize() {
        console.log('🎲 Randomizing all patterns');
        this.regenerateAllPatterns();
    }

    /**
     * Update musical parameters
     */
    updateParameters(params) {
        if (params.tempo !== undefined) {
            this.tempo = params.tempo;
            if (typeof Tone !== 'undefined' && Tone.Transport) {
                Tone.Transport.bpm.value = this.tempo;
            }
        }

        if (params.density !== undefined) {
            this.density = params.density;
            this.melodyGenerator.updateParameters({ density: this.density });
        }

        if (params.chaos !== undefined) {
            this.chaos = params.chaos;
        }

        if (params.swingAmount !== undefined) {
            this.swingAmount = params.swingAmount;
            this.swingFunction = createSwingFunction(this.swingAmount);
        }

        if (params.rootNote || params.scale) {
            this.rootNote = params.rootNote || this.rootNote;
            this.scale = params.scale || this.scale;
            this.melodyGenerator.updateParameters({
                root: this.rootNote,
                scale: this.scale
            });
            this.regenerateAllPatterns();
        }
    }

    /**
     * Start playback with Tone.js Transport
     */
    async startTransport(synths) {
        if (typeof Tone === 'undefined') {
            console.error('Tone.js not loaded!');
            return;
        }

        // Ensure audio context is started
        await Tone.start();
        console.log('🔊 Audio context started');

        this.synths = synths;
        this.isPlaying = true;

        // Set tempo
        Tone.Transport.bpm.value = this.tempo;

        // Schedule patterns
        this.schedulePatterns();

        // Schedule evolution
        this.scheduleEvolution();

        // Start transport
        Tone.Transport.start();
        console.log('▶️ Transport started');
    }

    /**
     * Schedule all patterns with Tone.Transport
     */
    schedulePatterns() {
        if (!this.synths) {
            console.error('No synths provided!');
            return;
        }

        // Clear existing sequences
        this.clearSequences();

        const sixteenthNote = '16n';

        // Schedule kick
        this.sequences.kick = new Tone.Sequence((time, step) => {
            if (this.patterns.kick.rhythm[step]) {
                const velocity = this.patterns.kick.velocity[step];
                const swingOffset = this.swingFunction(step);

                if (this.synths.kick) {
                    this.synths.kick.triggerAttackRelease('C1', '8n', time + swingOffset, velocity);
                }
            }
        }, [...Array(this.patternLength).keys()], sixteenthNote).start(0);

        // Schedule snare
        this.sequences.snare = new Tone.Sequence((time, step) => {
            if (this.patterns.snare.rhythm[step]) {
                const velocity = this.patterns.snare.velocity[step];
                const swingOffset = this.swingFunction(step);

                if (this.synths.snare) {
                    this.synths.snare.triggerAttackRelease('C3', '8n', time + swingOffset, velocity);
                }
            }
        }, [...Array(this.patternLength).keys()], sixteenthNote).start(0);

        // Schedule hihat
        this.sequences.hihat = new Tone.Sequence((time, step) => {
            if (this.patterns.hihat.rhythm[step]) {
                const velocity = this.patterns.hihat.velocity[step];
                const swingOffset = this.swingFunction(step);

                if (this.synths.hihat) {
                    this.synths.hihat.triggerAttackRelease('C5', '32n', time + swingOffset, velocity);
                }
            }
        }, [...Array(this.patternLength).keys()], sixteenthNote).start(0);

        // Schedule bass
        this.sequences.bass = new Tone.Sequence((time, step) => {
            const note = this.patterns.bass.notes[step];
            if (note) {
                const velocity = this.patterns.bass.velocity[step];
                const swingOffset = this.swingFunction(step);

                if (this.synths.bass) {
                    this.synths.bass.triggerAttackRelease(note.freq, '8n', time + swingOffset, velocity);
                }
            }
        }, [...Array(this.patternLength).keys()], sixteenthNote).start(0);

        // Schedule melody
        this.sequences.melody = new Tone.Sequence((time, step) => {
            const note = this.patterns.melody.notes[step];
            if (note) {
                const velocity = this.patterns.melody.velocity[step];
                const swingOffset = this.swingFunction(step);

                if (this.synths.melody) {
                    this.synths.melody.triggerAttackRelease(note.freq, '8n', time + swingOffset, velocity);
                }
            }
        }, [...Array(this.patternLength).keys()], sixteenthNote).start(0);

        console.log('📅 Patterns scheduled');
    }

    /**
     * Schedule evolution events
     */
    scheduleEvolution() {
        const barsInSeconds = (60 / this.tempo) * 4; // 4 beats per bar
        const evolutionInterval = barsInSeconds * this.evolutionBars;

        // Use Tone.Transport.scheduleRepeat for evolution
        if (this.evolutionEvent) {
            Tone.Transport.clear(this.evolutionEvent);
        }

        this.evolutionEvent = Tone.Transport.scheduleRepeat((time) => {
            this.currentBar += this.evolutionBars;
            this.evolve();

            // Reschedule patterns with new evolution
            // We need to do this on the next frame to avoid timing issues
            setTimeout(() => {
                this.schedulePatterns();
            }, 10);
        }, `${this.evolutionBars}m`);

        console.log(`⏰ Evolution scheduled every ${this.evolutionBars} bars`);
    }

    /**
     * Stop playback
     */
    stop() {
        if (typeof Tone !== 'undefined') {
            Tone.Transport.stop();
            this.clearSequences();
            console.log('⏹️ Transport stopped');
        }
        this.isPlaying = false;
    }

    /**
     * Clear all sequences
     */
    clearSequences() {
        Object.values(this.sequences).forEach(seq => {
            if (seq && seq.dispose) {
                seq.dispose();
            }
        });
        this.sequences = {};

        if (this.evolutionEvent && typeof Tone !== 'undefined') {
            Tone.Transport.clear(this.evolutionEvent);
            this.evolutionEvent = null;
        }
    }

    /**
     * Get current state for debugging
     */
    getState() {
        return {
            isPlaying: this.isPlaying,
            tempo: this.tempo,
            rootNote: this.rootNote,
            scale: this.scale,
            density: this.density,
            chaos: this.chaos,
            currentBar: this.currentBar,
            patterns: Object.keys(this.patterns)
        };
    }
}

// For browser usage without modules
if (typeof window !== 'undefined') {
    window.DubGenerator = DubGenerator;
}
