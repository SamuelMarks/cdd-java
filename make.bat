@echo off
setlocal EnableDelayedExpansion

if "%~1"=="" goto help
if "%~1"=="help" goto help
if "%~1"=="all" goto help
if "%~1"=="install_base" goto install_base
if "%~1"=="install_deps" goto install_deps

if "%~1"=="docs" goto docs

if "%~1"=="build_wasm" goto build_wasm
if "%~1"=="build_docker" goto build_docker
if "%~1"=="run_docker" goto run_docker

if "%~1"=="build_docs" (
    if not "%~2"=="" (
        call :build_docs "%~2"
    ) else (
        call :build_docs docs
    )
    goto :eof
)

if "%~1"=="build" (
    if not "%~2"=="" (
        call :build "%~2"
    ) else (
        call :build bin
    )
    goto :eof
)

if "%~1"=="test" goto test
if "%~1"=="run" goto run

echo Unknown target: %1
goto help

:help
echo Available targets:
echo   install_base : install language runtime (Java JDK)
echo   install_deps : install local dependencies
echo   docs         : build the API docs to target/docs and symlink docs/html
echo   build_docs   : build the API docs (specify dir as second arg, e.g. make.bat build_docs custom_dir)
echo   build        : build the CLI binary (specify dir as second arg, e.g. make.bat build custom_dir)
echo   build_wasm   : build WasmGC binary and JS wrapper via GraalVM 25 SVM-WASM
echo   build_docker : build Docker images
echo   run_docker   : run Docker images
echo   test         : run tests locally
echo   run          : run the CLI (e.g. make.bat run --version)
echo   help         : show this help text
echo   all          : show this help text
goto :eof

:install_base
echo Installing Java runtime...
echo Please ensure winget is installed or install Java manually.
winget install Microsoft.OpenJDK.17
goto :eof

:install_deps
echo Installing dependencies to lib/...
if not exist "lib" mkdir "lib"
call mvn dependency:copy-dependencies -DoutputDirectory=lib
if not exist "lib\jackson-core-2.15.2.jar" (
    curl -sL https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar -o "lib\jackson-core-2.15.2.jar"
)
if not exist "lib\jackson-annotations-2.15.2.jar" (
    curl -sL https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar -o "lib\jackson-annotations-2.15.2.jar"
)
if not exist "lib\jackson-databind-2.15.2.jar" (
    curl -sL https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar -o "lib\jackson-databind-2.15.2.jar"
)
if not exist "lib\jackson-datatype-jsr310-2.15.2.jar" (
    curl -sL https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.15.2/jackson-datatype-jsr310-2.15.2.jar -o "lib\jackson-datatype-jsr310-2.15.2.jar"
)
goto :eof

:docs
echo Building API docs to target\docs...
if exist "target\docs" rmdir /S /Q "target\docs"
mkdir "target\docs"
dir /s /B "src\main\java\*.java" | findstr /V "ApiIntegrationTest.java" > doc_sources.txt
javadoc -d "target\docs" -cp "lib/*;src/main/java" @doc_sources.txt
del doc_sources.txt
if not exist "docs" mkdir "docs"
if exist "docs\html" rmdir /S /Q "docs\html"
mklink /D "docs\html" "..\target\docs" >nul 2>&1 || xcopy "target\docs\*" "docs\html\" /S /E /I /Q >nul
goto :eof

:build_docs
set "DOCS_DIR=%~1"
echo Building API docs to %DOCS_DIR%...
if not exist "%DOCS_DIR%" mkdir "%DOCS_DIR%"
dir /s /B "src\main\java\*.java" > doc_sources.txt
javadoc -d "%DOCS_DIR%" -cp "lib/*;src/main/java" @doc_sources.txt
del doc_sources.txt
goto :eof

:build
set "BIN_DIR=%~1"
echo Building CLI to %BIN_DIR%...
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"
dir /s /B "src\main\java\*.java" > sources.txt
javac -d "%BIN_DIR%" -cp "lib/*;src/main/java" @sources.txt
del sources.txt
(
echo #!/usr/bin/env sh
echo SCRIPT_DIR="$(cd "$(dirname "$0")" ^&^& pwd)"
echo ROOT_DIR="$(cd "$SCRIPT_DIR/.." ^&^& pwd)"
echo exec java -cp "$ROOT_DIR/lib/*:$SCRIPT_DIR" cli.Main "$@"
) > "%BIN_DIR%\cdd-java"
(
echo @echo off
echo set "SCRIPT_DIR=%%~dp0"
echo java -cp "%%SCRIPT_DIR%%..\lib\*;%%SCRIPT_DIR%%" cli.Main %%*
) > "%BIN_DIR%\cdd-java.bat"
goto :eof

:test
echo Running tests...
dir /s /B "src\main\java\*.java" > sources.txt
dir /s /B "src\test\java\*.java" >> sources.txt
javac -cp "lib/*;src/main/java;src/test/java" @sources.txt
java -cp "lib/*;src/main/java;src/test/java" TestRunner
del sources.txt
goto :eof

:run
set "BIN_DIR=bin"
if not exist "%BIN_DIR%\cli\Main.class" call :build bin
shift
set "RUN_ARGS="
:run_loop
if "%~1"=="" goto run_exec
set "RUN_ARGS=%RUN_ARGS% %1"
shift
goto run_loop

:run_exec
java -cp "lib/*;%BIN_DIR%" cli.Main %RUN_ARGS%
goto :eof

:build_wasm
echo Building WASM variant...
python build_wasm.py
if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
if not exist "bin" mkdir "bin"
copy /Y "target\wasm\cdd-java.wasm" "bin\cdd-java.wasm"
copy /Y "target\wasm\cdd-java.js.wasm" "bin\cdd-java.js.wasm"
copy /Y "target\wasm\cdd-java.js" "bin\cdd-java.js"
pushd package
call npm install
call npm run build
popd
copy /Y "target\wasm\cdd-java.wasm" "package\dist\cdd-java.wasm"
copy /Y "target\wasm\cdd-java.js.wasm" "package\dist\cdd-java.js.wasm"
copy /Y "target\wasm\cdd-java.js" "package\dist\cdd-java.js"
goto :eof

:build_docker
echo Building docker images...
docker build -t cdd-java-alpine -f alpine.Dockerfile .
docker build -t cdd-java-debian -f debian.Dockerfile .
goto :eof

:run_docker
echo Testing docker images...
python test_docker.py
goto :eof
