#pragma once

#include "Oscillator.h"
#include "Filter.h"
#include "Envelope.h"
#include "LFO.h"
#include "../dsp/DSPUtils.h"

namespace dubtechno {

/**
 * Synthesizer Voice
 *
 * Complete voice with 3 oscillators, filter, 2 envelopes, and 2 LFOs.
 * Signal flow:
 *   Oscillators → Mix → Filter → Amp Envelope → Output
 * Modulation routing:
 *   Filter Env → Filter Cutoff
 *   LFO 1 → Filter Cutoff
 *   LFO 2 → Oscillator Pitch (vibrato)
 */
class Voice {
public:
    Voice();
    ~Voice() = default;

    // Voice control
    void noteOn(int midiNote, float velocity);
    void noteOff();
    bool isActive() const;

    // Processing
    float process();
    void reset();

    // Oscillator configuration
    void setOsc1Waveform(WaveformType type) { osc1_.setWaveform(type); }
    void setOsc2Waveform(WaveformType type) { osc2_.setWaveform(type); }
    void setOsc1Level(float level) { osc1Level_ = DSPUtils::clamp(level, 0.0f, 1.0f); }
    void setOsc2Level(float level) { osc2Level_ = DSPUtils::clamp(level, 0.0f, 1.0f); }
    void setOsc2Detune(float cents) { osc2Detune_ = cents; }  // -100 to +100 cents
    void setSubOscillatorLevel(float level) { osc1_.setSubOscillatorLevel(level); }

    // Filter configuration
    void setFilterType(FilterType type) { filter_.setType(type); }
    void setFilterCutoff(float hz) { filterCutoff_ = hz; }
    void setFilterResonance(float q) { filter_.setResonance(q); }
    void setFilterEnvAmount(float amount) { filterEnvAmount_ = amount; }
    void setFilterDrive(float drive) { filter_.setDrive(drive); }

    // Envelope configuration
    void setAmpAttack(float seconds) { ampEnvelope_.setAttack(seconds); }
    void setAmpDecay(float seconds) { ampEnvelope_.setDecay(seconds); }
    void setAmpSustain(float level) { ampEnvelope_.setSustain(level); }
    void setAmpRelease(float seconds) { ampEnvelope_.setRelease(seconds); }

    void setFilterAttack(float seconds) { filterEnvelope_.setAttack(seconds); }
    void setFilterDecay(float seconds) { filterEnvelope_.setDecay(seconds); }
    void setFilterSustain(float level) { filterEnvelope_.setSustain(level); }
    void setFilterRelease(float seconds) { filterEnvelope_.setRelease(seconds); }

    // LFO configuration
    void setLFO1Rate(float hz) { lfo1_.setRate(hz); }
    void setLFO1Waveform(LFOWaveform type) { lfo1_.setWaveform(type); }
    void setLFO1ToFilterAmount(float amount) { lfo1ToFilterAmount_ = amount; }

    void setLFO2Rate(float hz) { lfo2_.setRate(hz); }
    void setLFO2Waveform(LFOWaveform type) { lfo2_.setWaveform(type); }
    void setLFO2ToPitchAmount(float amount) { lfo2ToPitchAmount_ = amount; }

    // Getters
    int getMidiNote() const { return midiNote_; }
    float getVelocity() const { return velocity_; }

private:
    // Oscillators
    Oscillator osc1_;
    Oscillator osc2_;
    float osc1Level_;
    float osc2Level_;
    float osc2Detune_;  // In cents

    // Filter
    Filter filter_;
    float filterCutoff_;
    float filterEnvAmount_;

    // Envelopes
    Envelope ampEnvelope_;
    Envelope filterEnvelope_;

    // LFOs
    LFO lfo1_;  // Typically for filter
    LFO lfo2_;  // Typically for pitch
    float lfo1ToFilterAmount_;
    float lfo2ToPitchAmount_;

    // Voice state
    int midiNote_;
    float velocity_;
    bool isActive_;
};

} // namespace dubtechno
