#pragma once

#include "Effect.h"
#include "../dsp/Biquad.h"
#include <cmath>

namespace dubtechno {

/**
 * Distortion/Saturation Effect
 *
 * Adds warmth and harmonics to dub techno sounds
 *
 * Features:
 * - Multiple distortion algorithms
 * - Pre/post gain control
 * - Tone control (lowpass after distortion)
 * - Parallel mix (blend clean and distorted)
 */
class Distortion : public Effect {
public:
    enum DistortionType {
        HARD_CLIP,
        SOFT_CLIP,
        TUBE,
        WAVESHAPER
    };

    Distortion() {
        drive.reset(1.0f);
        tone.reset(8000.0f);
        outputGain.reset(1.0f);
        distType = SOFT_CLIP;
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);
        toneFilterLeft.init(sampleRate);
        toneFilterRight.init(sampleRate);
        updateToneFilter();
    }

    void reset() override {
        toneFilterLeft.reset();
        toneFilterRight.reset();
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed()) return;

        float driveAmount = drive.getNext();
        float outGain = outputGain.getNext();

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;
            float inputL = buffer[idx];
            float inputR = channels > 1 ? buffer[idx + 1] : inputL;

            // Apply pre-gain
            float distL = inputL * driveAmount;
            float distR = inputR * driveAmount;

            // Apply distortion algorithm
            distL = applyDistortion(distL);
            distR = applyDistortion(distR);

            // Apply tone filter
            distL = toneFilterLeft.process(distL);
            distR = toneFilterRight.process(distR);

            // Apply output gain
            distL *= outGain;
            distR *= outGain;

            // Mix (parallel distortion)
            float mixVal = mix.load(std::memory_order_relaxed);
            buffer[idx] = inputL * (1.0f - mixVal) + distL * mixVal;
            if (channels > 1) {
                buffer[idx + 1] = inputR * (1.0f - mixVal) + distR * mixVal;
            }
        }
    }

    void setDrive(float d) {
        drive.setTarget(DSPUtils::clamp(d, 1.0f, 20.0f));
    }

    void setTone(float freq) {
        tone.setTarget(DSPUtils::clamp(freq, 500.0f, 20000.0f));
        updateToneFilter();
    }

    void setOutputGain(float gain) {
        outputGain.setTarget(DSPUtils::clamp(gain, 0.1f, 2.0f));
    }

    void setDistortionType(DistortionType type) {
        distType = type;
    }

private:
    Biquad toneFilterLeft;
    Biquad toneFilterRight;

    SmoothParameter drive;
    SmoothParameter tone;
    SmoothParameter outputGain;

    DistortionType distType;

    float applyDistortion(float input) {
        switch (distType) {
            case HARD_CLIP:
                return DSPUtils::hardClip(input);

            case SOFT_CLIP:
                return std::tanh(input);

            case TUBE: {
                // Asymmetric tube-style saturation
                if (input > 0.0f) {
                    return 1.0f - std::exp(-input);
                } else {
                    return -1.0f + std::exp(input);
                }
            }

            case WAVESHAPER: {
                // Custom waveshaping function
                float x = DSPUtils::clamp(input, -1.0f, 1.0f);
                return x * (1.5f - 0.5f * x * x);
            }

            default:
                return input;
        }
    }

    void updateToneFilter() {
        float freq = tone.getCurrent();
        toneFilterLeft.setCoefficients(Biquad::LOWPASS, freq, 0.707f);
        toneFilterRight.setCoefficients(Biquad::LOWPASS, freq, 0.707f);
    }
};

} // namespace dubtechno
