#pragma once

#include "Voice.h"
#include <array>
#include <vector>

namespace dubtechno {

/**
 * Voice stealing strategies
 */
enum class VoiceStealMode {
    OLDEST,    // Steal the oldest playing note
    QUIETEST   // Steal the quietest note
};

/**
 * Voice Manager for Polyphonic Synthesis
 *
 * Manages a pool of voices for polyphonic playback.
 * Handles voice allocation, stealing, and note tracking.
 */
class VoiceManager {
public:
    static constexpr int MAX_VOICES = 16;

    VoiceManager();
    ~VoiceManager() = default;

    // Note control
    void noteOn(int midiNote, float velocity);
    void noteOff(int midiNote);
    void allNotesOff();

    // Processing
    void process(float* bufferLeft, float* bufferRight, int numFrames);

    // Configuration
    void setVoiceStealMode(VoiceStealMode mode) { stealMode_ = mode; }
    void setMaxPolyphony(int maxVoices);

    // Voice configuration (applied to all voices)
    void configureAllVoices(Voice& templateVoice);

    // Getters
    int getNumActiveVoices() const;
    int getMaxPolyphony() const { return maxPolyphony_; }

    // Direct voice access for configuration
    Voice& getVoice(int index) { return voices_[index]; }

private:
    Voice* findInactiveVoice();
    Voice* findVoiceForNote(int midiNote);
    Voice* stealVoice();

    std::array<Voice, MAX_VOICES> voices_;
    VoiceStealMode stealMode_;
    int maxPolyphony_;

    // Voice allocation tracking
    struct VoiceInfo {
        int voiceIndex;
        int midiNote;
        float startTime;  // For oldest note stealing
    };
    std::vector<VoiceInfo> activeVoices_;
    float currentTime_;
};

} // namespace dubtechno
