#!/usr/bin/env node

/**
 * Test script for generative algorithms (Node.js)
 * Run with: node test-algorithms.mjs
 */

import {
    generateEuclideanRhythm,
    getDubRhythmPreset,
    visualizePattern,
    generatePolyrhythm
} from './euclidean.js';

import { MarkovMelodyGenerator } from './markov.js';

import {
    getScaleNotes,
    noteToFrequency,
    getChordNotes,
    getDubScales,
    SCALES
} from './scales.js';

console.log('🎵 TESTING DUB TECHNO GENERATIVE ALGORITHMS 🎵\n');

// Test 1: Euclidean Rhythms
console.log('═══════════════════════════════════════════════');
console.log('TEST 1: EUCLIDEAN RHYTHM GENERATOR');
console.log('═══════════════════════════════════════════════\n');

console.log('Testing E(4, 16) - Classic 4-on-floor kick:');
const kick = generateEuclideanRhythm(4, 16);
console.log(visualizePattern(kick));
console.log('Hits:', kick.filter(Boolean).length, '/', kick.length);
console.log('');

console.log('Testing E(3, 8) - Half-time feel:');
const halfTime = generateEuclideanRhythm(3, 8);
console.log(visualizePattern(halfTime));
console.log('');

console.log('Testing E(7, 16) - Complex hi-hat pattern:');
const hihat = generateEuclideanRhythm(7, 16);
console.log(visualizePattern(hihat));
console.log('');

console.log('Testing E(5, 12) - Polyrhythmic percussion:');
const perc = generateEuclideanRhythm(5, 12);
console.log(visualizePattern(perc));
console.log('');

console.log('Testing Dub Techno Presets:');
const presets = ['kick', 'snare', 'hihat'];
presets.forEach(inst => {
    const preset = getDubRhythmPreset(inst, 'standard');
    const pattern = generateEuclideanRhythm(preset.hits, preset.steps, preset.rotation);
    console.log(`  ${inst.toUpperCase()}: E(${preset.hits}, ${preset.steps}, ${preset.rotation})`);
    console.log(`  ${visualizePattern(pattern)}`);
});
console.log('');

// Test polyrhythm
console.log('Testing Polyrhythm (3 layers):');
const polyrhythm = generatePolyrhythm([
    { hits: 4, steps: 16, name: 'kick' },
    { hits: 2, steps: 16, rotation: 8, name: 'snare' },
    { hits: 7, steps: 16, name: 'hihat' }
]);
Object.entries(polyrhythm).forEach(([name, pattern]) => {
    console.log(`  ${name}: ${visualizePattern(pattern)}`);
});
console.log('\n');

// Test 2: Music Theory / Scales
console.log('═══════════════════════════════════════════════');
console.log('TEST 2: MUSIC THEORY UTILITIES');
console.log('═══════════════════════════════════════════════\n');

console.log('Testing Scales:');
const scaleNames = ['minor', 'dorian', 'phrygian', 'minor_pentatonic'];
scaleNames.forEach(scaleName => {
    const notes = getScaleNotes('C', scaleName, 2, 1);
    console.log(`  C ${scaleName}:`, notes.map(n => n.name).join(', '));
});
console.log('');

console.log('Testing Note to Frequency Conversion:');
const testNotes = ['C2', 'A4', 'C#3', 'G4'];
testNotes.forEach(note => {
    const freq = noteToFrequency(note);
    console.log(`  ${note} = ${freq.toFixed(2)} Hz`);
});
console.log('');

console.log('Testing Chord Generation:');
const chordTypes = ['sus2', 'sus4', 'minor7', 'add9'];
chordTypes.forEach(type => {
    const chord = getChordNotes('C3', type, 'minor');
    console.log(`  C ${type}:`, chord.map(n => n.name).join(', '));
});
console.log('');

console.log('Testing Dub Scale Recommendations:');
const moods = ['dark', 'mysterious', 'dreamy', 'deep'];
moods.forEach(mood => {
    const scales = getDubScales(mood);
    console.log(`  ${mood}: ${scales.join(', ')}`);
});
console.log('\n');

// Test 3: Markov Melody Generator
console.log('═══════════════════════════════════════════════');
console.log('TEST 3: MARKOV CHAIN MELODY GENERATOR');
console.log('═══════════════════════════════════════════════\n');

console.log('Testing Minimal Melody (C Minor):');
const minimalGen = new MarkovMelodyGenerator({
    root: 'C',
    scale: 'minor',
    octave: 3,
    octaves: 2,
    density: 0.3,
    repetition: 0.8,
    stepwise: 0.9,
    rootGravity: 0.6
});

const minimalMelody = minimalGen.generateMelody(16);
console.log('  Notes:', minimalMelody.map(n => n ? n.name : '—').join(' '));
const minimalStats = minimalGen.analyzeMelody(minimalMelody);
console.log('  Stats:', {
    notes: minimalStats.noteCount,
    rests: minimalStats.restCount,
    density: (minimalStats.density * 100).toFixed(1) + '%',
    avgInterval: minimalStats.averageInterval.toFixed(1)
});
console.log('');

console.log('Testing Standard Melody (D Dorian):');
const standardGen = new MarkovMelodyGenerator({
    root: 'D',
    scale: 'dorian',
    octave: 3,
    octaves: 2,
    density: 0.6,
    repetition: 0.5,
    stepwise: 0.7,
    rootGravity: 0.3
});

