/**
 * Dub Techno Generator - Effects Chain
 * Creates and manages the audio effects chain using Tone.js
 */

class EffectsChain {
    constructor() {
        this.initialized = false;
        this.effects = {};
    }

    /**
     * Initialize all effects
     * Effects are chained in the following order:
     * Source -> BitCrusher -> Distortion -> Chorus -> Phaser -> Filter -> TapeEcho -> Delay ->
     * ConvolutionReverb -> Reverb -> Compressor -> Destination
     */
    init() {
        try {
            // === PRE-EFFECTS (Distortion/Modulation) ===

            // BitCrusher - lo-fi digital degradation
            this.effects.bitCrusher = new Tone.BitCrusher({
                bits: 8,
                wet: 0
            }).toDestination();

            // Warm Distortion - adds harmonic saturation
            this.effects.distortion = new Tone.Distortion({
                distortion: 0.2,
                wet: 0
            }).connect(this.effects.bitCrusher);

            // Chorus - stereo width and movement
            this.effects.chorus = new Tone.Chorus({
                frequency: 1.5,
                delayTime: 3.5,
                depth: 0.7,
                type: "sine",
                spread: 180,
                wet: 0
            }).connect(this.effects.distortion);

            // Phaser - sweeping notches for movement
            this.effects.phaser = new Tone.Phaser({
                frequency: 0.5,
                octaves: 3,
                stages: 10,
                Q: 10,
                baseFrequency: 350,
                wet: 0
            }).connect(this.effects.chorus);

            // === FILTERS ===

            // Auto Filter - sweeping filter for movement
            this.effects.filter = new Tone.AutoFilter({
                frequency: "0.5hz",
                type: "sine",
                depth: 0.6,
                baseFrequency: 200,
                octaves: 2.5,
                filter: {
                    type: "lowpass",
                    rolloff: -24,
                    Q: 2
                },
                wet: 0
            }).connect(this.effects.phaser);

            // === DELAYS ===

            // Tape Echo - vintage tape delay with wow & flutter
            // (Using FeedbackDelay with filtering to simulate tape)
            this.effects.tapeEcho = new Tone.FeedbackDelay({
                delayTime: "8n",
                feedback: 0.6,
                wet: 0
            }).connect(this.effects.filter);

            // Add subtle filtering to tape echo for warmth
            const tapeFilter = new Tone.Filter({
                frequency: 3000,
                type: "lowpass",
                rolloff: -12
            });
            this.effects.tapeEcho.connect(tapeFilter);
            tapeFilter.connect(this.effects.filter);

            // Ping Pong Delay - stereo delay
            this.effects.delay = new Tone.PingPongDelay({
                delayTime: "8n",
                feedback: 0.65,
                wet: 0
            }).connect(this.effects.tapeEcho);

            // === REVERBS ===

            // Convolution Reverb - for realistic spaces
            this.effects.convolutionReverb = new Tone.Reverb({
                decay: 8,
                preDelay: 0.01,
                wet: 0
            }).connect(this.effects.delay);

            // Algorithmic Reverb - deep spacious atmosphere
            this.effects.reverb = new Tone.Reverb({
                decay: 6,
                preDelay: 0.01,
                wet: 0
            }).connect(this.effects.convolutionReverb);

            // === MASTER EFFECTS ===

            // Master Compressor - glues everything together
            this.effects.compressor = new Tone.Compressor({
                threshold: -24,
                ratio: 4,
                attack: 0.003,
                release: 0.25,
                knee: 10
            }).connect(this.effects.reverb);

            // Master Volume
            this.masterVolume = new Tone.Volume(-10).connect(this.effects.compressor);

            console.log('✓ Effects chain initialized with 10 effects');
            this.initialized = true;

            // Return the master volume as the input point for the chain
            return this.masterVolume;

        } catch (error) {
            console.error('Failed to initialize effects:', error);
            throw error;
        }
    }

    /**
     * Get the input node for the effects chain
     */
    getInput() {
        if (!this.initialized) {
            throw new Error('Effects chain not initialized. Call init() first.');
        }
        return this.masterVolume;
    }

    /**
     * Toggle delay effect
     */
    setDelayEnabled(enabled) {
        if (this.effects.delay) {
            this.effects.delay.wet.value = enabled ? parseFloat(document.getElementById('delayWet').value) : 0;
        }
    }

    /**
     * Update delay parameters
     */
    setDelayTime(value) {
        if (this.effects.delay) {
            // Convert slider value (0.1-1) to note values
            // Shorter values = faster delays
            const noteValues = {
                0.1: "16n",
                0.25: "8n",
                0.5: "4n",
                0.75: "8n.",
                1.0: "2n"
            };

            // Find closest note value
            let closestValue = "8n";
            let minDiff = Infinity;

            for (let key in noteValues) {
                const diff = Math.abs(parseFloat(key) - value);
                if (diff < minDiff) {
                    minDiff = diff;
                    closestValue = noteValues[key];
                }
            }

            this.effects.delay.delayTime.value = closestValue;
        }
    }

