#include "LFO.h"
#include <cmath>

namespace dubtechno {

LFO::LFO()
    : phase_(0.0f)
    , phaseIncrement_(0.0f)
    , rate_(1.0f)
    , waveform_(LFOWaveform::SINE)
    , depth_(1.0f)
    , lastRandomValue_(0.0f)
    , nextRandomValue_(0.0f)
    , randomGenerator_(std::random_device{}())
    , randomDist_(-1.0f, 1.0f) {
    setRate(1.0f);
    nextRandomValue_ = randomDist_(randomGenerator_);
}

void LFO::setRate(float hz) {
    rate_ = DSPUtils::clamp(hz, 0.01f, 40.0f);
    phaseIncrement_ = rate_ / SAMPLE_RATE;
}

void LFO::setWaveform(LFOWaveform type) {
    waveform_ = type;
}

void LFO::setPhase(float phase) {
    phase_ = DSPUtils::clamp(phase, 0.0f, 1.0f);
}

void LFO::setDepth(float depth) {
    depth_ = DSPUtils::clamp(depth, 0.0f, 1.0f);
}

void LFO::reset() {
    phase_ = 0.0f;
    lastRandomValue_ = 0.0f;
    nextRandomValue_ = randomDist_(randomGenerator_);
}

float LFO::process() {
    float output = 0.0f;

    switch (waveform_) {
        case LFOWaveform::SINE:
            output = generateSine();
            break;
        case LFOWaveform::TRIANGLE:
            output = generateTriangle();
            break;
        case LFOWaveform::SAWTOOTH:
            output = generateSawtooth();
            break;
        case LFOWaveform::SQUARE:
            output = generateSquare();
            break;
        case LFOWaveform::RANDOM:
            output = generateRandom();
            break;
    }

    // Apply depth
    output *= depth_;

    // Advance phase
    phase_ += phaseIncrement_;
    if (phase_ >= 1.0f) {
        phase_ -= 1.0f;

        // Update random values on phase wrap
        if (waveform_ == LFOWaveform::RANDOM) {
            lastRandomValue_ = nextRandomValue_;
            nextRandomValue_ = randomDist_(randomGenerator_);
        }
    }

    return output;
}

float LFO::generateSine() {
    return std::sin(TWO_PI * phase_);
}

float LFO::generateTriangle() {
    if (phase_ < 0.5f) {
        return 4.0f * phase_ - 1.0f;
    } else {
        return -4.0f * phase_ + 3.0f;
    }
}

float LFO::generateSawtooth() {
    return 2.0f * phase_ - 1.0f;
}

float LFO::generateSquare() {
    return (phase_ < 0.5f) ? 1.0f : -1.0f;
}

float LFO::generateRandom() {
    // Sample & hold - interpolate between random values
    // for smoother transitions
    float t = phase_;
    return DSPUtils::lerp(lastRandomValue_, nextRandomValue_, t);
}

} // namespace dubtechno
