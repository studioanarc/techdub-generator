#pragma once

#include <atomic>
#include <cstring>
#include "../dsp/DSPUtils.h"

namespace dubtechno {

/**
 * Base class for all audio effects
 *
 * Provides common functionality:
 * - Bypass control
 * - Dry/wet mix control
 * - Thread-safe parameter updates
 * - Denormal prevention
 */
class Effect {
public:
    Effect() : bypassed(false), mix(1.0f), sampleRate(SAMPLE_RATE) {}

    virtual ~Effect() = default;

    /**
     * Process audio buffer
     * @param buffer Interleaved audio buffer [L, R, L, R, ...]
     * @param numFrames Number of frames (sample pairs for stereo)
     * @param channels Number of channels (1=mono, 2=stereo)
     */
    virtual void process(float* buffer, int numFrames, int channels) = 0;

    /**
     * Reset internal state (clear delays, filters, etc.)
     */
    virtual void reset() = 0;

    /**
     * Initialize with sample rate
     */
    virtual void init(float sampleRate) {
        this->sampleRate = sampleRate;
        reset();
    }

    /**
     * Set bypass state
     */
    void setBypassed(bool bypass) {
        bypassed.store(bypass, std::memory_order_relaxed);
    }

    bool isBypassed() const {
        return bypassed.load(std::memory_order_relaxed);
    }

    /**
     * Set dry/wet mix
     * @param mixAmount 0.0 = fully dry, 1.0 = fully wet
     */
    void setMix(float mixAmount) {
        mix.store(DSPUtils::clamp(mixAmount, 0.0f, 1.0f), std::memory_order_relaxed);
    }

    float getMix() const {
        return mix.load(std::memory_order_relaxed);
    }

protected:
    std::atomic<bool> bypassed;
    std::atomic<float> mix;
    float sampleRate;

    /**
     * Apply dry/wet mix to processed buffer
     * @param dry Original dry signal
     * @param wet Processed wet signal
     * @param numSamples Total number of samples (frames * channels)
     */
    void applyMix(const float* dry, float* wet, int numSamples) {
        float mixVal = mix.load(std::memory_order_relaxed);

        if (mixVal >= 0.999f) {
            // Fully wet, nothing to do
            return;
        }

        if (mixVal <= 0.001f) {
            // Fully dry, copy dry signal
            std::memcpy(wet, dry, numSamples * sizeof(float));
            return;
        }

        // Mix dry and wet
        float dryGain = 1.0f - mixVal;
        for (int i = 0; i < numSamples; ++i) {
            wet[i] = dry[i] * dryGain + wet[i] * mixVal;
        }
    }

    /**
     * Prevent denormal numbers (tiny values that cause CPU slowdown)
     */
    static inline float undenormalize(float value) {
        // Add tiny DC offset to prevent denormals
        constexpr float DC_OFFSET = 1.0e-25f;
        return value + DC_OFFSET;
    }

    /**
     * Smooth parameter changes to prevent zipper noise
     */
    class SmoothParameter {
    public:
        SmoothParameter(float initialValue = 0.0f, float smoothingTimeMs = 10.0f)
            : current(initialValue), target(initialValue) {
            setSmoothingTime(smoothingTimeMs);
        }

        void setSmoothingTime(float timeMs) {
            // Calculate one-pole coefficient for smoothing
            coefficient = 1.0f - std::exp(-1.0f / (timeMs * 0.001f * SAMPLE_RATE));
        }

        void setTarget(float newTarget) {
            target = newTarget;
        }

        float getNext() {
            current += coefficient * (target - current);
            return current;
        }

        float getCurrent() const {
            return current;
        }

        void reset(float value) {
            current = target = value;
        }

    private:
        float current;
        float target;
        float coefficient;
    };
};

} // namespace dubtechno
