@echo off
echo Building Debug APK...
gradlew.bat assembleDebug
echo.
echo ✅ Build complete! APK location:
echo app\build\outputs\apk\debug\app-debug.apk
