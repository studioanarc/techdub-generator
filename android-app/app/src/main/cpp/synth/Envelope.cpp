#include "Envelope.h"
#include <cmath>

namespace dubtechno {

Envelope::Envelope()
    : stage_(EnvelopeStage::IDLE)
    , currentLevel_(0.0f)
    , attackTime_(0.01f)
    , decayTime_(0.1f)
    , sustainLevel_(0.7f)
    , releaseTime_(0.2f)
    , attackCoeff_(0.0f)
    , decayCoeff_(0.0f)
    , releaseCoeff_(0.0f)
    , attackTarget_(1.0f)
    , decayTarget_(0.7f)
    , releaseTarget_(0.0f) {
    updateCoefficients();
}

void Envelope::setAttack(float seconds) {
    attackTime_ = DSPUtils::clamp(seconds, MIN_TIME, MAX_TIME);
    updateCoefficients();
}

void Envelope::setDecay(float seconds) {
    decayTime_ = DSPUtils::clamp(seconds, MIN_TIME, MAX_TIME);
    updateCoefficients();
}

void Envelope::setSustain(float level) {
    sustainLevel_ = DSPUtils::clamp(level, 0.0f, 1.0f);
    decayTarget_ = sustainLevel_;
}

void Envelope::setRelease(float seconds) {
    releaseTime_ = DSPUtils::clamp(seconds, MIN_TIME, MAX_TIME);
    updateCoefficients();
}

void Envelope::updateCoefficients() {
    // Calculate one-pole filter coefficients for exponential curves
    // The coefficient determines how quickly we approach the target
    attackCoeff_ = DSPUtils::calculateOnePoleCoefficent(attackTime_);
    decayCoeff_ = DSPUtils::calculateOnePoleCoefficent(decayTime_);
    releaseCoeff_ = DSPUtils::calculateOnePoleCoefficent(releaseTime_);
}

void Envelope::noteOn() {
    stage_ = EnvelopeStage::ATTACK;
    // Don't reset currentLevel - this allows for smoother re-triggers
}

void Envelope::noteOff() {
    stage_ = EnvelopeStage::RELEASE;
}

void Envelope::reset() {
    stage_ = EnvelopeStage::IDLE;
    currentLevel_ = 0.0f;
}

float Envelope::process() {
    switch (stage_) {
        case EnvelopeStage::IDLE:
            currentLevel_ = 0.0f;
            break;

        case EnvelopeStage::ATTACK:
            // Exponential rise to 1.0
            currentLevel_ += attackCoeff_ * (attackTarget_ - currentLevel_);

            // Move to decay when we're close enough to the target
            if (currentLevel_ >= 0.99f) {
                currentLevel_ = 1.0f;
                stage_ = EnvelopeStage::DECAY;
            }
            break;

        case EnvelopeStage::DECAY:
            // Exponential decay to sustain level
            currentLevel_ += decayCoeff_ * (decayTarget_ - currentLevel_);

            // Move to sustain when we're close enough to the target
            if (std::abs(currentLevel_ - sustainLevel_) < 0.001f) {
                currentLevel_ = sustainLevel_;
                stage_ = EnvelopeStage::SUSTAIN;
            }
            break;

        case EnvelopeStage::SUSTAIN:
            // Hold at sustain level
            currentLevel_ = sustainLevel_;
            break;

        case EnvelopeStage::RELEASE:
            // Exponential decay to 0
            currentLevel_ += releaseCoeff_ * (releaseTarget_ - currentLevel_);

            // Move to idle when we're close enough to zero
            if (currentLevel_ < 0.001f) {
                currentLevel_ = 0.0f;
                stage_ = EnvelopeStage::IDLE;
            }
            break;
    }

    return currentLevel_;
}

} // namespace dubtechno
