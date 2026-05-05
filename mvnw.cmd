@REM Maven Wrapper startup script for Windows
@echo off
setlocal

set MVNW_BASEDIR=%~dp0
set WRAPPER_JAR="%MVNW_BASEDIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

if exist %WRAPPER_JAR% (
    goto ok
)

echo Downloading Maven Wrapper...
if not exist "%MVNW_BASEDIR%.mvn\wrapper" mkdir "%MVNW_BASEDIR%.mvn\wrapper"
powershell -Command "(New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"

:ok
if exist %WRAPPER_JAR% (
    java -jar %WRAPPER_JAR% %*
) else (
    echo ERROR: Maven Wrapper jar not found
    exit /b 1
)
