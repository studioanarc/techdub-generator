#pragma once

#include "../dsp/DSPUtils.h"

namespace dubtechno {

/**
 * Filter types
 */
enum class FilterType {
    LOWPASS,
    HIGHPASS,
    BANDPASS,
    NOTCH
};

/**
 * Filter slopes (poles)
 */
enum class FilterSlope {
    SLOPE_12DB,  // 2-pole
    SLOPE_24DB   // 4-pole (two filters in series)
};

/**
 * Multi-mode State Variable Filter (SVF)
 *
 * Provides lowpass, highpass, bandpass, and notch filtering
 * with adjustable cutoff, resonance, and drive.
 * Stable at high resonance values.
 */
class Filter {
public:
    Filter();
    ~Filter() = default;

    // Configuration
    void setType(FilterType type);
    void setSlope(FilterSlope slope);
    void setCutoff(float hz);
    void setResonance(float q);  // 0.1 - 10.0
    void setDrive(float drive);   // 1.0 - 10.0 (pre-filter saturation)

    // Processing
    float process(float input);
    void reset();

    // Getters
    FilterType getType() const { return type_; }
    float getCutoff() const { return cutoff_; }
    float getResonance() const { return resonance_; }

private:
    void updateCoefficients();

    // SVF state variables (for first stage)
    float lowpass1_;
    float bandpass1_;
    float highpass1_;

    // SVF state variables (for second stage - 24dB slope)
    float lowpass2_;
    float bandpass2_;
    float highpass2_;

    // Parameters
    FilterType type_;
    FilterSlope slope_;
    float cutoff_;       // Hz
    float resonance_;    // Q factor
    float drive_;        // Pre-filter drive

    // Coefficients
    float f_;  // Frequency coefficient
    float q_;  // Damping coefficient

    // Smoothing
    float targetCutoff_;
    float cutoffSmooth_;
    float targetResonance_;
    float resonanceSmooth_;
};

} // namespace dubtechno
