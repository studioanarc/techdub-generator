// synths.js - Dub Techno Synthesizer Definitions
// Deep, atmospheric synths designed for dub techno

class DubSynths {
  constructor() {
    this.synths = {};
    // Create volume node but don't connect to destination yet
    // (will be connected through effects chain)
    this.masterVolume = new Tone.Volume(-6);
    this.initSynths();
  }

  initSynths() {
    // BASS SYNTH - Deep, subby bass
    this.synths.bass = new Tone.MonoSynth({
      oscillator: {
        type: 'sine'
      },
      envelope: {
        attack: 0.05,
        decay: 0.3,
        sustain: 0.4,
        release: 1.2
      },
      filter: {
        Q: 2,
        type: 'lowpass',
        frequency: 300,
        rolloff: -24
      },
      filterEnvelope: {
        attack: 0.05,
        decay: 0.3,
        sustain: 0.5,
        release: 1.5,
        baseFrequency: 100,
        octaves: 2.5
      }
    }).connect(this.masterVolume);

    // PAD SYNTH - Atmospheric pad for chords
    this.synths.pad = new Tone.PolySynth(Tone.Synth, {
      maxPolyphony: 4, // Limit voices to prevent performance issues
      oscillator: {
        type: 'sawtooth',
        partials: [1, 0.5, 0.3, 0.2]
      },
      envelope: {
        attack: 1.5,
        decay: 0.8,
        sustain: 0.7,
        release: 3.0
      },
      filter: {
        Q: 1,
        type: 'lowpass',
        frequency: 800,
        rolloff: -12
      },
      filterEnvelope: {
        attack: 2.0,
        decay: 1.0,
        sustain: 0.6,
        release: 3.0,
        baseFrequency: 200,
        octaves: 3
      }
    }).connect(this.masterVolume);

    this.synths.pad.volume.value = -12;

    // STAB SYNTH - Chord stabs
    this.synths.stab = new Tone.PolySynth(Tone.Synth, {
      maxPolyphony: 3, // Limit voices to prevent performance issues
      oscillator: {
        type: 'square'
      },
      envelope: {
        attack: 0.01,
        decay: 0.2,
        sustain: 0.1,
        release: 0.5
      },
      filter: {
        Q: 4,
        type: 'lowpass',
        frequency: 1200,
        rolloff: -24
      },
      filterEnvelope: {
        attack: 0.01,
        decay: 0.3,
        sustain: 0.2,
        release: 0.6,
        baseFrequency: 400,
        octaves: 4
      }
    }).connect(this.masterVolume);

    this.synths.stab.volume.value = -18;

    // KICK - Deep techno kick
    this.synths.kick = new Tone.MembraneSynth({
      pitchDecay: 0.05,
      octaves: 6,
      oscillator: {
        type: 'sine'
      },
      envelope: {
        attack: 0.001,
        decay: 0.4,
        sustain: 0.01,
        release: 0.4,
        attackCurve: 'exponential'
      }
    }).connect(this.masterVolume);

    this.synths.kick.volume.value = 0;

    // HAT - Minimal hi-hat
    this.synths.hat = new Tone.MetalSynth({
      frequency: 200,
      envelope: {
        attack: 0.001,
        decay: 0.1,
        release: 0.01
      },
      harmonicity: 5.1,
      modulationIndex: 32,
      resonance: 4000,
      octaves: 1.5
    }).connect(this.masterVolume);

    this.synths.hat.volume.value = -24;

    // NOISE - Atmospheric texture
    this.synths.noise = new Tone.NoiseSynth({
      noise: {
        type: 'pink'
      },
      envelope: {
        attack: 0.5,
        decay: 1.0,
        sustain: 0.3,
        release: 2.0
      }
    }).connect(this.masterVolume);

    this.synths.noise.volume.value = -30;
  }

  // Update synth parameters
  updateBass(params) {
    if (params.filterCutoff !== undefined) {
      this.synths.bass.filter.frequency.value = params.filterCutoff;
    }
    if (params.filterQ !== undefined) {
      this.synths.bass.filter.Q.value = params.filterQ;
    }
    if (params.attack !== undefined) {
      this.synths.bass.envelope.attack = params.attack;
    }
    if (params.decay !== undefined) {
      this.synths.bass.envelope.decay = params.decay;
    }
    if (params.sustain !== undefined) {
      this.synths.bass.envelope.sustain = params.sustain;
    }
    if (params.release !== undefined) {
      this.synths.bass.envelope.release = params.release;
    }
  }

  updatePad(params) {
    if (params.filterCutoff !== undefined) {
      this.synths.pad.set({ 'filter.frequency': params.filterCutoff });
    }
    if (params.attack !== undefined) {
      this.synths.pad.set({ 'envelope.attack': params.attack });
    }
    if (params.release !== undefined) {
      this.synths.pad.set({ 'envelope.release': params.release });
    }
  }

  updateStab(params) {
    if (params.filterCutoff !== undefined) {
      this.synths.stab.set({ 'filter.frequency': params.filterCutoff });
    }
    if (params.decay !== undefined) {
      this.synths.stab.set({ 'envelope.decay': params.decay });
    }
  }

  setMasterVolume(db) {
    this.masterVolume.volume.rampTo(db, 0.1);
  }

  dispose() {
    Object.values(this.synths).forEach(synth => synth.dispose());
    this.masterVolume.dispose();
  }
}
