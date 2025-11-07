#include "VoiceManager.h"
#include <algorithm>

namespace dubtechno {

VoiceManager::VoiceManager()
    : stealMode_(VoiceStealMode::OLDEST)
    , maxPolyphony_(MAX_VOICES)
    , currentTime_(0.0f) {
    activeVoices_.reserve(MAX_VOICES);
}

void VoiceManager::setMaxPolyphony(int maxVoices) {
    maxPolyphony_ = std::min(maxVoices, MAX_VOICES);
}

void VoiceManager::configureAllVoices(Voice& templateVoice) {
    // This would copy settings from templateVoice to all voices
    // For now, voices are configured individually via getVoice()
}

void VoiceManager::noteOn(int midiNote, float velocity) {
    // Check if this note is already playing
    Voice* voice = findVoiceForNote(midiNote);

    if (voice == nullptr) {
        // Find an inactive voice
        voice = findInactiveVoice();

        // If no inactive voice, steal one
        if (voice == nullptr) {
            voice = stealVoice();
        }
    }

    if (voice != nullptr) {
        voice->noteOn(midiNote, velocity);

        // Track this voice allocation
        int voiceIndex = voice - &voices_[0];

        // Remove old entry if re-triggering
        activeVoices_.erase(
            std::remove_if(activeVoices_.begin(), activeVoices_.end(),
                [voiceIndex](const VoiceInfo& info) {
                    return info.voiceIndex == voiceIndex;
                }),
            activeVoices_.end()
        );

        // Add new entry
        VoiceInfo info;
        info.voiceIndex = voiceIndex;
        info.midiNote = midiNote;
        info.startTime = currentTime_;
        activeVoices_.push_back(info);
    }
}

void VoiceManager::noteOff(int midiNote) {
    // Find all voices playing this note and release them
    for (auto& voice : voices_) {
        if (voice.isActive() && voice.getMidiNote() == midiNote) {
            voice.noteOff();
        }
    }

    // Clean up tracking (don't remove immediately, let voice release)
}

void VoiceManager::allNotesOff() {
    for (auto& voice : voices_) {
        voice.noteOff();
    }
    activeVoices_.clear();
}

void VoiceManager::process(float* bufferLeft, float* bufferRight, int numFrames) {
    // Clear buffers
    for (int i = 0; i < numFrames; ++i) {
        bufferLeft[i] = 0.0f;
        bufferRight[i] = 0.0f;
    }

    // Process each voice and sum to output
    for (int v = 0; v < maxPolyphony_; ++v) {
        Voice& voice = voices_[v];

        if (voice.isActive()) {
            for (int i = 0; i < numFrames; ++i) {
                float sample = voice.process();

                // Stereo output (mono for now, can add panning later)
                bufferLeft[i] += sample;
                bufferRight[i] += sample;
            }
        }
    }

    // Normalize output to prevent clipping
    float scale = 1.0f / std::sqrt(static_cast<float>(maxPolyphony_));
    for (int i = 0; i < numFrames; ++i) {
        bufferLeft[i] *= scale;
        bufferRight[i] *= scale;

        // Soft clip to prevent hard clipping
        bufferLeft[i] = DSPUtils::softClip(bufferLeft[i]);
        bufferRight[i] = DSPUtils::softClip(bufferRight[i]);
    }

    // Clean up inactive voice tracking
    activeVoices_.erase(
        std::remove_if(activeVoices_.begin(), activeVoices_.end(),
            [this](const VoiceInfo& info) {
                return !voices_[info.voiceIndex].isActive();
            }),
        activeVoices_.end()
    );

    // Update time
    currentTime_ += static_cast<float>(numFrames) / SAMPLE_RATE;
}

int VoiceManager::getNumActiveVoices() const {
    int count = 0;
    for (const auto& voice : voices_) {
        if (voice.isActive()) {
            count++;
        }
    }
    return count;
}

Voice* VoiceManager::findInactiveVoice() {
    for (auto& voice : voices_) {
        if (!voice.isActive()) {
            return &voice;
        }
    }
    return nullptr;
}

Voice* VoiceManager::findVoiceForNote(int midiNote) {
    for (auto& voice : voices_) {
        if (voice.isActive() && voice.getMidiNote() == midiNote) {
            return &voice;
        }
    }
    return nullptr;
}

Voice* VoiceManager::stealVoice() {
    if (activeVoices_.empty()) {
        return &voices_[0];  // Fallback
    }

    if (stealMode_ == VoiceStealMode::OLDEST) {
        // Find the oldest voice
        auto oldest = std::min_element(activeVoices_.begin(), activeVoices_.end(),
            [](const VoiceInfo& a, const VoiceInfo& b) {
                return a.startTime < b.startTime;
            });

        if (oldest != activeVoices_.end()) {
            return &voices_[oldest->voiceIndex];
        }
    } else {
        // Find the quietest voice (by envelope level)
        Voice* quietest = nullptr;
        float minLevel = 1.0f;

        for (auto& info : activeVoices_) {
            Voice& voice = voices_[info.voiceIndex];
            // Use velocity as a proxy for loudness (could use actual envelope level)
            float level = voice.getVelocity();
            if (level < minLevel) {
                minLevel = level;
                quietest = &voice;
            }
        }

        if (quietest != nullptr) {
            return quietest;
        }
    }

    return &voices_[0];  // Fallback
}

} // namespace dubtechno
