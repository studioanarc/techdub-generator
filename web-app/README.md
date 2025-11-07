# Dub Techno Generator - Web Application

A generative music web application that creates atmospheric dub techno soundscapes using Tone.js.

## Features

### Core Functionality (Phase 1)
- **Dual Synthesizer Engine**
  - Deep sub-bass synthesizer (MonoSsynth with sine waves)
  - Atmospheric pad synthesizer (PolySynth for chords)
  - Generative note patterns based on musical scales

- **Musical Control**
  - Tempo control (100-140 BPM)
  - Root note selection (12 chromatic notes)
  - Scale selection (Minor, Dorian, Phrygian, Minor Pentatonic)
  - Master volume control

- **Professional Effects Chain**
  - **Ping Pong Delay** - Classic dub tape echo with adjustable time, feedback, and wet/dry
  - **Reverb** - Deep spacious atmosphere with configurable decay time
  - **Auto Filter** - Sweeping low-pass filter for movement and texture
  - **Warm Distortion** - Subtle harmonic saturation
  - **Master Compressor** - Glues the mix together

- **Real-Time Visualization**
  - Canvas-based waveform display
  - Shows audio output in real-time

- **Dark Aesthetic UI**
  - Dub techno inspired dark theme (#1a1a1a background)
  - Neon green accents (#00ff88)
  - Touch-friendly controls
  - Responsive grid layout

## Technology Stack

- **Tone.js v14.8.49** - Web Audio API framework for synthesis and effects
- **Vanilla JavaScript** - No framework dependencies
- **HTML5 Canvas** - Real-time waveform visualization
- **CSS3** - Modern responsive styling

## Quick Start

### Run Locally

The application is a static web app that can be served from any HTTP server.

#### Option 1: Python HTTP Server (Recommended)

```bash
# Navigate to the web-app directory
cd /home/user/techdub-generator/web-app

# Python 3.x
python3 -m http.server 8000

# Python 2.x
python -m SimpleHTTPServer 8000
```

Then open your browser to: **http://localhost:8000**

#### Option 2: Node.js HTTP Server

```bash
# Install http-server globally
npm install -g http-server

# Navigate to the web-app directory
cd /home/user/techdub-generator/web-app

# Start server
http-server -p 8000
```

#### Option 3: VS Code Live Server

1. Install the "Live Server" extension in VS Code
2. Right-click on `index.html`
3. Select "Open with Live Server"

### Browser Requirements

- Modern browser with Web Audio API support
- Chrome, Firefox, Safari, or Edge (latest versions)
- JavaScript must be enabled
- User interaction required to start audio (browser security requirement)

## Usage Instructions

1. **Start the Application**
   - Click the **START** button (required for browser audio context)
   - The app will initialize the audio system
   - Music generation will begin automatically

2. **Adjust Musical Parameters**
   - Set your desired **Root Note** (default: D)
   - Choose a **Scale** (default: Minor)
   - Adjust **Tempo** (100-140 BPM, default: 120)

3. **Control Effects**
   - Toggle effects on/off with the switches
   - Adjust parameters for each effect in real-time
   - Experiment with different combinations

4. **Stop Playback**
   - Click the **STOP** button to halt generation

## File Structure

```
web-app/
├── index.html      # Main HTML structure and UI
├── style.css       # Dark aesthetic styling
├── app.js          # Main application logic and UI handlers
├── synth.js        # Synthesizer engine (bass + pads)
├── effects.js      # Effects chain configuration
└── README.md       # This file
```

## Sound Design Notes

### Dub Techno Characteristics
- **Bass**: Deep sub-bass (C1-C2 range) using pure sine waves
- **Pads**: Long, sustained atmospheric chords with slow attack/release
- **Delay**: 60-70% feedback for classic dub echo chains
- **Reverb**: 4-8 second decay for spacious atmosphere
- **Tempo**: 120-125 BPM is the sweet spot for dub techno

### Recommended Settings for Classic Dub Sound
- **Delay**: Time: 0.25, Feedback: 0.65, Wet: 0.3
- **Reverb**: Decay: 6s, Wet: 0.25
- **Filter**: Frequency: 0.5Hz, Depth: 0.6, Wet: 0.4
- **Root Note**: D or F (darker tonality)
- **Scale**: Minor or Dorian

## Development

### Code Structure

- **EffectsChain Class** (`effects.js`): Manages all audio effects with bypass functionality
- **SynthEngine Class** (`synth.js`): Handles synthesis, scales, and generative patterns
- **Main App** (`app.js`): Coordinates UI, audio initialization, and visualization

### Key Functions

```javascript
// Initialize audio (must be after user gesture)
await initAudio()

// Start playback
synthEngine.start()
Tone.Transport.start()

// Stop playback
synthEngine.stop()
Tone.Transport.stop()

// Update musical parameters
synthEngine.updateScale(rootNote, scaleType)
```

## Known Limitations

- Browser requires user interaction before audio playback
- Initial audio context startup may take 1-2 seconds
- Mobile browsers may have additional audio latency
- Reverb initialization can cause brief delay on first load

## Next Steps (Future Phases)

- Pattern sequencing and rhythm generation
- Additional synthesis modes (FM, granular)
- Preset saving/loading
- MIDI controller support
- Audio recording/export
- More advanced effects (chorus, phaser, tape saturation)
- Visual frequency analyzer

## Troubleshooting

**No sound when clicking START:**
- Check browser console for errors
- Ensure volume is not muted
- Try refreshing the page
- Verify Web Audio API is supported in your browser

**Performance issues:**
- Reduce reverb decay time
- Disable unused effects
- Close other browser tabs using audio
- Use a desktop browser for best performance

**Effects not audible:**
- Check that effect toggles are enabled (green)
- Increase the "Wet" parameter
- Verify master volume is not too low

## License

MIT License - Feel free to use and modify

## Credits

Built with Tone.js by Yotam Mann
Web Audio API by W3C
