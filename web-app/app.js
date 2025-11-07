/**
 * Dub Techno Generator - Main Application
 * Handles UI interaction, Tone.js initialization, and visualization
 */

// Application state
let audioInitialized = false;
let isPlaying = false;
let analyzer = null;
let waveformCanvas = null;
let waveformContext = null;

/**
 * Initialize the application
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log('Dub Techno Generator loaded');

    // Get canvas for waveform
    waveformCanvas = document.getElementById('waveform');
    waveformContext = waveformCanvas.getContext('2d');

    // Set up all UI event listeners
    setupEventListeners();
});

/**
 * Initialize Tone.js audio context
 * Must be called after user interaction (browser requirement)
 */
async function initAudio() {
    if (audioInitialized) return;

    try {
        console.log('Initializing audio context...');

        // Start Tone.js audio context
        await Tone.start();
        console.log('✓ Tone.js audio context started');

        // Initialize effects chain
        const effectsInput = effectsChain.init();

        // Initialize synth engine
        synthEngine.init(effectsInput);

        // Create analyzer for visualization
        analyzer = new Tone.Analyser('waveform', 1024);
        effectsChain.effects.compressor.connect(analyzer);

        // Start visualization loop
        drawWaveform();

        // Set initial effect states from UI
        applyEffectStates();

        audioInitialized = true;
        console.log('✓ Audio system initialized');

        // Update UI
        document.getElementById('startBtn').textContent = 'START';

    } catch (error) {
        console.error('Failed to initialize audio:', error);
        alert('Failed to initialize audio. Please refresh and try again.');
    }
}

/**
 * Set up all UI event listeners
 */
function setupEventListeners() {
    // Transport controls
    document.getElementById('startBtn').addEventListener('click', handleStart);
    document.getElementById('stopBtn').addEventListener('click', handleStop);

    // Tempo control
    const tempoSlider = document.getElementById('tempoSlider');
    tempoSlider.addEventListener('input', (e) => {
        const bpm = parseInt(e.target.value);
        document.getElementById('tempoValue').textContent = bpm;
        if (audioInitialized) {
            Tone.Transport.bpm.value = bpm;
        }
    });

    // Musical parameters
    document.getElementById('rootNote').addEventListener('change', (e) => {
        if (audioInitialized) {
            synthEngine.updateScale(e.target.value, synthEngine.currentScaleType);
        }
    });

    document.getElementById('scale').addEventListener('change', (e) => {
        if (audioInitialized) {
            synthEngine.updateScale(synthEngine.currentRoot, e.target.value);
        }
    });

    // Master volume
    const volumeSlider = document.getElementById('volumeSlider');
    volumeSlider.addEventListener('input', (e) => {
        const db = parseInt(e.target.value);
        document.getElementById('volumeValue').textContent = db;
        if (audioInitialized) {
            effectsChain.setMasterVolume(db);
        }
    });

    // Delay effect controls
    document.getElementById('delayToggle').addEventListener('change', (e) => {
        if (audioInitialized) {
            effectsChain.setDelayEnabled(e.target.checked);
        }
    });

    document.getElementById('delayTime').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('delayTimeValue').textContent = value.toFixed(2);
        if (audioInitialized) {
            effectsChain.setDelayTime(value);
        }
    });

    document.getElementById('delayFeedback').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('delayFeedbackValue').textContent = value.toFixed(2);
        if (audioInitialized) {
            effectsChain.setDelayFeedback(value);
        }
    });

    document.getElementById('delayWet').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('delayWetValue').textContent = value.toFixed(2);
        if (audioInitialized) {
            effectsChain.setDelayWet(value);
        }
    });

    // Reverb effect controls
    document.getElementById('reverbToggle').addEventListener('change', (e) => {
        if (audioInitialized) {
            effectsChain.setReverbEnabled(e.target.checked);
        }
    });

    document.getElementById('reverbDecay').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('reverbDecayValue').textContent = value.toFixed(1);
        if (audioInitialized) {
            effectsChain.setReverbDecay(value);
        }
    });

    document.getElementById('reverbWet').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('reverbWetValue').textContent = value.toFixed(2);
        if (audioInitialized) {
            effectsChain.setReverbWet(value);
        }
    });

    // Filter effect controls
    document.getElementById('filterToggle').addEventListener('change', (e) => {
        if (audioInitialized) {
            effectsChain.setFilterEnabled(e.target.checked);
        }
    });

    document.getElementById('filterFreq').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('filterFreqValue').textContent = value.toFixed(1);
        if (audioInitialized) {
            effectsChain.setFilterFrequency(value);
        }
    });

    document.getElementById('filterDepth').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('filterDepthValue').textContent = value.toFixed(2);
        if (audioInitialized) {
            effectsChain.setFilterDepth(value);
        }
    });

    document.getElementById('filterWet').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('filterWetValue').textContent = value.toFixed(2);
        if (audioInitialized) {
            effectsChain.setFilterWet(value);
        }
    });

    // Distortion effect controls
    document.getElementById('distortionToggle').addEventListener('change', (e) => {
        if (audioInitialized) {
            effectsChain.setDistortionEnabled(e.target.checked);
        }
    });

    document.getElementById('distortionAmount').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('distortionAmountValue').textContent = value.toFixed(2);
        if (audioInitialized) {
            effectsChain.setDistortionAmount(value);
        }
    });

    document.getElementById('distortionWet').addEventListener('input', (e) => {
        const value = parseFloat(e.target.value);
        document.getElementById('distortionWetValue').textContent = value.toFixed(2);
        if (audioInitialized) {
            effectsChain.setDistortionWet(value);
        }
    });
}

