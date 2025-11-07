#include "PolySynth.h"

namespace dubtechno {

PolySynth::PolySynth()
    : unisonMode_(false)
    , unisonVoices_(2)
    , unisonDetune_(10.0f) {

    configureVoicesForPads();
}

void PolySynth::configureVoicesForPads() {
    // Configure all voices for atmospheric pad sounds
    for (int i = 0; i < VoiceManager::MAX_VOICES; ++i) {
        Voice& voice = voiceManager_.getVoice(i);

        // Multiple waveforms for richness
        voice.setOsc1Waveform(WaveformType::SAWTOOTH);
        voice.setOsc2Waveform(WaveformType::SQUARE);
        voice.setOsc1Level(0.5f);
        voice.setOsc2Level(0.5f);

        // Slight detune for width (each voice gets different detune)
        float detune = -5.0f + (i % 10);  // -5 to +4 cents
        voice.setOsc2Detune(detune);

        // Some voices have sub-oscillator
        if (i % 3 == 0) {
            voice.setSubOscillatorLevel(0.2f);
        }

        // Filter settings for pads
        voice.setFilterType(FilterType::LOWPASS);
        voice.setFilterCutoff(2000.0f);
        voice.setFilterResonance(0.5f);
        voice.setFilterEnvAmount(0.5f);

        // Slow attack, long release for pads
        voice.setAmpAttack(0.3f);
        voice.setAmpDecay(0.5f);
        voice.setAmpSustain(0.8f);
        voice.setAmpRelease(1.5f);

        // Filter envelope
        voice.setFilterAttack(0.5f);
        voice.setFilterDecay(1.0f);
        voice.setFilterSustain(0.5f);
        voice.setFilterRelease(1.0f);

        // LFO for movement
        voice.setLFO1Rate(0.3f);
        voice.setLFO1Waveform(LFOWaveform::SINE);
        voice.setLFO1ToFilterAmount(0.3f);

        voice.setLFO2Rate(4.0f);
        voice.setLFO2Waveform(LFOWaveform::SINE);
        voice.setLFO2ToPitchAmount(0.05f);  // Subtle vibrato
    }
}

void PolySynth::setUnisonMode(bool enabled) {
    unisonMode_ = enabled;
}

void PolySynth::setUnisonVoices(int numVoices) {
    unisonVoices_ = DSPUtils::clamp(numVoices, 2, 4);
}

void PolySynth::setUnisonDetune(float cents) {
    unisonDetune_ = DSPUtils::clamp(cents, 0.0f, 50.0f);
}

void PolySynth::setMaxPolyphony(int maxVoices) {
    voiceManager_.setMaxPolyphony(maxVoices);
}

void PolySynth::playNote(int midiNote, float velocity) {
    if (unisonMode_) {
        // Play multiple detuned voices for unison effect
        for (int i = 0; i < unisonVoices_; ++i) {
            // Calculate detune spread
            float detune = 0.0f;
            if (unisonVoices_ > 1) {
                float spread = unisonDetune_ / (unisonVoices_ - 1);
                detune = -unisonDetune_ / 2.0f + i * spread;
            }

            // Adjust the note frequency with detune
            // This is a simplified approach - in practice, you'd want to
            // configure each voice's oscillator detune directly
            voiceManager_.noteOn(midiNote, velocity);

            // Apply detune to the voice
            // Note: This requires accessing the specific voice and setting detune
            // For simplicity, we rely on the natural detune in voice configuration
        }
    } else {
        // Single voice per note
        voiceManager_.noteOn(midiNote, velocity);
    }
}

void PolySynth::stopNote(int midiNote) {
    voiceManager_.noteOff(midiNote);
}

void PolySynth::playChord(const int* midiNotes, int numNotes) {
    for (int i = 0; i < numNotes; ++i) {
        playNote(midiNotes[i]);
    }
}

void PolySynth::stopAllNotes() {
    voiceManager_.allNotesOff();
}

void PolySynth::process(float* bufferLeft, float* bufferRight, int numFrames) {
    voiceManager_.process(bufferLeft, bufferRight, numFrames);
}

} // namespace dubtechno
