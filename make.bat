@echo off
setlocal EnableDelayedExpansion

if "%~1"=="" goto help
if "%~1"=="help" goto help
if "%~1"=="all" goto help
if "%~1"=="install_base" goto install_base
if "%~1"=="install_deps" goto install_deps

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
echo   build_docs   : build the API docs (specify dir as second arg, e.g. make.bat build_docs custom_dir)
echo   build        : build the CLI binary (specify dir as second arg, e.g. make.bat build custom_dir)
echo   build_wasm   : build WASM variant (Not implemented)
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
echo Dependencies already in lib/
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
echo WASM Support is not implemented for cdd-java. See WASM.md
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
