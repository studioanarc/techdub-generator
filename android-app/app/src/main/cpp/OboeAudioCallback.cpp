#include "OboeAudioCallback.h"
#include <android/log.h>
#include <algorithm>

#define LOG_TAG "OboeAudioCallback"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

OboeAudioCallback::OboeAudioCallback() {
    LOGD("OboeAudioCallback created - initializing synthesizers");

    // Initialize synthesizers
    mBassSynth = std::make_unique<dubtechno::MonoSynth>();
    mPadSynth = std::make_unique<dubtechno::PolySynth>();

    // Initialize buffers to zero
    std::fill(std::begin(mBassBufferL), std::end(mBassBufferL), 0.0f);
    std::fill(std::begin(mBassBufferR), std::end(mBassBufferR), 0.0f);
    std::fill(std::begin(mPadBufferL), std::end(mPadBufferL), 0.0f);
    std::fill(std::begin(mPadBufferR), std::end(mPadBufferR), 0.0f);

    LOGD("Synthesizers initialized successfully");
}

oboe::DataCallbackResult OboeAudioCallback::onAudioReady(
    oboe::AudioStream *audioStream,
    void *audioData,
    int32_t numFrames) {

    auto *outputBuffer = static_cast<float *>(audioData);
    int32_t channelCount = audioStream->getChannelCount();

    // Clamp numFrames to buffer size
    numFrames = std::min(numFrames, static_cast<int32_t>(MAX_BUFFER_SIZE));

    // Get master volume
    float masterVol = mVolume.load(std::memory_order_relaxed);

    // Lock-free audio processing (synthesizers are thread-safe)
    // Process bass synth
    mBassSynth->process(mBassBufferL, mBassBufferR, numFrames);

    // Process pad synth
    mPadSynth->process(mPadBufferL, mPadBufferR, numFrames);

    // Mix and output
    for (int32_t frame = 0; frame < numFrames; ++frame) {
        // Mix bass and pads (bass at 70%, pads at 30% for dub techno balance)
        float leftSample = (mBassBufferL[frame] * 0.7f) + (mPadBufferL[frame] * 0.3f);
        float rightSample = (mBassBufferR[frame] * 0.7f) + (mPadBufferR[frame] * 0.3f);

        // Apply master volume
        leftSample *= masterVol;
        rightSample *= masterVol;

        // Soft clip to prevent harsh clipping
        leftSample = dubtechno::DSPUtils::softClip(leftSample);
        rightSample = dubtechno::DSPUtils::softClip(rightSample);

        // Write to output buffer
        if (channelCount == 2) {
            outputBuffer[frame * 2] = leftSample;
            outputBuffer[frame * 2 + 1] = rightSample;
        } else if (channelCount == 1) {
            // Mono output - mix left and right
            outputBuffer[frame] = (leftSample + rightSample) * 0.5f;
        }
    }

    return oboe::DataCallbackResult::Continue;
}

// Synth control methods
void OboeAudioCallback::playBassNote(int midiNote, float velocity) {
    std::lock_guard<std::mutex> lock(mSynthMutex);
    if (mBassSynth) {
        mBassSynth->playNote(midiNote, velocity);
        LOGD("Bass note played: MIDI %d, velocity %.2f", midiNote, velocity);
    }
}

void OboeAudioCallback::stopBassNote() {
    std::lock_guard<std::mutex> lock(mSynthMutex);
    if (mBassSynth) {
        mBassSynth->stopNote();
        LOGD("Bass note stopped");
    }
}

void OboeAudioCallback::playPadNote(int midiNote, float velocity) {
    std::lock_guard<std::mutex> lock(mSynthMutex);
    if (mPadSynth) {
        mPadSynth->playNote(midiNote, velocity);
        LOGD("Pad note played: MIDI %d, velocity %.2f", midiNote, velocity);
    }
}

void OboeAudioCallback::stopPadNote(int midiNote) {
    std::lock_guard<std::mutex> lock(mSynthMutex);
    if (mPadSynth) {
        mPadSynth->stopNote(midiNote);
        LOGD("Pad note stopped: MIDI %d", midiNote);
    }
}

void OboeAudioCallback::stopAllNotes() {
    std::lock_guard<std::mutex> lock(mSynthMutex);
    if (mBassSynth) {
        mBassSynth->stopAllNotes();
    }
    if (mPadSynth) {
        mPadSynth->stopAllNotes();
    }
    LOGD("All notes stopped");
}

// Legacy methods (for backwards compatibility)
void OboeAudioCallback::setFrequency(float frequency) {
    // Convert frequency to MIDI note and play on bass synth
    int midiNote = dubtechno::DSPUtils::frequencyToMidiNote(frequency);
    playBassNote(midiNote, 1.0f);
    LOGD("Legacy setFrequency called: %.2f Hz -> MIDI %d", frequency, midiNote);
}

void OboeAudioCallback::setVolume(float volume) {
    // Clamp volume to 0-1 range
    volume = dubtechno::DSPUtils::clamp(volume, 0.0f, 1.0f);
    mVolume.store(volume, std::memory_order_relaxed);
    LOGD("Volume set to: %.2f", volume);
}
