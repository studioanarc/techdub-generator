package com.dubtechno.generator

import android.util.Log

/**
 * Kotlin wrapper for the native audio engine using Oboe.
 * Provides a simple API for controlling audio playback.
 */
object AudioEngine {
    private const val TAG = "AudioEngine"

    init {
        try {
            System.loadLibrary("dubtechno")
            Log.d(TAG, "Native library loaded successfully")
            initNative()
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library", e)
            throw e
        }
    }

    /**
     * Initialize the native audio engine
     */
    private external fun initNative()

    /**
     * Start the audio engine and begin playback
     * @return true if started successfully, false otherwise
     */
    external fun startNative(): Boolean

    /**
     * Stop the audio engine and end playback
     */
    external fun stopNative()

    /**
     * Pause the audio engine (keeps stream open)
     */
    external fun pauseNative()

    /**
     * Resume the audio engine after pause
     */
    external fun resumeNative()

    /**
     * Set the test tone frequency
     * @param frequency Frequency in Hz (20-20000)
     */
    external fun setFrequencyNative(frequency: Float)

    /**
     * Set the master volume
     * @param volume Volume level (0.0-1.0)
     */
    external fun setVolumeNative(volume: Float)

    /**
     * Get the current sample rate
     * @return Sample rate in Hz
     */
    external fun getSampleRateNative(): Int

    /**
     * Get the current buffer size
     * @return Buffer size in frames
     */
    external fun getBufferSizeNative(): Int

    /**
     * Get the estimated audio latency
     * @return Latency in milliseconds
     */
    external fun getLatencyMillisNative(): Double

    /**
     * Check if the audio engine is started
     * @return true if started, false otherwise
     */
    external fun isStartedNative(): Boolean

    /**
     * Destroy the native audio engine
     */
    external fun destroyNative()

    // Kotlin-friendly wrapper methods

    /**
     * Start audio playback
     */
    fun start(): Boolean {
        return try {
            val result = startNative()
            if (result) {
                Log.i(TAG, "Audio engine started")
                Log.i(TAG, "Sample rate: ${getSampleRateNative()} Hz")
                Log.i(TAG, "Buffer size: ${getBufferSizeNative()} frames")
                Log.i(TAG, "Latency: ${"%.2f".format(getLatencyMillisNative())} ms")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio engine", e)
            false
        }
    }

    /**
     * Stop audio playback
     */
    fun stop() {
        try {
            stopNative()
            Log.i(TAG, "Audio engine stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio engine", e)
        }
    }

    /**
     * Pause audio playback
     */
    fun pause() {
        try {
            pauseNative()
            Log.i(TAG, "Audio engine paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio engine", e)
        }
    }

    /**
     * Resume audio playback
     */
    fun resume() {
        try {
            resumeNative()
            Log.i(TAG, "Audio engine resumed")
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming audio engine", e)
        }
    }

    /**
     * Set the frequency of the test tone
     */
    fun setFrequency(frequency: Float) {
        try {
            setFrequencyNative(frequency.coerceIn(20f, 20000f))
        } catch (e: Exception) {
            Log.e(TAG, "Error setting frequency", e)
        }
    }

    /**
     * Set the master volume
     */
    fun setVolume(volume: Float) {
        try {
            setVolumeNative(volume.coerceIn(0f, 1f))
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
        }
    }

    /**
     * Get the current sample rate
     */
    fun getSampleRate(): Int {
        return try {
            getSampleRateNative()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sample rate", e)
            0
        }
    }

    /**
     * Get the current buffer size
     */
    fun getBufferSize(): Int {
        return try {
            getBufferSizeNative()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting buffer size", e)
            0
        }
    }

    /**
     * Get the estimated latency
     */
    fun getLatencyMillis(): Double {
        return try {
            getLatencyMillisNative()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting latency", e)
            0.0
        }
    }

    /**
     * Check if audio is playing
     */
    fun isStarted(): Boolean {
        return try {
            isStartedNative()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if started", e)
            false
        }
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        try {
            destroyNative()
            Log.i(TAG, "Audio engine destroyed")
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying audio engine", e)
        }
    }
}
