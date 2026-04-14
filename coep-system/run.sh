#!/bin/bash
# ============================================================
#  COEP System - Build & Run Script
# ============================================================

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_ROOT"
OUT_DIR="$PROJECT_ROOT/out"

# echo ""
# echo "  +============================================+"
# echo "  |   COEP System - Build & Run              |"
# echo "  +============================================+"
# echo ""

# ── Step 1: Find javac ────────────────────────────────────
JAVAC=$(which javac 2>/dev/null || echo "")
if [ -z "$JAVAC" ]; then
  # Try common JDK locations
  for loc in /usr/lib/jvm/java-*/bin/javac /usr/local/lib/jvm/*/bin/javac; do
    if [ -f "$loc" ]; then
      JAVAC="$loc"
      break
    fi
  done
fi

if [ -z "$JAVAC" ]; then
  echo "  [ERROR] javac not found. Please install JDK 11 or higher."
  echo "          Ubuntu/Debian: sudo apt-get install default-jdk"
  echo "          macOS:         brew install openjdk"
  exit 1
fi

JAVA=$(dirname "$JAVAC")/java
# echo "  [OK] Using javac: $JAVAC"
# echo ""

# ── Step 2: Compile ───────────────────────────────────────
mkdir -p "$OUT_DIR"
echo "  Compiling Java source files..."

"$JAVAC" -d "$OUT_DIR" \
  "$SRC_DIR"/utils/*.java \
  "$SRC_DIR"/users/*.java \
  "$SRC_DIR"/courses/*.java \
  "$SRC_DIR"/exams/*.java \
  "$SRC_DIR"/services/*.java \
  "$SRC_DIR"/controllers/*.java \
  "$SRC_DIR"/gui/*.java \
  "$SRC_DIR"/main/*.java

# echo "  [OK] Compilation successful!"
# echo ""

# ── Step 3: Run ───────────────────────────────────────────
# echo "  Starting COEP System..."
# echo ""

cd "$PROJECT_ROOT"
"$JAVA" -cp "$OUT_DIR" main.Main