    setDelayFeedback(value) {
        if (this.effects.delay) {
            // Clamp to prevent floating point errors
            this.effects.delay.feedback.value = Math.max(0, Math.min(0.99, value));
        }
    }

    setDelayWet(value) {
        if (this.effects.delay) {
            // Clamp to prevent floating point errors
            this.effects.delay.wet.value = Math.max(0, Math.min(1, value));
        }
    }

    /**
     * Toggle reverb effect
     */
    setReverbEnabled(enabled) {
        if (this.effects.reverb) {
            const wetValue = enabled ? parseFloat(document.getElementById('reverbWet')?.value || 0.25) : 0;
            this.effects.reverb.wet.value = Math.max(0, Math.min(1, wetValue));
        }
    }

    /**
     * Update reverb parameters
     */
    setReverbDecay(value) {
        if (this.effects.reverb) {
            // Clamp decay to valid range
            this.effects.reverb.decay = Math.max(0.1, Math.min(10, value));
        }
    }

    setReverbWet(value) {
        if (this.effects.reverb) {
            // Clamp to prevent floating point errors
            this.effects.reverb.wet.value = Math.max(0, Math.min(1, value));
        }
    }

    /**
     * Toggle auto filter effect
     */
    setFilterEnabled(enabled) {
        if (this.effects.filter) {
            const wetValue = enabled ? parseFloat(document.getElementById('filterWet')?.value || 0.3) : 0;
            this.effects.filter.wet.value = Math.max(0, Math.min(1, wetValue));

            if (enabled) {
                this.effects.filter.start();
            } else {
                this.effects.filter.stop();
            }
        }
    }

    /**
     * Update filter parameters
     */
    setFilterFrequency(value) {
        if (this.effects.filter) {
            // Clamp frequency to valid range
            this.effects.filter.frequency.value = Math.max(0.01, Math.min(20, value));
        }
    }

    setFilterDepth(value) {
        if (this.effects.filter) {
            // Clamp depth to 0-1 range
            this.effects.filter.depth.value = Math.max(0, Math.min(1, value));
        }
    }

    setFilterWet(value) {
        if (this.effects.filter) {
            // Clamp to prevent floating point errors
            this.effects.filter.wet.value = Math.max(0, Math.min(1, value));
        }
    }

    /**
     * Toggle distortion effect
     */
    setDistortionEnabled(enabled) {
        if (this.effects.distortion) {
            const wetValue = enabled ? parseFloat(document.getElementById('distortionWet')?.value || 0.2) : 0;
            this.effects.distortion.wet.value = Math.max(0, Math.min(1, wetValue));
        }
    }

    /**
     * Update distortion parameters
     */
    setDistortionAmount(value) {
        if (this.effects.distortion) {
            // Clamp distortion amount to valid range
            this.effects.distortion.distortion = Math.max(0, Math.min(1, value));
        }
    }

    setDistortionWet(value) {
        if (this.effects.distortion) {
            // Clamp to prevent floating point errors
            this.effects.distortion.wet.value = Math.max(0, Math.min(1, value));
        }
    }

    /**
     * BitCrusher controls
     */
    setBitCrusherEnabled(enabled) {
        if (this.effects.bitCrusher) {
            this.effects.bitCrusher.wet.value = enabled ? 1 : 0;
        }
    }

    setBitCrusherBits(value) {
        if (this.effects.bitCrusher) {
            // Clamp bits to valid range (1-16)
            this.effects.bitCrusher.bits = Math.max(1, Math.min(16, Math.round(value)));
        }
    }

    /**
     * Chorus controls
     */
    setChorusEnabled(enabled) {
        if (this.effects.chorus) {
            this.effects.chorus.wet.value = enabled ? 0.5 : 0;
        }
    }

    setChorusRate(value) {
        if (this.effects.chorus) {
            this.effects.chorus.frequency.value = Math.max(0.1, Math.min(10, value));
        }
    }

    setChorusDepth(value) {
        if (this.effects.chorus) {
            this.effects.chorus.depth = Math.max(0, Math.min(1, value));
        }
    }

    /**
     * Phaser controls
     */
    setPhaserEnabled(enabled) {
        if (this.effects.phaser) {
            this.effects.phaser.wet.value = enabled ? 0.5 : 0;
        }
    }

    setPhaserRate(value) {
        if (this.effects.phaser) {
            this.effects.phaser.frequency.value = Math.max(0.1, Math.min(10, value));
        }
    }

    setPhaserDepth(value) {
        if (this.effects.phaser) {
            this.effects.phaser.octaves = Math.max(0, Math.min(8, value));
        }
    }

    /**
     * Tape Echo controls
     */
    setTapeEchoEnabled(enabled) {
        if (this.effects.tapeEcho) {
            this.effects.tapeEcho.wet.value = enabled ? 0.4 : 0;
        }
    }

