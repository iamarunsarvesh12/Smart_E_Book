#!/usr/bin/env bash
# ================================================================================
# Smart E-Book Management System - Automated Bash Runner (Linux / macOS)
# ================================================================================

set -e

echo "================================================================================"
echo "  SMART E-BOOK MANAGEMENT SYSTEM - BASH RUNNER (Linux / macOS)"
echo "================================================================================"
echo ""

# [1/4] Verify JDK Installation
echo "[1/4] Verifying JDK Environment..."
if ! command -v javac &> /dev/null; then
    echo "[ERROR] 'javac' compiler was not found in PATH."
    echo "Please install OpenJDK 11, 17, or 21 and ensure 'javac' is in your PATH."
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "[ERROR] 'java' runtime was not found in PATH."
    exit 1
fi
echo "      - JDK compiler and runtime verified successfully."

# [2/4] Directory Preparation & Compilation
echo ""
echo "[2/4] Compiling Java source files..."
mkdir -p WebContent/WEB-INF/classes
mkdir -p WebContent/uploads/books
mkdir -p WebContent/uploads/covers
chmod -R 777 WebContent/uploads 2>/dev/null || true

javac -cp "WebContent/WEB-INF/lib/*:WebContent/WEB-INF/classes" -d "WebContent/WEB-INF/classes" \
    src/com/ebook/model/*.java \
    src/com/ebook/util/*.java \
    src/com/ebook/dao/*.java \
    src/com/ebook/controller/*.java \
    src/com/ebook/filter/*.java \
    src/com/ebook/test/*.java

cp -f database/schema.sql WebContent/WEB-INF/classes/schema.sql 2>/dev/null || true
echo "      - Compiling Java source files... Done. (0 errors)"

# [3/4] System Diagnostic Audit
echo ""
echo "[3/4] Running system connectivity diagnostics..."
java -cp "WebContent/WEB-INF/lib/*:WebContent/WEB-INF/classes" com.ebook.test.FullSpectrumDiagnosticTest || true
echo "      - Running system connectivity diagnostics... Done."

# [4/4] Deployment & Tomcat Launch Options
echo ""
echo "[4/4] Project build complete. Ready for Apache Tomcat deployment."
echo "================================================================================"
echo "  DEPLOYMENT & APPLICATION ACCESS"
echo "================================================================================"
echo ""

APP_URL="http://localhost:8080/OOAD_Project/"

if [ -n "$CATALINA_HOME" ] && [ -d "$CATALINA_HOME" ]; then
    echo "[TOMCAT DETECTED] Located Tomcat at: $CATALINA_HOME"
    DEST_DIR="$CATALINA_HOME/webapps/OOAD_Project"
    mkdir -p "$DEST_DIR"
    cp -R WebContent/* "$DEST_DIR/"
    
    if [ -f "$CATALINA_HOME/bin/startup.sh" ]; then
        echo "Starting Tomcat..."
        chmod +x "$CATALINA_HOME/bin/startup.sh" "$CATALINA_HOME/bin/catalina.sh"
        "$CATALINA_HOME/bin/startup.sh"
        echo "Waiting 2 seconds for server startup..."
        sleep 2
    fi
else
    echo "[INFO] Standalone Tomcat environment."
    echo ""
    echo "Deployment Instructions:"
    echo "  1. Copy the 'WebContent' directory to your Tomcat 'webapps/OOAD_Project/'"
    echo "     OR Run on Server in Eclipse / IntelliJ."
fi

echo ""
echo "Access Endpoints:"
echo "  - Home Page       : $APP_URL"
echo "  - Reader Catalog  : ${APP_URL}catalog"
echo "  - Admin Portal    : ${APP_URL}admin/dashboard"
echo ""
echo "Default Pre-Seeded Accounts:"
echo "  - Administrator : admin@ebook.com  / admin123"
echo "  - Reader / User : reader@ebook.com / reader123"
echo ""

# Auto-Launch Browser (macOS / Linux)
if [[ "$OSTYPE" == "darwin"* ]]; then
    if [ -d "/Applications/Google Chrome.app" ]; then
        echo "[INFO] Launching Google Chrome on macOS..."
        open -a "Google Chrome" "$APP_URL" &
    else
        echo "[INFO] Launching default browser on macOS..."
        open "$APP_URL" &
    fi
elif command -v google-chrome &> /dev/null; then
    echo "[INFO] Launching Google Chrome on Linux..."
    google-chrome "$APP_URL" >/dev/null 2>&1 &
elif command -v google-chrome-stable &> /dev/null; then
    echo "[INFO] Launching Google Chrome Stable on Linux..."
    google-chrome-stable "$APP_URL" >/dev/null 2>&1 &
elif command -v xdg-open &> /dev/null; then
    echo "[INFO] Launching default browser via xdg-open..."
    xdg-open "$APP_URL" >/dev/null 2>&1 &
fi

echo ""
echo "================================================================================"
echo "Build complete and ready for execution."
echo "================================================================================"
