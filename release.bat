@echo off
echo Building Release APK...
gradlew.bat assembleRelease
echo.
echo ✅ Release build complete! APK location:
echo app\build\outputs\apk\release\app-release-unsigned.apk
