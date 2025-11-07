// generators.js - Generative Music Engine for Dub Techno
// Creates evolving patterns using probability and music theory

class DubGenerators {
  constructor(synths, effects) {
    this.synths = synths;
    this.effects = effects;
    this.sequences = {};
    this.isPlaying = false;

    // Musical parameters
    this.root = 'C2';
    this.scale = ['C', 'D', 'Eb', 'F', 'G', 'Ab', 'Bb']; // C minor
    this.tempo = 120;

    // Generative parameters
    this.density = {
      bass: 0.7,
      pad: 0.3,
      stab: 0.4,
      kick: 1.0,
      hat: 0.6,
      noise: 0.2
    };

    this.chaos = 0.3; // Amount of randomness
    this.evolution = 0.5; // How much patterns evolve over time

    // Pattern memory for evolution
    this.patterns = {
      bass: [],
      pad: [],
      stab: []
    };

    this.stepCount = 0;
  }

  // Scale utilities
  getNote(degree, octave = 2) {
    const index = Math.abs(degree) % this.scale.length;
    return this.scale[index] + octave;
  }

  getChord(root, type = 'minor') {
    const rootIndex = this.scale.indexOf(root);
    if (rootIndex === -1) return [root + '3'];

    const chordIntervals = {
      minor: [0, 2, 4],
      major: [0, 2, 4],
      dim: [0, 2, 3],
      sus2: [0, 1, 4],
      sus4: [0, 3, 4]
    };

    const intervals = chordIntervals[type] || chordIntervals.minor;
    return intervals.map(i => this.getNote(rootIndex + i, 3));
  }

  // Probability-based note generation
  shouldTrigger(probability) {
    return Math.random() < probability;
  }

  // Euclidean rhythm generator
  euclidean(steps, pulses) {
    if (pulses >= steps) return Array(steps).fill(1);
    if (pulses === 0) return Array(steps).fill(0);

    const pattern = Array(steps).fill(0);
    const spacing = steps / pulses;

    for (let i = 0; i < pulses; i++) {
      const index = Math.floor(i * spacing);
      pattern[index] = 1;
    }

    return pattern;
  }

  // Initialize sequences
  initSequences() {
    Tone.Transport.bpm.value = this.tempo;

    // KICK - Steady 4/4 with occasional variations
    this.sequences.kick = new Tone.Sequence((time, step) => {
      const kickPattern = this.euclidean(16, 4);

      if (kickPattern[step % 16] || (this.shouldTrigger(this.chaos * 0.2) && step % 4 !== 0)) {
        this.synths.synths.kick.triggerAttackRelease('C1', '8n', time);
      }
    }, Array.from({ length: 16 }, (_, i) => i), '16n');

    // BASS - Generative bass line
    this.sequences.bass = new Tone.Sequence((time, step) => {
      if (step % 2 === 0 && this.shouldTrigger(this.density.bass)) {
        const rootNote = this.getNote(0, 2);
        const notes = [
          rootNote,
          this.getNote(4, 2), // Fifth
          this.getNote(3, 2), // Fourth
          this.getNote(2, 2)  // Third
        ];

        let note;
        if (this.shouldTrigger(this.chaos)) {
          note = notes[Math.floor(Math.random() * notes.length)];
        } else {
          note = notes[step % 4];
        }

        const duration = this.shouldTrigger(0.3) ? '4n' : '8n';
        this.synths.synths.bass.triggerAttackRelease(note, duration, time);
      }
    }, Array.from({ length: 16 }, (_, i) => i), '16n');

    // PAD - Slow evolving chords
    this.sequences.pad = new Tone.Sequence((time, step) => {
      if (step % 8 === 0 && this.shouldTrigger(this.density.pad)) {
        const root = this.scale[Math.floor(Math.random() * this.scale.length)];
        const chordTypes = ['minor', 'sus2', 'sus4'];
        const type = chordTypes[Math.floor(Math.random() * chordTypes.length)];
        const chord = this.getChord(root, type);

        this.synths.synths.pad.triggerAttackRelease(chord, '2n', time);
      }
    }, Array.from({ length: 16 }, (_, i) => i), '16n');

    // STAB - Rhythmic chord stabs
    this.sequences.stab = new Tone.Sequence((time, step) => {
      if (this.shouldTrigger(this.density.stab) && step % 4 === 2) {
        const root = this.scale[Math.floor(Math.random() * this.scale.length)];
        const chord = this.getChord(root, 'minor');

        this.synths.synths.stab.triggerAttackRelease(chord, '16n', time);
      }
    }, Array.from({ length: 16 }, (_, i) => i), '16n');

    // HAT - Euclidean hi-hat pattern
    this.sequences.hat = new Tone.Sequence((time, step) => {
      const hatPattern = this.euclidean(16, Math.floor(this.density.hat * 12));

      if (hatPattern[step % 16]) {
        this.synths.synths.hat.triggerAttackRelease('16n', time);
      }
    }, Array.from({ length: 16 }, (_, i) => i), '16n');

    // NOISE - Ambient atmospheric swells
    this.sequences.noise = new Tone.Sequence((time, step) => {
      if (step % 16 === 0 && this.shouldTrigger(this.density.noise)) {
        this.synths.synths.noise.triggerAttackRelease('1n', time);
      }
    }, Array.from({ length: 16 }, (_, i) => i), '16n');
  }

