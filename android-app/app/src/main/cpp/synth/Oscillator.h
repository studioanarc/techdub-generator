#pragma once

#include "../dsp/DSPUtils.h"

namespace dubtechno {

/**
 * Waveform types supported by the oscillator
 */
enum class WaveformType {
    SINE,
    TRIANGLE,
    SAWTOOTH,
    SQUARE,
    PULSE
};

/**
 * Anti-aliased Oscillator using PolyBLEP
 *
 * Generates various waveforms with band-limited synthesis to prevent aliasing.
 * Uses PolyBLEP (Polynomial Band-Limited Step) for sharp transitions.
 */
class Oscillator {
public:
    Oscillator();
    ~Oscillator() = default;

    // Configuration
    void setFrequency(float hz);
    void setWaveform(WaveformType type);
    void setPulseWidth(float width);  // 0.0 - 1.0, default 0.5
    void setSubOscillatorLevel(float level);  // 0.0 - 1.0
    void setSubOscillatorOctaves(int octaves);  // 1 or 2 octaves down

    // Processing
    float process();
    void reset();

    // Getters
    float getFrequency() const { return frequency_; }
    WaveformType getWaveform() const { return waveform_; }
    float getPulseWidth() const { return pulseWidth_; }

private:
    // PolyBLEP correction for band-limited synthesis
    float polyBLEP(float t, float dt);

    // Waveform generators (naive)
    float generateSine();
    float generateTriangle();
    float generateSawtooth();
    float generateSquare();
    float generatePulse();

    // State
    float phase_;           // Current phase [0, 1]
    float phaseIncrement_;  // Phase increment per sample
    float frequency_;       // Frequency in Hz
    WaveformType waveform_;
    float pulseWidth_;      // For pulse wave

    // Sub-oscillator
    float subPhase_;
    float subPhaseIncrement_;
    float subOscillatorLevel_;
    int subOscillatorOctaves_;

    // Smoothing
    float targetFrequency_;
    float frequencySmooth_;
};

} // namespace dubtechno
