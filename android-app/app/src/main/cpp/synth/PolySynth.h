#pragma once

#include "VoiceManager.h"
#include "../dsp/DSPUtils.h"

namespace dubtechno {

/**
 * Polyphonic Synthesizer
 *
 * Optimized for pads and melodic elements in dub techno.
 * Features:
 * - Polyphonic voice management (up to 16 voices)
 * - Unison mode (stack multiple voices per note with detune)
 * - Built-in chorus effect via detuning
 * - Optimized for lush, atmospheric sounds
 */
class PolySynth {
public:
    PolySynth();
    ~PolySynth() = default;

    // Note control
    void playNote(int midiNote, float velocity = 1.0f);
    void stopNote(int midiNote);
    void playChord(const int* midiNotes, int numNotes);
    void stopAllNotes();

    // Configuration
    void setUnisonMode(bool enabled);
    void setUnisonVoices(int numVoices);  // 2-4 voices per note
    void setUnisonDetune(float cents);     // Detune amount
    void setMaxPolyphony(int maxVoices);

    // Processing
    void process(float* bufferLeft, float* bufferRight, int numFrames);

    // Voice manager access for configuration
    VoiceManager& getVoiceManager() { return voiceManager_; }

    // Getters
    int getNumActiveVoices() const { return voiceManager_.getNumActiveVoices(); }
    bool isUnisonMode() const { return unisonMode_; }

private:
    void configureVoicesForPads();

    VoiceManager voiceManager_;

    // Unison mode
    bool unisonMode_;
    int unisonVoices_;
    float unisonDetune_;
};

} // namespace dubtechno
