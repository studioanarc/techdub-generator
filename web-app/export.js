// export.js - Audio Export Functionality
// Render and export audio using Tone.Offline

class AudioExporter {
  constructor(synths, effects, generators) {
    this.synths = synths;
    this.effects = effects;
    this.generators = generators;

    this.isExporting = false;
    this.exportProgress = 0;

    // Export options
    this.options = {
      duration: 60, // seconds
      sampleRate: 48000,
      bitDepth: 16, // 16 or 24
      format: 'wav'
    };
  }

  // Export audio offline
  async export(duration, onProgress) {
    if (this.isExporting) {
      throw new Error('Export already in progress');
    }

    this.isExporting = true;
    this.exportProgress = 0;

    try {
      // Store current state
      const wasPlaying = this.generators.isPlaying;
      const currentSettings = this.generators.getSettings();

      // Stop current playback
      if (wasPlaying) {
        this.generators.stop();
      }

      // Render offline
      const buffer = await this.renderOffline(duration, onProgress);

      // Convert to WAV
      const wavBlob = this.bufferToWave(buffer, this.options.bitDepth);

      // Restore state
      if (wasPlaying) {
        await this.generators.start();
      }

      this.isExporting = false;
      return wavBlob;

    } catch (error) {
      this.isExporting = false;
      throw error;
    }
  }

  // Render audio offline using Tone.Offline
  async renderOffline(duration, onProgress) {
    const durationSeconds = duration || this.options.duration;

    // Progress simulation (Tone.Offline doesn't provide native progress)
    const progressInterval = setInterval(() => {
      if (this.exportProgress < 95) {
        this.exportProgress += 5;
        if (onProgress) onProgress(this.exportProgress);
      }
    }, 100);

    const buffer = await Tone.Offline(async ({ transport }) => {
      // Create temporary instances for offline rendering
      const offlineSynths = new DubSynths();
      const offlineEffects = new DubEffects(Tone.Destination);
      const offlineGenerators = new DubGenerators(offlineSynths, offlineEffects);

      // Copy settings from current generators
      const settings = this.generators.getSettings();
      offlineGenerators.setRoot(settings.root);

      // Find scale name
      const scaleMap = {
        'C,D,Eb,F,G,Ab,Bb': 'minor',
        'C,D,Eb,F,G,A,Bb': 'dorian',
        'C,Db,Eb,F,G,Ab,Bb': 'phrygian',
        'C,D,E,F,G,A,B': 'major',
        'C,Eb,F,G,Bb': 'minor-pentatonic',
        'C,Eb,F,Gb,G,Bb': 'blues'
      };
      const scaleKey = settings.scale.join(',');
      const scaleName = scaleMap[scaleKey] || 'minor';
      offlineGenerators.setScale(scaleName);

      offlineGenerators.setTempo(settings.tempo);

      Object.entries(settings.density).forEach(([inst, val]) => {
        offlineGenerators.setDensity(inst, val);
      });

      offlineGenerators.setChaos(settings.chaos);
      offlineGenerators.setEvolution(settings.evolution);

      // Copy effects settings
      const effectsSettings = this.effects.getSettings();
      offlineEffects.updateDelay(effectsSettings.delay);
      offlineEffects.updateReverb(effectsSettings.reverb);
      offlineEffects.updateFilter(effectsSettings.filter);
      offlineEffects.updateDistortion(effectsSettings.distortion);

      // Start offline generators
      await offlineGenerators.start();

      // Wait for duration
      await transport.start();

    }, durationSeconds);

    clearInterval(progressInterval);
    this.exportProgress = 100;
    if (onProgress) onProgress(100);

    return buffer;
  }