/**
 * Apply initial effect states from UI toggles
 */
function applyEffectStates() {
    effectsChain.setDelayEnabled(document.getElementById('delayToggle').checked);
    effectsChain.setReverbEnabled(document.getElementById('reverbToggle').checked);
    effectsChain.setFilterEnabled(document.getElementById('filterToggle').checked);
    effectsChain.setDistortionEnabled(document.getElementById('distortionToggle').checked);
}

/**
 * Handle start button click
 */
async function handleStart() {
    try {
        // Initialize audio on first click
        if (!audioInitialized) {
            document.getElementById('startBtn').textContent = 'LOADING...';
            await initAudio();
        }

        if (isPlaying) return;

        // Set tempo from UI
        const bpm = parseInt(document.getElementById('tempoSlider').value);
        Tone.Transport.bpm.value = bpm;

        // Start the transport
        Tone.Transport.start();

        // Start synth sequences
        synthEngine.start();

        isPlaying = true;

        // Update UI
        document.getElementById('startBtn').classList.add('playing');
        document.querySelector('h1').classList.add('playing');

        console.log('✓ Playback started');

    } catch (error) {
        console.error('Failed to start playback:', error);
        alert('Failed to start playback. Please try again.');
    }
}

/**
 * Handle stop button click
 */
function handleStop() {
    if (!isPlaying) return;

    // Stop synth sequences
    synthEngine.stop();

    // Stop the transport
    Tone.Transport.stop();

    isPlaying = false;

    // Update UI
    document.getElementById('startBtn').classList.remove('playing');
    document.querySelector('h1').classList.remove('playing');

    console.log('✓ Playback stopped');
}

/**
 * Draw waveform visualization
 */
function drawWaveform() {
    requestAnimationFrame(drawWaveform);

    if (!analyzer) return;

    const width = waveformCanvas.width;
    const height = waveformCanvas.height;
    const values = analyzer.getValue();

    // Clear canvas
    waveformContext.fillStyle = '#1a1a1a';
    waveformContext.fillRect(0, 0, width, height);

    // Draw waveform
    waveformContext.lineWidth = 2;
    waveformContext.strokeStyle = '#00ff88';
    waveformContext.beginPath();

    const sliceWidth = width / values.length;
    let x = 0;

    for (let i = 0; i < values.length; i++) {
        const v = (values[i] + 1) / 2; // Normalize to 0-1
        const y = v * height;

        if (i === 0) {
            waveformContext.moveTo(x, y);
        } else {
            waveformContext.lineTo(x, y);
        }

        x += sliceWidth;
    }

    waveformContext.stroke();

    // Draw center line
    waveformContext.strokeStyle = '#404040';
    waveformContext.lineWidth = 1;
    waveformContext.beginPath();
    waveformContext.moveTo(0, height / 2);
    waveformContext.lineTo(width, height / 2);
    waveformContext.stroke();
}

/**
 * Cleanup on page unload
 */
window.addEventListener('beforeunload', () => {
    if (audioInitialized) {
        synthEngine.dispose();
        effectsChain.dispose();
        Tone.Transport.stop();
    }
});

// Log initial state
console.log('App initialized. Click START to begin.');
