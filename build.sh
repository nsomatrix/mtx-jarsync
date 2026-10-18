#!/usr/bin/env bash
set -euo pipefail

echo "=========================================================="
echo " Building Embed Advance Menu 3.6.3 (Clean J2ME Tool Source)"
echo "=========================================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

BUILD_DIR="target"
CLASSES_DIR="${BUILD_DIR}/classes"
TARGET_JAR="${BUILD_DIR}/EmbedAdvMenu.jar"
LIBS_BUILD_DIR="${BUILD_DIR}/libs_build"
GEN_RES_DIR="${BUILD_DIR}/generated_resources"

echo "[1/5] Preparing target build directories..."
rm -rf "${CLASSES_DIR}" "${LIBS_BUILD_DIR}" "${GEN_RES_DIR}"
mkdir -p "${CLASSES_DIR}" "${LIBS_BUILD_DIR}" "${GEN_RES_DIR}"

echo "[2/5] Compiling runtime injection libraries from source..."
# 1. MrQuyetlibs (Core isolation runtime)
mkdir -p "${LIBS_BUILD_DIR}/mrquyetlibs"
javac -source 1.8 -target 1.8 -Xlint:-options \
      -cp "lib/cldcapi11.jar:lib/midpapi20.jar:lib/microemu-jsr-75.jar" \
      -d "${LIBS_BUILD_DIR}/mrquyetlibs" \
      $(find libs/mrquyetlibs/src -name "*.java" | sort)
jar cf "${GEN_RES_DIR}/MrQuyetlibs.jar" -C "${LIBS_BUILD_DIR}/mrquyetlibs" .

# 2. MrQuyetlib (Standard in-game switcher overlay)
mkdir -p "${LIBS_BUILD_DIR}/mrquyetlib"
javac -source 1.8 -target 1.8 -Xlint:-options \
      -cp "lib/cldcapi11.jar:lib/midpapi20.jar:lib/microemu-jsr-75.jar:lib/nokia-ui.jar:${LIBS_BUILD_DIR}/mrquyetlibs" \
      -d "${LIBS_BUILD_DIR}/mrquyetlib" \
      $(find libs/mrquyetlib/src -name "*.java" | sort)
jar cf "${GEN_RES_DIR}/MrQuyetlib.jar" -C "${LIBS_BUILD_DIR}/mrquyetlib" .

# 3. MrQuyetSSlib (Screenshot & PNG encoder runtime)
mkdir -p "${LIBS_BUILD_DIR}/mrquyetsslib"
javac -source 1.8 -target 1.8 -Xlint:-options \
      -cp "lib/cldcapi11.jar:lib/midpapi20.jar:lib/microemu-jsr-75.jar:lib/nokia-ui.jar:${LIBS_BUILD_DIR}/mrquyetlibs" \
      -d "${LIBS_BUILD_DIR}/mrquyetsslib" \
      $(find libs/mrquyetsslib/src -name "*.java" | sort)
jar cf "${GEN_RES_DIR}/MrQuyetSSlib.jar" -C "${LIBS_BUILD_DIR}/mrquyetsslib" .

echo "[3/5] Compiling main editor application sources..."
JAVA_FILES=()
while IFS= read -r file; do
    JAVA_FILES+=("$file")
done < <(find src/main/java -name "*.java" | sort)

javac -source 1.8 -target 1.8 \
      -Xlint:-options \
      -cp "lib/cldcapi11.jar:lib/midpapi20.jar:lib/microemu-jsr-75.jar" \
      -d "${CLASSES_DIR}" \
      "${JAVA_FILES[@]}"

echo "[4/5] Packaging embedded runtime libraries and resources..."
cp -r src/main/resources/* "${CLASSES_DIR}/"
cp "${GEN_RES_DIR}/"*.jar "${CLASSES_DIR}/"
# Remove manifest from classes root so jar tool puts it in META-INF correctly
rm -rf "${CLASSES_DIR}/META-INF"

echo "[5/5] Packaging target JAR: ${TARGET_JAR}..."
jar cfm "${TARGET_JAR}" src/main/resources/META-INF/MANIFEST.MF -C "${CLASSES_DIR}" .

echo "=========================================================="
echo " Build Succeeded! 100% compiled from source."
echo " Output JAR: ${TARGET_JAR}"
ls -lh "${TARGET_JAR}"
echo "=========================================================="
