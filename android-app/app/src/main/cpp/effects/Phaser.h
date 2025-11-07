#pragma once

#include "Effect.h"
#include "../dsp/AllPassFilter.h"
#include <cmath>
#include <vector>

namespace dubtechno {

/**
 * Phaser Effect
 *
 * Creates sweeping notches in the frequency spectrum
 * Adds subtle movement to dub techno sounds
 *
 * Features:
 * - 4-12 all-pass filter stages
 * - LFO-controlled sweep
 * - Feedback parameter
 * - Stereo operation
 */
class Phaser : public Effect {
public:
    Phaser() : lfoPhase(0.0f) {
        rate.reset(0.5f);       // 0.5 Hz
        depth.reset(0.5f);      // Medium depth
        feedback.reset(0.5f);   // 50% feedback
        stages.reset(6.0f);     // 6 stages
        centerFreq.reset(1000.0f); // 1kHz center
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);

        // Initialize all-pass filters (12 stages max, 6 per channel)
        allpassFiltersL.resize(12);
        allpassFiltersR.resize(12);

        // Initialize with small delays
        for (int i = 0; i < 12; ++i) {
            int delayLen = 10 + i * 5; // Varying delay lengths
            allpassFiltersL[i].init(delayLen, 0.7f);
            allpassFiltersR[i].init(delayLen, 0.7f);
        }

        lfoPhase = 0.0f;
        feedbackSampleL = 0.0f;
        feedbackSampleR = 0.0f;
    }

    void reset() override {
        for (auto& apf : allpassFiltersL) {
            apf.clear();
        }
        for (auto& apf : allpassFiltersR) {
            apf.clear();
        }
        lfoPhase = 0.0f;
        feedbackSampleL = 0.0f;
        feedbackSampleR = 0.0f;
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed()) return;

        float lfoRate = rate.getNext();
        float depthAmount = depth.getNext();
        float fbAmount = feedback.getNext();
        int numStages = static_cast<int>(stages.getNext());
        numStages = DSPUtils::clamp(numStages, 2, 12);
        float centerF = centerFreq.getNext();

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;
            float inputL = buffer[idx];
            float inputR = channels > 1 ? buffer[idx + 1] : inputL;

            // Generate LFO
            float lfo = std::sin(TWO_PI * lfoPhase);
            lfoPhase += lfoRate / sampleRate;
            if (lfoPhase >= 1.0f) lfoPhase -= 1.0f;

            // Modulate all-pass coefficient
            // LFO controls the coefficient from -0.9 to +0.9
            float coefficient = lfo * 0.9f * depthAmount;

            // Update all-pass filter coefficients
            for (int s = 0; s < numStages; ++s) {
                allpassFiltersL[s].setCoefficient(coefficient);
                allpassFiltersR[s].setCoefficient(coefficient);
            }

            // Add feedback
            float processedL = inputL + feedbackSampleL * fbAmount;
            float processedR = inputR + feedbackSampleR * fbAmount;

            // Process through all-pass filter chain
            for (int s = 0; s < numStages; ++s) {
                processedL = allpassFiltersL[s].process(processedL);
                processedR = allpassFiltersR[s].process(processedR);
            }

            // Store for feedback
            feedbackSampleL = processedL;
            feedbackSampleR = processedR;

            // Mix
            float mixVal = mix.load(std::memory_order_relaxed);
            buffer[idx] = inputL * (1.0f - mixVal) + processedL * mixVal;
            if (channels > 1) {
                buffer[idx + 1] = inputR * (1.0f - mixVal) + processedR * mixVal;
            }
        }
    }

    void setRate(float rateHz) {
        rate.setTarget(DSPUtils::clamp(rateHz, 0.01f, 10.0f));
    }

    void setDepth(float d) {
        depth.setTarget(DSPUtils::clamp(d, 0.0f, 1.0f));
    }

    void setFeedback(float fb) {
        feedback.setTarget(DSPUtils::clamp(fb, 0.0f, 0.95f));
    }

    void setStages(int s) {
        stages.setTarget(static_cast<float>(DSPUtils::clamp(s, 2, 12)));
    }

    void setCenterFrequency(float freq) {
        centerFreq.setTarget(DSPUtils::clamp(freq, 200.0f, 5000.0f));
    }

private:
    std::vector<AllPassFilter> allpassFiltersL;
    std::vector<AllPassFilter> allpassFiltersR;

    float lfoPhase;
    float feedbackSampleL;
    float feedbackSampleR;

    SmoothParameter rate;
    SmoothParameter depth;
    SmoothParameter feedback;
    SmoothParameter stages;
    SmoothParameter centerFreq;
};

} // namespace dubtechno
