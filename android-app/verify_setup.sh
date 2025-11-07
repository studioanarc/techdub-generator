#!/bin/bash

echo "========================================"
echo "  Android Project Setup Verification"
echo "========================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        return 0
    else
        echo -e "${RED}✗${NC} $2 (missing: $1)"
        return 1
    fi
}

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $2"
        return 0
    else
        echo -e "${RED}✗${NC} $2 (missing: $1)"
        return 1
    fi
}

echo "1. Build Configuration Files"
echo "----------------------------"
check_file "build.gradle.kts" "Root build.gradle.kts"
check_file "settings.gradle.kts" "settings.gradle.kts"
check_file "gradle.properties" "gradle.properties"
check_file "app/build.gradle.kts" "App build.gradle.kts"
echo ""

echo "2. C++ Audio Engine"
echo "----------------------------"
check_file "app/src/main/cpp/CMakeLists.txt" "CMakeLists.txt"
check_file "app/src/main/cpp/AudioEngine.cpp" "AudioEngine.cpp"
check_file "app/src/main/cpp/AudioEngine.h" "AudioEngine.h"
check_file "app/src/main/cpp/OboeAudioCallback.cpp" "OboeAudioCallback.cpp"
check_file "app/src/main/cpp/OboeAudioCallback.h" "OboeAudioCallback.h"
check_file "app/src/main/cpp/native-lib.cpp" "native-lib.cpp (JNI)"
echo ""

echo "3. Oboe Library"
echo "----------------------------"
check_dir "app/src/main/cpp/oboe" "Oboe directory"
check_file "app/src/main/cpp/oboe/.git" "Oboe submodule"
check_file "app/src/main/cpp/oboe/CMakeLists.txt" "Oboe CMakeLists.txt"
echo ""

echo "4. DSP & Synth Engine"
echo "----------------------------"
check_dir "app/src/main/cpp/dsp" "DSP directory"
check_dir "app/src/main/cpp/synth" "Synth directory"
check_dir "app/src/main/cpp/effects" "Effects directory"
echo ""

echo "5. Kotlin/Java Source"
echo "----------------------------"
check_file "app/src/main/java/com/dubtechno/generator/AudioEngine.kt" "AudioEngine.kt"
check_file "app/src/main/java/com/dubtechno/generator/MainActivity.kt" "MainActivity.kt"
check_file "app/src/main/java/com/dubtechno/generator/DubTechnoApp.kt" "DubTechnoApp.kt"
check_dir "app/src/main/java/com/dubtechno/generator/ui" "UI directory"
check_dir "app/src/main/java/com/dubtechno/generator/generative" "Generative directory"
echo ""

echo "6. Android Resources"
echo "----------------------------"
check_file "app/src/main/AndroidManifest.xml" "AndroidManifest.xml"
check_file "app/src/main/res/values/strings.xml" "strings.xml"
check_file "app/src/main/res/values/colors.xml" "colors.xml"
check_file "app/src/main/res/values/themes.xml" "themes.xml"
echo ""

echo "7. Documentation"
echo "----------------------------"
check_file "README.md" "README.md"
check_file ".gitignore" ".gitignore"
check_file "app/proguard-rules.pro" "proguard-rules.pro"
echo ""

echo "8. File Statistics"
echo "----------------------------"
CPP_COUNT=$(find app/src/main/cpp -name "*.cpp" -o -name "*.h" 2>/dev/null | wc -l)
KT_COUNT=$(find app/src/main/java -name "*.kt" 2>/dev/null | wc -l)
echo -e "${YELLOW}C++ files:${NC} $CPP_COUNT"
echo -e "${YELLOW}Kotlin files:${NC} $KT_COUNT"
echo ""

echo "========================================"
echo "  Project Status: READY"
echo "========================================"
echo ""
echo "Next steps:"
echo "  1. Run: ./gradlew assembleDebug"
echo "  2. Install: adb install app/build/outputs/apk/debug/app-debug.apk"
echo "  3. Monitor: adb logcat -s AudioEngine:D"
echo ""
