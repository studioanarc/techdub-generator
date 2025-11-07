#pragma once

#include "Effect.h"
#include "../dsp/AllPassFilter.h"
#include <vector>

namespace dubtechno {

/**
 * Freeverb-style Reverb Effect
 *
 * Based on the classic Freeverb algorithm by Jezar at Dreampoint
 *
 * Architecture:
 * - 8 parallel comb filters (4 per channel)
 * - 4 series all-pass filters (2 per channel)
 * - Damping for high-frequency absorption
 * - Stereo width control
 *
 * Perfect for long, lush dub techno spaces (4-10 second decay)
 */
class Reverb : public Effect {
public:
    Reverb() {
        // Initialize parameters
        roomSize.reset(0.8f);        // Large room
        damping.reset(0.5f);         // Moderate damping
        width.reset(1.0f);           // Full stereo width
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);

        // Freeverb comb filter delay lengths (in samples at 44.1kHz)
        // Scaled for current sample rate
        const int combDelays[] = {1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617};
        const int allpassDelays[] = {556, 441, 341, 225};

        float scale = sampleRate / 44100.0f;

        // Initialize comb filters (8 total, 4 per channel)
        combFilters.resize(8);
        combBufferIndices.resize(8, 0);
        combFilterStore.resize(8, 0.0f);

        for (int i = 0; i < 8; ++i) {
            int delayLen = static_cast<int>(combDelays[i] * scale);
            combFilters[i].resize(delayLen, 0.0f);
        }

        // Initialize all-pass filters (4 total, shared then split)
        allpassFilters.resize(4);
        for (int i = 0; i < 4; ++i) {
            int delayLen = static_cast<int>(allpassDelays[i] * scale);
            allpassFilters[i].init(delayLen, 0.5f);
        }

        updateParameters();
    }

    void reset() override {
        // Clear all comb filter buffers
        for (auto& filter : combFilters) {
            std::fill(filter.begin(), filter.end(), 0.0f);
        }
        std::fill(combBufferIndices.begin(), combBufferIndices.end(), 0);
        std::fill(combFilterStore.begin(), combFilterStore.end(), 0.0f);

        // Clear all-pass filters
        for (auto& apf : allpassFilters) {
            apf.clear();
        }
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed()) return;

        updateParameters();

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;
            float inputL = buffer[idx];
            float inputR = channels > 1 ? buffer[idx + 1] : inputL;

            // Mix to mono for reverb input (with width control)
            float input = (inputL + inputR) * 0.5f;

            // Process through comb filters (parallel)
            float combOutL = 0.0f;
            float combOutR = 0.0f;

            // Left channel: comb filters 0-3
            for (int c = 0; c < 4; ++c) {
                combOutL += processComb(c, input);
            }

            // Right channel: comb filters 4-7
            for (int c = 4; c < 8; ++c) {
                combOutR += processComb(c, input);
            }

            // Process through all-pass filters (series)
            // Left channel: all-pass 0, 2
            float wetL = allpassFilters[0].process(combOutL);
            wetL = allpassFilters[2].process(wetL);

            // Right channel: all-pass 1, 3
            float wetR = allpassFilters[1].process(combOutR);
            wetR = allpassFilters[3].process(wetR);

            // Apply stereo width
            float widthVal = width.getCurrent();
            float mono = (wetL + wetR) * 0.5f;
            wetL = mono + (wetL - mono) * widthVal;
            wetR = mono + (wetR - mono) * widthVal;

            // Mix dry/wet
            float mixVal = mix.load(std::memory_order_relaxed);
            buffer[idx] = inputL * (1.0f - mixVal) + wetL * mixVal;
            if (channels > 1) {
                buffer[idx + 1] = inputR * (1.0f - mixVal) + wetR * mixVal;
            }
        }
    }

    // Parameter setters
    void setRoomSize(float size) {
        roomSize.setTarget(DSPUtils::clamp(size, 0.0f, 1.0f));
    }

    void setDamping(float damp) {
        damping.setTarget(DSPUtils::clamp(damp, 0.0f, 1.0f));
    }

    void setWidth(float w) {
        width.setTarget(DSPUtils::clamp(w, 0.0f, 1.0f));
    }

private:
    // Comb filters (delay buffers)
    std::vector<std::vector<float>> combFilters;
    std::vector<int> combBufferIndices;
    std::vector<float> combFilterStore; // For damping

    // All-pass filters
    std::vector<AllPassFilter> allpassFilters;

    // Parameters
    SmoothParameter roomSize;
    SmoothParameter damping;
    SmoothParameter width;

    // Computed coefficients
    float feedback;
    float damp1;
    float damp2;

    /**
     * Process a single comb filter
     */
    float processComb(int filterIndex, float input) {
        auto& buffer = combFilters[filterIndex];
        int& index = combBufferIndices[filterIndex];
        float& store = combFilterStore[filterIndex];

        // Read from buffer
        float output = buffer[index];

        // Apply damping (one-pole lowpass filter)
        store = (output * damp2) + (store * damp1);

        // Write to buffer with feedback
        buffer[index] = input + (store * feedback);

        // Advance index
        index = (index + 1) % buffer.size();

        // Prevent denormals
        if (std::abs(store) < 1e-25f) store = 0.0f;

        return output;
    }

    /**
     * Update internal parameters from smooth values
     */
    void updateParameters() {
        // Room size determines feedback amount
        float room = roomSize.getNext();
        feedback = 0.28f + room * 0.7f; // 0.28 - 0.98

        // Damping determines high-frequency absorption
        float damp = damping.getNext();
        damp1 = damp * 0.4f;      // Damping coefficient
        damp2 = 1.0f - damp1;     // Complement

        // Width is used directly in processing
        width.getNext();
    }
};

} // namespace dubtechno
