/**
 * Euclidean Rhythm Generator
 * Implements Björklund's algorithm for generating evenly-distributed rhythmic patterns
 * Perfect for creating compelling dub techno drum patterns
 *
 * E(k, n) = Distribute k hits over n steps as evenly as possible
 *
 * Examples:
 * - E(4, 16) = [x . . . x . . . x . . . x . . .] (standard 4-on-floor kick)
 * - E(3, 8)  = [x . . x . . x .] (half-time dub kick)
 * - E(5, 8)  = [x . x . x . x x] (complex polyrhythm)
 * - E(7, 16) = [x . x . x . x . x . x . x . . .] (sparse hi-hat)
 */

/**
 * Generate Euclidean rhythm using Björklund's algorithm
 * @param {number} hits - Number of pulses/hits (k)
 * @param {number} steps - Total number of steps (n)
 * @param {number} rotation - Rotate pattern by this many steps
 * @returns {boolean[]} - Array of true (hit) and false (rest)
 */
export function generateEuclideanRhythm(hits, steps, rotation = 0) {
    // Validate inputs
    if (hits < 0 || steps < 0) {
        throw new Error('Hits and steps must be non-negative');
    }
    if (hits > steps) {
        throw new Error('Hits cannot exceed steps');
    }

    // Edge cases
    if (steps === 0) return [];
    if (hits === 0) return new Array(steps).fill(false);
    if (hits === steps) return new Array(steps).fill(true);

    // Björklund's algorithm implementation
    // We represent the pattern as groups of [1] and [0]
    // Then repeatedly merge groups until we can't anymore

    // Start with k groups of [1] and (n-k) groups of [0]
    let groups = [];

    // Create k groups containing [1]
    for (let i = 0; i < hits; i++) {
        groups.push([1]);
    }

    // Create (n-k) groups containing [0]
    for (let i = 0; i < steps - hits; i++) {
        groups.push([0]);
    }

    // Björklund's algorithm: repeatedly merge groups
    let level = 0;

    while (level < steps) {
        let firstGroupSize = groups[0].length;
        let lastGroupSize = groups[groups.length - 1].length;

        // If all groups are the same size, we're done
        if (firstGroupSize === lastGroupSize) {
            break;
        }

        // Count how many groups of each size we have
        let sameAsFirst = 0;
        let sameAsLast = 0;

        for (let i = 0; i < groups.length; i++) {
            if (groups[i].length === firstGroupSize) {
                sameAsFirst++;
            } else {
                sameAsLast++;
            }
        }

        // If there's only one group of different size, we're done
        if (sameAsFirst === 1 || sameAsLast === 1) {
            break;
        }

        // Merge groups: take groups from the end and append to groups from the beginning
        let newGroups = [];
        let minPairs = Math.min(sameAsFirst, sameAsLast);

        // Merge the smaller count of pairs
        for (let i = 0; i < minPairs; i++) {
            newGroups.push([...groups[i], ...groups[sameAsFirst + i]]);
        }

        // Add remaining groups
        for (let i = minPairs; i < Math.max(sameAsFirst, sameAsLast); i++) {
            if (i < sameAsFirst) {
                newGroups.push([...groups[i]]);
            } else {
                newGroups.push([...groups[sameAsFirst + i - sameAsFirst]]);
            }
        }

        groups = newGroups;
        level++;
    }

    // Flatten the groups into a single array
    let pattern = groups.flat();

    // Convert to boolean array
    let result = pattern.map(x => x === 1);

    // Apply rotation if specified
    if (rotation !== 0) {
        rotation = ((rotation % steps) + steps) % steps; // Normalize rotation
        result = [...result.slice(rotation), ...result.slice(0, rotation)];
    }

    return result;
}

/**
 * Generate multiple layered Euclidean rhythms for polyrhythmic patterns
 * @param {Array<{hits: number, steps: number, rotation?: number}>} layers - Array of rhythm specs
 * @returns {Object} - Object with pattern arrays for each layer
 */
export function generatePolyrhythm(layers) {
    const patterns = {};

    layers.forEach((layer, index) => {
        const { hits, steps, rotation = 0, name = `layer${index}` } = layer;
        patterns[name] = generateEuclideanRhythm(hits, steps, rotation);
    });

    return patterns;
}

