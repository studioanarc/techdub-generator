# Impulse Response Files

This directory contains impulse response (IR) files for the convolution reverb effect.

## Included Impulse Responses

### plate.wav
A classic plate reverb impulse response - bright, smooth, and musical.
Ideal for pads and atmospheric sounds.

### chamber.wav
A natural chamber reverb with medium decay time.
Great for adding depth without overwhelming the mix.

## Using IR Files

To use these IRs in the app:

1. The files will be automatically loaded when you enable convolution reverb
2. You can switch between IRs using the IR selector in the Effects tab
3. Control the wet/dry mix to blend the reverb with your sound

## Adding Custom IRs

To add your own impulse responses:

1. Export your IR as a WAV file (mono or stereo)
2. Place it in this directory
3. Update the IR list in effects.js to include your new file

## Where to Find Free IR Files

- **OpenAIR** (openairlib.net) - Free acoustic impulse responses
- **Voxengo** - Free IR library
- **MusicDSP Archive** - Various free convolution reverb IRs

## File Formats

- Format: WAV
- Bit Depth: 16-bit or 24-bit
- Sample Rate: 44.1kHz or 48kHz recommended
- Channels: Mono or Stereo

## Note for Web Deployment

For production use, you should:
1. Download actual IR WAV files from the sources above
2. Replace these placeholder files with real audio files
3. Keep file sizes reasonable (< 1MB per IR) for web loading
4. Consider using compressed formats for faster loading
