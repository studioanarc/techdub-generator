#pragma once

#include "Effect.h"
#include "../dsp/DelayLine.h"
#include "../dsp/Biquad.h"

namespace dubtechno {

/**
 * Simple Delay Effect
 *
 * Features:
 * - Delay time: 1ms - 2000ms
 * - Feedback: 0% - 95%
 * - Ping-pong stereo mode
 * - Lowpass filter in feedback loop (prevents harshness)
 */
class Delay : public Effect {
public:
    Delay() : pingPongEnabled(false) {
        time.reset(500.0f);      // 500ms
        feedback.reset(0.5f);    // 50%
        filterFreq.reset(8000.0f); // 8kHz lowpass
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);

        // Initialize delay lines (2 second max)
        delayLeft.init(2.0f, sampleRate);
        delayRight.init(2.0f, sampleRate);

        // Initialize feedback filters
        filterLeft.init(sampleRate);
        filterRight.init(sampleRate);
        updateFilters();
    }

    void reset() override {
        delayLeft.clear();
        delayRight.clear();
        filterLeft.reset();
        filterRight.reset();
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed()) return;

        float delayTimeMs = time.getNext();
        float feedbackAmount = feedback.getNext();
        float delayTimeSamples = (delayTimeMs / 1000.0f) * sampleRate;

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;
            float dryL = buffer[idx];
            float dryR = channels > 1 ? buffer[idx + 1] : dryL;

            if (pingPongEnabled && channels > 1) {
                // Ping-pong: left delay feeds right, right feeds left
                float delayedL = delayLeft.read(delayTimeSamples);
                float delayedR = delayRight.read(delayTimeSamples);

                // Apply filter
                delayedL = filterLeft.process(delayedL);
                delayedR = filterRight.process(delayedR);

                // Cross feedback
                delayLeft.write(dryL + delayedR * feedbackAmount);
                delayRight.write(dryR + delayedL * feedbackAmount);

                buffer[idx] = dryL + delayedL * mix.load(std::memory_order_relaxed);
                buffer[idx + 1] = dryR + delayedR * mix.load(std::memory_order_relaxed);
            } else {
                // Normal stereo delay
                float delayedL = delayLeft.read(delayTimeSamples);
                float delayedR = delayRight.read(delayTimeSamples);

                // Apply filter
                delayedL = filterLeft.process(delayedL);
                delayedR = filterRight.process(delayedR);

                // Write with feedback
                delayLeft.write(dryL + delayedL * feedbackAmount);
                delayRight.write(dryR + delayedR * feedbackAmount);

                buffer[idx] = dryL + delayedL * mix.load(std::memory_order_relaxed);
                if (channels > 1) {
                    buffer[idx + 1] = dryR + delayedR * mix.load(std::memory_order_relaxed);
                }
            }
        }
    }

    void setTime(float timeMs) {
        time.setTarget(DSPUtils::clamp(timeMs, 1.0f, 2000.0f));
    }

    void setFeedback(float fb) {
        feedback.setTarget(DSPUtils::clamp(fb, 0.0f, 0.95f));
    }

    void setFilterFrequency(float freq) {
        filterFreq.setTarget(DSPUtils::clamp(freq, 500.0f, 20000.0f));
        updateFilters();
    }

    void setPingPong(bool enabled) {
        pingPongEnabled = enabled;
    }

private:
    DelayLine delayLeft;
    DelayLine delayRight;
    Biquad filterLeft;
    Biquad filterRight;

    SmoothParameter time;
    SmoothParameter feedback;
    SmoothParameter filterFreq;

    bool pingPongEnabled;

    void updateFilters() {
        float freq = filterFreq.getCurrent();
        filterLeft.setCoefficients(Biquad::LOWPASS, freq, 0.707f);
        filterRight.setCoefficients(Biquad::LOWPASS, freq, 0.707f);
    }
};

} // namespace dubtechno
