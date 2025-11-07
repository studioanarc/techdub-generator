# Dub Techno Generator - Controls Reference

Complete guide to every parameter and control in the Dub Techno Generator.

## Table of Contents
- [Transport Controls](#transport-controls)
- [Generator Parameters](#generator-parameters)
- [Effects Parameters](#effects-parameters)
- [Preset System](#preset-system)
- [Export Options](#export-options)
- [Sound Design Tips](#sound-design-tips)
- [Generative Algorithm Explanations](#generative-algorithm-explanations)

---

## Transport Controls

### Play Button
**Function:** Starts the generative music engine

- Initializes all synths and effects
- Starts Tone.js transport
- Begins pattern generation
- Enables visualization
- **Shortcut:** Space bar

**Usage Tips:**
- Your browser may require a user gesture before audio can play
- The first click might have a slight delay as audio context initializes
- Patterns evolve over time - give it 30+ seconds to hear the full effect

### Stop Button
**Function:** Stops playback completely

- Halts all sequences
- Stops transport
- Pauses visualization
- Releases all held notes
- **Shortcut:** Space bar (toggle)

**Usage Tips:**
- All parameter changes are preserved when stopped
- You can make adjustments while stopped
- Patterns will regenerate from scratch when you restart

### Randomize Button
**Function:** Generates new random parameters

- Randomizes scale selection
- Adjusts tempo (110-130 BPM)
- Varies density values
- Changes chaos amount
- Modifies effect parameters
- **Shortcut:** R key

**Usage Tips:**
- Great for creative exploration
- Can produce unexpected but interesting results
- Some combinations may be too sparse or too dense
- Try randomizing multiple times to find inspiration
- Save good results immediately as presets

---

## Generator Parameters

### Musical Parameters

#### Tempo
**Range:** 80 - 160 BPM
**Default:** 120 BPM
**Description:** Controls the speed of playback

**Sweet Spots:**
- **110-118 BPM** - Classic dub techno (Rhythm & Sound style)
- **118-125 BPM** - Modern dub techno
- **125-135 BPM** - Upbeat techno-dub
- **135+ BPM** - Fast, energetic (less traditional)

**Sound Design Impact:**
- Slower tempos emphasize space and atmosphere
- Faster tempos create more urgency and energy
- Delay times are tempo-synced, so tempo affects overall rhythmic feel

**Tap Tempo:**
- Click "Tap Tempo" button 4 times to set tempo by tapping
- Useful for matching external sources or finding a groove

#### Scale
**Options:**
- Minor (Natural Minor)
- Dorian
- Phrygian
- Major
- Minor Pentatonic
- Blues

**Description:** Determines which notes the generators can use

**Scale Characteristics:**

**Minor (Natural Minor)**
- Dark, melancholic mood
- Most common in dub techno
- Works well for deep, introspective tracks
- Example: C D Eb F G Ab Bb

**Dorian**
- Slightly brighter than natural minor
- Jazzy, sophisticated feel
- Great for evolving, complex harmonies
- Example: C D Eb F G A Bb

**Phrygian**
- Exotic, Spanish/Middle Eastern flavor
- Very dark and mysterious
- Creates tension and unease
- Example: C Db Eb F G Ab Bb

**Major**
- Bright, uplifting
- Unusual for dub techno but can work
- Good for experimental sounds
- Example: C D E F G A B

**Minor Pentatonic**
- Simple, cannot hit "wrong" notes
- Good for minimal, stripped-back tracks
- Very safe, always sounds good
- Example: C Eb F G Bb

**Blues**
- Includes blue notes (flattened 5th)
- Gritty, dirty feel
- Good for lo-fi, industrial vibes
- Example: C Eb F Gb G Bb

#### Chaos
**Range:** 0.0 - 1.0
**Default:** 0.3
**Description:** Amount of randomness in pattern generation

**Low Chaos (0.0 - 0.3):**
- Predictable, structured patterns
- Repetitive rhythms
- Clear, defined progressions
- Minimal surprises
- Good for: hypnotic, trance-inducing tracks

**Medium Chaos (0.3 - 0.6):**
- Balanced between structure and variation
- Patterns evolve but remain coherent
- Occasional unexpected notes
- Good for: dynamic, evolving compositions

**High Chaos (0.6 - 1.0):**
- Unpredictable, constantly changing
- Abstract, experimental feel
- Maximum variation
- Can become dissonant
- Good for: ambient, soundscape, glitch

**Technical Details:**
- Affects note selection probability
- Influences rhythmic placement
- Modulates effect parameter automation (future)

### Density Controls

Density controls determine how often each instrument triggers notes. Think of them as "how busy" each part is.

#### Kick Density
**Range:** 0.0 - 1.0
**Default:** 1.0
**Description:** How often the kick drum plays

**Guidelines:**
- **1.0** - Four-on-the-floor (every beat)
- **0.75** - Three of four beats
- **0.5** - Half of beats (creates space)
- **0.25** - Sparse, minimal kick
- **0.0** - No kick (ambient mode)

**Tips:**
- Dub techno traditionally has a steady kick (1.0)
- Lower values create more space and atmosphere
- Combine with chaos for occasional kick variations

#### Bass Density
**Range:** 0.0 - 1.0
**Default:** 0.7
**Description:** How often the bass synth plays

**Guidelines:**
- **0.8-1.0** - Continuous bass presence
- **0.5-0.7** - Traditional dub techno bass (sweet spot)
- **0.3-0.5** - Sparse, minimal bass
- **0.0-0.3** - Very minimal, ambient

**Tips:**
- Bass and kick work together - too much of both can be muddy
- Lower bass density lets the kick breathe
- Higher values create a fuller, more driving feel

#### Pad Density
**Range:** 0.0 - 1.0
**Default:** 0.3
**Description:** How often atmospheric pad chords play

**Guidelines:**
- **0.5-1.0** - Lush, atmospheric (Bladerunner style)
- **0.3-0.5** - Balanced atmosphere
- **0.1-0.3** - Minimal, sparse pads (classic dub)
- **0.0** - No pads (stripped-back)

**Tips:**
- Pads have long envelopes, so even low densities create atmosphere
- Too much pad can overwhelm other elements
- Works great with heavy reverb

#### Stab Density
**Range:** 0.0 - 1.0
**Default:** 0.4
**Description:** How often percussive chord stabs occur

**Guidelines:**
- **0.6-1.0** - Rhythmic, driving
- **0.3-0.6** - Balanced (sweet spot)
- **0.1-0.3** - Occasional accents
- **0.0** - No stabs (smoother flow)

**Tips:**
- Stabs add rhythmic interest
- Great for creating tension and release
- Works well with delay for dub effects

#### Hat Density
**Range:** 0.0 - 1.0
**Default:** 0.6
**Description:** How often hi-hats trigger

**Guidelines:**
- **0.7-1.0** - Busy, techno-style hats
- **0.5-0.7** - Balanced groove
- **0.3-0.5** - Minimal, sparse
- **0.0-0.3** - Very minimal

**Tips:**
- Uses Euclidean rhythm generation
- Creates polyrhythmic patterns
- Lower densities create more interesting rhythms

### Master Volume
**Range:** -40 dB to 0 dB
**Default:** -6 dB
**Description:** Overall output level

**Guidelines:**
- **-10 to -6 dB** - Safe mixing level (recommended)
- **-6 to -3 dB** - Louder, more present
- **-3 to 0 dB** - Maximum level (risk of clipping)

**Tips:**
- Leave headroom for effects (delay/reverb add gain)
- Watch the level meter visualization
- Export at -6 dB or lower for mastering headroom

---

## Effects Parameters

### Delay

#### Feedback
**Range:** 0.0 - 0.95
**Default:** 0.5
**Description:** How much of the delayed signal feeds back into the delay

**Guidelines:**
- **0.2-0.4** - Short, subtle echo
- **0.4-0.6** - Classic dub delay (sweet spot)
- **0.6-0.8** - Long, evolving echoes
- **0.8-0.95** - Infinite, self-oscillating (be careful!)

**Tips:**
- Higher feedback creates the classic dub "infinite echo"
- Be cautious above 0.8 - can become overwhelming
- Combine with wet amount for control
- Tempo-synced to maintain musical timing

#### Wet Amount
**Range:** 0.0 - 1.0
**Default:** 0.4
**Description:** Mix of dry (original) vs wet (delayed) signal

**Guidelines:**
- **0.1-0.3** - Subtle delay presence
- **0.3-0.5** - Balanced mix (sweet spot)
- **0.5-0.7** - Heavy delay character
- **0.7-1.0** - Delay dominates

**Tips:**
- Start around 0.3-0.4 for classic dub sound
- Higher values create more space and atmosphere
- Lower values keep the track tighter and more focused

### Reverb

#### Decay
**Range:** 0.1 - 10 seconds
**Default:** 4.0 seconds
**Description:** How long the reverb tail lasts

**Guidelines:**
- **0.5-2s** - Small room, tight sound
- **2-5s** - Medium hall (sweet spot for dub)
- **5-8s** - Large cathedral
- **8-10s** - Infinite, ambient space

**Tips:**
- Longer decay = more atmospheric
- Shorter decay = tighter, more defined
- Dub techno typically uses 4-6 seconds
- Match to tempo (slower tempo = longer decay works)

#### Wet Amount
**Range:** 0.0 - 1.0
**Default:** 0.3
**Description:** Mix of dry vs reverberated signal

**Guidelines:**
- **0.1-0.2** - Subtle space
- **0.2-0.4** - Balanced ambience (sweet spot)
- **0.4-0.6** - Atmospheric, spacious
- **0.6-1.0** - Fully ambient

**Tips:**
- Too much reverb muddies the mix
- Use more reverb on pads, less on bass/kick
- Combine with delay for full dub effect

### Filter

#### Cutoff Frequency
**Range:** 200 Hz - 10,000 Hz
**Default:** 5000 Hz
**Description:** Frequency above which sound is filtered out

**Guidelines:**
- **200-1000 Hz** - Dark, muffled, lo-fi
- **1000-3000 Hz** - Warm, reduced brightness
- **3000-6000 Hz** - Balanced (sweet spot)
- **6000-10000 Hz** - Bright, open

**Tips:**
- Lower cutoff = darker, more atmospheric
- Slowly sweep cutoff for movement
- Great for creating tension/release
- Classic dub uses moderate filtering (3-5kHz)

### Distortion

#### Amount
**Range:** 0.0 - 1.0
**Default:** 0.2
**Description:** Intensity of saturation/distortion

**Guidelines:**
- **0.0-0.2** - Subtle warmth (sweet spot)
- **0.2-0.4** - Noticeable saturation
- **0.4-0.7** - Heavy distortion
- **0.7-1.0** - Extreme, lo-fi

**Tips:**
- Low amounts add analog warmth
- Higher amounts create lo-fi, industrial vibes
- Use sparingly - can quickly become harsh
- Great for Andy Stott / industrial dub sound

---

## Preset System

### Factory Presets

#### Dark Minimal
**Character:** Sparse, deep, atmospheric
**Best For:** Minimal dub techno, hypnotic grooves
**Key Features:**
- Low density on most elements
- Long reverb decay (6s)
- Deep bass focus
- Perfect starting point for customization

#### Industrial
**Character:** Aggressive, distorted, chaotic
**Best For:** Industrial techno, harsh sounds
**Key Features:**
- High chaos (0.6)
- Heavy distortion
- Bit-crushed textures
- Fast tempo (128 BPM)

#### Bladerunner
**Character:** Cinematic, lush, atmospheric
**Best For:** Ambient dub, soundtracks
**Key Features:**
- High pad density (0.8)
- Long reverb decay (8s)
- Major scale for brightness
- Slower tempo (115 BPM)

#### Rhythm & Sound
**Character:** Classic dub techno
**Best For:** Traditional dub sound
**Key Features:**
- Balanced densities
- Heavy delay feedback (0.8)
- Pentatonic scale
- Mid-range tempo (118 BPM)

#### Andy Stott
**Character:** Lo-fi, textured, gritty
**Best For:** Experimental dub, lo-fi techno
**Key Features:**
- Medium-high distortion
- Moderate chaos (0.25)
- Filtered, muffled sound
- Slow evolution

### Saving Custom Presets

1. Adjust parameters to your liking
2. Click "Save Preset"
3. Enter a descriptive name
4. Preset is stored in browser localStorage

**Tips:**
- Use descriptive names (e.g., "Deep Space Dub" not "Preset 1")
- Save multiple variations
- Regularly export important presets as JSON files
- localStorage can be cleared - export backups!

### Importing/Exporting

**Export:**
- Click "Export Preset"
- Enter filename
- Downloads as .json file
- Share with others!

**Import:**
- Click "Import Preset"
- Select .json preset file
- Instantly loads all parameters

---

## Export Options

### Audio Export

**Duration Options:**
- 30 seconds - Quick previews
- 60 seconds - Standard export
- 120 seconds - Extended compositions
- 300 seconds (5 min) - Full tracks

**Format:**
- WAV (uncompressed)
- 16-bit or 24-bit depth
- 48kHz sample rate

**Process:**
1. Set up your perfect sound
2. Click "Export Audio"
3. Enter desired duration
4. Wait for rendering (shows progress)
5. File automatically downloads

**Tips:**
- Rendering happens offline (perfect quality)
- Export at -6dB for mastering headroom
- Longer exports take more time to render
- You can't recreate exact patterns (generative)
- When you hear something good, export it immediately!

---

## Sound Design Tips

### Creating Hypnotic Minimal Dub
1. Low chaos (0.1-0.2)
2. Sparse densities (bass 0.5, pad 0.2, stab 0.1)
3. Long reverb (5-7s)
4. Moderate delay feedback (0.5-0.6)
5. Minor or Dorian scale
6. Slow tempo (115-120 BPM)

### Creating Industrial Techno-Dub
1. High chaos (0.5-0.7)
2. High densities (most at 0.7-0.9)
3. Heavy distortion (0.4-0.6)
4. Short, aggressive reverb (2-3s)
5. Phrygian or Blues scale
6. Faster tempo (126-132 BPM)

### Creating Atmospheric Soundscapes
1. Medium chaos (0.3-0.4)
2. High pad density (0.7-0.9), low kick density (0.0-0.3)
3. Very long reverb (7-10s)
4. Heavy wet on delay and reverb (0.5-0.7)
5. Major or Dorian scale
6. Slow tempo (100-115 BPM)

### Creating Classic Dub Techno
1. Low-medium chaos (0.2-0.3)
2. Balanced densities (kick 1.0, bass 0.7, pad 0.3)
3. Heavy delay feedback (0.7-0.8)
4. Moderate reverb (4-5s)
5. Minor or Minor Pentatonic
6. 118-122 BPM (classic tempo)

---

## Generative Algorithm Explanations

### Probability-Based Generation

Each instrument has a probability (density) of triggering on each step. This creates organic, non-repetitive patterns.

**Example:**
- Bass density = 0.7
- Every 16th note, there's a 70% chance the bass plays
- Over time, this creates varied but consistent patterns

### Euclidean Rhythms

The hi-hat uses Euclidean rhythm generation - a mathematical way to distribute beats evenly.

**Example:**
- 16 steps, 12 pulses = evenly distributed pattern
- Creates polyrhythmic, interesting grooves
- Never sounds mechanical

### Scale-Based Note Selection

Notes are chosen from the selected scale, ensuring musical coherence.

**Process:**
1. Generator picks a scale degree (e.g., root, third, fifth)
2. Chaos parameter determines if it picks the "expected" note or a random one
3. Result: musical but unpredictable melodies

### Evolution System

Patterns slowly morph over time based on the evolution parameter.

**How It Works:**
- Previous patterns influence future patterns
- Creates slow, organic change
- Prevents exact repetition
- Evolution parameter controls rate of change

---

## Keyboard Shortcuts Reference

| Key | Action |
|-----|--------|
| **Space** | Play / Stop |
| **R** | Randomize |
| **Ctrl/Cmd + S** | Save Preset |
| **Ctrl/Cmd + Z** | Undo |
| **Ctrl/Cmd + Shift + Z** | Redo |

---

## Troubleshooting

### "I'm not hearing what I want"
- Try loading a factory preset first
- Adjust densities - too low = sparse, too high = muddy
- Check master volume
- Let it play for 30+ seconds (patterns evolve)

### "It sounds too random/chaotic"
- Lower the chaos parameter
- Use Minor Pentatonic scale (can't hit wrong notes)
- Increase specific densities for more consistency

### "It sounds too repetitive"
- Increase chaos parameter
- Lower some densities to create variation
- Change scale for different harmonic content

### "The bass/kick are muddy"
- Lower bass density slightly
- Reduce reverb wet amount
- Increase filter cutoff frequency

---

**Remember:** This is a generative system - embrace the unpredictability! The best results often come from happy accidents.

For more information, see README.md
