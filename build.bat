@echo off
echo ==========================================================
echo   Building AI Adaptive Study Planner (Core Java Servlets)
echo ==========================================================

set PROJECT_DIR=%~dp0
set CLASSES_DIR=%PROJECT_DIR%src\main\webapp\WEB-INF\classes
set RESOURCES_DIR=%PROJECT_DIR%src\main\resources
set WEBAPP_DIR=%PROJECT_DIR%src\main\webapp
set TARGET_WAR=%PROJECT_DIR%studyplanner.war
set TOMCAT_WEBAPPS=C:\Program Files\Apache Software Foundation\Tomcat 11.0\webapps

if not exist "%CLASSES_DIR%" mkdir "%CLASSES_DIR%"

echo [1/4] Compiling Java classes...
dir /s /b "%PROJECT_DIR%src\main\java\*.java" > "%PROJECT_DIR%sources.txt"
javac -encoding UTF-8 -cp "%PROJECT_DIR%lib\servlet-api.jar;%PROJECT_DIR%lib\gson-2.10.1.jar;%PROJECT_DIR%lib\mysql-connector-j.jar" -d "%CLASSES_DIR%" @"%PROJECT_DIR%sources.txt"
if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    del "%PROJECT_DIR%sources.txt"
    exit /b %ERRORLEVEL%
)
del "%PROJECT_DIR%sources.txt"
echo Compilation successful!

echo [2/4] Copying resources...
copy /y "%RESOURCES_DIR%\*.properties" "%CLASSES_DIR%\" > nul

echo [3/4] Packaging WAR file...
cd /d "%WEBAPP_DIR%"
jar -cvf "%TARGET_WAR%" * > nul
cd /d "%PROJECT_DIR%"
echo WAR packaged: %TARGET_WAR%

echo [4/4] Deploying to Tomcat...
if exist "%TOMCAT_WEBAPPS%" (
    copy /y "%TARGET_WAR%" "%TOMCAT_WEBAPPS%\studyplanner.war" > nul
    xcopy /e /i /y "%WEBAPP_DIR%" "%TOMCAT_WEBAPPS%\studyplanner" > nul
    echo Deployed to %TOMCAT_WEBAPPS%\studyplanner
    echo Web Application URL: http://localhost:8080/studyplanner/
) else (
    echo Tomcat webapps not found.
)

echo ==========================================================
echo   Build finished successfully!
echo ==========================================================
