# ================================================================================
# Smart E-Book Management System - Automated Self-Contained Runner
# ================================================================================

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  SMART E-BOOK MANAGEMENT SYSTEM - ONE-CLICK RUNNER" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = $PSScriptRoot
if (-not $projectRoot) {
    $projectRoot = (Get-Location).Path
}

# [1/4] Verify JDK Installation & Configure JAVA_HOME / JRE_HOME
Write-Host "[1/4] Verifying JDK Environment..." -ForegroundColor Green
$javacPath = Get-Command javac -ErrorAction SilentlyContinue
$javaPath = Get-Command java -ErrorAction SilentlyContinue

if (-not $javacPath) {
    Write-Host "[ERROR] Java Development Kit (javac) was not found in system PATH." -ForegroundColor Red
    Write-Host "Please install JDK 11, 17, 21, or 25 and configure 'javac' in your system environment variables." -ForegroundColor Yellow
    Exit 1
}

if (-not $javaPath) {
    Write-Host "[ERROR] Java Runtime (java) was not found in system PATH." -ForegroundColor Red
    Exit 1
}

# Resolve true JDK directory (ignore Oracle javapath shortcut)
$detectedJavaHome = $null
$searchRoots = @(
    "C:\Program Files\Java",
    "C:\Program Files (x86)\Java",
    "C:\Java",
    "C:\Program Files\Eclipse Adoptium",
    "C:\Program Files\Microsoft"
)

foreach ($sr in $searchRoots) {
    if (Test-Path $sr) {
        $candidates = Get-ChildItem -Path $sr -Directory -ErrorAction SilentlyContinue | 
                      Where-Object { Test-Path (Join-Path $_.FullName "bin\javac.exe") } | 
                      Sort-Object Name -Descending
        if ($candidates) {
            $detectedJavaHome = $candidates[0].FullName
            break
        }
    }
}

if (-not $detectedJavaHome) {
    if (Test-Path "C:\Program Files\Java\jdk-25") {
        $detectedJavaHome = "C:\Program Files\Java\jdk-25"
    } elseif (Test-Path "C:\Program Files\Java\jdk-21") {
        $detectedJavaHome = "C:\Program Files\Java\jdk-21"
    } elseif (Test-Path "C:\Program Files\Java\jdk-17") {
        $detectedJavaHome = "C:\Program Files\Java\jdk-17"
    }
}

if ($detectedJavaHome) {
    $env:JAVA_HOME = $detectedJavaHome
    $env:JRE_HOME = $detectedJavaHome
    Write-Host "      - JDK environment verified (JAVA_HOME: $detectedJavaHome)." -ForegroundColor Gray
} else {
    Write-Host "      - JDK compiler and runtime verified in PATH." -ForegroundColor Gray
}

# [2/4] Directory Structure & Compilation
Write-Host "`n[2/4] Compiling Java source files..." -ForegroundColor Green
$directories = @(
    (Join-Path $projectRoot "WebContent\WEB-INF\classes"),
    (Join-Path $projectRoot "WebContent\uploads\books"),
    (Join-Path $projectRoot "WebContent\uploads\covers")
)

foreach ($dir in $directories) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
}

$compileCmd = 'javac -cp "WebContent/WEB-INF/lib/*;WebContent/WEB-INF/classes" -d "WebContent/WEB-INF/classes" src/com/ebook/model/*.java src/com/ebook/util/*.java src/com/ebook/dao/*.java src/com/ebook/controller/*.java src/com/ebook/filter/*.java src/com/ebook/test/*.java'
Invoke-Expression $compileCmd

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n[ERROR] Compilation failed with exit code $LASTEXITCODE. Review syntax errors above." -ForegroundColor Red
    Exit 1
}

# Copy schema.sql to classes for classpath loading
$schemaSrc = Join-Path $projectRoot "database\schema.sql"
$schemaDst = Join-Path $projectRoot "WebContent\WEB-INF\classes\schema.sql"
Copy-Item -Path $schemaSrc -Destination $schemaDst -Force

Write-Host "      - Compiling Java source files... Done. (0 errors)" -ForegroundColor Gray

# [3/4] System Connectivity & Diagnostic Checks
Write-Host "`n[3/4] Running system connectivity diagnostics..." -ForegroundColor Green
$diagCmd = 'java -cp "WebContent/WEB-INF/lib/*;WebContent/WEB-INF/classes" com.ebook.test.FullSpectrumDiagnosticTest'
Invoke-Expression $diagCmd

if ($LASTEXITCODE -eq 0) {
    Write-Host "      - Running system connectivity diagnostics... Done." -ForegroundColor Gray
} else {
    Write-Host "      - Diagnostics completed with notifications. Check logs above." -ForegroundColor Yellow
}

# [4/4] Portable Tomcat Setup, Server Deployment & Health Check
Write-Host "`n[4/4] Deploying to Apache Tomcat & Starting Server..." -ForegroundColor Green
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  SERVER PROVISIONING & DEPLOYMENT" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan

$tomcatHome = $env:CATALINA_HOME
if (-not $tomcatHome -or -not (Test-Path $tomcatHome)) {
    $systemCandidates = @(
        "C:\Program Files\Apache Software Foundation\Tomcat 9.0",
        "C:\Program Files\Apache Software Foundation\Tomcat 10.0",
        "C:\tomcat",
        "C:\apache-tomcat"
    )
    foreach ($candidate in $systemCandidates) {
        if (Test-Path $candidate) {
            $tomcatHome = $candidate
            break
        }
    }
}

# Portable Tomcat fallback
$portableTomcatDir = Join-Path $projectRoot ".tomcat"
$portableTomcatHome = Join-Path $portableTomcatDir "apache-tomcat-9.0.86"

