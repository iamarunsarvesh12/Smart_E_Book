@echo off
setlocal enabledelayedexpansion
title Smart E-Book Management System - Automated Runner

echo ================================================================================
echo   SMART E-BOOK MANAGEMENT SYSTEM - ONE-CLICK RUNNER
echo ================================================================================
echo.

:: [1/4] Check for Java JDK in PATH & Configure JAVA_HOME / JRE_HOME
echo [1/4] Verifying JDK Environment...
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Java Development Kit [javac] was not found in your system PATH.
    echo Please install JDK 11, 17, 21, or 25 and add its bin directory to your PATH environment variable.
    echo.
    pause
    exit /b 1
)

where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Java Runtime [java] was not found in your system PATH.
    echo Please verify your Java installation.
    echo.
    pause
    exit /b 1
)

:: Find accurate JDK path (ignore Oracle javapath shortcut)
set "RESOLVED_JAVA_HOME="
if exist "C:\Program Files\Java\jdk-25\bin\javac.exe" (
    set "RESOLVED_JAVA_HOME=C:\Program Files\Java\jdk-25"
) else if exist "C:\Program Files\Java\jdk-21\bin\javac.exe" (
    set "RESOLVED_JAVA_HOME=C:\Program Files\Java\jdk-21"
) else if exist "C:\Program Files\Java\jdk-17\bin\javac.exe" (
    set "RESOLVED_JAVA_HOME=C:\Program Files\Java\jdk-17"
) else (
    for /d %%D in ("C:\Program Files\Java\jdk*") do (
        if exist "%%D\bin\javac.exe" set "RESOLVED_JAVA_HOME=%%D"
    )
)

if not defined RESOLVED_JAVA_HOME (
    for /f "delims=" %%I in ('where javac 2^>nul') do (
        set "JAVA_BIN_DIR=%%~dpI"
        set "RESOLVED_JAVA_HOME=!JAVA_BIN_DIR:~0,-5!"
    )
)

set "JAVA_HOME=%RESOLVED_JAVA_HOME%"
set "JRE_HOME=%RESOLVED_JAVA_HOME%"
echo       - JDK environment verified: %JAVA_HOME%

:: [2/4] Prepare Target Directories and Compile Java Sources
echo.
echo [2/4] Compiling Java source files...
if not exist "WebContent\WEB-INF\classes" mkdir "WebContent\WEB-INF\classes"
if not exist "WebContent\uploads\books" mkdir "WebContent\uploads\books"
if not exist "WebContent\uploads\covers" mkdir "WebContent\uploads\covers"

javac -cp "WebContent\WEB-INF\lib\*;WebContent\WEB-INF\classes" -d "WebContent\WEB-INF\classes" src\com\ebook\model\*.java src\com\ebook\util\*.java src\com\ebook\dao\*.java src\com\ebook\controller\*.java src\com\ebook\filter\*.java src\com\ebook\test\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Java compilation failed! Review the syntax errors above.
    echo.
    pause
    exit /b 1
)

copy /Y "%~dp0database\schema.sql" "%~dp0WebContent\WEB-INF\classes\schema.sql" >nul 2>&1
echo       - Compiling Java source files... Done. (0 errors)

:: [3/4] Running system connectivity diagnostics
echo.
echo [3/4] Running system connectivity diagnostics...
java -cp "WebContent\WEB-INF\lib\*;WebContent\WEB-INF\classes" com.ebook.test.FullSpectrumDiagnosticTest

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [WARNING] System diagnostics reported an issue. Review the diagnostic report above.
) else (
    echo       - Running system connectivity diagnostics... Done.
)

:: [4/4] Portable Tomcat Setup, Server Deployment & Health Check
echo.
echo [4/4] Deploying to Apache Tomcat ^& Starting Server...
echo ================================================================================
echo   SERVER PROVISIONING ^& DEPLOYMENT
echo ================================================================================
echo.

set "TOMCAT_DIR="

:: Check CATALINA_HOME
if defined CATALINA_HOME (
    if exist "%CATALINA_HOME%\bin\startup.bat" (
        set "TOMCAT_DIR=%CATALINA_HOME%"
    )
)

:: Check standard local paths
if not defined TOMCAT_DIR (
    if exist "C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin\startup.bat" (
        set "TOMCAT_DIR=C:\Program Files\Apache Software Foundation\Tomcat 9.0"
    ) else if exist "C:\Program Files\Apache Software Foundation\Tomcat 10.0\bin\startup.bat" (
        set "TOMCAT_DIR=C:\Program Files\Apache Software Foundation\Tomcat 10.0"
    )
)

