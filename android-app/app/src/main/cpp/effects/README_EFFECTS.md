# Dub Techno Effects Chain - Documentation

## Overview

Professional audio effects chain for dub techno music production, implemented in C++ for Android.

**Total Effects:** 10+
- TapeEcho (Roland Space Echo RE-201 inspired)
- Reverb (Freeverb algorithm)
- Compressor (Dynamics control)
- Delay (Ping-pong stereo)
- Filter (Auto-filter with LFO)
- Distortion (Multiple algorithms)
- BitCrusher (Lo-fi degradation)
- Chorus (Width and movement)
- Phaser (Sweeping notches)
- ConvolutionReverb (IR-based reverb)

---

## Architecture

### File Structure
```
cpp/
├── dsp/
│   ├── DSPUtils.h/cpp         # Utility functions
│   ├── DelayLine.h/cpp        # Circular buffer delay
│   ├── Biquad.h/cpp           # Biquad filter (all types)
│   └── AllPassFilter.h/cpp    # All-pass filter
├── effects/
│   ├── Effect.h               # Base class
│   ├── TapeEcho.h/cpp         # Tape echo effect
│   ├── Reverb.h/cpp           # Algorithmic reverb
│   ├── Compressor.h/cpp       # Dynamics compressor
│   ├── Delay.h/cpp            # Simple delay
│   ├── Filter.h/cpp           # Auto-filter
│   ├── Distortion.h/cpp       # Saturation/distortion
│   ├── BitCrusher.h/cpp       # Bit crusher
│   ├── Chorus.h/cpp           # Chorus effect
│   ├── Phaser.h/cpp           # Phaser effect
│   ├── ConvolutionReverb.h/cpp# Convolution reverb
│   ├── EffectsChain.h/cpp     # Chain manager
│   ├── EffectsJNI.h/cpp       # JNI bindings
│   └── README_EFFECTS.md      # This file
```

### Key Features
- **Thread-safe:** Atomic operations for parameter updates
- **Zero-latency:** All effects process in real-time
- **No zipper noise:** Smooth parameter interpolation
- **Denormal prevention:** CPU-friendly processing
- **Modular design:** Easy to add new effects

---

## Example Effect Chains

### 1. Classic Dub Techno Chain

**Perfect for deep, spacious dub techno**

```cpp
// Slot 0: TapeEcho (Main delay)
TapeEcho* tape = new TapeEcho();
tape->setTime(375.0f);        // Dotted eighth note at 125 BPM
tape->setFeedback(0.65f);     // 65% feedback for long repeats
tape->setSaturation(0.35f);   // Moderate tape saturation
tape->setAge(0.5f);           // Medium age for vintage sound
tape->setWowFlutter(0.2f);    // Subtle pitch variation
tape->setMix(0.4f);           // 40% wet
effectsChain.addEffect(tape, 0);

// Slot 1: Reverb (Space)
Reverb* reverb = new Reverb();
reverb->setRoomSize(0.85f);   // Large room
reverb->setDamping(0.6f);     // Some high-frequency absorption
reverb->setWidth(1.0f);       // Full stereo width
reverb->setMix(0.25f);        // 25% wet
effectsChain.addEffect(reverb, 1);

// Slot 2: Filter (Movement)
Filter* filter = new Filter();
filter->setBaseFrequency(800.0f);  // 800 Hz center
filter->setResonance(3.0f);        // Moderate resonance
filter->setDepth(0.6f);            // Noticeable sweep
filter->setRate(0.25f);            // Slow LFO (0.25 Hz)
filter->setMix(0.3f);              // 30% wet (subtle)
effectsChain.addEffect(filter, 2);

// Slot 3: Compressor (Glue)
Compressor* comp = new Compressor();
comp->setThreshold(-18.0f);   // -18 dB
comp->setRatio(3.0f);         // 3:1 ratio
comp->setAttack(15.0f);       // 15ms attack
comp->setRelease(120.0f);     // 120ms release
comp->setKnee(4.0f);          // Soft knee
comp->setMakeupGain(4.0f);    // +4 dB makeup
comp->setMix(1.0f);           // 100% wet (always on)
effectsChain.addEffect(comp, 3);

// Routing: SERIES (tape -> reverb -> filter -> comp)
effectsChain.setRoutingMode(EffectsChain::SERIES);
```

**Result:** Deep, spacious sound with vintage tape character, long reverb tails, subtle filter movement, and tight compression.

---

### 2. Minimal Dub Chain

**For minimal, stripped-down dub techno**

```cpp
// Slot 0: Delay (Simple ping-pong)
Delay* delay = new Delay();
delay->setTime(500.0f);       // Half note at 120 BPM
delay->setFeedback(0.5f);     // 50% feedback
delay->setPingPong(true);     // Stereo ping-pong
delay->setMix(0.35f);         // 35% wet
effectsChain.addEffect(delay, 0);

// Slot 1: Distortion (Warmth)
Distortion* dist = new Distortion();
dist->setDistortionType(Distortion::TUBE);
dist->setDrive(2.5f);         // Moderate drive
dist->setTone(6000.0f);       // 6kHz lowpass
dist->setOutputGain(0.7f);    // Reduce output
dist->setMix(0.2f);           // 20% wet (parallel)
effectsChain.addEffect(dist, 1);

// Slot 2: Compressor
Compressor* comp = new Compressor();
comp->setThreshold(-20.0f);
comp->setRatio(4.0f);
comp->setAttack(10.0f);
comp->setRelease(100.0f);
effectsChain.addEffect(comp, 2);

effectsChain.setRoutingMode(EffectsChain::SERIES);
```

