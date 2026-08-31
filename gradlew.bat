@echo off
setlocal
set ROOT_DIR=%~dp0
set GRADLE_VERSION=9.3.1
set DIST_DIR=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin\musicme
set GRADLE_HOME=%DIST_DIR%\gradle-%GRADLE_VERSION%
if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo Gradle distribution is missing. Please run the project from Android Studio or install Gradle %GRADLE_VERSION%.
  exit /b 1
)
call "%GRADLE_HOME%\bin\gradle.bat" -p "%ROOT_DIR%" %*
