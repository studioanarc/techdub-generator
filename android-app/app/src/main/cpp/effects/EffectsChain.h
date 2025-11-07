#pragma once

#include "Effect.h"
#include <vector>
#include <memory>
#include <mutex>
#include <string>
#include <map>

namespace dubtechno {

/**
 * Effects Chain Manager
 *
 * Manages a chain of audio effects with routing capabilities
 *
 * Features:
 * - 8 effect slots
 * - Series or parallel routing
 * - Per-slot bypass and mix control
 * - Reorderable chain
 * - Preset save/load
 * - Thread-safe parameter updates
 */
class EffectsChain {
public:
    static constexpr int MAX_SLOTS = 8;

    enum RoutingMode {
        SERIES,    // Effects in series (A -> B -> C)
        PARALLEL   // Effects in parallel (mix all outputs)
    };

    EffectsChain() : sampleRate(SAMPLE_RATE), routingMode(SERIES) {
        slots.resize(MAX_SLOTS);
        for (int i = 0; i < MAX_SLOTS; ++i) {
            slotOrder[i] = i;
        }
    }

    /**
     * Initialize effects chain
     */
    void init(float sampleRate) {
        std::lock_guard<std::mutex> lock(mutex);
        this->sampleRate = sampleRate;

        // Initialize all effects in slots
        for (auto& slot : slots) {
            if (slot.effect) {
                slot.effect->init(sampleRate);
            }
        }
    }

    /**
     * Add effect to a slot
     * Takes ownership of the effect pointer
     */
    void addEffect(std::unique_ptr<Effect> effect, int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return;

        std::lock_guard<std::mutex> lock(mutex);

        if (effect) {
            effect->init(sampleRate);
        }

        slots[slot].effect = std::move(effect);
        slots[slot].enabled = true;
    }

    /**
     * Remove effect from slot
     */
    void removeEffect(int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return;

        std::lock_guard<std::mutex> lock(mutex);
        slots[slot].effect.reset();
        slots[slot].enabled = false;
    }

    /**
     * Get effect from slot (for parameter control)
     */
    Effect* getEffect(int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return nullptr;
        return slots[slot].effect.get();
    }

    /**
     * Process audio through effects chain
     */
    void process(float* buffer, int numFrames, int channels) {
        std::lock_guard<std::mutex> lock(mutex);

        if (routingMode == SERIES) {
            processSeries(buffer, numFrames, channels);
        } else {
            processParallel(buffer, numFrames, channels);
        }
    }

    /**
     * Set slot processing order
     */
    void setSlotOrder(const int* order) {
        std::lock_guard<std::mutex> lock(mutex);
        for (int i = 0; i < MAX_SLOTS; ++i) {
            if (order[i] >= 0 && order[i] < MAX_SLOTS) {
                slotOrder[i] = order[i];
            }
        }
    }

    /**
     * Enable/disable a slot
     */
    void setSlotEnabled(int slot, bool enabled) {
        if (slot < 0 || slot >= MAX_SLOTS) return;
        std::lock_guard<std::mutex> lock(mutex);
        slots[slot].enabled = enabled;
    }

    /**
     * Set routing mode
     */
    void setRoutingMode(RoutingMode mode) {
        std::lock_guard<std::mutex> lock(mutex);
        routingMode = mode;
    }

    /**
     * Reset all effects
     */
    void reset() {
        std::lock_guard<std::mutex> lock(mutex);
        for (auto& slot : slots) {
            if (slot.effect) {
                slot.effect->reset();
            }
        }
    }

    /**
     * Save preset
     * Returns a map of effect parameters
     * NOTE: This is a simplified version - a full implementation would
     * serialize all effect parameters to JSON or binary format
     */
    std::map<std::string, float> savePreset() {
        std::lock_guard<std::mutex> lock(mutex);
        std::map<std::string, float> preset;

        // Example: Save mix levels and bypass states
        for (int i = 0; i < MAX_SLOTS; ++i) {
            if (slots[i].effect) {
                std::string prefix = "slot_" + std::to_string(i) + "_";
                preset[prefix + "mix"] = slots[i].effect->getMix();
                preset[prefix + "bypass"] = slots[i].effect->isBypassed() ? 1.0f : 0.0f;
                preset[prefix + "enabled"] = slots[i].enabled ? 1.0f : 0.0f;
            }
        }

        return preset;
    }

    /**
     * Load preset
     */
    void loadPreset(const std::map<std::string, float>& preset) {
        std::lock_guard<std::mutex> lock(mutex);

        for (const auto& param : preset) {
            // Parse parameter name and apply
            // Example: "slot_0_mix" -> slot 0, mix parameter
            // Full implementation would handle all effect-specific parameters
        }
    }

private:
    struct EffectSlot {
        std::unique_ptr<Effect> effect;
        bool enabled = false;
    };

    std::vector<EffectSlot> slots;
    int slotOrder[MAX_SLOTS];
    RoutingMode routingMode;
    float sampleRate;
    std::mutex mutex;

    // Temporary buffer for parallel processing
    std::vector<float> tempBuffer;

    /**
     * Process effects in series
     */
    void processSeries(float* buffer, int numFrames, int channels) {
        for (int i = 0; i < MAX_SLOTS; ++i) {
            int slotIdx = slotOrder[i];
            auto& slot = slots[slotIdx];

            if (slot.enabled && slot.effect && !slot.effect->isBypassed()) {
                slot.effect->process(buffer, numFrames, channels);
            }
        }
    }

    /**
     * Process effects in parallel
     */
    void processParallel(float* buffer, int numFrames, int channels) {
        int totalSamples = numFrames * channels;

        // Ensure temp buffer is large enough
        if (tempBuffer.size() < totalSamples) {
            tempBuffer.resize(totalSamples);
        }

        // Clear output buffer
        std::fill(tempBuffer.begin(), tempBuffer.begin() + totalSamples, 0.0f);

        int activeEffects = 0;

        // Process each effect in parallel and mix
        for (int i = 0; i < MAX_SLOTS; ++i) {
            auto& slot = slots[i];

            if (slot.enabled && slot.effect && !slot.effect->isBypassed()) {
                // Copy input to temp buffer
                std::vector<float> effectBuffer(buffer, buffer + totalSamples);

                // Process
                slot.effect->process(effectBuffer.data(), numFrames, channels);

                // Mix to output
                for (int s = 0; s < totalSamples; ++s) {
                    tempBuffer[s] += effectBuffer[s];
                }

                activeEffects++;
            }
        }

        // Normalize by number of active effects (prevent clipping)
        if (activeEffects > 0) {
            float gain = 1.0f / activeEffects;
            for (int s = 0; s < totalSamples; ++s) {
                buffer[s] = tempBuffer[s] * gain;
            }
        }
    }
};

} // namespace dubtechno
