// presets.js - Preset Management System
// Save, load, and manage presets for the dub techno generator

class PresetManager {
  constructor(synths, effects, generators) {
    this.synths = synths;
    this.effects = effects;
    this.generators = generators;

    this.currentPreset = null;
    this.presetHistory = [];
    this.maxHistory = 20;

    // Load factory presets
    this.factoryPresets = this.loadFactoryPresets();
  }

  // Capture current state as preset
  capturePreset(name, author = 'User') {
    const preset = {
      name: name,
      author: author,
      version: '1.0',
      timestamp: Date.now(),

      // Generator settings
      generator: this.generators.getSettings(),

      // Effects settings
      effects: this.effects.getSettings(),

      // Synth settings (simplified - main parameters)
      synths: {
        bass: {
          filterCutoff: this.synths.synths.bass.filter.frequency.value,
          filterQ: this.synths.synths.bass.filter.Q.value,
          attack: this.synths.synths.bass.envelope.attack,
          decay: this.synths.synths.bass.envelope.decay,
          sustain: this.synths.synths.bass.envelope.sustain,
          release: this.synths.synths.bass.envelope.release
        },
        masterVolume: this.synths.masterVolume.volume.value
      }
    };

    this.currentPreset = preset;
    return preset;
  }

  // Apply preset to all modules
  async applyPreset(preset, smooth = true) {
    if (!preset) return;

    const transitionTime = smooth ? 0.5 : 0;

    // Apply generator settings
    if (preset.generator) {
      this.generators.setRoot(preset.generator.root || 'C2');

      if (preset.generator.scale) {
        // Find scale name that matches the scale array
        const scaleMap = {
          'C,D,Eb,F,G,Ab,Bb': 'minor',
          'C,D,Eb,F,G,A,Bb': 'dorian',
          'C,Db,Eb,F,G,Ab,Bb': 'phrygian',
          'C,D,E,F,G,A,B': 'major',
          'C,Eb,F,G,Bb': 'minor-pentatonic',
          'C,Eb,F,Gb,G,Bb': 'blues'
        };
        const scaleKey = preset.generator.scale.join(',');
        const scaleName = scaleMap[scaleKey] || 'minor';
        this.generators.setScale(scaleName);
      }

      if (preset.generator.tempo) {
        this.generators.setTempo(preset.generator.tempo);
      }

      if (preset.generator.density) {
        Object.entries(preset.generator.density).forEach(([inst, val]) => {
          this.generators.setDensity(inst, val);
        });
      }

      if (preset.generator.chaos !== undefined) {
        this.generators.setChaos(preset.generator.chaos);
      }

      if (preset.generator.evolution !== undefined) {
        this.generators.setEvolution(preset.generator.evolution);
      }
    }

    // Apply effects settings
    if (preset.effects) {
      if (preset.effects.delay) {
        this.effects.updateDelay(preset.effects.delay);
      }
      if (preset.effects.reverb) {
        this.effects.updateReverb(preset.effects.reverb);
      }
      if (preset.effects.filter) {
        this.effects.updateFilter(preset.effects.filter);
      }
      if (preset.effects.distortion) {
        this.effects.updateDistortion(preset.effects.distortion);
      }
      if (preset.effects.bitCrusher) {
        this.effects.updateBitCrusher(preset.effects.bitCrusher);
      }
      if (preset.effects.chorus) {
        this.effects.updateChorus(preset.effects.chorus);
      }
      if (preset.effects.phaser) {
        this.effects.updatePhaser(preset.effects.phaser);
      }
      if (preset.effects.convolver && preset.effects.convolver.ir) {
        await this.effects.loadImpulseResponse(preset.effects.convolver.ir);
        this.effects.updateConvolver({ wet: preset.effects.convolver.wet });
      }
    }

    // Apply synth settings
    if (preset.synths) {
      if (preset.synths.bass) {
        this.synths.updateBass(preset.synths.bass);
      }
      if (preset.synths.masterVolume !== undefined) {
        this.synths.setMasterVolume(preset.synths.masterVolume);
      }
    }

    this.currentPreset = preset;

    // Restart generator if playing
    if (this.generators.isPlaying) {
      this.generators.stop();
      await this.generators.start();
    }
  }

