/**
 * Music Theory Utilities for Dub Techno Generation
 * Provides scales, note/frequency conversions, and chord generation
 *
 * Dub techno favors dark, moody scales:
 * - Natural Minor (most common)
 * - Dorian (slightly brighter, jazzy)
 * - Phrygian (dark, Spanish feel)
 * - Minor Pentatonic (safe, always sounds good)
 * - Whole Tone (experimental, dreamy)
 */

// Note names to MIDI note numbers (middle C = C4 = MIDI 60)
const NOTE_NAMES = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B'];
const ENHARMONIC_FLATS = {
    'Db': 'C#', 'Eb': 'D#', 'Gb': 'F#', 'Ab': 'G#', 'Bb': 'A#'
};

// Scale intervals (in semitones from root)
export const SCALES = {
    // Minor scales (dub techno favorites)
    'minor': [0, 2, 3, 5, 7, 8, 10],                    // Natural minor (Aeolian)
    'dorian': [0, 2, 3, 5, 7, 9, 10],                   // Dorian mode
    'phrygian': [0, 1, 3, 5, 7, 8, 10],                 // Phrygian mode (very dark)
    'minor_pentatonic': [0, 3, 5, 7, 10],               // Minor pentatonic (5 notes)
    'harmonic_minor': [0, 2, 3, 5, 7, 8, 11],           // Harmonic minor (exotic)

    // Experimental scales
    'whole_tone': [0, 2, 4, 6, 8, 10],                  // Whole tone (dreamy, floating)
    'blues': [0, 3, 5, 6, 7, 10],                       // Blues scale
    'chromatic': [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11], // All notes

    // Major scales (less common in dub but useful)
    'major': [0, 2, 4, 5, 7, 9, 11],                    // Major scale
    'mixolydian': [0, 2, 4, 5, 7, 9, 10],               // Mixolydian (dominant)
};

// Chord formulas (intervals from root)
export const CHORDS = {
    // Dub-friendly chords (suspended, extended)
    'sus2': [0, 2, 7],           // Suspended 2nd (no 3rd)
    'sus4': [0, 5, 7],           // Suspended 4th (no 3rd)
    'minor': [0, 3, 7],          // Minor triad
    'minor7': [0, 3, 7, 10],     // Minor 7th
    'minor9': [0, 3, 7, 10, 14], // Minor 9th
    'add9': [0, 4, 7, 14],       // Add 9 (major with 9th)

    // Additional chords
    'major': [0, 4, 7],          // Major triad
    'major7': [0, 4, 7, 11],     // Major 7th
    'dim': [0, 3, 6],            // Diminished
    'aug': [0, 4, 8],            // Augmented
};

/**
 * Convert note name to MIDI note number
 * @param {string} noteName - e.g., "C4", "A#2", "Bb3"
 * @returns {number} - MIDI note number (0-127)
 */
