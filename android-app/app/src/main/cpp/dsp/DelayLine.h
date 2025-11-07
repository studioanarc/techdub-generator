#pragma once

#include <vector>
#include <cstring>
#include "DSPUtils.h"

namespace dubtechno {

/**
 * Circular buffer delay line with interpolation
 *
 * Features:
 * - Variable delay time with linear interpolation
 * - Feedback loop support
 * - Maximum delay time: 5 seconds
 */
class DelayLine {
public:
    DelayLine() : writeIndex(0), bufferSize(0) {}

    /**
     * Initialize delay line
     * @param maxDelaySeconds Maximum delay time in seconds
     * @param sampleRate Sample rate in Hz
     */
    void init(float maxDelaySeconds, float sampleRate) {
        bufferSize = static_cast<int>(maxDelaySeconds * sampleRate) + 1;
        buffer.resize(bufferSize, 0.0f);
        writeIndex = 0;
    }

    /**
     * Write a sample to the delay line
     */
    void write(float sample) {
        buffer[writeIndex] = sample;
        writeIndex = (writeIndex + 1) % bufferSize;
    }

    /**
     * Read from delay line with linear interpolation
     * @param delayInSamples Delay time in samples (can be fractional)
     * @return Interpolated sample
     */
    float read(float delayInSamples) const {
        // Clamp delay to valid range
        delayInSamples = DSPUtils::clamp(delayInSamples, 0.0f,
                                         static_cast<float>(bufferSize - 1));

        // Calculate read position
        float readPos = writeIndex - delayInSamples;
        if (readPos < 0.0f) {
            readPos += bufferSize;
        }

        // Integer and fractional parts
        int index1 = static_cast<int>(readPos);
        int index2 = (index1 + 1) % bufferSize;
        float frac = readPos - index1;

        // Linear interpolation
        return DSPUtils::lerp(buffer[index1], buffer[index2], frac);
    }

    /**
     * Read and write in one operation (for feedback loops)
     * @param input Input sample
     * @param delayInSamples Delay time in samples
     * @param feedback Feedback amount (0.0 - 0.95)
     * @return Delayed sample
     */
    float readAndWrite(float input, float delayInSamples, float feedback = 0.0f) {
        float delayed = read(delayInSamples);
        write(input + delayed * feedback);
        return delayed;
    }

    /**
     * Tap at a specific delay time (read without affecting write position)
     */
    float tap(float delayInSamples) const {
        return read(delayInSamples);
    }

    /**
     * Clear the delay buffer
     */
    void clear() {
        std::fill(buffer.begin(), buffer.end(), 0.0f);
        writeIndex = 0;
    }

    int getBufferSize() const {
        return bufferSize;
    }

private:
    std::vector<float> buffer;
    int writeIndex;
    int bufferSize;
};

} // namespace dubtechno
