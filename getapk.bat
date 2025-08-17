@echo off
echo Copying APK files for phone installation...
echo.

if not exist "APKs" mkdir APKs

copy "app\build\outputs\apk\debug\app-debug.apk" "APKs\ESP32Controller-Debug.apk" >nul 2>&1
copy "app\build\outputs\apk\release\app-release.apk" "APKs\ESP32Controller-Release.apk" >nul 2>&1

echo ✅ APK files ready for installation:
echo.
echo   📱 APKs\ESP32Controller-Debug.apk   (for testing)
echo   📱 APKs\ESP32Controller-Release.apk (optimized)
echo.
echo 📋 Installation Instructions:
echo   1. Copy APK file to your Android phone
echo   2. Enable "Install from Unknown Sources" in Settings
echo   3. Tap the APK file to install
echo   4. Allow permissions when prompted
echo.
pause
