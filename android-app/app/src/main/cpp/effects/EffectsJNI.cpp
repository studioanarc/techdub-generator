#include "EffectsJNI.h"
#include <memory>

namespace dubtechno {

// Global effects chain
EffectsChain g_effectsChain;

extern "C" {

// ============================================================================
// Effects Chain Management
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeInitEffectsChain(
    JNIEnv* env,
    jobject obj,
    jfloat sampleRate
) {
    g_effectsChain.init(sampleRate);
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeResetEffectsChain(
    JNIEnv* env,
    jobject obj
) {
    g_effectsChain.reset();
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeSetRoutingMode(
    JNIEnv* env,
    jobject obj,
    jint mode
) {
    EffectsChain::RoutingMode routingMode =
        (mode == 0) ? EffectsChain::SERIES : EffectsChain::PARALLEL;
    g_effectsChain.setRoutingMode(routingMode);
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeSetSlotEnabled(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jboolean enabled
) {
    g_effectsChain.setSlotEnabled(slot, enabled);
}

// ============================================================================
// Effect Creation
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddTapeEcho(
    JNIEnv* env,
    jobject obj,
    jint slot
) {
    auto effect = std::make_unique<TapeEcho>();
    g_effectsChain.addEffect(std::move(effect), slot);
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddReverb(
    JNIEnv* env,
    jobject obj,
    jint slot
) {
    auto effect = std::make_unique<Reverb>();
    g_effectsChain.addEffect(std::move(effect), slot);
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddCompressor(
    JNIEnv* env,
    jobject obj,
    jint slot
) {
    auto effect = std::make_unique<Compressor>();
    g_effectsChain.addEffect(std::move(effect), slot);
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddDelay(
    JNIEnv* env,
    jobject obj,
    jint slot
) {
    auto effect = std::make_unique<Delay>();
    g_effectsChain.addEffect(std::move(effect), slot);
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddFilter(
    JNIEnv* env,
    jobject obj,
    jint slot
) {
    auto effect = std::make_unique<Filter>();
    g_effectsChain.addEffect(std::move(effect), slot);
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddDistortion(
    JNIEnv* env,
    jobject obj,
    jint slot
) {
    auto effect = std::make_unique<Distortion>();
    g_effectsChain.addEffect(std::move(effect), slot);
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeAddChorus(
    JNIEnv* env,
    jobject obj,
    jint slot
) {
    auto effect = std::make_unique<Chorus>();
    g_effectsChain.addEffect(std::move(effect), slot);
}

// ============================================================================
// Parameter Control - TapeEcho
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetTime(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat timeMs
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* tapeEcho = dynamic_cast<TapeEcho*>(effect)) {
        tapeEcho->setTime(timeMs);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetFeedback(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat feedback
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* tapeEcho = dynamic_cast<TapeEcho*>(effect)) {
        tapeEcho->setFeedback(feedback);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetSaturation(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat saturation
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* tapeEcho = dynamic_cast<TapeEcho*>(effect)) {
        tapeEcho->setSaturation(saturation);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetAge(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat age
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* tapeEcho = dynamic_cast<TapeEcho*>(effect)) {
        tapeEcho->setAge(age);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeTapeEchoSetWowFlutter(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat wowFlutter
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* tapeEcho = dynamic_cast<TapeEcho*>(effect)) {
        tapeEcho->setWowFlutter(wowFlutter);
    }
}

// ============================================================================
// Parameter Control - Reverb
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeReverbSetRoomSize(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat roomSize
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* reverb = dynamic_cast<Reverb*>(effect)) {
        reverb->setRoomSize(roomSize);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeReverbSetDamping(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat damping
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* reverb = dynamic_cast<Reverb*>(effect)) {
        reverb->setDamping(damping);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeReverbSetWidth(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat width
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* reverb = dynamic_cast<Reverb*>(effect)) {
        reverb->setWidth(width);
    }
}

// ============================================================================
// Parameter Control - Compressor
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorSetThreshold(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat thresholdDb
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* comp = dynamic_cast<Compressor*>(effect)) {
        comp->setThreshold(thresholdDb);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorSetRatio(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat ratio
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* comp = dynamic_cast<Compressor*>(effect)) {
        comp->setRatio(ratio);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorSetAttack(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat attackMs
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* comp = dynamic_cast<Compressor*>(effect)) {
        comp->setAttack(attackMs);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorSetRelease(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat releaseMs
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* comp = dynamic_cast<Compressor*>(effect)) {
        comp->setRelease(releaseMs);
    }
}

JNIEXPORT jfloat JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeCompressorGetGainReduction(
    JNIEnv* env,
    jobject obj,
    jint slot
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (auto* comp = dynamic_cast<Compressor*>(effect)) {
        return comp->getGainReduction();
    }
    return 0.0f;
}

// ============================================================================
// Common Parameters
// ============================================================================

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeSetEffectMix(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jfloat mix
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (effect) {
        effect->setMix(mix);
    }
}

JNIEXPORT void JNICALL
Java_com_dubtechno_generator_audio_EffectsEngine_nativeSetEffectBypassed(
    JNIEnv* env,
    jobject obj,
    jint slot,
    jboolean bypassed
) {
    Effect* effect = g_effectsChain.getEffect(slot);
    if (effect) {
        effect->setBypassed(bypassed);
    }
}

} // extern "C"

} // namespace dubtechno
