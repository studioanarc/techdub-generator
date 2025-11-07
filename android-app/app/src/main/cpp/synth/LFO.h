#pragma once

#include "../dsp/DSPUtils.h"
#include <random>

namespace dubtechno {

/**
 * LFO waveform types
 */
enum class LFOWaveform {
    SINE,
    TRIANGLE,
    SAWTOOTH,
    SQUARE,
    RANDOM  // Sample & Hold
};

/**
 * Low Frequency Oscillator (LFO)
 *
 * Generates slow-moving modulation signals for filter cutoff,
 * pitch, amplitude, etc. Frequencies typically 0.01 Hz - 40 Hz.
 */
class LFO {
public:
    LFO();
    ~LFO() = default;

    // Configuration
    void setRate(float hz);              // 0.01 - 40 Hz
    void setWaveform(LFOWaveform type);
    void setPhase(float phase);          // 0.0 - 1.0 (initial phase offset)
    void setDepth(float depth);          // 0.0 - 1.0 (modulation amount)

    // Processing
    float process();                     // Returns -1.0 to 1.0
    void reset();

    // Getters
    float getRate() const { return rate_; }
    LFOWaveform getWaveform() const { return waveform_; }
    float getDepth() const { return depth_; }

private:
    float generateSine();
    float generateTriangle();
    float generateSawtooth();
    float generateSquare();
    float generateRandom();

    float phase_;
    float phaseIncrement_;
    float rate_;
    LFOWaveform waveform_;
    float depth_;

    // For random waveform (sample & hold)
    float lastRandomValue_;
    float nextRandomValue_;
    std::mt19937 randomGenerator_;
    std::uniform_real_distribution<float> randomDist_;
};

} // namespace dubtechno