  // Convert AudioBuffer to WAV Blob
  bufferToWave(abuffer, bitDepth = 16) {
    const numOfChan = abuffer.numberOfChannels;
    const length = abuffer.length * numOfChan * (bitDepth / 8) + 44;
    const buffer = new ArrayBuffer(length);
    const view = new DataView(buffer);
    const channels = [];
    let offset = 0;
    let pos = 0;

    // Write WAVE header
    const setUint16 = (data) => {
      view.setUint16(pos, data, true);
      pos += 2;
    };

    const setUint32 = (data) => {
      view.setUint32(pos, data, true);
      pos += 4;
    };

    // "RIFF" chunk descriptor
    setUint32(0x46464952); // "RIFF"
    setUint32(length - 8); // file length - 8
    setUint32(0x45564157); // "WAVE"

    // "fmt " sub-chunk
    setUint32(0x20746d66); // "fmt "
    setUint32(16); // SubChunk1Size (16 for PCM)
    setUint16(1); // AudioFormat (1 for PCM)
    setUint16(numOfChan); // NumChannels
    setUint32(abuffer.sampleRate); // SampleRate
    setUint32(abuffer.sampleRate * numOfChan * (bitDepth / 8)); // ByteRate
    setUint16(numOfChan * (bitDepth / 8)); // BlockAlign
    setUint16(bitDepth); // BitsPerSample

    // "data" sub-chunk
    setUint32(0x61746164); // "data"
    setUint32(length - pos - 4); // SubChunk2Size

    // Get channels
    for (let i = 0; i < numOfChan; i++) {
      channels.push(abuffer.getChannelData(i));
    }

    // Write interleaved data
    if (bitDepth === 16) {
      while (pos < length) {
        for (let i = 0; i < numOfChan; i++) {
          let sample = Math.max(-1, Math.min(1, channels[i][offset]));
          sample = sample < 0 ? sample * 0x8000 : sample * 0x7FFF;
          view.setInt16(pos, sample, true);
          pos += 2;
        }
        offset++;
      }
    } else if (bitDepth === 24) {
      while (pos < length) {
        for (let i = 0; i < numOfChan; i++) {
          let sample = Math.max(-1, Math.min(1, channels[i][offset]));
          sample = sample < 0 ? sample * 0x800000 : sample * 0x7FFFFF;

          // Write 24-bit sample (little-endian)
          view.setUint8(pos, (sample) & 0xFF);
          view.setUint8(pos + 1, (sample >> 8) & 0xFF);
          view.setUint8(pos + 2, (sample >> 16) & 0xFF);
          pos += 3;
        }
        offset++;
      }
    }

    return new Blob([buffer], { type: 'audio/wav' });
  }

  // Download WAV file
  downloadWave(blob, filename) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename || `dubtechno-${Date.now()}.wav`;
    a.click();
    URL.revokeObjectURL(url);
  }

  // Record live session (using MediaRecorder)
  async recordLive(duration, onProgress) {
    if (!Tone.context.rawContext.createMediaStreamDestination) {
      throw new Error('MediaRecorder not supported in this browser');
    }

    return new Promise((resolve, reject) => {
      try {
        // Create media stream from Tone destination
        const dest = Tone.context.rawContext.createMediaStreamDestination();
        Tone.Destination.connect(dest);

        const mediaRecorder = new MediaRecorder(dest.stream);
        const chunks = [];

        mediaRecorder.ondataavailable = (e) => {
          if (e.data.size > 0) {
            chunks.push(e.data);
          }
        };

        mediaRecorder.onstop = () => {
          const blob = new Blob(chunks, { type: 'audio/webm' });
          Tone.Destination.disconnect(dest);
          resolve(blob);
        };

        mediaRecorder.onerror = (e) => {
          reject(e);
        };

        // Start recording
        mediaRecorder.start();

        // Progress tracking
        const startTime = Date.now();
        const progressInterval = setInterval(() => {
          const elapsed = (Date.now() - startTime) / 1000;
          const progress = Math.min(100, (elapsed / duration) * 100);
          if (onProgress) onProgress(progress);

          if (elapsed >= duration) {
            clearInterval(progressInterval);
            mediaRecorder.stop();
          }
        }, 100);

      } catch (error) {
        reject(error);
      }
    });
  }

  // Set export options
  setOptions(options) {
    this.options = { ...this.options, ...options };
  }

  // Get export options
  getOptions() {
    return { ...this.options };
  }
}
