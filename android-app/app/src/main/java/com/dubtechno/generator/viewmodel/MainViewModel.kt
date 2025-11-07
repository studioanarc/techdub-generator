package com.dubtechno.generator.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dubtechno.generator.audio.AudioEngine
import com.dubtechno.generator.data.*
import com.dubtechno.generator.generative.GenerativeEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Main ViewModel managing the entire app state
 * Coordinates between AudioEngine, GenerativeEngine, and UI
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Repository
    val presetRepository = PresetRepository(application)

    // Audio engines (placeholder - will be connected to native code)
    private val audioEngine = AudioEngine.getInstance()
    private lateinit var generativeEngine: GenerativeEngine

    // UI State - Playback
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // UI State - Generative Controls
    private val _config = MutableStateFlow(GenerativeConfig())
    val config: StateFlow<GenerativeConfig> = _config.asStateFlow()

    // UI State - Synth Settings
    private val _synthSettings = MutableStateFlow(SynthSettings())
    val synthSettings: StateFlow<SynthSettings> = _synthSettings.asStateFlow()

    // UI State - Effects
    private val _effectSettings = MutableStateFlow(EffectSettings())
    val effectSettings: StateFlow<EffectSettings> = _effectSettings.asStateFlow()

    // UI State - Presets
    private val _presets = MutableStateFlow<List<Preset>>(emptyList())
    val presets: StateFlow<List<Preset>> = _presets.asStateFlow()

    private val _currentPreset = MutableStateFlow<Preset?>(null)
    val currentPreset: StateFlow<Preset?> = _currentPreset.asStateFlow()

    // UI State - Visualizer
    private val _waveformData = MutableStateFlow(FloatArray(256) { 0f })
    val waveformData: StateFlow<FloatArray> = _waveformData.asStateFlow()

    // UI State - Messages
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var visualizerJob: Job? = null

    init {
        initialize()
    }

    /**
     * Initialize the audio engine and load presets
     */
    private fun initialize() {
        viewModelScope.launch {
            try {
                // Initialize audio engine
                audioEngine.initialize(44100, 256)

                // Initialize generative engine
                generativeEngine = GenerativeEngine(audioEngine, _config.value)

                // Load factory presets
                presetRepository.initializeFactoryPresets()

                // Load all presets
                loadPresets()

                _isInitialized.value = true
                showMessage("Audio engine initialized")
            } catch (e: Exception) {
                showMessage("Failed to initialize: ${e.message}")
            }
        }
    }

    /**
     * Play/Start the generative engine
     */
    fun play() {
        if (!_isInitialized.value) {
            showMessage("Please wait for initialization")
            return
        }

        viewModelScope.launch {
            try {
                audioEngine.start()
                generativeEngine.start()
                _isPlaying.value = true
                startVisualizer()
                showMessage("Playing")
            } catch (e: Exception) {
                showMessage("Failed to start: ${e.message}")
            }
        }
    }

    /**
     * Stop playback
     */
    fun stop() {
        viewModelScope.launch {
            try {
                generativeEngine.stop()
                audioEngine.stop()
                _isPlaying.value = false
                stopVisualizer()
                showMessage("Stopped")
            } catch (e: Exception) {
                showMessage("Failed to stop: ${e.message}")
            }
        }
    }

    /**
     * Randomize generative parameters
     */
    fun randomize() {
        viewModelScope.launch {
            generativeEngine.randomize()
            _config.value = generativeEngine.getConfig()
            showMessage("Randomized!")
        }
    }

    // ==================== Generative Controls ====================

    fun setTempo(bpm: Float) {
        _config.value = _config.value.copy(tempo = bpm)
        updateGenerativeConfig()
    }

    fun setRootNote(note: Int) {
        _config.value = _config.value.copy(rootNote = note)
        updateGenerativeConfig()
    }

    fun setScale(scale: ScaleType) {
        _config.value = _config.value.copy(scale = scale)
        updateGenerativeConfig()
    }

    fun setDensity(density: Float) {
        _config.value = _config.value.copy(density = density)
        updateGenerativeConfig()
    }

    fun setChaos(chaos: Float) {
        _config.value = _config.value.copy(chaos = chaos)
        updateGenerativeConfig()
    }

    fun setSwing(swing: Float) {
        _config.value = _config.value.copy(swing = swing)
        updateGenerativeConfig()
    }

    fun setEuclideanDensity(density: Float) {
        _config.value = _config.value.copy(euclideanDensity = density)
        updateGenerativeConfig()
    }

    fun setNoteLength(length: Float) {
        _config.value = _config.value.copy(noteLength = length)
        updateGenerativeConfig()
    }

    fun setHumanize(amount: Float) {
        _config.value = _config.value.copy(humanize = amount)
        updateGenerativeConfig()
    }

    private fun updateGenerativeConfig() {
        if (::generativeEngine.isInitialized) {
            generativeEngine.updateConfig(_config.value)
        }
    }

    // ==================== Synth Controls ====================

    fun updateBassSynth(params: BassSynthParams) {
        _synthSettings.value = _synthSettings.value.copy(bass = params)
        audioEngine.updateBassSynth(params)
    }

    fun updateMelodySynth(params: MelodySynthParams) {
        _synthSettings.value = _synthSettings.value.copy(melody = params)
        audioEngine.updateMelodySynth(params)
    }

    fun updateChordSynth(params: ChordSynthParams) {
        _synthSettings.value = _synthSettings.value.copy(chords = params)
        audioEngine.updateChordSynth(params)
    }

    fun setMasterVolume(volume: Float) {
        _synthSettings.value = _synthSettings.value.copy(masterVolume = volume)
        audioEngine.setMasterVolume(volume)
    }

    // ==================== Effect Controls ====================

    fun updateDelay(params: DelayParams) {
        _effectSettings.value = _effectSettings.value.copy(delay = params)
        audioEngine.updateDelay(params)
    }

    fun updateReverb(params: ReverbParams) {
        _effectSettings.value = _effectSettings.value.copy(reverb = params)
        audioEngine.updateReverb(params)
    }

    fun updateChorus(params: ChorusParams) {
        _effectSettings.value = _effectSettings.value.copy(chorus = params)
        audioEngine.updateChorus(params)
    }

    fun updateFilter(params: FilterParams) {
        _effectSettings.value = _effectSettings.value.copy(filter = params)
        audioEngine.updateFilter(params)
    }

    fun updateCompressor(params: CompressorParams) {
        _effectSettings.value = _effectSettings.value.copy(compressor = params)
        audioEngine.updateCompressor(params)
    }

    fun updateSaturation(params: SaturationParams) {
        _effectSettings.value = _effectSettings.value.copy(saturation = params)
        audioEngine.updateSaturation(params)
    }

    // ==================== Preset Management ====================

    fun loadPresets() {
        viewModelScope.launch {
            presetRepository.getAllPresets().onSuccess { presetList ->
                _presets.value = presetList
            }
        }
    }

    fun loadPreset(preset: Preset) {
        viewModelScope.launch {
            _config.value = preset.config
            _synthSettings.value = preset.synthSettings
            _effectSettings.value = preset.effectSettings
            _currentPreset.value = preset

            // Apply to engines
            updateGenerativeConfig()
            audioEngine.updateBassSynth(preset.synthSettings.bass)
            audioEngine.updateMelodySynth(preset.synthSettings.melody)
            audioEngine.updateChordSynth(preset.synthSettings.chords)
            audioEngine.updateDelay(preset.effectSettings.delay)
            audioEngine.updateReverb(preset.effectSettings.reverb)

            showMessage("Loaded preset: ${preset.name}")
        }
    }

    fun saveCurrentAsPreset(name: String) {
        viewModelScope.launch {
            val preset = Preset(
                name = name,
                config = _config.value,
                synthSettings = _synthSettings.value,
                effectSettings = _effectSettings.value
            )

            presetRepository.savePreset(preset).onSuccess {
                loadPresets()
                _currentPreset.value = preset
                showMessage("Saved preset: $name")
            }.onFailure {
                showMessage("Failed to save preset")
            }
        }
    }

    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            presetRepository.deletePreset(preset.id).onSuccess {
                loadPresets()
                if (_currentPreset.value?.id == preset.id) {
                    _currentPreset.value = null
                }
                showMessage("Deleted preset: ${preset.name}")
            }
        }
    }

    fun exportPreset(preset: Preset): String {
        return presetRepository.exportPreset(preset)
    }

    fun importPreset(jsonString: String) {
        viewModelScope.launch {
            presetRepository.importPreset(jsonString).onSuccess { preset ->
                loadPresets()
                showMessage("Imported preset: ${preset.name}")
            }.onFailure {
                showMessage("Failed to import preset")
            }
        }
    }

    // ==================== Visualizer ====================

    private fun startVisualizer() {
        visualizerJob = viewModelScope.launch {
            while (_isPlaying.value) {
                val data = audioEngine.getWaveformData()
                _waveformData.value = data
                delay(50) // Update at ~20 FPS
            }
        }
    }

    private fun stopVisualizer() {
        visualizerJob?.cancel()
        visualizerJob = null
        _waveformData.value = FloatArray(256) { 0f }
    }

    // ==================== Utilities ====================

    private fun showMessage(msg: String) {
        _message.value = msg
        viewModelScope.launch {
            delay(3000)
            _message.value = null
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    override fun onCleared() {
        super.onCleared()
        if (_isPlaying.value) {
            stop()
        }
        audioEngine.shutdown()
    }
}
