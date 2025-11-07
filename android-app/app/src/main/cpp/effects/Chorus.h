#pragma once

#include "Effect.h"
#include "../dsp/DelayLine.h"
#include <cmath>
#include <vector>

namespace dubtechno {

/**
 * Chorus Effect
 *
 * Creates width and movement by pitch modulation
 * Great for pads and atmospheres in dub techno
 *
 * Features:
 * - 2-4 modulated delay lines (voices)
 * - Independent LFO per voice
 * - Rate and depth control
 */
class Chorus : public Effect {
public:
    Chorus() {
        rate.reset(0.5f);      // 0.5 Hz
        depth.reset(0.5f);     // Medium depth
        voices.reset(3.0f);    // 3 voices
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);

        // Initialize delay lines (50ms max)
        delayLines.resize(4);
        lfoPhases.resize(4, 0.0f);

        for (int i = 0; i < 4; ++i) {
            delayLines[i].init(0.05f, sampleRate);
            lfoPhases[i] = i * 0.25f; // Phase offset per voice
        }
    }

    void reset() override {
        for (auto& delay : delayLines) {
            delay.clear();
        }
        std::fill(lfoPhases.begin(), lfoPhases.end(), 0.0f);
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed()) return;

        float lfoRate = rate.getNext();
        float depthAmount = depth.getNext();
        int numVoices = static_cast<int>(voices.getNext());
        numVoices = DSPUtils::clamp(numVoices, 1, 4);

        // Base delay time: 20ms
        float baseDelay = 0.02f * sampleRate;
        // Depth: ±5ms
        float maxModulation = 0.005f * sampleRate * depthAmount;

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;
            float inputL = buffer[idx];
            float inputR = channels > 1 ? buffer[idx + 1] : inputL;

            // Mix input to mono
            float input = (inputL + inputR) * 0.5f;

            float chorusL = 0.0f;
            float chorusR = 0.0f;

            // Process each voice
            for (int v = 0; v < numVoices; ++v) {
                // Generate LFO
                float lfo = std::sin(TWO_PI * lfoPhases[v]);
                lfoPhases[v] += lfoRate / sampleRate;
                if (lfoPhases[v] >= 1.0f) lfoPhases[v] -= 1.0f;

                // Modulate delay time
                float delayTime = baseDelay + lfo * maxModulation;

                // Write to delay line
                delayLines[v].write(input);

                // Read modulated delay
                float delayed = delayLines[v].read(delayTime);

                // Pan voices across stereo field
                float pan = static_cast<float>(v) / (numVoices - 1.0f);
                if (numVoices == 1) pan = 0.5f;

                chorusL += delayed * (1.0f - pan);
                chorusR += delayed * pan;
            }

            // Normalize by number of voices
            chorusL /= numVoices;
            chorusR /= numVoices;

            // Mix
            float mixVal = mix.load(std::memory_order_relaxed);
            buffer[idx] = inputL * (1.0f - mixVal) + chorusL * mixVal;
            if (channels > 1) {
                buffer[idx + 1] = inputR * (1.0f - mixVal) + chorusR * mixVal;
            }
        }
    }

    void setRate(float rateHz) {
        rate.setTarget(DSPUtils::clamp(rateHz, 0.01f, 10.0f));
    }

    void setDepth(float d) {
        depth.setTarget(DSPUtils::clamp(d, 0.0f, 1.0f));
    }

    void setVoices(int v) {
        voices.setTarget(static_cast<float>(DSPUtils::clamp(v, 1, 4)));
    }

private:
    std::vector<DelayLine> delayLines;
    std::vector<float> lfoPhases;

    SmoothParameter rate;
    SmoothParameter depth;
    SmoothParameter voices;
};

} // namespace dubtechno