  // Start playback
  async start() {
    if (this.isPlaying) return;

    await Tone.start();

    this.initSequences();

    // Start all sequences
    Object.values(this.sequences).forEach(seq => seq.start(0));

    Tone.Transport.start();
    this.isPlaying = true;
  }

  // Stop playback
  stop() {
    if (!this.isPlaying) return;

    Tone.Transport.stop();

    // Stop and dispose sequences
    Object.values(this.sequences).forEach(seq => {
      seq.stop();
      seq.dispose();
    });

    this.sequences = {};
    this.isPlaying = false;
  }

  // Update musical parameters
  setRoot(note) {
    this.root = note;
    if (this.isPlaying) {
      this.stop();
      this.start();
    }
  }

  setScale(scaleName) {
    const scales = {
      'minor': ['C', 'D', 'Eb', 'F', 'G', 'Ab', 'Bb'],
      'dorian': ['C', 'D', 'Eb', 'F', 'G', 'A', 'Bb'],
      'phrygian': ['C', 'Db', 'Eb', 'F', 'G', 'Ab', 'Bb'],
      'major': ['C', 'D', 'E', 'F', 'G', 'A', 'B'],
      'minor-pentatonic': ['C', 'Eb', 'F', 'G', 'Bb'],
      'blues': ['C', 'Eb', 'F', 'Gb', 'G', 'Bb']
    };

    this.scale = scales[scaleName] || scales.minor;

    if (this.isPlaying) {
      this.stop();
      this.start();
    }
  }

  setTempo(bpm) {
    this.tempo = bpm;
    Tone.Transport.bpm.rampTo(bpm, 2);
  }

  // Update generative parameters
  setDensity(instrument, value) {
    this.density[instrument] = Math.max(0, Math.min(1, value));
  }

  setChaos(value) {
    this.chaos = Math.max(0, Math.min(1, value));
  }

  setEvolution(value) {
    this.evolution = Math.max(0, Math.min(1, value));
  }

  // Randomize all parameters
  randomize() {
    // Random scale
    const scales = ['minor', 'dorian', 'phrygian', 'major-pentatonic', 'blues'];
    this.setScale(scales[Math.floor(Math.random() * scales.length)]);

    // Random tempo (110-130 BPM typical for dub techno)
    this.setTempo(110 + Math.random() * 20);

    // Random densities
    this.density.bass = 0.5 + Math.random() * 0.4;
    this.density.pad = 0.2 + Math.random() * 0.4;
    this.density.stab = 0.2 + Math.random() * 0.5;
    this.density.hat = 0.4 + Math.random() * 0.5;
    this.density.noise = 0.1 + Math.random() * 0.3;

    // Random chaos
    this.chaos = 0.2 + Math.random() * 0.4;

    // Random effects
    this.effects.updateDelay({
      feedback: 0.3 + Math.random() * 0.4,
      wet: 0.3 + Math.random() * 0.3
    });

    this.effects.updateReverb({
      decay: 2 + Math.random() * 4,
      wet: 0.2 + Math.random() * 0.3
    });

    this.effects.updateFilter({
      frequency: 2000 + Math.random() * 4000
    });

    // Restart if playing
    if (this.isPlaying) {
      this.stop();
      this.start();
    }
  }

  // Get current settings
  getSettings() {
    return {
      root: this.root,
      scale: this.scale,
      tempo: this.tempo,
      density: { ...this.density },
      chaos: this.chaos,
      evolution: this.evolution
    };
  }

  dispose() {
    this.stop();
  }
}
