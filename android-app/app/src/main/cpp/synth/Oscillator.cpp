#include "Oscillator.h"
#include <cmath>

namespace dubtechno {

Oscillator::Oscillator()
    : phase_(0.0f)
    , phaseIncrement_(0.0f)
    , frequency_(440.0f)
    , waveform_(WaveformType::SINE)
    , pulseWidth_(0.5f)
    , subPhase_(0.0f)
    , subPhaseIncrement_(0.0f)
    , subOscillatorLevel_(0.0f)
    , subOscillatorOctaves_(1)
    , targetFrequency_(440.0f)
    , frequencySmooth_(0.001f) {
    setFrequency(440.0f);
}

void Oscillator::setFrequency(float hz) {
    targetFrequency_ = DSPUtils::clamp(hz, 20.0f, 20000.0f);
}

void Oscillator::setWaveform(WaveformType type) {
    waveform_ = type;
}

void Oscillator::setPulseWidth(float width) {
    pulseWidth_ = DSPUtils::clamp(width, 0.01f, 0.99f);
}

void Oscillator::setSubOscillatorLevel(float level) {
    subOscillatorLevel_ = DSPUtils::clamp(level, 0.0f, 1.0f);
}

void Oscillator::setSubOscillatorOctaves(int octaves) {
    subOscillatorOctaves_ = (octaves == 2) ? 2 : 1;
}

void Oscillator::reset() {
    phase_ = 0.0f;
    subPhase_ = 0.0f;
}

float Oscillator::process() {
    // Smooth frequency changes to avoid clicks
    frequency_ = DSPUtils::smooth(frequency_, targetFrequency_, frequencySmooth_);
    phaseIncrement_ = frequency_ / SAMPLE_RATE;
    subPhaseIncrement_ = phaseIncrement_ / (1 << subOscillatorOctaves_);

    // Generate main oscillator output
    float output = 0.0f;
    switch (waveform_) {
        case WaveformType::SINE:
            output = generateSine();
            break;
        case WaveformType::TRIANGLE:
            output = generateTriangle();
            break;
        case WaveformType::SAWTOOTH:
            output = generateSawtooth();
            break;
        case WaveformType::SQUARE:
            output = generateSquare();
            break;
        case WaveformType::PULSE:
            output = generatePulse();
            break;
    }

    // Add sub-oscillator (always sine wave, 1-2 octaves down)
    if (subOscillatorLevel_ > 0.0f) {
        float subOsc = std::sin(TWO_PI * subPhase_);
        output += subOsc * subOscillatorLevel_;

        // Normalize to prevent clipping
        output /= (1.0f + subOscillatorLevel_);
    }

    // Advance phases
    phase_ += phaseIncrement_;
    subPhase_ += subPhaseIncrement_;

    // Wrap phases
    if (phase_ >= 1.0f) phase_ -= 1.0f;
    if (subPhase_ >= 1.0f) subPhase_ -= 1.0f;

    return output;
}

// PolyBLEP (Polynomial Band-Limited Step)
// Reduces aliasing on sharp transitions
float Oscillator::polyBLEP(float t, float dt) {
    // t is the current phase position, dt is phase increment
    if (t < dt) {
        t = t / dt;
        return t + t - t * t - 1.0f;
    } else if (t > 1.0f - dt) {
        t = (t - 1.0f) / dt;
        return t * t + t + t + 1.0f;
    }
    return 0.0f;
}

float Oscillator::generateSine() {
    // Sine wave doesn't need anti-aliasing (already band-limited)
    return std::sin(TWO_PI * phase_);
}

float Oscillator::generateTriangle() {
    // Triangle wave with PolyBLEP
    float value = 0.0f;

    if (phase_ < 0.5f) {
        value = 4.0f * phase_ - 1.0f;
    } else {
        value = -4.0f * phase_ + 3.0f;
    }

    // PolyBLEP correction at discontinuities
    value += polyBLEP(phase_, phaseIncrement_);
    value -= polyBLEP(std::fmod(phase_ + 0.5f, 1.0f), phaseIncrement_);

    return value;
}

float Oscillator::generateSawtooth() {
    // Sawtooth wave with PolyBLEP
    float value = 2.0f * phase_ - 1.0f;

    // PolyBLEP correction at discontinuity
    value -= polyBLEP(phase_, phaseIncrement_);

    return value;
}

float Oscillator::generateSquare() {
    // Square wave (50% duty cycle pulse)
    float value = (phase_ < 0.5f) ? 1.0f : -1.0f;

    // PolyBLEP correction at both transitions
    value += polyBLEP(phase_, phaseIncrement_);
    value -= polyBLEP(std::fmod(phase_ + 0.5f, 1.0f), phaseIncrement_);

    return value;
}

float Oscillator::generatePulse() {
    // Variable width pulse wave with PolyBLEP
    float value = (phase_ < pulseWidth_) ? 1.0f : -1.0f;

    // PolyBLEP correction at both transitions
    value += polyBLEP(phase_, phaseIncrement_);
    value -= polyBLEP(std::fmod(phase_ + (1.0f - pulseWidth_), 1.0f), phaseIncrement_);

    return value;
}

} // namespace dubtechno
