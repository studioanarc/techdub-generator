// ui.js - Enhanced UI Controller
// Main application controller with advanced interactions

class DubTechnoUI {
  constructor() {
    this.synths = null;
    this.effects = null;
    this.generators = null;
    this.visualizer = null;
    this.presetManager = null;
    this.exporter = null;

    this.isPlaying = false;
    this.advancedMode = false;
    this.audioInitialized = false; // Track if audio is initialized

    // Undo/redo
    this.history = [];
    this.historyIndex = -1;
    this.maxHistory = 50;

    // Parameter debouncing
    this.parameterTimeouts = {};
    this.debounceTime = 50;

    // Tap tempo
    this.tapTimes = [];
    this.maxTapInterval = 2000;

    this.init();
  }

  async init() {
    // Setup UI event listeners FIRST (before audio init)
    this.setupEventListeners();

    // Setup keyboard shortcuts
    this.setupKeyboardShortcuts();

    console.log('Dub Techno Generator UI ready - click Play to start audio');
  }

  async initAudio() {
    if (this.audioInitialized) return;

    console.log('Initializing audio engine...');

    // Start Tone.js audio context
    await Tone.start();

    // Initialize audio modules
    this.synths = new DubSynths();
    this.effects = new EffectsChain();
    this.effects.init(); // Initialize effects chain
    this.generators = new DubGenerators(this.synths, this.effects);
    this.visualizer = new DubVisualizer('visualizer');
    this.presetManager = new PresetManager(this.synths, this.effects, this.generators);
    this.exporter = new AudioExporter(this.synths, this.effects, this.generators);

    // Connect synth master volume to effects chain
    const effectsInput = this.effects.getInput();
    this.synths.masterVolume.connect(effectsInput);

    // Load factory presets into UI
    this.loadFactoryPresetsUI();

    // Initialize with default preset
    await this.loadFactoryPreset('dark-minimal');

    this.audioInitialized = true;
    console.log('Audio engine initialized successfully');
  }

