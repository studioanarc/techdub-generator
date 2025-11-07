#pragma once

#include <vector>
#include "DSPUtils.h"

namespace dubtechno {

/**
 * All-pass filter for reverb and phaser effects
 *
 * Preserves amplitude but changes phase relationship
 * Used extensively in Freeverb and Schroeder reverbs
 */
class AllPassFilter {
public:
    AllPassFilter() : bufferIndex(0), bufferSize(0) {}

    /**
     * Initialize all-pass filter
     * @param delaySamples Delay length in samples
     * @param coefficient Feedback/feedforward coefficient (-1.0 to 1.0)
     */
    void init(int delaySamples, float coefficient = 0.5f) {
        bufferSize = delaySamples;
        buffer.resize(bufferSize, 0.0f);
        bufferIndex = 0;
        this->coefficient = coefficient;
    }

    /**
     * Process a single sample
     */
    float process(float input) {
        if (bufferSize == 0) return input;

        // Read from delay buffer
        float delayed = buffer[bufferIndex];

        // All-pass formula: y[n] = -x[n] + x[n-D] + g * y[n-D]
        float output = -input + delayed + coefficient * (input - coefficient * delayed);

        // Store in delay buffer
        buffer[bufferIndex] = input + coefficient * delayed;

        // Advance buffer index
        bufferIndex = (bufferIndex + 1) % bufferSize;

        // Prevent denormals
        if (std::abs(output) < 1e-25f) output = 0.0f;

        return output;
    }

    /**
     * Set feedback coefficient
     */
    void setCoefficient(float coef) {
        coefficient = DSPUtils::clamp(coef, -0.99f, 0.99f);
    }

    /**
     * Clear buffer
     */
    void clear() {
        std::fill(buffer.begin(), buffer.end(), 0.0f);
        bufferIndex = 0;
    }

private:
    std::vector<float> buffer;
    int bufferIndex;
    int bufferSize;
    float coefficient;
};

} // namespace dubtechno
