#pragma once

#include "Effect.h"
#include <cmath>
#include <algorithm>

namespace dubtechno {

/**
 * Dynamics Compressor
 *
 * Essential for dub techno - provides the "glue" that holds everything together
 *
 * Features:
 * - RMS or peak detection
 * - Attack and release envelopes
 * - Threshold, ratio, knee
 * - Makeup gain
 * - Gain reduction metering
 * - Optional sidechain input
 */
class Compressor : public Effect {
public:
    enum DetectionMode {
        PEAK,
        RMS
    };

    Compressor() : envelope(0.0f), gainReduction(0.0f) {
        // Initialize parameters
        threshold.reset(-20.0f);     // -20 dB
        ratio.reset(4.0f);           // 4:1
        attack.reset(10.0f);         // 10ms
        release.reset(100.0f);       // 100ms
        knee.reset(3.0f);            // 3 dB soft knee
        makeupGain.reset(0.0f);      // 0 dB
        detectionMode = RMS;
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);
        envelope = 0.0f;
        gainReduction = 0.0f;
        updateCoefficients();
    }

    void reset() override {
        envelope = 0.0f;
        gainReduction = 0.0f;
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed()) return;

        // Get smooth parameters
        float thresholdDb = threshold.getNext();
        float ratioVal = ratio.getNext();
        float kneeDb = knee.getNext();
        float makeupDb = makeupGain.getNext();

        updateCoefficients();

        float makeupLinear = DSPUtils::dbToLinear(makeupDb);

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;
            float inputL = buffer[idx];
            float inputR = channels > 1 ? buffer[idx + 1] : inputL;

            // Detect input level
            float inputLevel = detectLevel(inputL, inputR);

            // Convert to dB
            float inputDb = DSPUtils::linearToDb(inputLevel);

            // Calculate gain reduction
            float gainReductionDb = 0.0f;

            if (inputDb > thresholdDb) {
                // Above threshold - apply compression
                float overThreshold = inputDb - thresholdDb;

                // Soft knee
                if (kneeDb > 0.0f && overThreshold < kneeDb) {
                    // In the knee region - gradual compression
                    float kneeRatio = overThreshold / kneeDb;
                    gainReductionDb = overThreshold * kneeRatio * (1.0f - 1.0f / ratioVal) * 0.5f;
                } else {
                    // Above knee - full compression
                    if (kneeDb > 0.0f) {
                        overThreshold -= kneeDb;
                        gainReductionDb = kneeDb * (1.0f - 1.0f / ratioVal) * 0.5f;
                        gainReductionDb += overThreshold * (1.0f - 1.0f / ratioVal);
                    } else {
                        gainReductionDb = overThreshold * (1.0f - 1.0f / ratioVal);
                    }
                }
            }

            // Update envelope follower
            if (gainReductionDb > envelope) {
                // Attack
                envelope = envelope + attackCoef * (gainReductionDb - envelope);
            } else {
                // Release
                envelope = envelope + releaseCoef * (gainReductionDb - envelope);
            }

            // Store for metering
            gainReduction = envelope;

            // Convert envelope to linear gain
            float gainLinear = DSPUtils::dbToLinear(-envelope);

            // Apply compression and makeup gain
            buffer[idx] = inputL * gainLinear * makeupLinear;
            if (channels > 1) {
                buffer[idx + 1] = inputR * gainLinear * makeupLinear;
            }
        }
    }

    // Parameter setters
    void setThreshold(float thresholdDb) {
        threshold.setTarget(DSPUtils::clamp(thresholdDb, -60.0f, 0.0f));
    }

    void setRatio(float r) {
        ratio.setTarget(DSPUtils::clamp(r, 1.0f, 20.0f));
    }

    void setAttack(float attackMs) {
        attack.setTarget(DSPUtils::clamp(attackMs, 0.1f, 100.0f));
        updateCoefficients();
    }

    void setRelease(float releaseMs) {
        release.setTarget(DSPUtils::clamp(releaseMs, 10.0f, 1000.0f));
        updateCoefficients();
    }

    void setKnee(float kneeDb) {
        knee.setTarget(DSPUtils::clamp(kneeDb, 0.0f, 12.0f));
    }

    void setMakeupGain(float gainDb) {
        makeupGain.setTarget(DSPUtils::clamp(gainDb, 0.0f, 24.0f));
    }

    void setDetectionMode(DetectionMode mode) {
        detectionMode = mode;
    }

    /**
     * Get current gain reduction in dB (for metering)
     */
    float getGainReduction() const {
        return gainReduction;
    }

private:
    // State
    float envelope;
    float gainReduction;

    // RMS detection buffer
    static constexpr int RMS_WINDOW_SIZE = 256;
    float rmsBuffer[RMS_WINDOW_SIZE] = {0};
    int rmsIndex = 0;

    // Parameters
    SmoothParameter threshold;
    SmoothParameter ratio;
    SmoothParameter attack;
    SmoothParameter release;
    SmoothParameter knee;
    SmoothParameter makeupGain;

    DetectionMode detectionMode;

    // Computed coefficients
    float attackCoef;
    float releaseCoef;

    /**
     * Detect input level (peak or RMS)
     */
    float detectLevel(float left, float right) {
        float level;

        if (detectionMode == PEAK) {
            // Peak detection - use maximum absolute value
            level = std::max(std::abs(left), std::abs(right));
        } else {
            // RMS detection - root mean square
            float sumSquared = left * left + right * right;
            rmsBuffer[rmsIndex] = sumSquared;
            rmsIndex = (rmsIndex + 1) % RMS_WINDOW_SIZE;

            // Calculate RMS
            float sum = 0.0f;
            for (int i = 0; i < RMS_WINDOW_SIZE; ++i) {
                sum += rmsBuffer[i];
            }
            level = std::sqrt(sum / RMS_WINDOW_SIZE);
        }

        return level;
    }

    /**
     * Update attack and release coefficients
     */
    void updateCoefficients() {
        float attackMs = attack.getCurrent();
        float releaseMs = release.getCurrent();

        // Calculate time constants (one-pole filter coefficients)
        attackCoef = 1.0f - std::exp(-1.0f / (attackMs * 0.001f * sampleRate));
        releaseCoef = 1.0f - std::exp(-1.0f / (releaseMs * 0.001f * sampleRate));
    }
};

} // namespace dubtechno
