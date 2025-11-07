#include "Voice.h"
#include <cmath>

namespace dubtechno {

Voice::Voice()
    : osc1Level_(0.5f)
    , osc2Level_(0.5f)
    , osc2Detune_(0.0f)
    , filterCutoff_(1000.0f)
    , filterEnvAmount_(0.0f)
    , lfo1ToFilterAmount_(0.0f)
    , lfo2ToPitchAmount_(0.0f)
    , midiNote_(0)
    , velocity_(0.0f)
    , isActive_(false) {

    // Default settings for dub techno
    osc1_.setWaveform(WaveformType::SINE);
    osc2_.setWaveform(WaveformType::SAWTOOTH);

    filter_.setType(FilterType::LOWPASS);
    filter_.setSlope(FilterSlope::SLOPE_24DB);
    filter_.setCutoff(1000.0f);
    filter_.setResonance(0.707f);

    // Default envelope settings
    ampEnvelope_.setAttack(0.01f);
    ampEnvelope_.setDecay(0.3f);
    ampEnvelope_.setSustain(0.7f);
    ampEnvelope_.setRelease(0.5f);

    filterEnvelope_.setAttack(0.05f);
    filterEnvelope_.setDecay(0.5f);
    filterEnvelope_.setSustain(0.3f);
    filterEnvelope_.setRelease(0.3f);

    // LFO defaults
    lfo1_.setRate(0.5f);
    lfo1_.setWaveform(LFOWaveform::SINE);

    lfo2_.setRate(5.0f);
    lfo2_.setWaveform(LFOWaveform::SINE);
}

void Voice::noteOn(int midiNote, float velocity) {
    midiNote_ = midiNote;
    velocity_ = DSPUtils::clamp(velocity, 0.0f, 1.0f);
    isActive_ = true;

    // Calculate base frequency
    float baseFreq = DSPUtils::midiNoteToFrequency(midiNote);

    // Set oscillator frequencies
    osc1_.setFrequency(baseFreq);

    // Osc2 with detune
    float detuneRatio = std::pow(2.0f, osc2Detune_ / 1200.0f);  // cents to ratio
    osc2_.setFrequency(baseFreq * detuneRatio);

    // Trigger envelopes
    ampEnvelope_.noteOn();
    filterEnvelope_.noteOn();
}

void Voice::noteOff() {
    ampEnvelope_.noteOff();
    filterEnvelope_.noteOff();
}

bool Voice::isActive() const {
    return isActive_ && ampEnvelope_.isActive();
}

void Voice::reset() {
    osc1_.reset();
    osc2_.reset();
    filter_.reset();
    ampEnvelope_.reset();
    filterEnvelope_.reset();
    lfo1_.reset();
    lfo2_.reset();
    isActive_ = false;
}

float Voice::process() {
    if (!isActive()) {
        return 0.0f;
    }

    // Process LFOs
    float lfo1Value = lfo1_.process();
    float lfo2Value = lfo2_.process();

    // Apply pitch modulation from LFO2 (vibrato)
    if (lfo2ToPitchAmount_ > 0.0f) {
        float baseFreq = DSPUtils::midiNoteToFrequency(midiNote_);
        float pitchMod = lfo2Value * lfo2ToPitchAmount_;  // +/- semitones
        float modFreq = baseFreq * std::pow(2.0f, pitchMod / 12.0f);

        osc1_.setFrequency(modFreq);

        float detuneRatio = std::pow(2.0f, osc2Detune_ / 1200.0f);
        osc2_.setFrequency(modFreq * detuneRatio);
    }

    // Generate oscillator outputs
    float osc1Out = osc1_.process() * osc1Level_;
    float osc2Out = osc2_.process() * osc2Level_;

    // Mix oscillators
    float oscMix = (osc1Out + osc2Out) / (osc1Level_ + osc2Level_ + 0.001f);

    // Apply velocity
    oscMix *= (0.5f + 0.5f * velocity_);  // Velocity sensitivity

    // Process filter envelope
    float filterEnv = filterEnvelope_.process();

    // Calculate filter cutoff with modulation
    float modulatedCutoff = filterCutoff_;

    // Add filter envelope modulation
    if (filterEnvAmount_ > 0.0f) {
        modulatedCutoff += filterEnv * filterEnvAmount_ * 5000.0f;  // Up to 5kHz modulation
    }

    // Add LFO1 modulation to filter
    if (lfo1ToFilterAmount_ > 0.0f) {
        modulatedCutoff += lfo1Value * lfo1ToFilterAmount_ * 2000.0f;  // Up to 2kHz modulation
    }

    // Clamp cutoff to valid range
    modulatedCutoff = DSPUtils::clamp(modulatedCutoff, 20.0f, 20000.0f);

    // Apply filter cutoff modulation
    filter_.setCutoff(modulatedCutoff);

    // Process filter
    float filtered = filter_.process(oscMix);

    // Process amplitude envelope
    float ampEnv = ampEnvelope_.process();

    // Apply amplitude envelope
    float output = filtered * ampEnv;

    // Update activity state
    if (!ampEnvelope_.isActive()) {
        isActive_ = false;
    }

    return output;
}

} // namespace dubtechno
