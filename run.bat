@echo off
echo ==========================================================
echo   Starting AI Adaptive Study Planner
echo ==========================================================

set TOMCAT_HOME=C:\Program Files\Apache Software Foundation\Tomcat 11.0

echo [1/3] Building and packaging application...
call build.bat

echo [2/3] Checking server on port 8080...
netstat -ano | findstr ":8080" | findstr "LISTENING" > nul
if %ERRORLEVEL% equ 0 (
    echo Tomcat is already running on port 8080!
) else (
    echo Starting Tomcat 11 in background...
    set "CATALINA_HOME=%TOMCAT_HOME%"
    set "CATALINA_BASE=%TOMCAT_HOME%"
    start "" "%TOMCAT_HOME%\bin\catalina.bat" start
    timeout /t 4 /nobreak > nul
)

echo [3/3] Opening Google Chrome...
if exist "C:\Program Files\Google\Chrome\Application\chrome.exe" (
    start "" "C:\Program Files\Google\Chrome\Application\chrome.exe" "http://localhost:8080/studyplanner/"
) else (
    start http://localhost:8080/studyplanner/
)

echo.
echo ==========================================================
echo   Application is ready!
echo   URL: http://localhost:8080/studyplanner/
echo ==========================================================