  // Save preset to localStorage
  saveToLocalStorage(preset, key) {
    try {
      const presetJSON = JSON.stringify(preset);
      localStorage.setItem(`dubtechno_preset_${key}`, presetJSON);

      // Update preset list
      const presetList = this.getPresetList();
      if (!presetList.includes(key)) {
        presetList.push(key);
        localStorage.setItem('dubtechno_preset_list', JSON.stringify(presetList));
      }

      return true;
    } catch (error) {
      console.error('Error saving preset to localStorage:', error);
      return false;
    }
  }

  // Load preset from localStorage
  loadFromLocalStorage(key) {
    try {
      const presetJSON = localStorage.getItem(`dubtechno_preset_${key}`);
      if (!presetJSON) return null;

      return JSON.parse(presetJSON);
    } catch (error) {
      console.error('Error loading preset from localStorage:', error);
      return null;
    }
  }

  // Get list of saved presets
  getPresetList() {
    try {
      const listJSON = localStorage.getItem('dubtechno_preset_list');
      return listJSON ? JSON.parse(listJSON) : [];
    } catch (error) {
      return [];
    }
  }

  // Delete preset from localStorage
  deleteFromLocalStorage(key) {
    try {
      localStorage.removeItem(`dubtechno_preset_${key}`);

      const presetList = this.getPresetList();
      const index = presetList.indexOf(key);
      if (index > -1) {
        presetList.splice(index, 1);
        localStorage.setItem('dubtechno_preset_list', JSON.stringify(presetList));
      }

      return true;
    } catch (error) {
      console.error('Error deleting preset:', error);
      return false;
    }
  }

