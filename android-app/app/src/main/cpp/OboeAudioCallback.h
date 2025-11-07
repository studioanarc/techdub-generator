#ifndef DUBTECHNO_OBOEAUDIOCALLBACK_H
#define DUBTECHNO_OBOEAUDIOCALLBACK_H

#include <oboe/Oboe.h>
#include <atomic>
#include <memory>
#include <mutex>
#include "synth/MonoSynth.h"
#include "synth/PolySynth.h"

class OboeAudioCallback : public oboe::AudioStreamDataCallback {
public:
    OboeAudioCallback();
    ~OboeAudioCallback() override = default;

    // Oboe callback - called on audio thread
    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames) override;

    // Synth control (thread-safe)
    void playBassNote(int midiNote, float velocity = 1.0f);
    void stopBassNote();
    void playPadNote(int midiNote, float velocity = 1.0f);
    void stopPadNote(int midiNote);
    void stopAllNotes();

    // Legacy parameter setters (for backwards compatibility)
    void setFrequency(float frequency);
    void setVolume(float volume);

private:
    // Synthesizers
    std::unique_ptr<dubtechno::MonoSynth> mBassSynth;
    std::unique_ptr<dubtechno::PolySynth> mPadSynth;

    // Master volume
    std::atomic<float> mVolume{0.7f};

    // Thread safety
    std::mutex mSynthMutex;

    // Audio buffers (pre-allocated to avoid allocations in audio thread)
    static constexpr int MAX_BUFFER_SIZE = 2048;
    float mBassBufferL[MAX_BUFFER_SIZE];
    float mBassBufferR[MAX_BUFFER_SIZE];
    float mPadBufferL[MAX_BUFFER_SIZE];
    float mPadBufferR[MAX_BUFFER_SIZE];
};

#endif // DUBTECHNO_OBOEAUDIOCALLBACK_H
