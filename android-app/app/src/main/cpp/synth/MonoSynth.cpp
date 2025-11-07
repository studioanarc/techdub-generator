#include "MonoSynth.h"
#include <cmath>

namespace dubtechno {

MonoSynth::MonoSynth()
    : portamentoTime_(0.0f)
    , currentFrequency_(0.0f)
    , targetFrequency_(0.0f)
    , portamentoCoeff_(1.0f)
    , legatoMode_(false)
    , lastMidiNote_(-1) {

    // Configure for bass sounds
    voice_.setOsc1Waveform(WaveformType::SINE);
    voice_.setOsc2Waveform(WaveformType::SINE);
    voice_.setOsc1Level(0.7f);
    voice_.setOsc2Level(0.3f);
    voice_.setOsc2Detune(-12.0f);  // One octave down

    // Prominent sub-oscillator for deep bass
    voice_.setSubOscillatorLevel(0.5f);

    // Filter settings for bass
    voice_.setFilterType(FilterType::LOWPASS);
    voice_.setFilterCutoff(800.0f);
    voice_.setFilterResonance(1.5f);
    voice_.setFilterEnvAmount(0.3f);

    // Punchy envelope
    voice_.setAmpAttack(0.001f);   // Very fast attack
    voice_.setAmpDecay(0.2f);
    voice_.setAmpSustain(0.7f);
    voice_.setAmpRelease(0.3f);

    // Filter envelope
    voice_.setFilterAttack(0.01f);
    voice_.setFilterDecay(0.3f);
    voice_.setFilterSustain(0.3f);
    voice_.setFilterRelease(0.2f);
}

void MonoSynth::setPortamento(float seconds) {
    portamentoTime_ = DSPUtils::clamp(seconds, 0.0f, 2.0f);
    updatePortamento();
}

void MonoSynth::setLegatoMode(bool enabled) {
    legatoMode_ = enabled;
}

void MonoSynth::updatePortamento() {
    if (portamentoTime_ > 0.001f) {
        portamentoCoeff_ = DSPUtils::calculateOnePoleCoefficent(portamentoTime_);
    } else {
        portamentoCoeff_ = 1.0f;  // Instant change
    }
}

void MonoSynth::playNote(int midiNote, float velocity) {
    targetFrequency_ = DSPUtils::midiNoteToFrequency(midiNote);

    // Check for legato mode
    bool isLegato = legatoMode_ && voice_.isActive() && (lastMidiNote_ >= 0);

    if (!isLegato) {
        // Not legato - trigger new note with envelope
        currentFrequency_ = targetFrequency_;
        voice_.noteOn(midiNote, velocity);
    } else {
        // Legato - change pitch without re-triggering envelope
        // The portamento will handle the pitch glide
        // We just update the voice's frequency continuously in process()
    }

    lastMidiNote_ = midiNote;
}

void MonoSynth::stopNote() {
    voice_.noteOff();
    lastMidiNote_ = -1;
}

void MonoSynth::stopAllNotes() {
    stopNote();
}

void MonoSynth::process(float* bufferLeft, float* bufferRight, int numFrames) {
    if (!voice_.isActive()) {
        // Fill with silence
        for (int i = 0; i < numFrames; ++i) {
            bufferLeft[i] = 0.0f;
            bufferRight[i] = 0.0f;
        }
        return;
    }

    // Process portamento (pitch glide)
    if (portamentoTime_ > 0.001f && currentFrequency_ > 0.0f) {
        currentFrequency_ = DSPUtils::smooth(currentFrequency_, targetFrequency_, portamentoCoeff_);
    } else {
        currentFrequency_ = targetFrequency_;
    }

    // Process audio
    for (int i = 0; i < numFrames; ++i) {
        float sample = voice_.process();

        // Mono to stereo
        bufferLeft[i] = sample;
        bufferRight[i] = sample;
    }
}

} // namespace dubtechno