**Result:** Clean, minimal sound with rhythmic delay and subtle warmth.

---

### 3. Experimental Dub Chain

**For more adventurous, textured dub techno**

```cpp
// Slot 0: BitCrusher (Texture)
BitCrusher* crush = new BitCrusher();
crush->setSampleRateReduction(8.0f);  // Moderate degradation
crush->setBitDepth(10.0f);            // 10-bit
crush->setMix(0.15f);                 // 15% wet (subtle)
effectsChain.addEffect(crush, 0);

// Slot 1: TapeEcho
TapeEcho* tape = new TapeEcho();
tape->setTime(400.0f);
tape->setFeedback(0.7f);
tape->setSaturation(0.5f);
tape->setAge(0.7f);           // Heavy age for lo-fi
tape->setWowFlutter(0.4f);    // Noticeable flutter
tape->setMix(0.5f);
effectsChain.addEffect(tape, 1);

// Slot 2: Chorus (Width)
Chorus* chorus = new Chorus();
chorus->setRate(0.3f);
chorus->setDepth(0.6f);
chorus->setVoices(3);
chorus->setMix(0.25f);
effectsChain.addEffect(chorus, 2);

// Slot 3: Reverb (Long decay)
Reverb* reverb = new Reverb();
reverb->setRoomSize(0.95f);   // Huge space
reverb->setDamping(0.4f);     // Less damping = longer
reverb->setWidth(1.0f);
reverb->setMix(0.35f);
effectsChain.addEffect(reverb, 3);

// Slot 4: Phaser (Movement)
Phaser* phaser = new Phaser();
phaser->setRate(0.15f);       // Very slow
phaser->setDepth(0.5f);
phaser->setStages(8);
phaser->setFeedback(0.6f);
phaser->setMix(0.2f);
effectsChain.addEffect(phaser, 4);

effectsChain.setRoutingMode(EffectsChain::SERIES);
```

**Result:** Textured, evolving soundscape with lo-fi character and movement.

---

## Performance Benchmarks

### CPU Usage per Effect (estimated)
Based on 48kHz sample rate, 256-sample buffer, stereo processing

| Effect | CPU % | Notes |
|--------|-------|-------|
| TapeEcho | 3-5% | Multi-tap with modulation |
| Reverb | 2-4% | 8 comb + 4 all-pass filters |
| Compressor | 1-2% | RMS detection + envelope |
| Delay | 1-2% | Simple delay with filter |
| Filter | 1-2% | Biquad + LFO |
| Distortion | 0.5-1% | Lightweight processing |
| BitCrusher | 0.5-1% | Very efficient |
| Chorus | 2-3% | Multiple delay lines |
| Phaser | 2-3% | 4-12 all-pass stages |
| ConvolutionReverb | 5-15% | Depends on IR length |

**Total for Classic Chain:** ~8-13% CPU
**Headroom:** Plenty for synthesis + UI

### Memory Usage
- Delay buffers: ~2MB per effect (5 seconds @ 48kHz stereo)
- Total for 8-slot chain: ~16MB
- Negligible compared to modern Android devices

---

## Parameter Ranges

### TapeEcho
- **Time:** 1 - 2000ms (musical intervals: 125ms, 250ms, 375ms, 500ms)
- **Feedback:** 0.0 - 0.95 (sweet spot: 0.5-0.7)
- **Saturation:** 0.0 - 1.0 (0.3-0.4 for dub)
- **Age:** 0.0 - 1.0 (0.4-0.6 for vintage)
- **WowFlutter:** 0.0 - 1.0 (0.1-0.3 for subtle)

### Reverb
- **RoomSize:** 0.0 - 1.0 (0.7-0.9 for dub)
- **Damping:** 0.0 - 1.0 (0.5-0.7)
- **Width:** 0.0 - 1.0 (1.0 for full stereo)
- **Mix:** 0.0 - 1.0 (0.2-0.4 for dub)

### Compressor
- **Threshold:** -60 - 0 dB (-24 to -12 dB typical)
- **Ratio:** 1.0 - 20.0 (2:1 to 4:1 for dub)
- **Attack:** 0.1 - 100ms (10-20ms)
- **Release:** 10 - 1000ms (80-150ms)
- **Knee:** 0 - 12 dB (3-6 dB)
- **Makeup:** 0 - 24 dB (auto-adjust to taste)

### Filter
- **BaseFrequency:** 20 - 10000 Hz (500-2000 Hz sweet spot)
- **Resonance:** 0.5 - 10.0 (2-4 for dub)
- **Depth:** 0.0 - 2.0 (0.5-1.0)
- **Rate:** 0.01 - 20 Hz (0.1-0.5 Hz for slow sweeps)