  // Export preset as JSON file
  exportPreset(preset, filename) {
    const json = JSON.stringify(preset, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = filename || `${preset.name.replace(/\s+/g, '-').toLowerCase()}.json`;
    a.click();

    URL.revokeObjectURL(url);
  }

  // Import preset from JSON file
  async importPreset(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = (e) => {
        try {
          const preset = JSON.parse(e.target.result);
          resolve(preset);
        } catch (error) {
          reject(new Error('Invalid preset file format'));
        }
      };

      reader.onerror = () => {
        reject(new Error('Error reading file'));
      };

      reader.readAsText(file);
    });
  }

  // Factory presets
  loadFactoryPresets() {
    return {
      'dark-minimal': {
        name: 'Dark Minimal',
        author: 'Factory',
        version: '1.0',
        description: 'Sparse, deep bass, long reverb',
        generator: {
          root: 'C2',
          scale: ['C', 'D', 'Eb', 'F', 'G', 'Ab', 'Bb'],
          tempo: 120,
          density: {
            bass: 0.6,
            pad: 0.2,
            stab: 0.1,
            kick: 1.0,
            hat: 0.4,
            noise: 0.15
          },
          chaos: 0.2,
          evolution: 0.3
        },
        effects: {
          delay: { time: 0.375, feedback: 0.6, wet: 0.5 },
          reverb: { decay: 6.0, wet: 0.4, preDelay: 0.02 },
          filter: { frequency: 3000, Q: 1, type: 'lowpass' },
          distortion: { amount: 0.1, wet: 0.1 },
          bitCrusher: { bits: 8, wet: 0 },
          chorus: { frequency: 0.5, depth: 0.3, wet: 0 },
          phaser: { frequency: 0.3, octaves: 2, wet: 0 }
        },
        synths: {
          bass: {
            filterCutoff: 250,
            filterQ: 2,
            attack: 0.05,
            decay: 0.4,
            sustain: 0.3,
            release: 1.5
          },
          masterVolume: -6
        }
      },

      'industrial': {
        name: 'Industrial',
        author: 'Factory',
        version: '1.0',
        description: 'Distorted, bit-crushed, chaotic',
        generator: {
          root: 'C2',
          scale: ['C', 'Db', 'Eb', 'F', 'G', 'Ab', 'Bb'],
          tempo: 128,
          density: {
            bass: 0.8,
            pad: 0.4,
            stab: 0.6,
            kick: 1.0,
            hat: 0.7,
            noise: 0.4
          },
          chaos: 0.6,
          evolution: 0.7
        },
        effects: {
          delay: { time: 0.25, feedback: 0.3, wet: 0.3 },
          reverb: { decay: 2.0, wet: 0.2, preDelay: 0.01 },
          filter: { frequency: 2000, Q: 4, type: 'lowpass' },
          distortion: { amount: 0.6, wet: 0.4 },
          bitCrusher: { bits: 4, wet: 0.3 },
          chorus: { frequency: 0.8, depth: 0.5, wet: 0.2 },
          phaser: { frequency: 0.5, octaves: 4, wet: 0.3 }
        },
        synths: {
          bass: {
            filterCutoff: 400,
            filterQ: 4,
            attack: 0.01,
            decay: 0.2,
            sustain: 0.5,
            release: 0.8
          },
          masterVolume: -3
        }
      },

      'bladerunner': {
        name: 'Bladerunner',
        author: 'Factory',
        version: '1.0',
        description: 'Johannsson-inspired, atmospheric, FM-heavy',
        generator: {
          root: 'C2',
          scale: ['C', 'D', 'E', 'F', 'G', 'A', 'B'],
          tempo: 115,
          density: {
            bass: 0.5,
            pad: 0.8,
            stab: 0.3,
            kick: 0.7,
            hat: 0.3,
            noise: 0.5
          },
          chaos: 0.3,
          evolution: 0.6
        },
        effects: {
          delay: { time: 0.5, feedback: 0.7, wet: 0.6 },
          reverb: { decay: 8.0, wet: 0.5, preDelay: 0.05 },
          filter: { frequency: 6000, Q: 0.5, type: 'lowpass' },
          distortion: { amount: 0.2, wet: 0.15 },
          bitCrusher: { bits: 8, wet: 0 },
          chorus: { frequency: 0.3, depth: 0.6, wet: 0.4 },
          phaser: { frequency: 0.2, octaves: 2, wet: 0.2 }
        },
        synths: {
          bass: {
            filterCutoff: 300,
            filterQ: 1.5,
            attack: 0.1,
            decay: 0.5,
            sustain: 0.6,
            release: 2.0
          },
          masterVolume: -8
        }
      },

      'rhythm-and-sound': {
        name: 'Rhythm & Sound',
        author: 'Factory',
        version: '1.0',
        description: 'Classic dub, heavy delay, simple patterns',
        generator: {
          root: 'C2',
          scale: ['C', 'Eb', 'F', 'G', 'Bb'],
          tempo: 118,
          density: {
            bass: 0.7,
            pad: 0.3,
            stab: 0.2,
            kick: 1.0,
            hat: 0.5,
            noise: 0.2
          },
          chaos: 0.15,
          evolution: 0.2
        },
        effects: {
          delay: { time: 0.375, feedback: 0.8, wet: 0.7 },
          reverb: { decay: 5.0, wet: 0.35, preDelay: 0.02 },
          filter: { frequency: 4000, Q: 1, type: 'lowpass' },
          distortion: { amount: 0.15, wet: 0.2 },
          bitCrusher: { bits: 8, wet: 0 },
          chorus: { frequency: 0.4, depth: 0.4, wet: 0.15 },
          phaser: { frequency: 0.3, octaves: 2, wet: 0 }
        },
        synths: {
          bass: {
            filterCutoff: 280,
            filterQ: 2.5,
            attack: 0.05,
            decay: 0.3,
            sustain: 0.4,
            release: 1.2
          },
          masterVolume: -6
        }
      },

      'andy-stott': {
        name: 'Andy Stott',
        author: 'Factory',
        version: '1.0',
        description: 'Lo-fi, tape saturation, slow evolution',
        generator: {
          root: 'C2',
          scale: ['C', 'D', 'Eb', 'F', 'G', 'A', 'Bb'],
          tempo: 112,
          density: {
            bass: 0.8,
            pad: 0.5,
            stab: 0.4,
            kick: 1.0,
            hat: 0.6,
            noise: 0.3
          },
          chaos: 0.25,
          evolution: 0.4
        },
        effects: {
          delay: { time: 0.333, feedback: 0.5, wet: 0.4 },
          reverb: { decay: 3.5, wet: 0.3, preDelay: 0.01 },
          filter: { frequency: 2500, Q: 2, type: 'lowpass' },
          distortion: { amount: 0.4, wet: 0.3 },
          bitCrusher: { bits: 6, wet: 0.2 },
          chorus: { frequency: 0.6, depth: 0.5, wet: 0.25 },
          phaser: { frequency: 0.4, octaves: 3, wet: 0.15 }
        },
        synths: {
          bass: {
            filterCutoff: 350,
            filterQ: 3,
            attack: 0.08,
            decay: 0.4,
            sustain: 0.5,
            release: 1.8
          },
          masterVolume: -5
        }
      }
    };
  }

  // Get factory preset by name
  getFactoryPreset(name) {
    return this.factoryPresets[name] || null;
  }

  // Get all factory preset names
  getFactoryPresetNames() {
    return Object.keys(this.factoryPresets);
  }
}
