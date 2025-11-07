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
     * Source -> Distortion -> Filter -> Delay -> Reverb -> Compressor -> Destination
     */
    init() {
        try {
            // Warm Distortion - adds harmonic saturation
            this.effects.distortion = new Tone.Distortion({
                distortion: 0.2,
                wet: 0
            }).toDestination();

            // Auto Filter - sweeping low-pass filter for movement
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
            }).connect(this.effects.distortion);

            // Ping Pong Delay - classic dub techno tape echo simulation
            this.effects.delay = new Tone.PingPongDelay({
                delayTime: "8n",
                feedback: 0.65,
                wet: 0
            }).connect(this.effects.filter);

            // Reverb - deep spacious atmosphere
            this.effects.reverb = new Tone.Reverb({
                decay: 6,
                preDelay: 0.01,
                wet: 0
            }).connect(this.effects.delay);

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

            console.log('✓ Effects chain initialized');
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
            this.effects.delay.feedback.value = value;
        }
    }

    setDelayWet(value) {
        if (this.effects.delay && document.getElementById('delayToggle').checked) {
            this.effects.delay.wet.value = value;
        }
    }

    /**
     * Toggle reverb effect
     */
    setReverbEnabled(enabled) {
        if (this.effects.reverb) {
            this.effects.reverb.wet.value = enabled ? parseFloat(document.getElementById('reverbWet').value) : 0;
        }
    }

    /**
     * Update reverb parameters
     */
    setReverbDecay(value) {
        if (this.effects.reverb) {
            this.effects.reverb.decay = value;
        }
    }

    setReverbWet(value) {
        if (this.effects.reverb && document.getElementById('reverbToggle').checked) {
            this.effects.reverb.wet.value = value;
        }
    }

    /**
     * Toggle auto filter effect
     */
    setFilterEnabled(enabled) {
        if (this.effects.filter) {
            this.effects.filter.wet.value = enabled ? parseFloat(document.getElementById('filterWet').value) : 0;

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
            this.effects.filter.frequency.value = value;
        }
    }

    setFilterDepth(value) {
        if (this.effects.filter) {
            this.effects.filter.depth.value = value;
        }
    }

    setFilterWet(value) {
        if (this.effects.filter && document.getElementById('filterToggle').checked) {
            this.effects.filter.wet.value = value;
        }
    }

    /**
     * Toggle distortion effect
     */
    setDistortionEnabled(enabled) {
        if (this.effects.distortion) {
            this.effects.distortion.wet.value = enabled ? parseFloat(document.getElementById('distortionWet').value) : 0;
        }
    }

    /**
     * Update distortion parameters
     */
    setDistortionAmount(value) {
        if (this.effects.distortion) {
            this.effects.distortion.distortion = value;
        }
    }

    setDistortionWet(value) {
        if (this.effects.distortion && document.getElementById('distortionToggle').checked) {
            this.effects.distortion.wet.value = value;
        }
    }

    /**
     * Set master volume
     */
    setMasterVolume(db) {
        if (this.masterVolume) {
            this.masterVolume.volume.value = db;
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
