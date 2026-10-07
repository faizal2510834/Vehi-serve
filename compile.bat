@echo off
echo Compiling project...
if not exist bin mkdir bin
dir /s /B src\*.java > sources.txt
javac -d bin -cp "lib\ojdbc11.jar;src" @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo Compilation Failed!
) else (
    echo Compilation Successful!
)
del sources.txt
pause
