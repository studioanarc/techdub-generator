#include "AudioEngine.h"
#include <android/log.h>

#define LOG_TAG "AudioEngine"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

AudioEngine::AudioEngine() {
    LOGD("AudioEngine constructor");
    mCallback = std::make_unique<OboeAudioCallback>();
}

AudioEngine::~AudioEngine() {
    LOGD("AudioEngine destructor");
    stop();
}

bool AudioEngine::start() {
    std::lock_guard<std::mutex> lock(mMutex);

    if (mIsStarted) {
        LOGD("Audio engine already started");
        return true;
    }

    LOGI("Starting audio engine...");

    // Open the audio stream
    oboe::Result result = openStream();
    if (result != oboe::Result::OK) {
        LOGE("Failed to open audio stream: %s", oboe::convertToText(result));
        return false;
    }

    // Start the stream
    result = mStream->requestStart();
    if (result != oboe::Result::OK) {
        LOGE("Failed to start audio stream: %s", oboe::convertToText(result));
        closeStream();
        return false;
    }

    mIsStarted = true;

    // Log stream information
    LOGI("Audio stream started successfully");
    LOGI("Sample rate: %d Hz", mStream->getSampleRate());
    LOGI("Buffer size: %d frames", mStream->getBufferSizeInFrames());
    LOGI("Frames per burst: %d", mStream->getFramesPerBurst());
    LOGI("Channel count: %d", mStream->getChannelCount());
    LOGI("Format: %s", oboe::convertToText(mStream->getFormat()));
    LOGI("Performance mode: %s", oboe::convertToText(mStream->getPerformanceMode()));
    LOGI("Sharing mode: %s", oboe::convertToText(mStream->getSharingMode()));

    // Calculate and log latency
    double latency = getLatencyMillis();
    LOGI("Estimated latency: %.2f ms", latency);

    return true;
}

void AudioEngine::stop() {
    std::lock_guard<std::mutex> lock(mMutex);

    if (!mIsStarted) {
        LOGD("Audio engine already stopped");
        return;
    }

    LOGI("Stopping audio engine...");
    closeStream();
    mIsStarted = false;
    LOGI("Audio engine stopped");
}

void AudioEngine::pause() {
    std::lock_guard<std::mutex> lock(mMutex);

    if (!mIsStarted || !mStream) {
        return;
    }

    oboe::Result result = mStream->requestPause();
    if (result != oboe::Result::OK) {
        LOGE("Failed to pause stream: %s", oboe::convertToText(result));
    } else {
        LOGI("Audio stream paused");
    }
}

void AudioEngine::resume() {
    std::lock_guard<std::mutex> lock(mMutex);

    if (!mIsStarted || !mStream) {
        return;
    }

    oboe::Result result = mStream->requestStart();
    if (result != oboe::Result::OK) {
        LOGE("Failed to resume stream: %s", oboe::convertToText(result));
    } else {
        LOGI("Audio stream resumed");
    }
}

void AudioEngine::setFrequency(float frequency) {
    if (mCallback) {
        mCallback->setFrequency(frequency);
    }
}

void AudioEngine::setVolume(float volume) {
    if (mCallback) {
        mCallback->setVolume(volume);
    }
}

void AudioEngine::playBassNote(int midiNote, float velocity) {
    if (mCallback) {
        mCallback->playBassNote(midiNote, velocity);
    }
}

void AudioEngine::stopBassNote() {
    if (mCallback) {
        mCallback->stopBassNote();
    }
}

void AudioEngine::playPadNote(int midiNote, float velocity) {
    if (mCallback) {
        mCallback->playPadNote(midiNote, velocity);
    }
}

void AudioEngine::stopPadNote(int midiNote) {
    if (mCallback) {
        mCallback->stopPadNote(midiNote);
    }
}

void AudioEngine::stopAllNotes() {
    if (mCallback) {
        mCallback->stopAllNotes();
    }
}

int32_t AudioEngine::getSampleRate() const {
    std::lock_guard<std::mutex> lock(mMutex);
    if (mStream) {
        return mStream->getSampleRate();
    }
    return kSampleRate;
}

int32_t AudioEngine::getBufferSize() const {
    std::lock_guard<std::mutex> lock(mMutex);
    if (mStream) {
        return mStream->getBufferSizeInFrames();
    }
    return 0;
}

double AudioEngine::getLatencyMillis() const {
    std::lock_guard<std::mutex> lock(mMutex);
    if (!mStream) {
        return 0.0;
    }

    // Calculate latency based on buffer size and sample rate
    int32_t bufferSize = mStream->getBufferSizeInFrames();
    int32_t sampleRate = mStream->getSampleRate();

    if (sampleRate > 0) {
        return (bufferSize * 1000.0) / sampleRate;
    }

    return 0.0;
}

oboe::Result AudioEngine::openStream() {
    oboe::AudioStreamBuilder builder;

    builder.setDataCallback(mCallback.get())
        ->setDirection(oboe::Direction::Output)
        ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
        ->setSharingMode(oboe::SharingMode::Exclusive)  // Try exclusive first
        ->setFormat(oboe::AudioFormat::Float)
        ->setChannelCount(kChannelCount)
        ->setSampleRate(kSampleRate)
        ->setUsage(oboe::Usage::Media)
        ->setContentType(oboe::ContentType::Music);

    oboe::Result result = builder.openStream(mStream);

    if (result != oboe::Result::OK) {
        LOGE("Failed to open stream: %s", oboe::convertToText(result));
        return result;
    }

    // If exclusive mode failed, the stream will fall back to shared mode automatically
    if (mStream->getSharingMode() != oboe::SharingMode::Exclusive) {
        LOGD("Exclusive mode not available, using shared mode");
    }

    // Set buffer size to optimal value (twice the burst size for low latency)
    oboe::ResultWithValue<int32_t> setBufferResult =
        mStream->setBufferSizeInFrames(mStream->getFramesPerBurst() * 2);

    if (setBufferResult) {
        LOGD("Buffer size set to %d frames", setBufferResult.value());
    }

    return result;
}

void AudioEngine::closeStream() {
    if (mStream) {
        oboe::Result result = mStream->requestStop();
        if (result != oboe::Result::OK) {
            LOGE("Error stopping stream: %s", oboe::convertToText(result));
        }

        result = mStream->close();
        if (result != oboe::Result::OK) {
            LOGE("Error closing stream: %s", oboe::convertToText(result));
        }

        mStream.reset();
    }
}
