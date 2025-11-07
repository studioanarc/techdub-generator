#pragma once

#include "Effect.h"
#include "../dsp/DelayLine.h"
#include "../dsp/Biquad.h"
#include <vector>
#include <cmath>

namespace dubtechno {

/**
 * Tape Echo Effect - Inspired by Roland Space Echo RE-201
 *
 * THE definitive effect for dub techno!
 *
 * Features:
 * - Multi-tap delay (4 taps)
 * - Tape saturation/distortion per tap
 * - Wow & flutter (pitch modulation)
 * - Age parameter (noise, filtering, dropout)
 * - Tone control (prevents bass buildup)
 */
class TapeEcho : public Effect {
public:
    TapeEcho() : lfoPhase(0.0f) {
        // Initialize parameters
        time.reset(500.0f);              // 500ms delay
        feedback.reset(0.6f);            // 60% feedback
        saturation.reset(0.3f);          // Moderate saturation
        age.reset(0.5f);                 // Medium age
        wowFlutter.reset(0.15f);         // Subtle modulation
        toneControl.reset(5000.0f);      // 5kHz tone filter
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);

        // Initialize delay lines (5 second max)
        delayLeft.init(5.0f, sampleRate);
        delayRight.init(5.0f, sampleRate);

        // Initialize tone filters (lowpass to prevent bass buildup)
        toneFilterLeft.init(sampleRate);
        toneFilterRight.init(sampleRate);
        updateToneFilter();

        // Initialize age filters (highpass to simulate tape aging)
        ageFilterLeft.init(sampleRate);
        ageFilterRight.init(sampleRate);

        // LFO for wow & flutter
        lfoPhase = 0.0f;
        lfoRate = 0.5f; // 0.5 Hz
    }

    void reset() override {
        delayLeft.clear();
        delayRight.clear();
        toneFilterLeft.reset();
        toneFilterRight.reset();
        ageFilterLeft.reset();
        ageFilterRight.reset();
        lfoPhase = 0.0f;
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed()) return;

        // Smooth parameter changes
        float delayTimeMs = time.getNext();
        float feedbackAmount = feedback.getNext();
        float satAmount = saturation.getNext();
        float ageAmount = age.getNext();
        float wowFlutterAmount = wowFlutter.getNext();

        float delayTimeSamples = (delayTimeMs / 1000.0f) * sampleRate;

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;
            float dryL = buffer[idx];
            float dryR = channels > 1 ? buffer[idx + 1] : dryL;

            // Wow & flutter modulation (pitch variation)
            float lfo = std::sin(TWO_PI * lfoPhase);
            lfoPhase += lfoRate / sampleRate;
            if (lfoPhase >= 1.0f) lfoPhase -= 1.0f;

            // Modulate delay time
            float modAmount = wowFlutterAmount * 3.0f; // Up to 3ms modulation
            float modDelayL = delayTimeSamples + lfo * modAmount * sampleRate / 1000.0f;
            float modDelayR = delayTimeSamples + std::sin(TWO_PI * (lfoPhase + 0.25f)) * modAmount * sampleRate / 1000.0f;

            // Read multi-taps
            float tap1L = delayLeft.tap(modDelayL * 0.25f);
            float tap2L = delayLeft.tap(modDelayL * 0.5f);
            float tap3L = delayLeft.tap(modDelayL * 0.75f);
            float tap4L = delayLeft.tap(modDelayL);

            float tap1R = delayRight.tap(modDelayR * 0.25f);
            float tap2R = delayRight.tap(modDelayR * 0.5f);
            float tap3R = delayRight.tap(modDelayR * 0.75f);
            float tap4R = delayRight.tap(modDelayR);

            // Mix taps with varying levels (simulates multi-head tape)
            float wetL = tap1L * 0.4f + tap2L * 0.6f + tap3L * 0.7f + tap4L * 1.0f;
            float wetR = tap1R * 0.4f + tap2R * 0.6f + tap3R * 0.7f + tap4R * 1.0f;

            // Apply tape saturation to each tap
            wetL = applySaturation(wetL, satAmount);
            wetR = applySaturation(wetR, satAmount);

