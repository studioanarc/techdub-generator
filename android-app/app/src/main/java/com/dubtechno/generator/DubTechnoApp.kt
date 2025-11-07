package com.dubtechno.generator

import android.app.Application
import android.util.Log

class DubTechnoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DubTechnoApp initialized")
    }

    override fun onTerminate() {
        super.onTerminate()
        // Clean up audio engine
        try {
            AudioEngine.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying audio engine", e)
        }
    }

    companion object {
        private const val TAG = "DubTechnoApp"
    }
}
