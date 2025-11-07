#include "Filter.h"
#include <cmath>

namespace dubtechno {

Filter::Filter()
    : lowpass1_(0.0f)
    , bandpass1_(0.0f)
    , highpass1_(0.0f)
    , lowpass2_(0.0f)
    , bandpass2_(0.0f)
    , highpass2_(0.0f)
    , type_(FilterType::LOWPASS)
    , slope_(FilterSlope::SLOPE_24DB)
    , cutoff_(1000.0f)
    , resonance_(0.707f)
    , drive_(1.0f)
    , f_(0.0f)
    , q_(0.0f)
    , targetCutoff_(1000.0f)
    , cutoffSmooth_(0.01f)
    , targetResonance_(0.707f)
    , resonanceSmooth_(0.001f) {
    updateCoefficients();
}

void Filter::setType(FilterType type) {
    type_ = type;
}

void Filter::setSlope(FilterSlope slope) {
    slope_ = slope;
}

void Filter::setCutoff(float hz) {
    targetCutoff_ = DSPUtils::clamp(hz, 20.0f, 20000.0f);
}

void Filter::setResonance(float q) {
    targetResonance_ = DSPUtils::clamp(q, 0.1f, 10.0f);
}

void Filter::setDrive(float drive) {
    drive_ = DSPUtils::clamp(drive, 1.0f, 10.0f);
}

void Filter::reset() {
    lowpass1_ = 0.0f;
    bandpass1_ = 0.0f;
    highpass1_ = 0.0f;
    lowpass2_ = 0.0f;
    bandpass2_ = 0.0f;
    highpass2_ = 0.0f;
}

void Filter::updateCoefficients() {
    // Smooth parameter changes to avoid clicks
    cutoff_ = DSPUtils::smooth(cutoff_, targetCutoff_, cutoffSmooth_);
    resonance_ = DSPUtils::smooth(resonance_, targetResonance_, resonanceSmooth_);

    // Calculate SVF coefficients
    // f = 2 * sin(π * cutoff / sampleRate)
    float omega = PI * cutoff_ / SAMPLE_RATE;
    f_ = 2.0f * std::sin(omega);

    // Clamp f to prevent instability
    f_ = DSPUtils::clamp(f_, 0.0f, 1.0f);

    // q = 1 / resonance (damping)
    q_ = 1.0f / resonance_;

    // Limit q to prevent filter explosion
    q_ = DSPUtils::clamp(q_, 0.1f, 2.0f);
}

float Filter::process(float input) {
    // Update coefficients (with smoothing)
    updateCoefficients();

    // Apply drive (pre-filter saturation)
    if (drive_ > 1.0f) {
        input *= drive_;
        input = DSPUtils::softClip(input);
    }

    // First stage (2-pole)
    lowpass1_ += f_ * bandpass1_;
    highpass1_ = input - lowpass1_ - q_ * bandpass1_;
    bandpass1_ += f_ * highpass1_;

    // Clamp state variables to prevent denormals
    lowpass1_ = DSPUtils::clamp(lowpass1_, -2.0f, 2.0f);
    bandpass1_ = DSPUtils::clamp(bandpass1_, -2.0f, 2.0f);

    float output1 = 0.0f;
    switch (type_) {
        case FilterType::LOWPASS:
            output1 = lowpass1_;
            break;
        case FilterType::HIGHPASS:
            output1 = highpass1_;
            break;
        case FilterType::BANDPASS:
            output1 = bandpass1_;
            break;
        case FilterType::NOTCH:
            output1 = lowpass1_ + highpass1_;
            break;
    }

    // Second stage (for 24dB slope)
    if (slope_ == FilterSlope::SLOPE_24DB) {
        lowpass2_ += f_ * bandpass2_;
        highpass2_ = output1 - lowpass2_ - q_ * bandpass2_;
        bandpass2_ += f_ * highpass2_;

        // Clamp state variables
        lowpass2_ = DSPUtils::clamp(lowpass2_, -2.0f, 2.0f);
        bandpass2_ = DSPUtils::clamp(bandpass2_, -2.0f, 2.0f);

        float output2 = 0.0f;
        switch (type_) {
            case FilterType::LOWPASS:
                output2 = lowpass2_;
                break;
            case FilterType::HIGHPASS:
                output2 = highpass2_;
                break;
            case FilterType::BANDPASS:
                output2 = bandpass2_;
                break;
            case FilterType::NOTCH:
                output2 = lowpass2_ + highpass2_;
                break;
        }

        return output2;
    }

    return output1;
}

} // namespace dubtechno