/**
 * Get common dub techno Euclidean rhythm presets
 * @param {string} instrumentType - 'kick', 'snare', 'hihat', 'perc'
 * @param {string} feel - 'minimal', 'standard', 'busy'
 * @returns {Object} - {hits, steps, rotation}
 */
export function getDubRhythmPreset(instrumentType, feel = 'standard') {
    const presets = {
        kick: {
            minimal: { hits: 3, steps: 16, rotation: 0 },    // Sparse, deep dub
            standard: { hits: 4, steps: 16, rotation: 0 },    // Classic 4-on-floor
            busy: { hits: 5, steps: 16, rotation: 0 }         // More active
        },
        snare: {
            minimal: { hits: 1, steps: 16, rotation: 8 },     // Single backbeat
            standard: { hits: 2, steps: 16, rotation: 8 },    // Classic backbeat
            busy: { hits: 3, steps: 16, rotation: 5 }         // Syncopated
        },
        hihat: {
            minimal: { hits: 5, steps: 16, rotation: 0 },     // Sparse texture
            standard: { hits: 7, steps: 16, rotation: 0 },    // Medium density
            busy: { hits: 11, steps: 16, rotation: 0 }        // Complex pattern
        },
        perc: {
            minimal: { hits: 3, steps: 8, rotation: 0 },      // Simple rhythm
            standard: { hits: 5, steps: 12, rotation: 0 },    // Polyrhythmic
            busy: { hits: 7, steps: 13, rotation: 0 }         // Very complex
        }
    };

    return presets[instrumentType]?.[feel] || { hits: 4, steps: 16, rotation: 0 };
}

/**
 * Visualize a rhythm pattern as a string
 * @param {boolean[]} pattern - Rhythm pattern
 * @param {string} hitChar - Character for hits
 * @param {string} restChar - Character for rests
 * @returns {string} - Visual representation
 */
export function visualizePattern(pattern, hitChar = 'x', restChar = '.') {
    return pattern.map((hit, i) => {
        // Add separator every 4 steps for readability
        const sep = (i > 0 && i % 4 === 0) ? ' ' : '';
        return sep + (hit ? hitChar : restChar);
    }).join(' ');
}

/**
 * Apply probability-based triggering to a pattern
 * @param {boolean[]} pattern - Base rhythm pattern
 * @param {number} probability - Probability (0-1) of each hit actually triggering
 * @returns {boolean[]} - Probabilistic pattern
 */
export function applyProbability(pattern, probability = 1.0) {
    return pattern.map(hit => hit && (Math.random() < probability));
}

/**
 * Create variations of a pattern by shifting or inverting
 * @param {boolean[]} pattern - Base pattern
 * @param {string} variation - 'shift', 'invert', 'reverse', 'double'
 * @returns {boolean[]} - Varied pattern
 */
export function variatePattern(pattern, variation) {
    switch (variation) {
        case 'shift':
            // Shift by 1 step
            return [...pattern.slice(1), pattern[0]];

        case 'invert':
            // Flip hits and rests
            return pattern.map(hit => !hit);

        case 'reverse':
            // Reverse the pattern
            return [...pattern].reverse();

        case 'double':
            // Each step becomes two steps
            return pattern.flatMap(hit => [hit, hit]);

        case 'halftime':
            // Every other step
            return pattern.filter((_, i) => i % 2 === 0);

        default:
            return pattern;
    }
}

/**
 * Combine two patterns using logical operations
 * @param {boolean[]} pattern1 - First pattern
 * @param {boolean[]} pattern2 - Second pattern
 * @param {string} operation - 'and', 'or', 'xor'
 * @returns {boolean[]} - Combined pattern
 */
export function combinePatterns(pattern1, pattern2, operation = 'or') {
    const maxLength = Math.max(pattern1.length, pattern2.length);
    const result = [];

    for (let i = 0; i < maxLength; i++) {
        const a = pattern1[i % pattern1.length];
        const b = pattern2[i % pattern2.length];

        switch (operation) {
            case 'and':
                result.push(a && b);
                break;
            case 'or':
                result.push(a || b);
                break;
            case 'xor':
                result.push((a || b) && !(a && b));
                break;
            default:
                result.push(a || b);
        }
    }

    return result;
}

// For browser usage without modules
if (typeof window !== 'undefined') {
    window.EuclideanRhythm = {
        generateEuclideanRhythm,
        generatePolyrhythm,
        getDubRhythmPreset,
        visualizePattern,
        applyProbability,
        variatePattern,
        combinePatterns
    };
}
