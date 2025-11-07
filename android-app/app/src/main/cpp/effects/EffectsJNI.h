#pragma once

#include <jni.h>
#include "EffectsChain.h"
#include "TapeEcho.h"
#include "Reverb.h"
#include "Compressor.h"
#include "Delay.h"
#include "Filter.h"
#include "Distortion.h"
#include "BitCrusher.h"
#include "Chorus.h"
#include "Phaser.h"
#include "ConvolutionReverb.h"

namespace dubtechno {

/**
 * JNI Bindings for Effects
 *
 * Provides Java/Kotlin interface to C++ effects
 *
 * Thread-safe parameter control using atomic operations
 */

// Global effects chain instance
// In a real app, this would be managed by the audio engine
extern EffectsChain g_effectsChain;

extern "C" {

// ============================================================================
// Effects Chain Management
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeInitEffectsChain(
    JNIEnv* env,
    jobject obj,
    jfloat sampleRate
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeResetEffectsChain(
    JNIEnv* env,
    jobject obj
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeSetRoutingMode(
    JNIEnv* env,
    jobject obj,
    jint mode // 0 = SERIES, 1 = PARALLEL
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeSetSlotEnabled(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jboolean enabled
);

// ============================================================================
// Effect Creation (adds to chain)
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddTapeEcho(
    JNIEnv* env,
    jobject obj,
    jint slot
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddReverb(
    JNIEnv* env,
    jobject obj,
    jint slot
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddCompressor(
    JNIEnv* env,
    jobject obj,
    jint slot
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddDelay(
    JNIEnv* env,
    jobject obj,
    jint slot
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddFilter(
    JNIEnv* env,
    jobject obj,
    jint slot
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddDistortion(
    JNIEnv* env,
    jobject obj,
    jint slot
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddChorus(
    JNIEnv* env,
    jobject obj,
    jint slot
);

// ============================================================================
// Parameter Control - TapeEcho
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetTime(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat timeMs
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetFeedback(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat feedback
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetSaturation(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat saturation
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetAge(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat age
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetWowFlutter(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat wowFlutter
);

// ============================================================================
// Parameter Control - Reverb
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeReverbSetRoomSize(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat roomSize
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeReverbSetDamping(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat damping
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeReverbSetWidth(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat width
);

// ============================================================================
// Parameter Control - Compressor
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorSetThreshold(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat thresholdDb
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorSetRatio(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat ratio
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorSetAttack(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat attackMs
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorSetRelease(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat releaseMs
);

JNIEXPORT jfloat JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorGetGainReduction(
    JNIEnv* env,
    jobject obj,
    jint slot
);

// ============================================================================
// Common Parameters (all effects)
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeSetEffectMix(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat mix
);

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeSetEffectBypassed(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jboolean bypassed
);

} // extern "C"

} // namespace dubtechno
