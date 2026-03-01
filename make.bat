@echo off
setlocal

if "%~1"=="" goto help
if "%~1"=="help" goto help
if "%~1"=="all" goto help
if "%~1"=="install_base" goto install_base
if "%~1"=="install_deps" goto install_deps
if "%~1"=="build_docs" goto build_docs
if "%~1"=="test" goto test
if "%~1"=="run" goto run

echo Unknown target: %1
goto help

:help
echo Available targets:
echo   install_base : install language runtime (Java JDK)
echo   install_deps : install local dependencies
echo   build_docs   : build the API docs (specify dir as second arg, e.g. make build_docs custom_dir)
echo   test         : run tests locally
echo   run          : run server (if applicable)
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
set DOCS_DIR=docs
if not "%~2"=="" set DOCS_DIR=%~2
echo Building API docs to %DOCS_DIR%...
if not exist "%DOCS_DIR%" mkdir "%DOCS_DIR%"
dir /s /B "src\main\java\*.java" > doc_sources.txt
javadoc -d "%DOCS_DIR%" -cp "lib/*;src/main/java" @doc_sources.txt
del doc_sources.txt
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
echo Nothing to run; this is a pure client SDK.
exit /b 0
