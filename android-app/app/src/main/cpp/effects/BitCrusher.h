#pragma once

#include "Effect.h"
#include <cmath>

namespace dubtechno {

/**
 * Bit Crusher Effect
 *
 * Lo-fi digital degradation for dub techno texture
 *
 * Features:
 * - Sample rate reduction (44.1kHz → 500Hz)
 * - Bit depth reduction (24-bit → 1-bit)
 * - Optional dithering
 */
class BitCrusher : public Effect {
public:
    BitCrusher() : holdSampleL(0.0f), holdSampleR(0.0f), holdCounter(0) {
        sampleRateReduction.reset(1.0f);  // No reduction
        bitDepth.reset(16.0f);            // 16-bit
        dither.reset(0.0f);               // No dither
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);
        reset();
    }

    void reset() override {
        holdSampleL = 0.0f;
        holdSampleR = 0.0f;
        holdCounter = 0;
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed()) return;

        float srReduction = sampleRateReduction.getNext();
        float bits = bitDepth.getNext();
        float ditherAmount = dither.getNext();

        // Calculate sample hold interval
        int holdInterval = static_cast<int>(srReduction);
        if (holdInterval < 1) holdInterval = 1;

        // Calculate bit depth quantization
        float levels = std::pow(2.0f, bits);
        float step = 2.0f / levels;

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;
            float inputL = buffer[idx];
            float inputR = channels > 1 ? buffer[idx + 1] : inputL;

            // Sample rate reduction (sample and hold)
            if (holdCounter == 0) {
                holdSampleL = inputL;
                holdSampleR = inputR;
            }
            holdCounter = (holdCounter + 1) % holdInterval;

            // Bit depth reduction (quantization)
            float crushedL = holdSampleL;
            float crushedR = holdSampleR;

            if (bits < 16.0f) {
                // Add dither before quantization
                if (ditherAmount > 0.0f) {
                    float ditherL = (static_cast<float>(rand()) / RAND_MAX - 0.5f) * step * ditherAmount;
                    float ditherR = (static_cast<float>(rand()) / RAND_MAX - 0.5f) * step * ditherAmount;
                    crushedL += ditherL;
                    crushedR += ditherR;
                }

                // Quantize
                crushedL = std::floor(crushedL / step + 0.5f) * step;
                crushedR = std::floor(crushedR / step + 0.5f) * step;

                // Clamp
                crushedL = DSPUtils::clamp(crushedL, -1.0f, 1.0f);
                crushedR = DSPUtils::clamp(crushedR, -1.0f, 1.0f);
            }

            // Mix
            float mixVal = mix.load(std::memory_order_relaxed);
            buffer[idx] = inputL * (1.0f - mixVal) + crushedL * mixVal;
            if (channels > 1) {
                buffer[idx + 1] = inputR * (1.0f - mixVal) + crushedR * mixVal;
            }
        }
    }

    void setSampleRateReduction(float reduction) {
        sampleRateReduction.setTarget(DSPUtils::clamp(reduction, 1.0f, 100.0f));
    }

    void setBitDepth(float bits) {
        bitDepth.setTarget(DSPUtils::clamp(bits, 1.0f, 16.0f));
    }

    void setDither(float amount) {
        dither.setTarget(DSPUtils::clamp(amount, 0.0f, 1.0f));
    }

private:
    float holdSampleL;
    float holdSampleR;
    int holdCounter;

    SmoothParameter sampleRateReduction;
    SmoothParameter bitDepth;
    SmoothParameter dither;
};

} // namespace dubtechno
