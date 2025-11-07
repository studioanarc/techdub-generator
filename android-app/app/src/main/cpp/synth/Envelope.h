#pragma once

#include "../dsp/DSPUtils.h"

namespace dubtechno {

/**
 * Envelope stages
 */
enum class EnvelopeStage {
    IDLE,
    ATTACK,
    DECAY,
    SUSTAIN,
    RELEASE
};

/**
 * ADSR Envelope Generator
 *
 * Generates envelope shapes with exponential curves for more musical results.
 * Attack, Decay, and Release use exponential curves.
 * Sustain is a constant level.
 */
class Envelope {
public:
    Envelope();
    ~Envelope() = default;

    // Configuration
    void setAttack(float seconds);
    void setDecay(float seconds);
    void setSustain(float level);    // 0.0 - 1.0
    void setRelease(float seconds);

    // Trigger
    void noteOn();
    void noteOff();
    void reset();

    // Processing
    float process();

    // State
    bool isActive() const { return stage_ != EnvelopeStage::IDLE; }
    EnvelopeStage getStage() const { return stage_; }
    float getCurrentLevel() const { return currentLevel_; }

private:
    void updateCoefficients();

    EnvelopeStage stage_;
    float currentLevel_;

    // ADSR parameters (in seconds)
    float attackTime_;
    float decayTime_;
    float sustainLevel_;
    float releaseTime_;

    // Coefficients (calculated from time)
    float attackCoeff_;
    float decayCoeff_;
    float releaseCoeff_;

    // Target levels for each stage
    float attackTarget_;
    float decayTarget_;
    float releaseTarget_;

    // Minimum time to prevent division by zero
    static constexpr float MIN_TIME = 0.001f;  // 1ms
    static constexpr float MAX_TIME = 10.0f;   // 10 seconds
};

} // namespace dubtechno
