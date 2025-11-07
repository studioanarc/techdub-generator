cmake_minimum_required(VERSION 3.22.1)

project("dubtechno")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Add Oboe library
add_subdirectory(oboe)

# DSP source files
set(DSP_SOURCES
    dsp/DSPUtils.cpp
    dsp/Biquad.cpp
    dsp/DelayLine.cpp
    dsp/AllPassFilter.cpp
)

# Effects source files
set(EFFECTS_SOURCES
    effects/Delay.cpp
    effects/Reverb.cpp
    effects/Filter.cpp
    effects/Chorus.cpp
    effects/Phaser.cpp
    effects/Distortion.cpp
    effects/BitCrusher.cpp
    effects/Compressor.cpp
    effects/TapeEcho.cpp
    effects/ConvolutionReverb.cpp
)

# Synth source files
set(SYNTH_SOURCES
    synth/Oscillator.cpp
    synth/Filter.cpp
    synth/Envelope.cpp
    synth/LFO.cpp
    synth/Voice.cpp
    synth/VoiceManager.cpp
    synth/MonoSynth.cpp
    synth/PolySynth.cpp
)

# Main source files
set(DUBTECHNO_SOURCES
    native-lib.cpp
    AudioEngine.cpp
    OboeAudioCallback.cpp
    ${DSP_SOURCES}
    ${EFFECTS_SOURCES}
    ${SYNTH_SOURCES}
)

# Create shared library
add_library(dubtechno SHARED ${DUBTECHNO_SOURCES})

# Include directories
target_include_directories(dubtechno PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/oboe/include
)

# Link libraries
target_link_libraries(dubtechno
    oboe
    log
    OpenSLES
    android
)

# Compiler flags
target_compile_options(dubtechno PRIVATE
    -Wall
    -Wextra
    -Wno-unused-parameter
    -fexceptions
    -frtti
    -ffast-math
)

# Enable optimization for release builds
if(CMAKE_BUILD_TYPE MATCHES Release)
    target_compile_options(dubtechno PRIVATE -O3 -DNDEBUG)
endif()

# Enable NEON for ARM (for future SIMD optimizations)
if(${ANDROID_ABI} STREQUAL "arm64-v8a")
    target_compile_options(dubtechno PRIVATE -march=armv8-a)
elseif(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    target_compile_options(dubtechno PRIVATE -mfpu=neon)
endif()