---

## Integration with Audio Engine

### In OboeAudioCallback

```cpp
#include "effects/EffectsChain.h"

class DubTechnoAudioCallback : public oboe::AudioStreamCallback {
private:
    dubtechno::EffectsChain effectsChain;

public:
    void init(float sampleRate) {
        effectsChain.init(sampleRate);

        // Set up default chain
        setupClassicDubChain();
    }

    oboe::DataCallbackResult onAudioReady(
        oboe::AudioStream* stream,
        void* audioData,
        int32_t numFrames
    ) override {
        float* outputBuffer = static_cast<float*>(audioData);
        int channels = stream->getChannelCount();

        // 1. Generate synth audio
        synth.process(outputBuffer, numFrames, channels);

        // 2. Process through effects chain
        effectsChain.process(outputBuffer, numFrames, channels);

        return oboe::DataCallbackResult::Continue;
    }

    void setupClassicDubChain() {
        // Add effects as shown in examples above
    }
};
```

---

## Kotlin/Java Interface

### Example Usage

```kotlin
class EffectsEngine {
    // Native methods
    private external fun nativeInitEffectsChain(sampleRate: Float)
    private external fun nativeAddTapeEcho(slot: Int)
    private external fun nativeTapeEchoSetTime(slot: Int, timeMs: Float)
    private external fun nativeSetEffectMix(slot: Int, mix: Float)

    fun setupDubTechnoChain() {
        nativeInitEffectsChain(48000f)

        // Add TapeEcho to slot 0
        nativeAddTapeEcho(0)
        nativeTapeEchoSetTime(0, 375f)
        nativeTapeEchoSetFeedback(0, 0.65f)
        nativeSetEffectMix(0, 0.4f)

        // Add Reverb to slot 1
        nativeAddReverb(1)
        nativeReverbSetRoomSize(1, 0.85f)
        nativeSetEffectMix(1, 0.25f)
    }
}
```

---

## Sound Design Tips

### For Authentic Dub Techno:
1. **Use long delays** (300-500ms) synced to tempo
2. **High feedback** (60-75%) for infinite echoes
3. **Large reverb** with 4-10 second decay
4. **Gentle compression** (3:1 to 4:1 ratio)
5. **Slow filter sweeps** (0.1-0.5 Hz)
6. **Tape saturation** for warmth and character

### Tempo-Synced Delay Times (120 BPM):
- Quarter note: 500ms
- Dotted eighth: 375ms
- Eighth note: 250ms
- Sixteenth: 125ms

Formula: `delayMs = (60000 / BPM) * beatDivision`

### Layering Effects:
- **Pads:** Reverb + Chorus (wide, lush)
- **Basslines:** Compressor + Tape saturation (tight, warm)
- **Chords:** TapeEcho + Reverb (spacious, evolving)
- **Percussion:** BitCrusher + Phaser (texture, movement)

---

## Advanced Techniques

### 1. Feedback Loops
Route effect output back to input for infinite processing:
```cpp
// Create feedback loop with delay
// WARNING: Use carefully to avoid runaway feedback!
delay->setFeedback(0.95f);  // Near-infinite
compressor->setThreshold(-30.0f);  // Limiter to prevent clipping
```

### 2. Sidechain Ducking
Use kick to duck delay/reverb:
```cpp
// In compressor (future enhancement)
compressor->setSidechainInput(kickBuffer);
compressor->setRatio(10.0f);  // Heavy ducking
```

### 3. Parallel Processing
Use PARALLEL routing mode for "New York" style compression:
```cpp
effectsChain.setRoutingMode(EffectsChain::PARALLEL);
compressor->setRatio(8.0f);  // Heavy compression
compressor->setMix(0.3f);    // Blend with dry
```

---

## Troubleshooting

### Audio Glitches / Pops
- Check buffer sizes (256-512 samples ideal)
- Ensure all parameters use smooth interpolation
- Verify denormal prevention is active

### High CPU Usage
- Reduce IR length in ConvolutionReverb
- Lower quality settings (fewer taps, shorter delays)
- Use SIMD optimization flags in CMake

### Thin Sound
- Increase reverb mix and room size
- Add chorus for width
- Use parallel distortion for thickness

### Muddy Sound
- Increase damping in reverb
- Use highpass filter in feedback loops
- Reduce low-frequency content with EQ

---

## Future Enhancements

- [ ] MIDI sync for tempo-locked delays
- [ ] Stereo widener effect
- [ ] Multiband compressor
- [ ] Sidechain input routing
- [ ] FFT-based convolution for ConvolutionReverb
- [ ] SIMD optimization (NEON for ARM)
- [ ] Preset management system
- [ ] Visual feedback (spectrum analyzer, level meters)
- [ ] LFO visualization
- [ ] Effect morphing (crossfade between presets)

---

## Credits

Effects algorithms based on:
- **Freeverb** by Jezar at Dreampoint
- **Biquad Cookbook** by Robert Bristow-Johnson
- **Space Echo RE-201** Roland Corporation
- **Digital Signal Processing** literature

Implementation: Dub Techno Generator Project