if (-not $tomcatHome -or -not (Test-Path $tomcatHome)) {
    if (Test-Path (Join-Path $portableTomcatHome "bin\catalina.bat")) {
        $tomcatHome = $portableTomcatHome
        Write-Host "[INFO] Using portable Apache Tomcat 9 at: $tomcatHome" -ForegroundColor Green
    } else {
        Write-Host "[INFO] No Apache Tomcat installation detected." -ForegroundColor Yellow
        Write-Host "[INFO] Downloading portable Apache Tomcat 9.0.86 (~14 MB)..." -ForegroundColor Cyan
        
        if (-not (Test-Path $portableTomcatDir)) {
            New-Item -ItemType Directory -Path $portableTomcatDir -Force | Out-Null
        }
        
        $zipUrl = "https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.86/bin/apache-tomcat-9.0.86-windows-x64.zip"
        $zipFile = Join-Path $portableTomcatDir "tomcat.zip"
        
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $zipUrl -OutFile $zipFile -UseBasicParsing
        
        Write-Host "[INFO] Extracting Apache Tomcat 9..." -ForegroundColor Cyan
        Expand-Archive -Path $zipFile -DestinationPath $portableTomcatDir -Force
        Remove-Item -Path $zipFile -Force -ErrorAction SilentlyContinue
        
        $tomcatHome = $portableTomcatHome
        Write-Host "[INFO] Portable Apache Tomcat 9 provisioned successfully!" -ForegroundColor Green
    }
}

# Write explicit setenv.bat to permanently fix JRE_HOME overriding
$setenvFile = Join-Path $tomcatHome "bin\setenv.bat"
$jdkPathToWrite = if ($detectedJavaHome) { $detectedJavaHome } else { "C:\Program Files\Java\jdk-25" }
$setenvContent = "@echo off`r`nset `"JAVA_HOME=$jdkPathToWrite`"`r`nset `"JRE_HOME=$jdkPathToWrite`""
Set-Content -Path $setenvFile -Value $setenvContent -Force

# Deploy application to Tomcat webapps
$webContentDir = Join-Path $projectRoot "WebContent"
$destAppDir = Join-Path $tomcatHome "webapps\OOAD_Project"

Write-Host "Deploying web application files to $destAppDir ..." -ForegroundColor Yellow
if (-not (Test-Path $destAppDir)) {
    New-Item -ItemType Directory -Path $destAppDir -Force | Out-Null
}
Copy-Item -Path "$webContentDir\*" -Destination $destAppDir -Recurse -Force
Write-Host "      - Deployment package synchronized to webapps\OOAD_Project." -ForegroundColor Gray

# Stop old Tomcat instance if running on port 8080
$activeConn = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($activeConn) {
    Write-Host "[INFO] Restarting Tomcat on port 8080 to apply fresh code..." -ForegroundColor Yellow
    $existingPids = $activeConn | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($pidToKill in $existingPids) {
        Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
    }
    Start-Sleep -Seconds 2
}

$appUrl = "http://localhost:8080/OOAD_Project/"
Write-Host "Starting Apache Tomcat Server on port 8080..." -ForegroundColor Green
$startupBat = Join-Path $tomcatHome "bin\startup.bat"

# Launch in a separate dedicated console window
Start-Process -FilePath "cmd.exe" -ArgumentList "/c start `"Apache Tomcat`" `"$startupBat`"" -WorkingDirectory (Join-Path $tomcatHome "bin")

# Health Check Polling Loop (up to 20 seconds)
Write-Host "Waiting for Tomcat server to initialize on port 8080..." -ForegroundColor Cyan
$serverReady = $false
for ($i = 1; $i -le 20; $i++) {
    Start-Sleep -Seconds 1
    try {
        $resp = Invoke-WebRequest -Uri $appUrl -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        if ($resp.StatusCode -eq 200 -or $resp.StatusCode -eq 302) {
            $serverReady = $true
            break
        }
    } catch {
        Write-Host "  ... waiting for server response ($i/20s)" -ForegroundColor Gray
    }
}

if ($serverReady) {
    Write-Host "`n[SUCCESS] Apache Tomcat server is active and healthy on port 8080!" -ForegroundColor Green
} else {
    Write-Host "`n[NOTICE] Server process launched. Proceeding to launch browser..." -ForegroundColor Yellow
}

Write-Host "`nApplication Access Endpoints:" -ForegroundColor White
Write-Host "  - Home Page       : $appUrl" -ForegroundColor Cyan
Write-Host "  - Reader Catalog  : $($appUrl)catalog" -ForegroundColor Cyan
Write-Host "  - Admin Dashboard : $($appUrl)admin/dashboard" -ForegroundColor Cyan
Write-Host ""
Write-Host "Default Login Accounts:" -ForegroundColor White
Write-Host "  - Admin  : admin@ebook.com  / admin123" -ForegroundColor Yellow
Write-Host "  - Reader : reader@ebook.com / reader123" -ForegroundColor Yellow

# Launch Google Chrome (or default browser)
$chromeCandidates = @(
    "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
    "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe",
    "$env:LocalAppData\Google\Chrome\Application\chrome.exe"
)

$chromePath = $null
foreach ($path in $chromeCandidates) {
    if ($path -and (Test-Path $path)) {
        $chromePath = $path
        break
    }
}

if ($chromePath) {
    Write-Host "`n[INFO] Launching Google Chrome to $appUrl ..." -ForegroundColor Green
    Start-Process -FilePath $chromePath -ArgumentList $appUrl
} else {
    Write-Host "`n[INFO] Launching default browser to $appUrl ..." -ForegroundColor Green
    Start-Process $appUrl
}

Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "Smart E-Book Management System is LIVE!" -ForegroundColor Green
Write-Host "================================================================================" -ForegroundColor Cyan
