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
    // Initialize audio modules
    this.synths = new DubSynths();
    this.effects = new EffectsChain();
    this.effects.init(); // Initialize effects chain
    this.generators = new DubGenerators(this.synths, this.effects);
    this.visualizer = new DubVisualizer('visualizer');
    this.presetManager = new PresetManager(this.synths, this.effects, this.generators);
    this.exporter = new AudioExporter(this.synths, this.effects, this.generators);

    // Connect synth master volume to effects chain
    // (All synths are already connected to synths.masterVolume)
    const effectsInput = this.effects.getInput();
    this.synths.masterVolume.connect(effectsInput);

    // Setup UI event listeners
    this.setupEventListeners();

    // Load factory presets into UI
    this.loadFactoryPresetsUI();

    // Initialize with default preset
    this.loadFactoryPreset('dark-minimal');

    // Setup keyboard shortcuts
    this.setupKeyboardShortcuts();

    console.log('Dub Techno Generator initialized');
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
      this.generators.setTempo(parseFloat(value));
    });

    this.bindParameter('chaos', (value) => {
      this.generators.setChaos(parseFloat(value));
    });

    this.bindParameter('bass-density', (value) => {
      this.generators.setDensity('bass', parseFloat(value));
    });

    this.bindParameter('pad-density', (value) => {
      this.generators.setDensity('pad', parseFloat(value));
    });

    this.bindParameter('stab-density', (value) => {
      this.generators.setDensity('stab', parseFloat(value));
    });

    this.bindParameter('hat-density', (value) => {
      this.generators.setDensity('hat', parseFloat(value));
    });

    this.bindParameter('kick-density', (value) => {
      this.generators.setDensity('kick', parseFloat(value));
    });

    // Scale selection
    const scaleSelect = document.getElementById('scale');
    if (scaleSelect) {
      scaleSelect.addEventListener('change', (e) => {
        this.generators.setScale(e.target.value);
      });
    }

    // Effects parameters
    this.bindParameter('delay-feedback', (value) => {
      this.effects.updateDelay({ feedback: parseFloat(value) });
    });

    this.bindParameter('delay-wet', (value) => {
      this.effects.updateDelay({ wet: parseFloat(value) });
    });

    this.bindParameter('reverb-decay', (value) => {
      this.effects.updateReverb({ decay: parseFloat(value) });
    });

    this.bindParameter('reverb-wet', (value) => {
      this.effects.updateReverb({ wet: parseFloat(value) });
    });

    this.bindParameter('filter-cutoff', (value) => {
      this.effects.updateFilter({ frequency: parseFloat(value) });
    });

    this.bindParameter('distortion-amount', (value) => {
      this.effects.updateDistortion({ amount: parseFloat(value) });
    });

    this.bindParameter('master-volume', (value) => {
      this.synths.setMasterVolume(parseFloat(value));
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
  bindParameter(id, callback) {
    const element = document.getElementById(id);
    if (!element) return;

    const updateValue = () => {
      const value = element.value;
      callback(value);

      // Update value display if exists
      const display = document.getElementById(`${id}-value`);
      if (display) {
        display.textContent = parseFloat(value).toFixed(2);
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
