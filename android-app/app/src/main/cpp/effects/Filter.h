#pragma once

#include "Effect.h"
#include "../dsp/Biquad.h"
#include <cmath>

namespace dubtechno {

/**
 * Auto-Filter Effect
 *
 * Sweeping filter controlled by LFO
 * Essential for creating movement in dub techno
 *
 * Features:
 * - Filter types: Lowpass, Highpass, Bandpass
 * - LFO-controlled cutoff frequency
 * - Resonance control
 * - Tempo-syncable
 */
class Filter : public Effect {
public:
    Filter() : lfoPhase(0.0f) {
        baseFrequency.reset(1000.0f);  // 1kHz center
        resonance.reset(2.0f);         // Q factor
        depth.reset(0.5f);             // LFO depth
        rate.reset(0.5f);              // 0.5 Hz LFO
        filterType = Biquad::LOWPASS;
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);
        filterLeft.init(sampleRate);
        filterRight.init(sampleRate);
        lfoPhase = 0.0f;
    }

    void reset() override {
        filterLeft.reset();
        filterRight.reset();
        lfoPhase = 0.0f;
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed()) return;

        float baseFreq = baseFrequency.getNext();
        float q = resonance.getNext();
        float depthAmount = depth.getNext();
        float lfoRate = rate.getNext();

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;

            // Generate LFO (sine wave)
            float lfo = std::sin(TWO_PI * lfoPhase);
            lfoPhase += lfoRate / sampleRate;
            if (lfoPhase >= 1.0f) lfoPhase -= 1.0f;

            // Modulate frequency
            // LFO range: baseFreq * (1 - depth) to baseFreq * (1 + depth)
            float modulatedFreq = baseFreq * (1.0f + lfo * depthAmount);
            modulatedFreq = DSPUtils::clamp(modulatedFreq, 20.0f, 20000.0f);

            // Update filter coefficients (only when frequency changes significantly)
            static float lastFreq = -1.0f;
            if (std::abs(modulatedFreq - lastFreq) > 10.0f) {
                filterLeft.setCoefficients(filterType, modulatedFreq, q);
                filterRight.setCoefficients(filterType, modulatedFreq, q);
                lastFreq = modulatedFreq;
            }

            // Process
            float inputL = buffer[idx];
            float inputR = channels > 1 ? buffer[idx + 1] : inputL;

            float wetL = filterLeft.process(inputL);
            float wetR = filterRight.process(inputR);

            // Mix
            float mixVal = mix.load(std::memory_order_relaxed);
            buffer[idx] = inputL * (1.0f - mixVal) + wetL * mixVal;
            if (channels > 1) {
                buffer[idx + 1] = inputR * (1.0f - mixVal) + wetR * mixVal;
            }
        }
    }

    void setBaseFrequency(float freq) {
        baseFrequency.setTarget(DSPUtils::clamp(freq, 20.0f, 10000.0f));
    }

    void setResonance(float q) {
        resonance.setTarget(DSPUtils::clamp(q, 0.5f, 10.0f));
    }

    void setDepth(float d) {
        depth.setTarget(DSPUtils::clamp(d, 0.0f, 2.0f));
    }

    void setRate(float rateHz) {
        rate.setTarget(DSPUtils::clamp(rateHz, 0.01f, 20.0f));
    }

    void setFilterType(Biquad::FilterType type) {
        filterType = type;
    }

private:
    Biquad filterLeft;
    Biquad filterRight;

    float lfoPhase;

    SmoothParameter baseFrequency;
    SmoothParameter resonance;
    SmoothParameter depth;
    SmoothParameter rate;

    Biquad::FilterType filterType;
};

} // namespace dubtechno
