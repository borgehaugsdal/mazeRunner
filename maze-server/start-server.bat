@echo off
setlocal
cd /d %~dp0

if exist gradlew.bat (
  echo Running gradlew.bat bootRun...
  call gradlew.bat bootRun
  exit /b %errorlevel%
)

set "JAR="
for /f "delims=" %%F in ('dir /b /s build\libs\*.jar 2^>nul') do (
  set "JAR=%%F"
  goto :runJar
)
echo No gradlew.bat or jar found. Run: gradlew.bat build
exit /b 1

:runJar
echo Running %JAR%...
java -jar "%JAR%"
exit /b %errorlevel%
