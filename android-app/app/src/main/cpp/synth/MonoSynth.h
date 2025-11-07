#pragma once

#include "Voice.h"
#include "../dsp/DSPUtils.h"

namespace dubtechno {

/**
 * Monophonic Bass Synthesizer
 *
 * Optimized for deep, punchy bass sounds typical of dub techno.
 * Features:
 * - Single voice (monophonic)
 * - Portamento (pitch glide)
 * - Legato mode (no re-trigger on note change)
 * - Optimized for low frequencies
 */
class MonoSynth {
public:
    MonoSynth();
    ~MonoSynth() = default;

    // Note control
    void playNote(int midiNote, float velocity = 1.0f);
    void stopNote();
    void stopAllNotes();

    // Configuration
    void setPortamento(float seconds);  // Glide time
    void setLegatoMode(bool enabled);

    // Processing
    void process(float* bufferLeft, float* bufferRight, int numFrames);

    // Voice access for configuration
    Voice& getVoice() { return voice_; }

    // Getters
    bool isPlaying() const { return voice_.isActive(); }
    float getPortamento() const { return portamentoTime_; }
    bool isLegatoMode() const { return legatoMode_; }

private:
    Voice voice_;

    // Portamento
    float portamentoTime_;
    float currentFrequency_;
    float targetFrequency_;
    float portamentoCoeff_;

    // Legato mode
    bool legatoMode_;
    int lastMidiNote_;

    void updatePortamento();
};

} // namespace dubtechno
