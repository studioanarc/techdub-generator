# Dub Techno Generator

**A professional browser-based generative music web application for creating infinite dub techno compositions.**

![Version](https://img.shields.io/badge/version-3.0-00ff88)
![License](https://img.shields.io/badge/license-MIT-blue)
![Status](https://img.shields.io/badge/status-production-00ff88)

## Features

### 🎹 Advanced Sound Generation
- **Deep Bass Synth** - Subby, analog-style bass with resonant filtering
- **Atmospheric Pads** - Lush, evolving chord pads with long envelopes
- **Chord Stabs** - Percussive chord hits for rhythmic interest
- **Techno Drums** - Classic 808-style kick and minimal hi-hats
- **Noise Textures** - Pink noise swells for atmospheric depth

### 🎚️ Professional Effects Chain
- **Delay** - Classic dub delay with tempo sync and feedback control
- **Reverb** - Spacious algorithmic reverb with adjustable decay
- **Convolution Reverb** - High-quality IR-based reverb (optional)
- **Filter** - Resonant lowpass filter for movement
- **Distortion** - Warm analog-style saturation
- **Bit Crusher** - Lo-fi digital degradation
- **Chorus** - Stereo width and shimmer
- **Phaser** - Sweeping phaser effect

### 🤖 Generative Music Engine
- **Probability-Based Pattern Generation** - Evolving, non-repeating patterns
- **Euclidean Rhythm Generator** - Mathematically perfect rhythms
- **Musical Scale System** - Multiple scales (Minor, Dorian, Phrygian, Blues, etc.)
- **Density Controls** - Individual control over each instrument's activity
- **Chaos Parameter** - Adjustable randomness for unpredictable variation
- **Evolution System** - Patterns that slowly morph over time

### 📊 Real-Time Visualization
- **Waveform Display** - Live oscilloscope-style waveform
- **Spectrum Analyzer** - Frequency-based bar visualization
- **Activity Indicators** - Visual feedback for kick, bass, mid, and high frequencies
- **Level Meter** - Master output level monitoring

### 💾 Preset Management
- **5 Factory Presets** - Professionally designed starting points
  - *Dark Minimal* - Sparse, deep, atmospheric
  - *Industrial* - Aggressive, distorted, chaotic
  - *Bladerunner* - Cinematic, lush, evolving
  - *Rhythm & Sound* - Classic dub techno
  - *Andy Stott* - Lo-fi, textured, gritty
- **Save/Load User Presets** - Store your creations locally
- **Import/Export** - Share presets as JSON files

### 🎵 Audio Export
- **Offline Rendering** - High-quality WAV export
- **Flexible Duration** - Export 30s, 1min, 2min, or 5min tracks
- **16-bit/24-bit WAV** - Professional audio quality
- **Progress Indicator** - Visual feedback during rendering

### 🎨 Beautiful UI
- **Dark Theme** - Easy on the eyes, professional aesthetic
- **Glass-Morphism Design** - Modern, translucent interface elements
- **Tabbed Interface** - Organized into Generator, Effects, Presets, and Export tabs
- **Responsive Layout** - Works on desktop, tablet, and mobile
- **Keyboard Shortcuts** - Fast workflow with hotkeys
- **Touch-Friendly** - Gesture support for touchscreens

## Quick Start

### Running Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/dub-techno-generator.git
   cd dub-techno-generator/web-app
   ```

2. **Serve with a local web server:**
   ```bash
   # Using Python
   python -m http.server 8000

   # OR using Node.js
   npx serve
   ```

3. **Open in browser:**
   ```
   http://localhost:8000
   ```

4. **Click Play** and enjoy!

### No Installation Required

Simply open `index.html` in a modern web browser. All audio processing happens in your browser using the Web Audio API.

## User Guide

### Basic Controls

1. **Play/Stop** - Start or stop the generative engine
2. **Randomize** - Generate new random parameters
3. **Tempo** - Adjust BPM (80-160)
4. **Scale** - Choose musical scale
5. **Chaos** - Control randomness level
6. **Density Sliders** - Adjust activity for each instrument

### Tabs

#### Generator Tab
- **Musical Parameters** - Tempo, scale, chaos
- **Density Controls** - Individual instrument probabilities
- **Master Volume** - Overall output level
- **Advanced Mode** - (Future) Per-synth parameter control

#### Effects Tab
- **Delay** - Feedback and wet amount
- **Reverb** - Decay time and wet amount
- **Filter** - Cutoff frequency
- **Distortion** - Saturation amount

#### Presets Tab
- **Factory Presets** - Click to load professional presets
- **Save Preset** - Store current settings
- **Load Preset** - Recall saved settings
- **Import/Export** - Share presets as files

#### Export Tab
- **Export Audio** - Render to high-quality WAV
- **Choose Duration** - Select export length
- **Progress Bar** - Monitor rendering progress

### Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| **Space** | Play / Stop |
| **R** | Randomize parameters |
| **Ctrl/Cmd + Z** | Undo |
| **Ctrl/Cmd + Shift + Z** | Redo |
| **Ctrl/Cmd + S** | Save preset |

### Tips for Great Results

1. **Start with a preset** - Load a factory preset to hear the system in action
2. **Tweak gradually** - Small changes to chaos and density have big impacts
3. **Let it evolve** - Patterns change over time, give it 30-60 seconds
4. **Use delay and reverb together** - This is the essence of dub techno
5. **Low tempo** - Dub techno typically sits around 118-125 BPM
6. **Minor scales** - Dorian and natural minor work best
7. **Export when you find magic** - You can't reproduce generative results exactly

## Technical Details

### Built With
- **Tone.js** (v14.8) - Web Audio framework
- **Vanilla JavaScript** - No framework dependencies
- **Web Audio API** - All synthesis and effects
- **HTML5 Canvas** - Visualization rendering

### Browser Compatibility
- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Safari 14+
- ✅ Edge 90+

### Performance
- Optimized for 60fps visualization
- Low CPU usage (< 20% on modern hardware)
- Debounced parameter changes
- Efficient audio graph routing

### File Structure
```
web-app/
├── index.html           # Main HTML file
├── style.css            # Professional dark theme CSS
├── synths.js            # Synthesizer definitions
├── effects.js           # Effects chain
├── generators.js        # Generative music engine
├── visualizer.js        # Real-time visualization
├── presets.js           # Preset management system
├── export.js            # Audio export functionality
├── ui.js                # Main UI controller
├── assets/
│   └── impulse-responses/  # IR files for convolution
└── examples/
    ├── dark-minimal.json
    ├── industrial.json
    ├── bladerunner.json
    ├── rhythm-and-sound.json
    └── andy-stott.json
```

## Advanced Usage

### Creating Custom Presets

1. Adjust parameters to your liking
2. Click **Save Preset** in the Presets tab
3. Enter a name
4. Preset is saved to localStorage
5. Optionally export as JSON for sharing

### Importing Presets

1. Download a preset JSON file
2. Click **Import Preset**
3. Select the file
4. Preset is immediately loaded

### Adding Custom Impulse Responses

1. Get free IR files from:
   - OpenAIR (openairlib.net)
   - Voxengo
   - MusicDSP Archive

2. Place WAV files in `assets/impulse-responses/`

3. Update `effects.js` to reference new IRs

## Development

### Local Development Setup

```bash
# Clone the repo
git clone https://github.com/yourusername/dub-techno-generator.git
cd dub-techno-generator/web-app

# Start a development server
python -m http.server 8000
```

### Modifying Parameters

**To change synth parameters:**
Edit `synths.js` - Adjust oscillator types, envelopes, filters

**To change effects:**
Edit `effects.js` - Modify effect parameters, add new effects

**To change generative logic:**
Edit `generators.js` - Modify pattern algorithms, probabilities

**To change visualization:**
Edit `visualizer.js` - Customize colors, animations

### Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## Troubleshooting

### No Sound
- Check browser supports Web Audio API
- Click Play (browsers require user gesture)
- Check system volume
- Try refreshing the page

### Performance Issues
- Close other browser tabs
- Disable unnecessary effects
- Reduce visualization complexity
- Try a different browser

### Preset Won't Load
- Check JSON format validity
- Ensure all required fields present
- Try a factory preset first
- Clear localStorage if corrupted

## Credits

**Inspired by:**
- Basic Channel / Rhythm & Sound
- Deepchord
- Andy Stott
- Jóhann Jóhannsson

**Built with:**
- Tone.js by Yotam Mann
- Web Audio API
- HTML5 Canvas

## License

MIT License - See LICENSE file for details

## Support

- **Documentation**: See CONTROLS.md for detailed parameter explanations
- **Issues**: Report bugs on GitHub
- **Discussions**: Share creations and ask questions

---

**Made with ❤️ for the dub techno community**

*Generated music is infinite - every session is unique*
