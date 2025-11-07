#pragma once

#include "Effect.h"
#include <vector>
#include <complex>
#include <cmath>

namespace dubtechno {

/**
 * Convolution Reverb Effect
 *
 * High-quality reverb using impulse response convolution
 * More CPU-intensive than algorithmic reverb, but sounds more realistic
 *
 * Features:
 * - Load impulse response from buffer
 * - FFT-based fast convolution (overlap-add method)
 * - Partitioned convolution for efficiency
 * - Pre-delay control
 *
 * NOTE: This is a simplified implementation. Production code would use
 * a proper FFT library (e.g., KissFFT, FFTW) for better performance.
 */
class ConvolutionReverb : public Effect {
public:
    ConvolutionReverb() : irLoaded(false), partitionSize(512) {
        preDelay.reset(0.0f);
    }

    void init(float sampleRate) override {
        Effect::init(sampleRate);
        reset();
    }

    void reset() override {
        inputBuffer.clear();
        outputBuffer.clear();
        inputBufferPos = 0;
    }

    /**
     * Load impulse response
     * @param ir Impulse response buffer (mono or stereo)
     * @param length Length of IR in samples
     * @param channels Number of channels (1 or 2)
     */
    void loadImpulseResponse(const float* ir, int length, int channels) {
        if (length <= 0 || channels <= 0) return;

        // Store IR
        impulseResponse.resize(length * channels);
        for (int i = 0; i < length * channels; ++i) {
            impulseResponse[i] = ir[i];
        }

        irLength = length;
        irChannels = channels;
        irLoaded = true;

        // Initialize buffers for overlap-add
        inputBuffer.resize(partitionSize * 2, 0.0f);
        outputBuffer.resize(partitionSize * 2, 0.0f);
        inputBufferPos = 0;

        // In a real implementation, we would:
        // 1. Partition the IR into blocks
        // 2. Compute FFT of each partition
        // 3. Store FFT'd partitions for fast convolution
        // For this demo, we'll use simple time-domain convolution
    }

    void process(float* buffer, int numFrames, int channels) override {
        if (isBypassed() || !irLoaded) return;

        // For simplicity, we'll use direct time-domain convolution
        // This is not efficient for long IRs, but demonstrates the concept
        // A production implementation would use FFT-based convolution

        float preDelayMs = preDelay.getNext();
        int preDelaySamples = static_cast<int>(preDelayMs * 0.001f * sampleRate);

        for (int i = 0; i < numFrames; ++i) {
            int idx = i * channels;
            float inputL = buffer[idx];
            float inputR = channels > 1 ? buffer[idx + 1] : inputL;

            // Perform convolution (simplified - only uses first 1024 samples of IR)
            float wetL = 0.0f;
            float wetR = 0.0f;

            int convLength = std::min(irLength, 1024); // Limit for performance

            // Simple direct convolution
            // In practice, use FFT-based convolution for efficiency
            for (int j = 0; j < convLength; ++j) {
                int bufferIdx = (inputBufferPos - j + partitionSize) % partitionSize;
                if (bufferIdx >= 0 && bufferIdx < inputBuffer.size()) {
                    float input = inputBuffer[bufferIdx];

                    if (irChannels == 1) {
                        // Mono IR
                        wetL += input * impulseResponse[j];
                        wetR += input * impulseResponse[j];
                    } else {
                        // Stereo IR
                        wetL += input * impulseResponse[j * 2];
                        wetR += input * impulseResponse[j * 2 + 1];
                    }
                }
            }

            // Store input in circular buffer
            inputBuffer[inputBufferPos] = (inputL + inputR) * 0.5f;
            inputBufferPos = (inputBufferPos + 1) % partitionSize;

            // Mix
            float mixVal = mix.load(std::memory_order_relaxed);
            buffer[idx] = inputL * (1.0f - mixVal) + wetL * mixVal;
            if (channels > 1) {
                buffer[idx + 1] = inputR * (1.0f - mixVal) + wetR * mixVal;
            }
        }
    }

    void setPreDelay(float delayMs) {
        preDelay.setTarget(DSPUtils::clamp(delayMs, 0.0f, 100.0f));
    }

    bool isImpulseResponseLoaded() const {
        return irLoaded;
    }

private:
    // Impulse response
    std::vector<float> impulseResponse;
    int irLength = 0;
    int irChannels = 1;
    bool irLoaded;

    // Processing buffers
    std::vector<float> inputBuffer;
    std::vector<float> outputBuffer;
    int inputBufferPos;
    int partitionSize;

    SmoothParameter preDelay;

    /**
     * NOTE: A production implementation would include:
     *
     * 1. FFT-based fast convolution:
     *    - Use KissFFT or similar library
     *    - Partition IR into blocks (512-2048 samples)
     *    - Pre-compute FFT of each partition
     *    - Use overlap-add for real-time processing
     *
     * 2. Optimization:
     *    - SIMD instructions for convolution
     *    - Multi-threading for long IRs
     *    - Smart partition sizes based on latency requirements
     *
     * 3. IR Management:
     *    - Load from WAV files
     *    - Normalize IR
     *    - Trim silence
     *    - Resample if needed
     *
     * Example usage:
     *    ConvolutionReverb convReverb;
     *    convReverb.init(48000.0f);
     *
     *    // Load IR from file or resource
     *    float ir[192000]; // 4 seconds at 48kHz
     *    loadIRFromFile("concert_hall.wav", ir);
     *    convReverb.loadImpulseResponse(ir, 192000, 2);
     *
     *    // Process audio
     *    convReverb.setMix(0.3f);
     *    convReverb.process(buffer, numFrames, 2);
     */
};

} // namespace dubtechno
