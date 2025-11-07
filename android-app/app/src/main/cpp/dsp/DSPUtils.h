#pragma once

#include <cmath>
#include <algorithm>

namespace dubtechno {

// Constants
constexpr float SAMPLE_RATE = 48000.0f;
constexpr float PI = 3.14159265358979323846f;
constexpr float TWO_PI = 6.28318530717958647692f;
constexpr float HALF_PI = 1.57079632679489661923f;

/**
 * DSP Utility Functions for Audio Synthesis
 * Provides fast approximations and common conversions
 */
class DSPUtils {
public:
    // MIDI Note Conversion
    static inline float midiNoteToFrequency(int midiNote) {
        return 440.0f * std::pow(2.0f, (midiNote - 69) / 12.0f);
    }

    static inline int frequencyToMidiNote(float frequency) {
        return static_cast<int>(std::round(69.0f + 12.0f * std::log2(frequency / 440.0f)));
    }

    // Fast sine approximation using Bhaskara I's formula
    // Accurate to ~0.001 for most audio applications
    static inline float fastSin(float x) {
        // Normalize to [-PI, PI]
        while (x > PI) x -= TWO_PI;
        while (x < -PI) x += TWO_PI;

        // Bhaskara I approximation
        const float x2 = x * x;
        const float numerator = 16.0f * x * (PI - std::abs(x));
        const float denominator = 5.0f * PI * PI - 4.0f * std::abs(x) * (PI - std::abs(x));
        return numerator / denominator;
    }

    // Fast cosine using sine
    static inline float fastCos(float x) {
        return fastSin(x + HALF_PI);
    }

    // Linear interpolation
    static inline float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    // Clamp value between min and max
    static inline float clamp(float value, float min, float max) {
        return std::min(std::max(value, min), max);
    }

    // Soft clipping (tanh-like saturation)
    static inline float softClip(float x) {
        if (x >= 1.0f) return 1.0f;
        if (x <= -1.0f) return -1.0f;
        return x - (x * x * x) / 3.0f;
    }

    // Hard clipping
    static inline float hardClip(float x) {
        return clamp(x, -1.0f, 1.0f);
    }

    // dB to linear amplitude conversion
    static inline float dbToLinear(float db) {
        return std::pow(10.0f, db / 20.0f);
    }

    // Linear amplitude to dB conversion
    static inline float linearToDb(float linear) {
        return 20.0f * std::log10(std::max(linear, 1e-6f));
    }

    // Exponential envelope curve
    static inline float exponentialCurve(float linear, float curve = 4.0f) {
        return (std::exp(linear * curve) - 1.0f) / (std::exp(curve) - 1.0f);
    }

    // Smooth parameter changes (one-pole filter)
    static inline float smooth(float current, float target, float smoothing) {
        return current + smoothing * (target - current);
    }

    // Calculate one-pole coefficient from time constant
    static inline float calculateOnePoleCoefficent(float timeInSeconds) {
        return 1.0f - std::exp(-1.0f / (timeInSeconds * SAMPLE_RATE));
    }

    // Wrap phase to [0, 1]
    static inline float wrapPhase(float phase) {
        while (phase >= 1.0f) phase -= 1.0f;
        while (phase < 0.0f) phase += 1.0f;
        return phase;
    }
};

} // namespace dubtechno