            // Apply age effect (filtering + noise)
            wetL = applyAge(wetL, ageAmount, ageFilterLeft);
            wetR = applyAge(wetR, ageAmount, ageFilterRight);

            // Apply tone control (feedback filtering)
            wetL = toneFilterLeft.process(wetL);
            wetR = toneFilterRight.process(wetR);

            // Write to delay with feedback
            delayLeft.write(dryL + wetL * feedbackAmount);
            delayRight.write(dryR + wetR * feedbackAmount);

            // Mix dry/wet
            buffer[idx] = dryL + wetL * mix.load(std::memory_order_relaxed);
            if (channels > 1) {
                buffer[idx + 1] = dryR + wetR * mix.load(std::memory_order_relaxed);
            }
        }
    }

    // Parameter setters
    void setTime(float timeMs) {
        time.setTarget(DSPUtils::clamp(timeMs, 1.0f, 2000.0f));
    }

    void setFeedback(float fb) {
        feedback.setTarget(DSPUtils::clamp(fb, 0.0f, 0.95f));
    }

    void setSaturation(float sat) {
        saturation.setTarget(DSPUtils::clamp(sat, 0.0f, 1.0f));
    }

    void setAge(float a) {
        age.setTarget(DSPUtils::clamp(a, 0.0f, 1.0f));
        updateAgeFilters();
    }

    void setWowFlutter(float wf) {
        wowFlutter.setTarget(DSPUtils::clamp(wf, 0.0f, 1.0f));
    }

    void setTone(float freq) {
        toneControl.setTarget(DSPUtils::clamp(freq, 500.0f, 10000.0f));
        updateToneFilter();
    }

private:
    // Delay lines
    DelayLine delayLeft;
    DelayLine delayRight;

    // Filters
    Biquad toneFilterLeft;
    Biquad toneFilterRight;
    Biquad ageFilterLeft;
    Biquad ageFilterRight;

    // LFO for wow & flutter
    float lfoPhase;
    float lfoRate;

    // Smooth parameters
    SmoothParameter time;
    SmoothParameter feedback;
    SmoothParameter saturation;
    SmoothParameter age;
    SmoothParameter wowFlutter;
    SmoothParameter toneControl;

    /**
     * Apply tape saturation (soft clipping with asymmetry)
     */
    float applySaturation(float input, float amount) {
        if (amount < 0.01f) return input;

        // Pre-gain
        float gained = input * (1.0f + amount * 3.0f);

        // Asymmetric soft clipping (simulates tape saturation)
        float output;
        if (gained > 0.0f) {
            // Positive half (more saturation)
            output = std::tanh(gained * 1.5f) / 1.5f;
        } else {
            // Negative half (less saturation)
            output = std::tanh(gained) ;
        }

        return output;
    }

    /**
     * Apply age effect (filtering + subtle noise)
     */
    float applyAge(float input, float ageAmount, Biquad& filter) {
        if (ageAmount < 0.01f) return input;

        // Apply highpass filter (simulates loss of bass with age)
        float filtered = filter.process(input);

        // Add subtle noise (tape hiss)
        float noise = (static_cast<float>(rand()) / RAND_MAX - 0.5f) * 0.002f * ageAmount;

        // Mix original, filtered, and noise
        return input * (1.0f - ageAmount * 0.3f) + filtered * ageAmount * 0.3f + noise;
    }

    void updateToneFilter() {
        float freq = toneControl.getCurrent();
        toneFilterLeft.setCoefficients(Biquad::LOWPASS, freq, 0.707f);
        toneFilterRight.setCoefficients(Biquad::LOWPASS, freq, 0.707f);
    }

    void updateAgeFilters() {
        float ageAmount = age.getCurrent();
        // Higher age = more bass loss
        float hpFreq = 100.0f + ageAmount * 400.0f; // 100Hz - 500Hz
        ageFilterLeft.setCoefficients(Biquad::HIGHPASS, hpFreq, 0.707f);
        ageFilterRight.setCoefficients(Biquad::HIGHPASS, hpFreq, 0.707f);
    }
};

} // namespace dubtechno
