@echo off
echo Running full build and install...
gradlew.bat clean assembleDebug installDebug
echo ✅ Build and install complete!
