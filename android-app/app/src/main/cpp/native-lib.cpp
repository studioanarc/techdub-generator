#include <jni.h>
#include <string>
#include <memory>
#include <android/log.h>
#include "AudioEngine.h"

#define LOG_TAG "NativeLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global audio engine instance
static std::unique_ptr<AudioEngine> gAudioEngine;

extern "C" {

// Initialize the audio engine
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_initNative(JNIEnv *env, jobject /* this */) {
    LOGD("Initializing native audio engine");
    if (!gAudioEngine) {
        gAudioEngine = std::make_unique<AudioEngine>();
    }
}

// Start the audio engine
JNIEXPORT jboolean JNICALL
Java_com_dubtechno_generator_AudioEngine_startNative(JNIEnv *env, jobject /* this */) {
    LOGD("Starting audio engine from JNI");
    if (!gAudioEngine) {
        gAudioEngine = std::make_unique<AudioEngine>();
    }
    return gAudioEngine->start();
}

// Stop the audio engine
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_stopNative(JNIEnv *env, jobject /* this */) {
    LOGD("Stopping audio engine from JNI");
    if (gAudioEngine) {
        gAudioEngine->stop();
    }
}

// Pause the audio engine
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_pauseNative(JNIEnv *env, jobject /* this */) {
    LOGD("Pausing audio engine from JNI");
    if (gAudioEngine) {
        gAudioEngine->pause();
    }
}

// Resume the audio engine
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_resumeNative(JNIEnv *env, jobject /* this */) {
    LOGD("Resuming audio engine from JNI");
    if (gAudioEngine) {
        gAudioEngine->resume();
    }
}

// Set frequency
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_setFrequencyNative(
    JNIEnv *env, jobject /* this */, jfloat frequency) {
    if (gAudioEngine) {
        gAudioEngine->setFrequency(frequency);
    }
}

// Set volume
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_setVolumeNative(
    JNIEnv *env, jobject /* this */, jfloat volume) {
    if (gAudioEngine) {
        gAudioEngine->setVolume(volume);
    }
}

// Get sample rate
JNIEXPORT jint JNICALL
Java_com_dubtechno_generator_AudioEngine_getSampleRateNative(
    JNIEnv *env, jobject /* this */) {
    if (gAudioEngine) {
        return gAudioEngine->getSampleRate();
    }
    return 0;
}

// Get buffer size
JNIEXPORT jint JNICALL
Java_com_dubtechno_generator_AudioEngine_getBufferSizeNative(
    JNIEnv *env, jobject /* this */) {
    if (gAudioEngine) {
        return gAudioEngine->getBufferSize();
    }
    return 0;
}

// Get latency
JNIEXPORT jdouble JNICALL
Java_com_dubtechno_generator_AudioEngine_getLatencyMillisNative(
    JNIEnv *env, jobject /* this */) {
    if (gAudioEngine) {
        return gAudioEngine->getLatencyMillis();
    }
    return 0.0;
}

// Check if started
JNIEXPORT jboolean JNICALL
Java_com_dubtechno_generator_AudioEngine_isStartedNative(
    JNIEnv *env, jobject /* this */) {
    if (gAudioEngine) {
        return gAudioEngine->isStarted();
    }
    return false;
}

// Destroy the audio engine
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_destroyNative(JNIEnv *env, jobject /* this */) {
    LOGD("Destroying native audio engine");
    if (gAudioEngine) {
        gAudioEngine->stop();
        gAudioEngine.reset();
    }
}

// ========== SYNTH CONTROL JNI METHODS ==========

// Play bass note
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_playBassNoteNative(
    JNIEnv *env, jobject /* this */, jint midiNote, jfloat velocity) {
    if (gAudioEngine) {
        gAudioEngine->playBassNote(static_cast<int>(midiNote), static_cast<float>(velocity));
        LOGD("JNI: Playing bass note MIDI %d, velocity %.2f", midiNote, velocity);
    }
}

// Stop bass note
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_stopBassNoteNative(JNIEnv *env, jobject /* this */) {
    if (gAudioEngine) {
        gAudioEngine->stopBassNote();
        LOGD("JNI: Stopping bass note");
    }
}

// Play pad note
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_playPadNoteNative(
    JNIEnv *env, jobject /* this */, jint midiNote, jfloat velocity) {
    if (gAudioEngine) {
        gAudioEngine->playPadNote(static_cast<int>(midiNote), static_cast<float>(velocity));
        LOGD("JNI: Playing pad note MIDI %d, velocity %.2f", midiNote, velocity);
    }
}

// Stop pad note
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_stopPadNoteNative(
    JNIEnv *env, jobject /* this */, jint midiNote) {
    if (gAudioEngine) {
        gAudioEngine->stopPadNote(static_cast<int>(midiNote));
        LOGD("JNI: Stopping pad note MIDI %d", midiNote);
    }
}

// Stop all notes
JNIEXPORT void JNICALL
Java_com_dubtechno_generator_AudioEngine_stopAllNotesNative(JNIEnv *env, jobject /* this */) {
    if (gAudioEngine) {
        gAudioEngine->stopAllNotes();
        LOGD("JNI: Stopping all notes");
    }
}

} // extern "C"