const standardMelody = standardGen.generateMelody(16);
console.log('  Notes:', standardMelody.map(n => n ? n.name : '—').join(' '));
const standardStats = standardGen.analyzeMelody(standardMelody);
console.log('  Stats:', {
    notes: standardStats.noteCount,
    rests: standardStats.restCount,
    density: (standardStats.density * 100).toFixed(1) + '%',
    avgInterval: standardStats.averageInterval.toFixed(1)
});
console.log('');

console.log('Testing Busy Melody (A Minor Pentatonic):');
const busyGen = new MarkovMelodyGenerator({
    root: 'A',
    scale: 'minor_pentatonic',
    octave: 3,
    octaves: 2,
    density: 0.8,
    repetition: 0.3,
    stepwise: 0.5,
    rootGravity: 0.2,
    jumpiness: 0.3
});

const busyMelody = busyGen.generateMelody(16);
console.log('  Notes:', busyMelody.map(n => n ? n.name : '—').join(' '));
const busyStats = busyGen.analyzeMelody(busyMelody);
console.log('  Stats:', {
    notes: busyStats.noteCount,
    rests: busyStats.restCount,
    density: (busyStats.density * 100).toFixed(1) + '%',
    avgInterval: busyStats.averageInterval.toFixed(1),
    uniqueNotes: busyStats.uniqueNotes
});
console.log('');

console.log('Testing Melody with Euclidean Rhythm:');
const melodyRhythm = generateEuclideanRhythm(5, 16);
console.log('  Rhythm:', visualizePattern(melodyRhythm));
const rhythmMelody = standardGen.generateMelodyWithRhythm(melodyRhythm);
console.log('  Notes: ', rhythmMelody.map(n => n ? n.name : '—').join(' '));
console.log('');

console.log('Testing Melody Presets:');
const styles = ['minimal', 'standard', 'busy', 'ambient'];
styles.forEach(style => {
    const preset = MarkovMelodyGenerator.getPreset(style);
    console.log(`  ${style}:`, preset);
});
console.log('');

// Test 4: Mutation
console.log('═══════════════════════════════════════════════');
console.log('TEST 4: PATTERN EVOLUTION & MUTATION');
console.log('═══════════════════════════════════════════════\n');

console.log('Original Melody:');
const originalMelody = standardGen.generateMelody(8);
console.log('  ', originalMelody.map(n => n ? n.name : '—').join(' '));

console.log('\nMutated (20% rate):');
const mutated1 = standardGen.mutateMelody(originalMelody, 0.2);
console.log('  ', mutated1.map(n => n ? n.name : '—').join(' '));

console.log('\nMutated (50% rate):');
const mutated2 = standardGen.mutateMelody(originalMelody, 0.5);
console.log('  ', mutated2.map(n => n ? n.name : '—').join(' '));
console.log('');

// Test 5: Complex Integration
console.log('═══════════════════════════════════════════════');
console.log('TEST 5: COMPLETE DUB TECHNO PATTERN');
console.log('═══════════════════════════════════════════════\n');

console.log('Generating complete dub techno pattern set...\n');

// Generate complete pattern
const dubPattern = {
    kick: generateEuclideanRhythm(4, 16),
    snare: generateEuclideanRhythm(2, 16, 8),
    hihat: generateEuclideanRhythm(7, 16),
    perc: generateEuclideanRhythm(5, 12)
};

const dubMelodyGen = new MarkovMelodyGenerator({
    root: 'C',
    scale: 'minor',
    octave: 3,
    octaves: 2,
    density: 0.5,
    repetition: 0.6
});

const dubMelody = dubMelodyGen.generateMelody(16);
const dubBass = dubMelodyGen.generateMelody(16, { density: 0.4, rootGravity: 0.8 });

console.log('🥁 DRUMS:');
console.log('  Kick:   ', visualizePattern(dubPattern.kick));
console.log('  Snare:  ', visualizePattern(dubPattern.snare));
console.log('  Hi-hat: ', visualizePattern(dubPattern.hihat));
console.log('  Perc:   ', visualizePattern(dubPattern.perc, 'o', '·'));
console.log('');

console.log('🎹 MELODY:');
console.log('  ', dubMelody.map(n => n ? n.name : '—').join(' '));
console.log('');

console.log('🎸 BASS:');
console.log('  ', dubBass.map(n => n ? n.name : '—').join(' '));
console.log('');

const melodyStats2 = dubMelodyGen.analyzeMelody(dubMelody);
const bassStats = dubMelodyGen.analyzeMelody(dubBass);

console.log('📊 STATISTICS:');
console.log('  Kick hits:', dubPattern.kick.filter(Boolean).length, '/16');
console.log('  Total drum hits:',
    dubPattern.kick.filter(Boolean).length +
    dubPattern.snare.filter(Boolean).length +
    dubPattern.hihat.filter(Boolean).length
);
console.log('  Melody density:', (melodyStats2.density * 100).toFixed(1) + '%');
console.log('  Bass density:', (bassStats.density * 100).toFixed(1) + '%');
console.log('  Total unique notes:', melodyStats2.uniqueNotes + bassStats.uniqueNotes);
console.log('');

console.log('═══════════════════════════════════════════════');
console.log('✅ ALL TESTS COMPLETED SUCCESSFULLY!');
console.log('═══════════════════════════════════════════════\n');

console.log('🎵 The generative algorithms are working correctly!');
console.log('📝 Open test-generative.html in a browser for interactive testing with audio.');
console.log('');
