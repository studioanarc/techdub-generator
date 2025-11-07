/**
 * Dub Techno Generator - Synthesizer Engine
 * Creates and manages the bass synth and pad synth
 */

class SynthEngine {
    constructor() {
        this.initialized = false;
        this.bassSynth = null;
        this.padSynth = null;
        this.currentScale = [];
        this.currentRoot = 'D';
        this.currentScaleType = 'minor';
        this.isPlaying = false;

        // Scale definitions (intervals from root)
        this.scales = {
            minor: [0, 2, 3, 5, 7, 8, 10],          // Natural minor
            dorian: [0, 2, 3, 5, 7, 9, 10],         // Dorian mode
            phrygian: [0, 1, 3, 5, 7, 8, 10],       // Phrygian mode
            minorPentatonic: [0, 3, 5, 7, 10]       // Minor pentatonic
        };
    }

    /**
     * Initialize synthesizers and connect to effects chain
     */
    init(effectsInput) {
        try {
            // Bass Synth - Deep sub-bass focused MonoSynth
            this.bassSynth = new Tone.MonoSynth({
                oscillator: {
                    type: "sine"  // Pure sine wave for deep sub bass
                },
                filter: {
                    Q: 2,
                    type: "lowpass",
                    rolloff: -24,
                    frequency: 200  // Low-pass for warmth
                },
                envelope: {
                    attack: 0.08,
                    decay: 0.3,
                    sustain: 0.6,
                    release: 1.2
                },
                filterEnvelope: {
                    attack: 0.06,
                    decay: 0.4,
                    sustain: 0.3,
                    release: 1.5,
                    baseFrequency: 60,
                    octaves: 2.5,
                    exponent: 2
                },
                volume: -8
            }).connect(effectsInput);

            // Pad Synth - Atmospheric PolySynth for chords
            this.padSynth = new Tone.PolySynth(Tone.Synth, {
                oscillator: {
                    type: "triangle8"  // Rich harmonics
                },
                filter: {
                    Q: 1,
                    type: "lowpass",
                    rolloff: -12,
                    frequency: 800
                },
                envelope: {
                    attack: 1.5,
                    decay: 0.8,
                    sustain: 0.7,
                    release: 3.0
                },
                filterEnvelope: {
                    attack: 1.0,
                    decay: 1.2,
                    sustain: 0.5,
                    release: 2.5,
                    baseFrequency: 400,
                    octaves: 2,
                    exponent: 2
                },
                volume: -16
            }).connect(effectsInput);

            this.initialized = true;
            console.log(' Synth engine initialized');

            // Set initial scale
            this.updateScale(this.currentRoot, this.currentScaleType);

        } catch (error) {
            console.error('Failed to initialize synth engine:', error);
            throw error;
        }
    }

    /**
     * Update the musical scale based on root note and scale type
     */
    updateScale(root, scaleType) {
        this.currentRoot = root;
        this.currentScaleType = scaleType;

        const rootMidi = Tone.Frequency(root + "1").toMidi();
        const intervals = this.scales[scaleType];

        // Generate scale notes across 2 octaves
        this.currentScale = [];
        for (let octave = 0; octave < 2; octave++) {
            intervals.forEach(interval => {
                const midiNote = rootMidi + interval + (octave * 12);
                const noteName = Tone.Frequency(midiNote, "midi").toNote();
                this.currentScale.push(noteName);
            });
        }

        console.log(`Scale updated: ${root} ${scaleType}`, this.currentScale);
    }

    /**
     * Get a random note from the current scale
     */
    getRandomNote() {
        if (this.currentScale.length === 0) return 'C2';
        const index = Math.floor(Math.random() * this.currentScale.length);
        return this.currentScale[index];
    }

    /**
     * Get a low bass note (first few notes of the scale)
     */
    getBassNote() {
        if (this.currentScale.length === 0) return 'C1';
        // Use only the lowest 3 notes for bass
        const index = Math.floor(Math.random() * Math.min(3, this.currentScale.length));
        const note = this.currentScale[index];
        // Transpose down an octave for deep bass
        const frequency = Tone.Frequency(note).transpose(-12);
        return frequency.toNote();
    }

    /**
     * Get a chord (3 notes from the scale)
     */
    getChord() {
        if (this.currentScale.length < 3) return [this.currentScale[0]];

        // Pick a random starting position
        const startIndex = Math.floor(Math.random() * (this.currentScale.length - 6));

        // Build triad: root, third, fifth (intervals 0, 2, 4 in scale)
        return [
            this.currentScale[startIndex],
            this.currentScale[startIndex + 2],
            this.currentScale[startIndex + 4]
        ];
    }

    /**
     * Start the generative sequence
     */
    start() {
        if (this.isPlaying) return;

        this.isPlaying = true;

        // Bass pattern - plays every 2 bars
        this.bassLoop = new Tone.Loop((time) => {
            const note = this.getBassNote();
            const duration = "1n";  // Whole note - sustained bass
            this.bassSynth.triggerAttackRelease(note, duration, time);
        }, "2m").start(0);

        // Pad pattern - plays chords every 4 bars
        this.padLoop = new Tone.Loop((time) => {
            const chord = this.getChord();
            const duration = "2m";  // 2 measures - long atmospheric chords
            this.padSynth.triggerAttackRelease(chord, duration, time);
        }, "4m").start(0);

        // Additional bass hits for movement (optional)
        this.bassAccentLoop = new Tone.Loop((time) => {
            // Randomly trigger extra bass notes (60% chance)
            if (Math.random() > 0.4) {
                const note = this.getBassNote();
                const duration = "8n";
                this.bassSynth.triggerAttackRelease(note, duration, time, 0.7);
            }
        }, "1m").start(0);

        console.log(' Synth sequences started');
    }

    /**
     * Stop the generative sequence
     */
    stop() {
        if (!this.isPlaying) return;

        this.isPlaying = false;

        if (this.bassLoop) this.bassLoop.stop();
        if (this.padLoop) this.padLoop.stop();
        if (this.bassAccentLoop) this.bassAccentLoop.stop();

        // Release any held notes
        this.bassSynth.triggerRelease();
        this.padSynth.releaseAll();

        console.log(' Synth sequences stopped');
    }

    /**
     * Trigger a single bass note (for testing)
     */
    triggerBassNote() {
        if (!this.initialized) {
            console.error('Synth engine not initialized');
            return;
        }

        const note = this.getBassNote();
        this.bassSynth.triggerAttackRelease(note, "2n");
        console.log(`Bass note triggered: ${note}`);
    }

    /**
     * Trigger a chord (for testing)
     */
    triggerChord() {
        if (!this.initialized) {
            console.error('Synth engine not initialized');
            return;
        }

        const chord = this.getChord();
        this.padSynth.triggerAttackRelease(chord, "2n");
        console.log(`Chord triggered:`, chord);
    }

    /**
     * Cleanup and dispose of synthesizers
     */
    dispose() {
        this.stop();

        if (this.bassSynth) this.bassSynth.dispose();
        if (this.padSynth) this.padSynth.dispose();

        this.initialized = false;
        console.log(' Synth engine disposed');
    }
}

// Export the synth engine instance
const synthEngine = new SynthEngine();
