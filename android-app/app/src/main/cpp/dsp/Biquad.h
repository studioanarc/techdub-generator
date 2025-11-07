#pragma once

#include <cmath>
#include "DSPUtils.h"

namespace dubtechno {

/**
 * Biquad filter using standard cookbook formulas
 *
 * Supports all common filter types:
 * - Lowpass, Highpass, Bandpass, Notch
 * - Peaking EQ, Low Shelf, High Shelf
 * - All-pass
 */
class Biquad {
public:
    enum FilterType {
        LOWPASS,
        HIGHPASS,
        BANDPASS,
        NOTCH,
        PEAK,
        LOWSHELF,
        HIGHSHELF,
        ALLPASS
    };

    Biquad() : sampleRate(SAMPLE_RATE) {
        reset();
    }

    void init(float sampleRate) {
        this->sampleRate = sampleRate;
        reset();
    }

    /**
     * Configure filter coefficients
     * @param type Filter type
     * @param frequency Cutoff/center frequency in Hz
     * @param q Q factor (resonance) - typically 0.5 to 10.0
     * @param gain Gain in dB (for peak/shelf filters)
     */
    void setCoefficients(FilterType type, float frequency, float q = 0.707f, float gain = 0.0f) {
        float w0 = TWO_PI * frequency / sampleRate;
        float cos_w0 = std::cos(w0);
        float sin_w0 = std::sin(w0);
        float alpha = sin_w0 / (2.0f * q);
        float A = std::pow(10.0f, gain / 40.0f); // For shelf/peak filters

        float a0, a1, a2, b0, b1, b2;

        switch (type) {
            case LOWPASS:
                b0 = (1.0f - cos_w0) / 2.0f;
                b1 = 1.0f - cos_w0;
                b2 = (1.0f - cos_w0) / 2.0f;
                a0 = 1.0f + alpha;
                a1 = -2.0f * cos_w0;
                a2 = 1.0f - alpha;
                break;

            case HIGHPASS:
                b0 = (1.0f + cos_w0) / 2.0f;
                b1 = -(1.0f + cos_w0);
                b2 = (1.0f + cos_w0) / 2.0f;
                a0 = 1.0f + alpha;
                a1 = -2.0f * cos_w0;
                a2 = 1.0f - alpha;
                break;

            case BANDPASS:
                b0 = alpha;
                b1 = 0.0f;
                b2 = -alpha;
                a0 = 1.0f + alpha;
                a1 = -2.0f * cos_w0;
                a2 = 1.0f - alpha;
                break;

            case NOTCH:
                b0 = 1.0f;
                b1 = -2.0f * cos_w0;
                b2 = 1.0f;
                a0 = 1.0f + alpha;
                a1 = -2.0f * cos_w0;
                a2 = 1.0f - alpha;
                break;

            case PEAK:
                b0 = 1.0f + alpha * A;
                b1 = -2.0f * cos_w0;
                b2 = 1.0f - alpha * A;
                a0 = 1.0f + alpha / A;
                a1 = -2.0f * cos_w0;
                a2 = 1.0f - alpha / A;
                break;

            case LOWSHELF: {
                float S = 1.0f; // Shelf slope
                float beta = std::sqrt(A) / q;
                b0 = A * ((A + 1.0f) - (A - 1.0f) * cos_w0 + beta * sin_w0);
                b1 = 2.0f * A * ((A - 1.0f) - (A + 1.0f) * cos_w0);
                b2 = A * ((A + 1.0f) - (A - 1.0f) * cos_w0 - beta * sin_w0);
                a0 = (A + 1.0f) + (A - 1.0f) * cos_w0 + beta * sin_w0;
                a1 = -2.0f * ((A - 1.0f) + (A + 1.0f) * cos_w0);
                a2 = (A + 1.0f) + (A - 1.0f) * cos_w0 - beta * sin_w0;
                break;
            }

            case HIGHSHELF: {
                float beta = std::sqrt(A) / q;
                b0 = A * ((A + 1.0f) + (A - 1.0f) * cos_w0 + beta * sin_w0);
                b1 = -2.0f * A * ((A - 1.0f) + (A + 1.0f) * cos_w0);
                b2 = A * ((A + 1.0f) + (A - 1.0f) * cos_w0 - beta * sin_w0);
                a0 = (A + 1.0f) - (A - 1.0f) * cos_w0 + beta * sin_w0;
                a1 = 2.0f * ((A - 1.0f) - (A + 1.0f) * cos_w0);
                a2 = (A + 1.0f) - (A - 1.0f) * cos_w0 - beta * sin_w0;
                break;
            }

            case ALLPASS:
                b0 = 1.0f - alpha;
                b1 = -2.0f * cos_w0;
                b2 = 1.0f + alpha;
                a0 = 1.0f + alpha;
                a1 = -2.0f * cos_w0;
                a2 = 1.0f - alpha;
                break;

            default:
                // Unity gain (bypass)
                b0 = 1.0f; b1 = 0.0f; b2 = 0.0f;
                a0 = 1.0f; a1 = 0.0f; a2 = 0.0f;
                break;
        }

        // Normalize coefficients
        this->b0 = b0 / a0;
        this->b1 = b1 / a0;
        this->b2 = b2 / a0;
        this->a1 = a1 / a0;
        this->a2 = a2 / a0;
    }

    /**
     * Process a single sample
     */
    float process(float input) {
        // Direct Form 2 (transposed)
        float output = b0 * input + z1;
        z1 = b1 * input - a1 * output + z2;
        z2 = b2 * input - a2 * output;

        // Prevent denormals
        if (std::abs(z1) < 1e-25f) z1 = 0.0f;
        if (std::abs(z2) < 1e-25f) z2 = 0.0f;

        return output;
    }

    /**
     * Reset filter state
     */
    void reset() {
        z1 = z2 = 0.0f;
        b0 = 1.0f; b1 = 0.0f; b2 = 0.0f;
        a1 = 0.0f; a2 = 0.0f;
    }

private:
    float sampleRate;

    // Filter coefficients
    float b0, b1, b2; // Numerator
    float a1, a2;     // Denominator (a0 is normalized to 1)

    // State variables (Direct Form 2)
    float z1, z2;
};

} // namespace dubtechno
