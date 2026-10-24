# =====================================================================
# AI Adaptive Study Planner - PowerShell Build & Deploy Script
# =====================================================================

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Building AI Adaptive Study Planner (Core Java + Servlets)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$projectDir = $PSScriptRoot
$classesDir = "$projectDir\src\main\webapp\WEB-INF\classes"
$libDir = "$projectDir\src\main\webapp\WEB-INF\lib"
$resourcesDir = "$projectDir\src\main\resources"
$webappDir = "$projectDir\src\main\webapp"
$targetWar = "$projectDir\studyplanner.war"
$tomcatWebapps = "C:\Program Files\Apache Software Foundation\Tomcat 11.0\webapps"

# 1. Clean and Ensure Classes Directory
if (!(Test-Path $classesDir)) {
    New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
}

# 2. Compile Java Source Files
Write-Host "`n[1/4] Compiling Java source files..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path "$projectDir\src\main\java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
$classpath = "$projectDir\lib\servlet-api.jar;$projectDir\lib\gson-2.10.1.jar;$projectDir\lib\mysql-connector-j.jar"

$compileOutput = javac -encoding UTF-8 -cp $classpath -d $classesDir $javaFiles 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation FAILED:" -ForegroundColor Red
    Write-Host $compileOutput
    exit 1
}
Write-Host "  -> Compilation successful! $(($javaFiles).Count) Java files compiled." -ForegroundColor Green

# 3. Copy Properties Files to Classes Directory
Write-Host "`n[2/4] Copying resources (db.properties, ai.properties)..." -ForegroundColor Yellow
Copy-Item "$resourcesDir\*.properties" -Destination $classesDir -Force
Write-Host "  -> Properties copied." -ForegroundColor Green

# 4. Package WAR file
Write-Host "`n[3/4] Packaging WAR file: $targetWar..." -ForegroundColor Yellow
if (Test-Path $targetWar) {
    Remove-Item $targetWar -Force
}
Set-Location $webappDir
jar -cvf $targetWar * | Out-Null
Set-Location $projectDir

if (Test-Path $targetWar) {
    $warSize = (Get-Item $targetWar).Length / 1MB
    Write-Host "  -> WAR created successfully! ($([math]::Round($warSize, 2)) MB)" -ForegroundColor Green
} else {
    Write-Host "WAR packaging failed." -ForegroundColor Red
    exit 1
}

# 5. Deploy to Tomcat 11 webapps
Write-Host "`n[4/4] Deploying to Apache Tomcat 11..." -ForegroundColor Yellow
if (Test-Path $tomcatWebapps) {
    $destAppDir = "$tomcatWebapps\studyplanner"
    $destWar = "$tomcatWebapps\studyplanner.war"

    # Copy WAR
    Copy-Item $targetWar -Destination $destWar -Force
    
    # Also sync unpacked directory for instant availability
    if (!(Test-Path $destAppDir)) {
        New-Item -ItemType Directory -Force -Path $destAppDir | Out-Null
    }
    Copy-Item "$webappDir\*" -Destination $destAppDir -Recurse -Force

    Write-Host "  -> Deployed to Tomcat webapps: $destAppDir" -ForegroundColor Green
    Write-Host "  -> Webapp URL: http://localhost:8080/studyplanner/" -ForegroundColor Cyan
} else {
    Write-Host "Tomcat webapps directory not found at $tomcatWebapps. You can manually deploy $targetWar" -ForegroundColor Yellow
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "  BUILD AND DEPLOYMENT COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