  setupEventListeners() {
    // Transport controls
    const playBtn = document.getElementById('play-btn');
    const stopBtn = document.getElementById('stop-btn');
    const randomizeBtn = document.getElementById('randomize-btn');

    if (playBtn) {
      playBtn.addEventListener('click', () => this.play());
    }

    if (stopBtn) {
      stopBtn.addEventListener('click', () => this.stop());
    }

    if (randomizeBtn) {
      randomizeBtn.addEventListener('click', () => this.randomize());
    }

    // Generator parameters
    this.bindParameter('tempo', (value) => {
      if (this.generators) this.generators.setTempo(parseFloat(value));
    });

    this.bindParameter('chaos', (value) => {
      if (this.generators) this.generators.setChaos(parseFloat(value));
    });

    this.bindParameter('bass-density', (value) => {
      if (this.generators) this.generators.setDensity('bass', parseFloat(value));
    });

    this.bindParameter('pad-density', (value) => {
      if (this.generators) this.generators.setDensity('pad', parseFloat(value));
    });

    this.bindParameter('stab-density', (value) => {
      if (this.generators) this.generators.setDensity('stab', parseFloat(value));
    });

    this.bindParameter('hat-density', (value) => {
      if (this.generators) this.generators.setDensity('hat', parseFloat(value));
    });

    this.bindParameter('kick-density', (value) => {
      if (this.generators) this.generators.setDensity('kick', parseFloat(value));
    });

    // Scale selection
    const scaleSelect = document.getElementById('scale');
    if (scaleSelect) {
      scaleSelect.addEventListener('change', (e) => {
        if (this.generators) this.generators.setScale(e.target.value);
      });
    }

    // === EFFECT TOGGLES ===

    // BitCrusher
    const bitcrusherToggle = document.getElementById('bitcrusher-toggle');
    if (bitcrusherToggle) {
      bitcrusherToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setBitCrusherEnabled(e.target.checked);
      });
    }

    // Distortion
    const distortionToggle = document.getElementById('distortion-toggle');
    if (distortionToggle) {
      distortionToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setDistortionEnabled(e.target.checked);
      });
    }

    // Chorus
    const chorusToggle = document.getElementById('chorus-toggle');
    if (chorusToggle) {
      chorusToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setChorusEnabled(e.target.checked);
      });
    }

    // Phaser
    const phaserToggle = document.getElementById('phaser-toggle');
    if (phaserToggle) {
      phaserToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setPhaserEnabled(e.target.checked);
      });
    }

    // Granular Processor
    const granularToggle = document.getElementById('granular-toggle');
    if (granularToggle) {
      granularToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setGranularEnabled(e.target.checked);
      });
    }

    // Auto Filter
    const filterToggle = document.getElementById('filter-toggle');
    if (filterToggle) {
      filterToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setFilterEnabled(e.target.checked);
      });
    }

    // Tape Echo
    const tapeEchoToggle = document.getElementById('tape-echo-toggle');
    if (tapeEchoToggle) {
      tapeEchoToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setTapeEchoEnabled(e.target.checked);
      });
    }

    // Ping Pong Delay
    const delayToggle = document.getElementById('delay-toggle');
    if (delayToggle) {
      delayToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setDelayEnabled(e.target.checked);
      });
    }

    // Probability Delay
    const probabilityDelayToggle = document.getElementById('probability-delay-toggle');
    if (probabilityDelayToggle) {
      probabilityDelayToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setProbabilityDelayEnabled(e.target.checked);
      });
    }

    // Spectral Freeze
    const spectralFreezeToggle = document.getElementById('spectral-freeze-toggle');
    if (spectralFreezeToggle) {
      spectralFreezeToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setSpectralFreezeEnabled(e.target.checked);
      });
    }

    // Spectral Freeze Active checkbox
    const spectralFreezeActive = document.getElementById('spectral-freeze-active');
    if (spectralFreezeActive) {
      spectralFreezeActive.addEventListener('change', (e) => {
        if (this.effects) this.effects.setSpectralFreezeActive(e.target.checked);
      });
    }

    // Convolution Reverb
    const convolutionReverbToggle = document.getElementById('convolution-reverb-toggle');
    if (convolutionReverbToggle) {
      convolutionReverbToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setConvolutionReverbEnabled(e.target.checked);
      });
    }

    // Algorithmic Reverb
    const reverbToggle = document.getElementById('reverb-toggle');
    if (reverbToggle) {
      reverbToggle.addEventListener('change', (e) => {
        if (this.effects) this.effects.setReverbEnabled(e.target.checked);
      });
    }

    // === EFFECT PARAMETERS ===

    // BitCrusher parameters
    this.bindParameter('bitcrusher-bits', (value) => {
      if (this.effects) this.effects.setBitCrusherBits(parseFloat(value));
    }, (value) => Math.round(value)); // Show as integer

    // Distortion parameters
    this.bindParameter('distortion-amount', (value) => {
      if (this.effects) this.effects.setDistortionAmount(parseFloat(value));
    });

    // Chorus parameters
    this.bindParameter('chorus-rate', (value) => {
      if (this.effects) this.effects.setChorusRate(parseFloat(value));
    });

    this.bindParameter('chorus-depth', (value) => {
      if (this.effects) this.effects.setChorusDepth(parseFloat(value));
    });

    // Phaser parameters
    this.bindParameter('phaser-rate', (value) => {
      if (this.effects) this.effects.setPhaserRate(parseFloat(value));
    });

    this.bindParameter('phaser-depth', (value) => {
      if (this.effects) this.effects.setPhaserDepth(parseFloat(value));
    });

    // Granular Processor parameters
    this.bindParameter('granular-size', (value) => {
      if (this.effects) this.effects.setGranularSize(parseFloat(value) / 1000); // Convert ms to seconds
    }, (value) => Math.round(value)); // Show as integer ms

    this.bindParameter('granular-density', (value) => {
      if (this.effects) this.effects.setGranularDensity(parseFloat(value));
    });

    this.bindParameter('granular-pitch', (value) => {
      if (this.effects) this.effects.setGranularPitch(parseFloat(value));
    }, (value) => {
      // Show with + sign for positive values
      const val = Math.round(value);
      return val > 0 ? `+${val}` : val.toString();
    });

    this.bindParameter('granular-wet', (value) => {
      if (this.effects) this.effects.setGranularWet(parseFloat(value));
    });

    // Auto Filter parameters
    this.bindParameter('filter-cutoff', (value) => {
      if (this.effects) this.effects.setFilterFrequency(parseFloat(value));
    });

    // Tape Echo parameters
    this.bindParameter('tape-echo-time', (value) => {
      if (this.effects) this.effects.setTapeEchoTime(parseFloat(value));
    }, (value) => {
      // Convert slider value to note value display
      const noteValues = {
        0.1: "16n",
        0.2: "8n.",
        0.3: "8n",
        0.4: "4n.",
        0.5: "4n",
        0.6: "2n.",
        0.7: "2n",
        0.8: "1n.",
        0.9: "1n",
        1.0: "1m"
      };

      const val = parseFloat(value);
      let closestNote = "8n";
      let minDiff = Infinity;

      for (let key in noteValues) {
        const diff = Math.abs(parseFloat(key) - val);
        if (diff < minDiff) {
          minDiff = diff;
          closestNote = noteValues[key];
        }
      }

      return closestNote;
    });

    this.bindParameter('tape-echo-feedback', (value) => {
      if (this.effects) this.effects.setTapeEchoFeedback(parseFloat(value));
    });

    // Ping Pong Delay parameters
    this.bindParameter('delay-feedback', (value) => {
      if (this.effects) this.effects.setDelayFeedback(parseFloat(value));
    });

    this.bindParameter('delay-wet', (value) => {
      if (this.effects) this.effects.setDelayWet(parseFloat(value));
    });

    // Probability Delay parameters
    this.bindParameter('probability-delay-probability', (value) => {
      if (this.effects) this.effects.setProbabilityDelayProbability(parseFloat(value));
    });

    this.bindParameter('probability-delay-rate', (value) => {
      if (this.effects) this.effects.setProbabilityDelayRate(parseFloat(value));
    });

    this.bindParameter('probability-delay-feedback', (value) => {
      if (this.effects) this.effects.setProbabilityDelayFeedback(parseFloat(value));
    });

    this.bindParameter('probability-delay-wet', (value) => {
      if (this.effects) this.effects.setProbabilityDelayWet(parseFloat(value));
    });

    // Spectral Freeze parameters
    this.bindParameter('spectral-freeze-decay', (value) => {
      if (this.effects) this.effects.setSpectralFreezeDecay(parseFloat(value));
    });

    this.bindParameter('spectral-freeze-filter', (value) => {
      if (this.effects) this.effects.setSpectralFreezeFilter(parseFloat(value));
    }, (value) => Math.round(value)); // Show as integer Hz

    this.bindParameter('spectral-freeze-wet', (value) => {
      if (this.effects) this.effects.setSpectralFreezeWet(parseFloat(value));
    });

    // Convolution Reverb parameters
    this.bindParameter('convolution-reverb-decay', (value) => {
      if (this.effects) this.effects.setConvolutionReverbDecay(parseFloat(value));
    });

    // Algorithmic Reverb parameters
    this.bindParameter('reverb-decay', (value) => {
      if (this.effects) this.effects.setReverbDecay(parseFloat(value));
    });

    this.bindParameter('reverb-wet', (value) => {
      if (this.effects) this.effects.setReverbWet(parseFloat(value));
    });

    // Master Volume
    this.bindParameter('master-volume', (value) => {
      if (this.effects) {
        const db = parseFloat(value);
        this.effects.setMasterVolume(db);
      }
    });

    // Preset controls
    const savePresetBtn = document.getElementById('save-preset-btn');
    const loadPresetBtn = document.getElementById('load-preset-btn');
    const exportPresetBtn = document.getElementById('export-preset-btn');
    const importPresetBtn = document.getElementById('import-preset-btn');

    if (savePresetBtn) {
      savePresetBtn.addEventListener('click', () => this.savePreset());
    }

    if (loadPresetBtn) {
      loadPresetBtn.addEventListener('click', () => this.loadPresetDialog());
    }

    if (exportPresetBtn) {
      exportPresetBtn.addEventListener('click', () => this.exportPreset());
    }

    if (importPresetBtn) {
      importPresetBtn.addEventListener('click', () => {
        document.getElementById('preset-file-input').click();
      });
    }

    const presetFileInput = document.getElementById('preset-file-input');
    if (presetFileInput) {
      presetFileInput.addEventListener('change', (e) => this.importPreset(e));
    }

    // Export controls
    const exportBtn = document.getElementById('export-btn');
    if (exportBtn) {
      exportBtn.addEventListener('click', () => this.exportAudio());
    }

    // Advanced mode toggle
    const advancedToggle = document.getElementById('advanced-toggle');
    if (advancedToggle) {
      advancedToggle.addEventListener('change', (e) => {
        this.toggleAdvancedMode(e.target.checked);
      });
    }

    // Tab switching
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        this.switchTab(e.target.dataset.tab);
      });
    });

    // Tap tempo
    const tapTempoBtn = document.getElementById('tap-tempo-btn');
    if (tapTempoBtn) {
      tapTempoBtn.addEventListener('click', () => this.tapTempo());
    }
  }

  // Bind parameter with debouncing
  bindParameter(id, callback, customFormatter = null) {
    const element = document.getElementById(id);
    if (!element) return;

    const updateValue = () => {
      const value = element.value;
      callback(value);

      // Update value display if exists
      const display = document.getElementById(`${id}-value`);
      if (display) {
        if (customFormatter) {
          display.textContent = customFormatter(value);
        } else {
          display.textContent = parseFloat(value).toFixed(2);
        }
      }
    };

    element.addEventListener('input', () => {
      // Debounce
      if (this.parameterTimeouts[id]) {
        clearTimeout(this.parameterTimeouts[id]);
      }

      this.parameterTimeouts[id] = setTimeout(() => {
        updateValue();
        this.saveToHistory();
      }, this.debounceTime);
    });

    // Initial value
    updateValue();
  }

  // Play/Stop
  async play() {
    if (this.isPlaying) return;

    // Initialize audio on first play
    if (!this.audioInitialized) {
      const playBtn = document.getElementById('play-btn');
      if (playBtn) {
        playBtn.textContent = 'Loading...';
        playBtn.disabled = true;
      }

      try {
        await this.initAudio();
      } catch (error) {
        console.error('Failed to initialize audio:', error);
        if (playBtn) {
          playBtn.textContent = 'Error';
          playBtn.disabled = false;
        }
        return;
      }

      if (playBtn) {
        playBtn.disabled = false;
      }
    }

    await this.generators.start();
    this.visualizer.start();
    this.isPlaying = true;

    const playBtn = document.getElementById('play-btn');
    if (playBtn) {
      playBtn.classList.add('active');
      playBtn.textContent = 'Playing...';
    }
  }

  stop() {
    if (!this.isPlaying) return;

    this.generators.stop();
    this.visualizer.stop();
    this.isPlaying = false;

    const playBtn = document.getElementById('play-btn');
    if (playBtn) {
      playBtn.classList.remove('active');
      playBtn.textContent = 'Play';
    }
  }

  // Randomize
  randomize() {
    if (!this.audioInitialized) {
      console.warn('Audio not initialized yet. Click Play first.');
      return;
    }
    this.generators.randomize();
    this.updateUIFromState();
    this.saveToHistory();
  }

  // Factory presets
  loadFactoryPresetsUI() {
    const presetList = document.getElementById('factory-presets');
    if (!presetList) return;

    const names = this.presetManager.getFactoryPresetNames();
    names.forEach(name => {
      const btn = document.createElement('button');
      btn.className = 'preset-btn';
      btn.textContent = this.presetManager.getFactoryPreset(name).name;
      btn.addEventListener('click', () => this.loadFactoryPreset(name));
      presetList.appendChild(btn);
    });
  }

  async loadFactoryPreset(name) {
    const preset = this.presetManager.getFactoryPreset(name);
    if (!preset) return;

    await this.presetManager.applyPreset(preset);
    this.updateUIFromState();
    this.saveToHistory();

    this.showNotification(`Loaded preset: ${preset.name}`);
  }

  // Preset management
  savePreset() {
    const name = prompt('Enter preset name:');
    if (!name) return;

    const preset = this.presetManager.capturePreset(name);
    this.presetManager.saveToLocalStorage(preset, name.toLowerCase().replace(/\s+/g, '-'));

    this.showNotification(`Saved preset: ${name}`);
  }

  loadPresetDialog() {
    const presetList = this.presetManager.getPresetList();
    if (presetList.length === 0) {
      alert('No saved presets found');
      return;
    }

    const name = prompt(`Available presets:\n${presetList.join('\n')}\n\nEnter preset name to load:`);
    if (!name) return;

    const preset = this.presetManager.loadFromLocalStorage(name);
    if (preset) {
      this.presetManager.applyPreset(preset);
      this.updateUIFromState();
      this.saveToHistory();
      this.showNotification(`Loaded preset: ${preset.name}`);
    } else {
      alert('Preset not found');
    }
  }

  exportPreset() {
    const name = prompt('Enter preset name for export:');
    if (!name) return;

    const preset = this.presetManager.capturePreset(name);
    this.presetManager.exportPreset(preset);

    this.showNotification(`Exported preset: ${name}`);
  }

  async importPreset(event) {
    const file = event.target.files[0];
    if (!file) return;

    try {
      const preset = await this.presetManager.importPreset(file);
      await this.presetManager.applyPreset(preset);
      this.updateUIFromState();
      this.saveToHistory();

      this.showNotification(`Imported preset: ${preset.name}`);
    } catch (error) {
      alert('Error importing preset: ' + error.message);
    }

    // Reset file input
    event.target.value = '';
  }

  // Audio export
  async exportAudio() {
    const duration = parseInt(prompt('Export duration (seconds):', '60'));
    if (!duration || duration <= 0) return;

    const exportBtn = document.getElementById('export-btn');
    if (exportBtn) {
      exportBtn.disabled = true;
      exportBtn.textContent = 'Exporting...';
    }

    const progressBar = document.getElementById('export-progress');
    if (progressBar) {
      progressBar.style.display = 'block';
      progressBar.value = 0;
    }

    try {
      const blob = await this.exporter.export(duration, (progress) => {
        if (progressBar) {
          progressBar.value = progress;
        }
        if (exportBtn) {
          exportBtn.textContent = `Exporting... ${Math.round(progress)}%`;
        }
      });

      this.exporter.downloadWave(blob, `dubtechno-${Date.now()}.wav`);
      this.showNotification('Export complete!');

    } catch (error) {
      alert('Error exporting audio: ' + error.message);
    } finally {
      if (exportBtn) {
        exportBtn.disabled = false;
        exportBtn.textContent = 'Export Audio';
      }
      if (progressBar) {
        progressBar.style.display = 'none';
      }
    }
  }

  // Update UI from current state
  updateUIFromState() {
    const settings = this.generators.getSettings();

    this.setInputValue('tempo', settings.tempo);
    this.setInputValue('chaos', settings.chaos);
    this.setInputValue('bass-density', settings.density.bass);
    this.setInputValue('pad-density', settings.density.pad);
    this.setInputValue('stab-density', settings.density.stab);
    this.setInputValue('hat-density', settings.density.hat);

    const effectsSettings = this.effects.getSettings();
    this.setInputValue('delay-feedback', effectsSettings.delay.feedback);
    this.setInputValue('delay-wet', effectsSettings.delay.wet);
    this.setInputValue('reverb-decay', effectsSettings.reverb.decay);
    this.setInputValue('reverb-wet', effectsSettings.reverb.wet);
    this.setInputValue('filter-cutoff', effectsSettings.filter.frequency);
  }

  setInputValue(id, value) {
    const element = document.getElementById(id);
    if (element) {
      element.value = value;

      const display = document.getElementById(`${id}-value`);
      if (display) {
        display.textContent = parseFloat(value).toFixed(2);
      }
    }
  }

  // Undo/Redo
  saveToHistory() {
    const state = {
      generator: this.generators.getSettings(),
      effects: this.effects.getSettings()
    };

    // Remove future history if we're not at the end
    if (this.historyIndex < this.history.length - 1) {
      this.history = this.history.slice(0, this.historyIndex + 1);
    }

    this.history.push(state);

    // Limit history size
    if (this.history.length > this.maxHistory) {
      this.history.shift();
    } else {
      this.historyIndex++;
    }
  }

  undo() {
    if (this.historyIndex > 0) {
      this.historyIndex--;
      this.restoreFromHistory(this.history[this.historyIndex]);
    }
  }

  redo() {
    if (this.historyIndex < this.history.length - 1) {
      this.historyIndex++;
      this.restoreFromHistory(this.history[this.historyIndex]);
    }
  }

  restoreFromHistory(state) {
    // Apply state without saving to history
    // Implementation would restore all parameters
    this.updateUIFromState();
  }

  // Tap tempo
  tapTempo() {
    const now = Date.now();

    // Clear old taps
    this.tapTimes = this.tapTimes.filter(t => now - t < this.maxTapInterval);

    this.tapTimes.push(now);

    if (this.tapTimes.length >= 4) {
      // Calculate average interval
      let totalInterval = 0;
      for (let i = 1; i < this.tapTimes.length; i++) {
        totalInterval += this.tapTimes[i] - this.tapTimes[i - 1];
      }
      const avgInterval = totalInterval / (this.tapTimes.length - 1);
      const bpm = 60000 / avgInterval;

      this.generators.setTempo(Math.round(bpm));
      this.setInputValue('tempo', Math.round(bpm));

      this.showNotification(`Tempo set to ${Math.round(bpm)} BPM`);
    }
  }

  // Advanced mode toggle
  toggleAdvancedMode(enabled) {
    this.advancedMode = enabled;
    const advancedPanel = document.getElementById('advanced-panel');
    if (advancedPanel) {
      advancedPanel.style.display = enabled ? 'block' : 'none';
    }
  }

  // Tab switching
  switchTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
      tab.classList.remove('active');
    });

    // Remove active from all buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.classList.remove('active');
    });

    // Show selected tab
    const tab = document.getElementById(`${tabName}-tab`);
    if (tab) {
      tab.classList.add('active');
    }

    // Activate button
    const btn = document.querySelector(`[data-tab="${tabName}"]`);
    if (btn) {
      btn.classList.add('active');
    }
  }

  // Keyboard shortcuts
  setupKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
      // Ignore if typing in input
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
        return;
      }

      switch (e.key) {
        case ' ':
          e.preventDefault();
          if (this.isPlaying) {
            this.stop();
          } else {
            this.play();
          }
          break;

        case 'r':
        case 'R':
          e.preventDefault();
          this.randomize();
          break;

        case 'z':
          if (e.ctrlKey || e.metaKey) {
            e.preventDefault();
            if (e.shiftKey) {
              this.redo();
            } else {
              this.undo();
            }
          }
          break;

        case 's':
          if (e.ctrlKey || e.metaKey) {
            e.preventDefault();
            this.savePreset();
          }
          break;
      }
    });
  }

  // Notifications
  showNotification(message) {
    const notif = document.getElementById('notification');
    if (notif) {
      notif.textContent = message;
      notif.classList.add('show');

      setTimeout(() => {
        notif.classList.remove('show');
      }, 3000);
    } else {
      console.log('Notification:', message);
    }
  }
}

// Initialize on load
window.addEventListener('DOMContentLoaded', () => {
  window.dubTechnoUI = new DubTechnoUI();
});
