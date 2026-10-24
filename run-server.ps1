# =====================================================================
# AI Adaptive Study Planner - Server Management Script
# =====================================================================
param (
    [Parameter(Position=0)]
    [ValidateSet("start", "stop", "restart", "status", "run")]
    [string]$Action = "start"
)

$tomcatDir = "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
$catalinaBat = "$tomcatDir\bin\catalina.bat"

if (!(Test-Path $catalinaBat)) {
    Write-Host "Error: Catalina.bat not found at $catalinaBat" -ForegroundColor Red
    exit 1
}

$env:CATALINA_HOME = $tomcatDir
$env:CATALINA_BASE = $tomcatDir

switch ($Action) {
    "start" {
        Write-Host "Starting Tomcat 11 in background..." -ForegroundColor Cyan
        & "$catalinaBat" start
        Start-Sleep -Seconds 3
        Write-Host "Tomcat started! Access the app at: http://localhost:8080/studyplanner/" -ForegroundColor Green
    }
    "run" {
        Write-Host "Starting Tomcat 11 in foreground (press Ctrl+C to stop)..." -ForegroundColor Cyan
        & "$catalinaBat" run
    }
    "stop" {
        Write-Host "Stopping Tomcat 11..." -ForegroundColor Yellow
        & "$catalinaBat" stop
        Write-Host "Tomcat stopped." -ForegroundColor Green
    }
    "restart" {
        Write-Host "Restarting Tomcat 11..." -ForegroundColor Yellow
        & "$catalinaBat" stop
        Start-Sleep -Seconds 2
        & "$catalinaBat" start
        Start-Sleep -Seconds 3
        Write-Host "Tomcat restarted: http://localhost:8080/studyplanner/" -ForegroundColor Green
    }
    "status" {
        $conn = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
        if ($conn) {
            Write-Host "Tomcat is RUNNING on port 8080! PID: $($conn[0].OwningProcess)" -ForegroundColor Green
            Write-Host "Webapp URL: http://localhost:8080/studyplanner/" -ForegroundColor Cyan
        } else {
            Write-Host "Tomcat is STOPPED (port 8080 is idle)." -ForegroundColor Yellow
        }
    }
}