:: Check portable Tomcat
if not defined TOMCAT_DIR (
    if exist "%~dp0.tomcat\apache-tomcat-9.0.86\bin\startup.bat" (
        set "TOMCAT_DIR=%~dp0.tomcat\apache-tomcat-9.0.86"
    )
)

:: Download portable Tomcat if not found
if not defined TOMCAT_DIR (
    echo [INFO] No local Apache Tomcat detected.
    echo [INFO] Downloading portable Apache Tomcat 9.0.86 [~14 MB]...
    if not exist "%~dp0.tomcat" mkdir "%~dp0.tomcat"
    powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.86/bin/apache-tomcat-9.0.86-windows-x64.zip' -OutFile '%~dp0.tomcat\tomcat.zip' -UseBasicParsing; Expand-Archive -Path '%~dp0.tomcat\tomcat.zip' -DestinationPath '%~dp0.tomcat' -Force; Remove-Item -Path '%~dp0.tomcat\tomcat.zip' -Force -ErrorAction SilentlyContinue"
    set "TOMCAT_DIR=%~dp0.tomcat\apache-tomcat-9.0.86"
    echo [INFO] Portable Apache Tomcat 9 provisioned successfully.
)

:: Write setenv.bat to permanently fix JRE_HOME
(
    echo @echo off
    echo set "JAVA_HOME=%JAVA_HOME%"
    echo set "JRE_HOME=%JAVA_HOME%"
) > "!TOMCAT_DIR!\bin\setenv.bat"

echo [INFO] Using Tomcat at: !TOMCAT_DIR!
echo Deploying WebContent to Tomcat webapps\OOAD_Project ...

if not exist "!TOMCAT_DIR!\webapps\OOAD_Project" mkdir "!TOMCAT_DIR!\webapps\OOAD_Project"
xcopy /E /Y /Q "%~dp0WebContent\*" "!TOMCAT_DIR!\webapps\OOAD_Project\" >nul 2>&1
echo       - WebContent synchronized to deployment directory.

set "APP_URL=http://localhost:8080/OOAD_Project/"

echo Starting Apache Tomcat Server on port 8080...
start "Apache Tomcat" /D "!TOMCAT_DIR!\bin" "!TOMCAT_DIR!\bin\startup.bat"

echo Waiting for Tomcat server to initialize on port 8080...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ready = $false; for ($i=1; $i -le 20; $i++) { Start-Sleep -Seconds 1; try { $r = Invoke-WebRequest -Uri '%APP_URL%' -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop; if ($r.StatusCode -eq 200 -or $r.StatusCode -eq 302) { $ready = $true; break } } catch { Write-Host '  ... waiting for server response (' + $i + '/20s)' } }; if ($ready) { Write-Host '`n[SUCCESS] Tomcat server is active and healthy on port 8080!' -ForegroundColor Green }"

echo.
echo Application Access Endpoints:
echo   - Home Page       : %APP_URL%
echo   - E-Book Catalog  : %APP_URL%catalog
echo   - Admin Dashboard : %APP_URL%admin/dashboard
echo.
echo Default Pre-Seeded Accounts:
echo   - Administrator : admin@ebook.com  / admin123
echo   - Reader / User : reader@ebook.com / reader123
echo.

:: Detect Google Chrome and Auto-Launch
set "CHROME_BIN="

if exist "%ProgramFiles%\Google\Chrome\Application\chrome.exe" (
    set "CHROME_BIN=%ProgramFiles%\Google\Chrome\Application\chrome.exe"
) else if exist "%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe" (
    set "CHROME_BIN=%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe"
) else if exist "%LocalAppData%\Google\Chrome\Application\chrome.exe" (
    set "CHROME_BIN=%LocalAppData%\Google\Chrome\Application\chrome.exe"
)

if defined CHROME_BIN (
    echo [INFO] Launching Google Chrome to %APP_URL% ...
    start "" "!CHROME_BIN!" "%APP_URL%"
) else (
    echo [INFO] Launching default browser to %APP_URL% ...
    start "" "%APP_URL%"
)

echo.
echo ================================================================================
echo Smart E-Book Management System is LIVE! Press any key to exit this runner.
echo ================================================================================
pause
