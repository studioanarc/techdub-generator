#ifndef DUBTECHNO_AUDIOENGINE_H
#define DUBTECHNO_AUDIOENGINE_H

#include <oboe/Oboe.h>
#include <memory>
#include <mutex>
#include "OboeAudioCallback.h"

class AudioEngine {
public:
    AudioEngine();
    ~AudioEngine();

    // Stream lifecycle
    bool start();
    void stop();
    void pause();
    void resume();

    // Audio parameters
    void setFrequency(float frequency);
    void setVolume(float volume);

    // Synth control
    void playBassNote(int midiNote, float velocity = 1.0f);
    void stopBassNote();
    void playPadNote(int midiNote, float velocity = 1.0f);
    void stopPadNote(int midiNote);
    void stopAllNotes();

    // Stream information
    bool isStarted() const { return mIsStarted; }
    int32_t getSampleRate() const;
    int32_t getBufferSize() const;
    double getLatencyMillis() const;

private:
    // Oboe audio stream
    std::shared_ptr<oboe::AudioStream> mStream;
    std::unique_ptr<OboeAudioCallback> mCallback;

    // State
    bool mIsStarted{false};
    mutable std::mutex mMutex;

    // Stream configuration
    static constexpr int32_t kSampleRate = 48000;
    static constexpr int32_t kChannelCount = 2;  // Stereo

    // Helper methods
    void closeStream();
    oboe::Result openStream();
};

#endif // DUBTECHNO_AUDIOENGINE_H
