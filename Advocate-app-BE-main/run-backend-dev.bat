@echo off
title Advocate Backend (Development)
cd /d "%~dp0"

echo ========================================
echo  Starting Advocate Backend (DEV)
echo ========================================
echo.

:: Check Java installation
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo        Please install JDK 21 or later.
    pause
    exit /b 1
)

:: Print Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do set JAVA_VER=%%g
echo [OK] Java version: %JAVA_VER%

:: Check Maven Wrapper exists
if not exist "%~dp0mvnw.cmd" (
    echo [ERROR] mvnw.cmd not found.
    echo        Expected at: %~dp0mvnw.cmd
    pause
    exit /b 1
)
echo [OK] Maven Wrapper found
echo.

echo Starting Spring Boot with profile: dev
echo Backend URL: http://localhost:8080
echo.

call mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Backend failed to start. Check the logs above.
    pause
    exit /b 1
)

echo.
echo Backend stopped.
pause