    setTapeEchoTime(value) {
        if (this.effects.tapeEcho) {
            // Convert slider value (0.1-1) to note values
            const noteValues = {
                0.1: "16n",
                0.2: "8n.",
                0.3: "8n",
                0.4: "4n.",
                0.5: "4n",
                0.6: "2n.",
                0.7: "2n",
                0.8: "1n.",
                0.9: "1n",
                1.0: "1m"
            };

            // Find closest note value
            let closestValue = "8n";
            let minDiff = Infinity;

            for (let key in noteValues) {
                const diff = Math.abs(parseFloat(key) - value);
                if (diff < minDiff) {
                    minDiff = diff;
                    closestValue = noteValues[key];
                }
            }

            this.effects.tapeEcho.delayTime.value = closestValue;
        }
    }

    setTapeEchoFeedback(value) {
        if (this.effects.tapeEcho) {
            this.effects.tapeEcho.feedback.value = Math.max(0, Math.min(0.95, value));
        }
    }

    /**
     * Convolution Reverb controls
     */
    setConvolutionReverbEnabled(enabled) {
        if (this.effects.convolutionReverb) {
            this.effects.convolutionReverb.wet.value = enabled ? 0.3 : 0;
        }
    }

    setConvolutionReverbDecay(value) {
        if (this.effects.convolutionReverb) {
            this.effects.convolutionReverb.decay = Math.max(0.1, Math.min(20, value));
        }
    }

    /**
     * Set master volume
     */
    setMasterVolume(db) {
        if (this.masterVolume) {
            // Clamp dB to reasonable range (-60dB to +6dB)
            this.masterVolume.volume.value = Math.max(-60, Math.min(6, db));
        }
    }

    /**
     * Get current settings
     */
    getSettings() {
        return {
            delay: {
                enabled: document.getElementById('delayToggle')?.checked || false,
                time: this.effects.delay?.delayTime.value || "8n",
                feedback: this.effects.delay?.feedback.value || 0.65,
                wet: this.effects.delay?.wet.value || 0.3
            },
            reverb: {
                enabled: document.getElementById('reverbToggle')?.checked || false,
                decay: this.effects.reverb?.decay || 6,
                wet: this.effects.reverb?.wet.value || 0.25
            },
            filter: {
                enabled: document.getElementById('filterToggle')?.checked || false,
                frequency: this.effects.filter?.frequency.value || 0.5,
                depth: this.effects.filter?.depth.value || 0.6,
                wet: this.effects.filter?.wet.value || 0.3
            },
            distortion: {
                enabled: document.getElementById('distortionToggle')?.checked || false,
                amount: this.effects.distortion?.distortion || 0.2,
                wet: this.effects.distortion?.wet.value || 0.2
            },
            masterVolume: this.masterVolume?.volume.value || -6
        };
    }

    /**
     * Apply settings from preset
     */
    applySettings(settings) {
        if (!settings) return;

        // Apply delay
        if (settings.delay) {
            if (settings.delay.enabled !== undefined) this.setDelayEnabled(settings.delay.enabled);
            if (settings.delay.feedback !== undefined) this.setDelayFeedback(settings.delay.feedback);
            if (settings.delay.wet !== undefined) this.setDelayWet(settings.delay.wet);
        }

        // Apply reverb
        if (settings.reverb) {
            if (settings.reverb.enabled !== undefined) this.setReverbEnabled(settings.reverb.enabled);
            if (settings.reverb.decay !== undefined) this.setReverbDecay(settings.reverb.decay);
            if (settings.reverb.wet !== undefined) this.setReverbWet(settings.reverb.wet);
        }

        // Apply filter
        if (settings.filter) {
            if (settings.filter.enabled !== undefined) this.setFilterEnabled(settings.filter.enabled);
            if (settings.filter.frequency !== undefined) this.setFilterFrequency(settings.filter.frequency);
            if (settings.filter.depth !== undefined) this.setFilterDepth(settings.filter.depth);
            if (settings.filter.wet !== undefined) this.setFilterWet(settings.filter.wet);
        }

        // Apply distortion
        if (settings.distortion) {
            if (settings.distortion.enabled !== undefined) this.setDistortionEnabled(settings.distortion.enabled);
            if (settings.distortion.amount !== undefined) this.setDistortionAmount(settings.distortion.amount);
            if (settings.distortion.wet !== undefined) this.setDistortionWet(settings.distortion.wet);
        }

        // Apply master volume
        if (settings.masterVolume !== undefined) {
            this.setMasterVolume(settings.masterVolume);
        }
    }

    /**
     * Cleanup and dispose of all effects
     */
    dispose() {
        if (this.masterVolume) this.masterVolume.dispose();
        if (this.effects.compressor) this.effects.compressor.dispose();
        if (this.effects.reverb) this.effects.reverb.dispose();
        if (this.effects.delay) this.effects.delay.dispose();
        if (this.effects.filter) this.effects.filter.dispose();
        if (this.effects.distortion) this.effects.distortion.dispose();

        this.initialized = false;
        console.log('✓ Effects chain disposed');
    }
}

// Export the effects chain instance
const effectsChain = new EffectsChain();