export function noteNameToMidi(noteName) {
    // Parse note name (e.g., "C#4" or "Bb2")
    const match = noteName.match(/^([A-G][#b]?)(-?\d+)$/);
    if (!match) {
        throw new Error(`Invalid note name: ${noteName}`);
    }

    let [, note, octave] = match;
    octave = parseInt(octave);

    // Handle flats
    if (note.includes('b')) {
        note = ENHARMONIC_FLATS[note] || note;
    }

    const noteIndex = NOTE_NAMES.indexOf(note);
    if (noteIndex === -1) {
        throw new Error(`Unknown note: ${note}`);
    }

    // MIDI note = (octave + 1) * 12 + noteIndex
    // C4 = 60, A4 = 69, etc.
    return (octave + 1) * 12 + noteIndex;
}

/**
 * Convert MIDI note number to frequency in Hz
 * @param {number} midiNote - MIDI note number (0-127)
 * @returns {number} - Frequency in Hz
 */
export function midiToFrequency(midiNote) {
    // A4 (MIDI 69) = 440 Hz
    // f = 440 * 2^((n - 69) / 12)
    return 440 * Math.pow(2, (midiNote - 69) / 12);
}

/**
 * Convert note name directly to frequency
 * @param {string} noteName - e.g., "C2", "A4"
 * @returns {number} - Frequency in Hz
 */
export function noteToFrequency(noteName) {
    const midi = noteNameToMidi(noteName);
    return midiToFrequency(midi);
}

/**
 * Convert MIDI note number back to note name
 * @param {number} midiNote - MIDI note number
 * @param {boolean} useFlats - Use flats instead of sharps
 * @returns {string} - Note name (e.g., "C4")
 */
export function midiToNoteName(midiNote, useFlats = false) {
    const octave = Math.floor(midiNote / 12) - 1;
    const noteIndex = midiNote % 12;
    let noteName = NOTE_NAMES[noteIndex];

    // Convert to flats if requested
    if (useFlats && noteName.includes('#')) {
        const flatEquivalents = {
            'C#': 'Db', 'D#': 'Eb', 'F#': 'Gb', 'G#': 'Ab', 'A#': 'Bb'
        };
        noteName = flatEquivalents[noteName] || noteName;
    }

    return `${noteName}${octave}`;
}

/**
 * Get all notes in a scale
 * @param {string} root - Root note name (e.g., "C", "A#")
 * @param {string} scaleName - Scale name from SCALES
 * @param {number} octave - Starting octave
 * @param {number} octaves - Number of octaves to generate (default 1)
 * @returns {Array<{name: string, midi: number, freq: number}>} - Scale notes
 */
export function getScaleNotes(root, scaleName, octave = 2, octaves = 1) {
    const scaleIntervals = SCALES[scaleName];
    if (!scaleIntervals) {
        throw new Error(`Unknown scale: ${scaleName}`);
    }

    const rootNote = `${root}${octave}`;
    const rootMidi = noteNameToMidi(rootNote);
    const notes = [];

    for (let oct = 0; oct < octaves; oct++) {
        for (let interval of scaleIntervals) {
            const midiNote = rootMidi + interval + (oct * 12);
            notes.push({
                name: midiToNoteName(midiNote),
                midi: midiNote,
                freq: midiToFrequency(midiNote),
                degree: scaleIntervals.indexOf(interval % 12) + 1 + (oct * scaleIntervals.length)
            });
        }
    }

    return notes;
}

/**
 * Get notes for a chord
 * @param {string} root - Root note name or MIDI number
 * @param {string} chordType - Chord type from CHORDS
 * @param {string} scaleName - Optional: constrain to scale
 * @returns {Array<{name: string, midi: number, freq: number}>} - Chord notes
 */
export function getChordNotes(root, chordType, scaleName = null) {
    const chordIntervals = CHORDS[chordType];
    if (!chordIntervals) {
        throw new Error(`Unknown chord type: ${chordType}`);
    }

    // Determine root MIDI note
    let rootMidi;
    if (typeof root === 'number') {
        rootMidi = root;
    } else {
        rootMidi = noteNameToMidi(root);
    }

    const notes = chordIntervals.map(interval => {
        const midiNote = rootMidi + interval;
        return {
            name: midiToNoteName(midiNote),
            midi: midiNote,
            freq: midiToFrequency(midiNote),
            interval: interval
        };
    });

    // If scale is specified, filter to only scale notes
    if (scaleName) {
        const scaleNotes = getScaleNotes(
            midiToNoteName(rootMidi).slice(0, -1), // Remove octave
            scaleName,
            Math.floor(rootMidi / 12) - 1,
            2
        );
        const scaleMidiSet = new Set(scaleNotes.map(n => n.midi % 12));

        return notes.filter(note => scaleMidiSet.has(note.midi % 12));
    }

    return notes;
}

/**
 * Get a random note from a scale
 * @param {string} root - Root note
 * @param {string} scaleName - Scale name
 * @param {number} octave - Octave
 * @param {number} octaves - Number of octaves
 * @returns {Object} - Random note object
 */
export function getRandomScaleNote(root, scaleName, octave = 2, octaves = 1) {
    const notes = getScaleNotes(root, scaleName, octave, octaves);
    return notes[Math.floor(Math.random() * notes.length)];
}

/**
 * Get interval between two notes in semitones
 * @param {number|string} note1 - First note (MIDI or name)
 * @param {number|string} note2 - Second note (MIDI or name)
 * @returns {number} - Interval in semitones
 */
export function getInterval(note1, note2) {
    const midi1 = typeof note1 === 'number' ? note1 : noteNameToMidi(note1);
    const midi2 = typeof note2 === 'number' ? note2 : noteNameToMidi(note2);
    return Math.abs(midi2 - midi1);
}

/**
 * Transpose a note by semitones
 * @param {number|string} note - Note to transpose
 * @param {number} semitones - Number of semitones (can be negative)
 * @returns {Object} - Transposed note
 */
export function transposeNote(note, semitones) {
    const midi = typeof note === 'number' ? note : noteNameToMidi(note);
    const transposed = midi + semitones;

    return {
        name: midiToNoteName(transposed),
        midi: transposed,
        freq: midiToFrequency(transposed)
    };
}

/**
 * Quantize a MIDI note to the nearest scale note
 * @param {number} midiNote - MIDI note to quantize
 * @param {string} root - Scale root
 * @param {string} scaleName - Scale name
 * @returns {Object} - Quantized note
 */
export function quantizeToScale(midiNote, root, scaleName) {
    const octave = Math.floor(midiNote / 12) - 1;
    const scaleNotes = getScaleNotes(root, scaleName, octave - 1, 3);

    // Find closest scale note
    let closest = scaleNotes[0];
    let minDistance = Math.abs(midiNote - closest.midi);

    for (let note of scaleNotes) {
        const distance = Math.abs(midiNote - note.midi);
        if (distance < minDistance) {
            minDistance = distance;
            closest = note;
        }
    }

    return closest;
}

/**
 * Get dub-friendly scale recommendations
 * @param {string} mood - 'dark', 'mysterious', 'dreamy', 'deep'
 * @returns {string[]} - Recommended scale names
 */
export function getDubScales(mood = 'dark') {
    const recommendations = {
        'dark': ['minor', 'phrygian', 'harmonic_minor'],
        'mysterious': ['dorian', 'minor_pentatonic', 'whole_tone'],
        'dreamy': ['whole_tone', 'dorian', 'major'],
        'deep': ['minor', 'minor_pentatonic', 'phrygian']
    };

    return recommendations[mood] || recommendations['dark'];
}

/**
 * Get common bass note movements in dub techno
 * @param {number} rootMidi - Root note MIDI
 * @param {string} scaleName - Scale being used
 * @returns {Object} - Common bass movements
 */
export function getDubBassMovements(rootMidi, scaleName) {
    const scale = SCALES[scaleName];
    if (!scale) return { root: rootMidi };

    return {
        root: rootMidi,                           // Stay on root (most common)
        fourth: rootMidi + scale[3],              // Move to 4th degree (subdominant)
        fifth: rootMidi + scale[4],               // Move to 5th degree (dominant)
        octave: rootMidi + 12,                    // Octave jump
        octaveDown: rootMidi - 12                 // Octave drop
    };
}

/**
 * Create a swing timing function for groovy rhythms
 * @param {number} swingAmount - 0 (no swing) to 1 (full triplet swing)
 * @returns {Function} - Function that takes beat position and returns timing offset
 */
export function createSwingFunction(swingAmount = 0.5) {
    return (stepIndex, subdivision = 16) => {
        // Apply swing to even-numbered 16th notes
        if (stepIndex % 2 === 1) {
            // Delay the offbeat by swing amount
            // swingAmount 0.5 = 50% swing (triplet feel)
            // swingAmount 0.3 = 30% swing (subtle)
            return swingAmount * 0.125; // 0.125 = 16th note in 4/4
        }
        return 0;
    };
}

// For browser usage without modules
if (typeof window !== 'undefined') {
    window.MusicTheory = {
        SCALES,
        CHORDS,
        noteNameToMidi,
        midiToFrequency,
        noteToFrequency,
        midiToNoteName,
        getScaleNotes,
        getChordNotes,
        getRandomScaleNote,
        getInterval,
        transposeNote,
        quantizeToScale,
        getDubScales,
        getDubBassMovements,
        createSwingFunction
    };
}
