// visualizer.js - Real-time Audio Visualization
// Waveform and spectrum analyzer with activity indicators

class DubVisualizer {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    this.ctx = this.canvas.getContext('2d');
    this.animationId = null;
    this.isRunning = false;

    // Analyzers
    this.waveform = new Tone.Waveform(2048);
    this.fft = new Tone.FFT(512);
    this.meter = new Tone.Meter();

    // Connect to master output
    Tone.Destination.connect(this.waveform);
    Tone.Destination.connect(this.fft);
    Tone.Destination.connect(this.meter);

    // Visualization mode
    this.mode = 'both'; // 'waveform', 'spectrum', 'both'

    // Colors
    this.colors = {
      background: '#0a0a0a',
      waveform: '#00ff88',
      spectrumLow: '#00ff88',
      spectrumMid: '#00ccff',
      spectrumHigh: '#0088ff',
      grid: '#1a1a1a',
      text: '#00ff88'
    };

    // Activity tracking
    this.activity = {
      kick: 0,
      bass: 0,
      mid: 0,
      high: 0
    };

    // Performance optimization
    this.frameSkip = 0;
    this.targetFPS = 60;

    this.initCanvas();
  }

  initCanvas() {
    // Set canvas size
    this.resize();
    window.addEventListener('resize', () => this.resize());
  }

  resize() {
    const rect = this.canvas.getBoundingClientRect();
    this.canvas.width = rect.width * window.devicePixelRatio;
    this.canvas.height = rect.height * window.devicePixelRatio;
    this.ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
  }

  start() {
    if (this.isRunning) return;
    this.isRunning = true;
    this.draw();
  }

  stop() {
    if (!this.isRunning) return;
    this.isRunning = false;
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
    }
  }

  setMode(mode) {
    this.mode = mode;
  }

  draw() {
    if (!this.isRunning) return;

    this.animationId = requestAnimationFrame(() => this.draw());

    const width = this.canvas.width / window.devicePixelRatio;
    const height = this.canvas.height / window.devicePixelRatio;

    // Clear canvas
    this.ctx.fillStyle = this.colors.background;
    this.ctx.fillRect(0, 0, width, height);

    // Draw grid
    this.drawGrid(width, height);

    // Draw based on mode
    if (this.mode === 'waveform') {
      this.drawWaveform(width, height);
    } else if (this.mode === 'spectrum') {
      this.drawSpectrum(width, height);
    } else if (this.mode === 'both') {
      this.drawWaveform(width, height / 2);
      this.drawSpectrum(width, height / 2, height / 2);
    }

    // Draw activity indicators
    this.drawActivityIndicators(width, height);

    // Draw level meter
    this.drawLevelMeter(width, height);
  }

  drawGrid(width, height) {
    this.ctx.strokeStyle = this.colors.grid;
    this.ctx.lineWidth = 1;

    // Horizontal lines
    const hLines = 8;
    for (let i = 0; i <= hLines; i++) {
      const y = (height / hLines) * i;
      this.ctx.beginPath();
      this.ctx.moveTo(0, y);
      this.ctx.lineTo(width, y);
      this.ctx.stroke();
    }

    // Vertical lines
    const vLines = 16;
    for (let i = 0; i <= vLines; i++) {
      const x = (width / vLines) * i;
      this.ctx.beginPath();
      this.ctx.moveTo(x, 0);
      this.ctx.lineTo(x, height);
      this.ctx.stroke();
    }
  }

  drawWaveform(width, height, offsetY = 0) {
    const values = this.waveform.getValue();
    const sliceWidth = width / values.length;

    this.ctx.lineWidth = 2;
    this.ctx.strokeStyle = this.colors.waveform;
    this.ctx.shadowBlur = 10;
    this.ctx.shadowColor = this.colors.waveform;

    this.ctx.beginPath();

    for (let i = 0; i < values.length; i++) {
      const x = i * sliceWidth;
      const v = (values[i] + 1) / 2; // Normalize -1 to 1 => 0 to 1
      const y = v * height + offsetY;

      if (i === 0) {
        this.ctx.moveTo(x, y);
      } else {
        this.ctx.lineTo(x, y);
      }
    }

    this.ctx.stroke();
    this.ctx.shadowBlur = 0;

    // Center line
    this.ctx.strokeStyle = this.colors.grid;
    this.ctx.lineWidth = 1;
    this.ctx.beginPath();
    this.ctx.moveTo(0, height / 2 + offsetY);
    this.ctx.lineTo(width, height / 2 + offsetY);
    this.ctx.stroke();
  }

  drawSpectrum(width, height, offsetY = 0) {
    const values = this.fft.getValue();
    const barWidth = width / values.length;
    const barSpacing = 1;

    // Track frequency bands for activity detection
    let lowSum = 0, midSum = 0, highSum = 0;

    for (let i = 0; i < values.length; i++) {
      const value = values[i];
      const normalized = (value + 140) / 140; // Normalize dB values
      const barHeight = Math.max(0, normalized * height);

      // Frequency-based color gradient
      let color;
      if (i < values.length * 0.2) {
        color = this.colors.spectrumLow;
        lowSum += normalized;
      } else if (i < values.length * 0.6) {
        color = this.colors.spectrumMid;
        midSum += normalized;
      } else {
        color = this.colors.spectrumHigh;
        highSum += normalized;
      }

      // Draw bar
      this.ctx.fillStyle = color;
      this.ctx.shadowBlur = 5;
      this.ctx.shadowColor = color;

      const x = i * barWidth;
      const y = offsetY + height - barHeight;

      this.ctx.fillRect(x, y, barWidth - barSpacing, barHeight);
    }

    this.ctx.shadowBlur = 0;

    // Update activity levels
    this.activity.bass = lowSum / (values.length * 0.2);
    this.activity.mid = midSum / (values.length * 0.4);
    this.activity.high = highSum / (values.length * 0.4);

    // Detect kick hits (strong low frequency)
    if (this.activity.bass > 0.6) {
      this.activity.kick = 1.0;
    } else {
      this.activity.kick *= 0.9; // Decay
    }
  }

  drawActivityIndicators(width, height) {
    const indicatorSize = 20;
    const spacing = 10;
    const x = width - (indicatorSize + spacing) * 4 - spacing;
    const y = spacing;

    // Kick indicator
    this.drawIndicator(x, y, indicatorSize, this.activity.kick, 'KICK');

    // Bass indicator
    this.drawIndicator(x + (indicatorSize + spacing), y, indicatorSize,
      Math.min(1, this.activity.bass), 'BASS');

    // Mid indicator
    this.drawIndicator(x + (indicatorSize + spacing) * 2, y, indicatorSize,
      Math.min(1, this.activity.mid), 'MID');

    // High indicator
    this.drawIndicator(x + (indicatorSize + spacing) * 3, y, indicatorSize,
      Math.min(1, this.activity.high), 'HIGH');
  }

  drawIndicator(x, y, size, level, label) {
    // Background
    this.ctx.fillStyle = '#1a1a1a';
    this.ctx.fillRect(x, y, size, size);

    // Level fill
    const fillHeight = size * level;
    const gradient = this.ctx.createLinearGradient(x, y + size, x, y);
    gradient.addColorStop(0, this.colors.spectrumLow);
    gradient.addColorStop(1, this.colors.spectrumHigh);

    this.ctx.fillStyle = gradient;
    this.ctx.fillRect(x, y + size - fillHeight, size, fillHeight);

    // Border
    this.ctx.strokeStyle = this.colors.waveform;
    this.ctx.lineWidth = 1;
    this.ctx.strokeRect(x, y, size, size);

    // Label
    this.ctx.fillStyle = this.colors.text;
    this.ctx.font = '8px monospace';
    this.ctx.fillText(label, x, y + size + 10);
  }

  drawLevelMeter(width, height) {
    const level = this.meter.getValue();
    const normalized = Math.max(0, (level + 60) / 60); // -60dB to 0dB

    const meterWidth = 200;
    const meterHeight = 10;
    const x = 10;
    const y = height - meterHeight - 10;

    // Background
    this.ctx.fillStyle = '#1a1a1a';
    this.ctx.fillRect(x, y, meterWidth, meterHeight);

    // Level bar
    const fillWidth = meterWidth * normalized;
    const gradient = this.ctx.createLinearGradient(x, y, x + meterWidth, y);
    gradient.addColorStop(0, this.colors.spectrumLow);
    gradient.addColorStop(0.7, this.colors.spectrumMid);
    gradient.addColorStop(0.9, '#ff8800');
    gradient.addColorStop(1, '#ff0000');

    this.ctx.fillStyle = gradient;
    this.ctx.fillRect(x, y, fillWidth, meterHeight);

    // Border
    this.ctx.strokeStyle = this.colors.waveform;
    this.ctx.lineWidth = 1;
    this.ctx.strokeRect(x, y, meterWidth, meterHeight);

    // Label
    this.ctx.fillStyle = this.colors.text;
    this.ctx.font = '10px monospace';
    const dbValue = Math.round(level * 10) / 10;
    this.ctx.fillText(`LEVEL: ${dbValue} dB`, x + meterWidth + 10, y + meterHeight);
  }

  // Flash effect on kick hits (can be triggered externally)
  flashKick() {
    this.activity.kick = 1.0;
  }

  // Get analyser nodes for external connections
  getAnalysers() {
    return {
      waveform: this.waveform,
      fft: this.fft,
      meter: this.meter
    };
  }

  dispose() {
    this.stop();
    this.waveform.dispose();
    this.fft.dispose();
    this.meter.dispose();
  }
}
