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

echo "[1/4] Preparing target build directories..."
rm -rf "${CLASSES_DIR}"
mkdir -p "${CLASSES_DIR}"

echo "[2/4] Compiling Java sources with J2ME CLDC 1.1 / MIDP 2.0 / JSR-75 stubs..."
# Collect all Java source files
JAVA_FILES=()
while IFS= read -r file; do
    JAVA_FILES+=("$file")
done < <(find src/main/java -name "*.java" | sort)

javac -source 1.8 -target 1.8 \
      -Xlint:-options \
      -cp "lib/cldcapi11.jar:lib/midpapi20.jar:lib/microemu-jsr-75.jar" \
      -d "${CLASSES_DIR}" \
      "${JAVA_FILES[@]}"

echo "[3/4] Copying embedded resources..."
cp -r src/main/resources/* "${CLASSES_DIR}/"
# Remove manifest from classes root so jar tool puts it in META-INF correctly
rm -rf "${CLASSES_DIR}/META-INF"

echo "[4/4] Packaging target JAR: ${TARGET_JAR}..."
jar cfm "${TARGET_JAR}" src/main/resources/META-INF/MANIFEST.MF -C "${CLASSES_DIR}" .

echo "=========================================================="
echo " Build Succeeded!"
echo " Output JAR: ${TARGET_JAR}"
ls -lh "${TARGET_JAR}"
echo "=========================================================="
