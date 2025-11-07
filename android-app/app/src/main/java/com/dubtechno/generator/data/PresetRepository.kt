package com.dubtechno.generator.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing presets (save, load, delete)
 * Uses SharedPreferences for simple persistence
 */
class PresetRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Save a preset to storage
     */
    suspend fun savePreset(preset: Preset): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(preset)
            prefs.edit().putString(preset.id, jsonString).apply()

            // Also save to preset index
            val presetIds = getPresetIds().toMutableSet()
            presetIds.add(preset.id)
            prefs.edit().putStringSet(KEY_PRESET_INDEX, presetIds).apply()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load a specific preset by ID
     */
    suspend fun loadPreset(id: String): Result<Preset?> = withContext(Dispatchers.IO) {
        try {
            val jsonString = prefs.getString(id, null)
            if (jsonString != null) {
                val preset = json.decodeFromString<Preset>(jsonString)
                Result.success(preset)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all saved presets
     */
    suspend fun getAllPresets(): Result<List<Preset>> = withContext(Dispatchers.IO) {
        try {
            val presetIds = getPresetIds()
            val presets = presetIds.mapNotNull { id ->
                val jsonString = prefs.getString(id, null)
                try {
                    jsonString?.let { json.decodeFromString<Preset>(it) }
                } catch (e: Exception) {
                    null // Skip corrupted presets
                }
            }.sortedByDescending { it.timestamp }

            Result.success(presets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a preset
     */
    suspend fun deletePreset(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            prefs.edit().remove(id).apply()

            // Remove from index
            val presetIds = getPresetIds().toMutableSet()
            presetIds.remove(id)
            prefs.edit().putStringSet(KEY_PRESET_INDEX, presetIds).apply()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if factory presets have been initialized
     */
    suspend fun initializeFactoryPresets(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val initialized = prefs.getBoolean(KEY_FACTORY_INITIALIZED, false)
            if (!initialized) {
                PresetFactory.getAllFactoryPresets().forEach { preset ->
                    savePreset(preset)
                }
                prefs.edit().putBoolean(KEY_FACTORY_INITIALIZED, true).apply()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export preset as JSON string
     */
    fun exportPreset(preset: Preset): String {
        return json.encodeToString(preset)
    }

    /**
     * Import preset from JSON string
     */
    suspend fun importPreset(jsonString: String): Result<Preset> = withContext(Dispatchers.IO) {
        try {
            val preset = json.decodeFromString<Preset>(jsonString)
            savePreset(preset)
            Result.success(preset)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear all user presets (keep factory presets)
     */
    suspend fun clearUserPresets(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val allPresets = getAllPresets().getOrThrow()
            val factoryPresetNames = PresetFactory.getAllFactoryPresets().map { it.name }.toSet()

            allPresets.forEach { preset ->
                if (preset.name !in factoryPresetNames) {
                    deletePreset(preset.id)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getPresetIds(): Set<String> {
        return prefs.getStringSet(KEY_PRESET_INDEX, emptySet()) ?: emptySet()
    }

    companion object {
        private const val PREFS_NAME = "dub_techno_presets"
        private const val KEY_PRESET_INDEX = "preset_index"
        private const val KEY_FACTORY_INITIALIZED = "factory_initialized"
    }
}
