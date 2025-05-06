@echo off
SETLOCAL EnableDelayedExpansion

:: Determine script directory
SET "headlessPath=%~dp0"

:: Set paths for Windows
SET "gamaIniPath=%headlessPath%..\Gama.ini"
SET "pluginPath=%headlessPath%..\plugins"

:: Java detection
SET "java=java"
IF EXIST "%headlessPath%..\jdk" (
    SET "java=%headlessPath%..\jdk\bin\java.exe"
) ELSE (
    FOR /f "tokens=2 delims==" %%a IN ('java -version 2^>^&1 ^| findstr "version"') DO SET "javaVersion=%%a"
    SET "javaVersion=!javaVersion:"=!"
    IF "!javaVersion:~2,2!" == "23" (
        echo You should use Java 23 to run GAMA
        echo Found you using version : !javaVersion!
        exit /b 1
    )
)

:: Argument parsing
SET memory=0
SET userWorkspace=
SET args=

:ParseArgs
IF "%~1"=="" GOTO ArgsDone
    IF "%~1"=="-m" (
        SET "memory=%~2"
        SHIFT
        SHIFT
        GOTO ParseArgs
    )
    IF "%~1"=="-ws" (
        SET "userWorkspace=%~2"
        SHIFT
        SHIFT
        GOTO ParseArgs
    )
    SET "args=!args! %1"
    SHIFT
    GOTO ParseArgs
:ArgsDone

:: Memory configuration
SET "memorySetting="
IF "%memory%"=="0" (
    FOR /f "usebackq delims=" %%a IN (`findstr /c:"-Xmx" "%gamaIniPath%"`) DO SET "memorySetting=%%a"
    IF "!memorySetting!"=="" SET "memorySetting=-Xmx4096m"
) ELSE (
    SET "memorySetting=-Xmx%memory%"
)

:: Determine workspace behavior
SET "workspaceCreate=1"
IF NOT "x%args%"=="x" (
    echo.%args% | findstr /c:"-help" /c:"-version" /c:"-validate" /c:"-test" /c:"-xml" /c:"-batch" /c:"-write-xmi" /c:"-socket" >nul && SET "workspaceCreate=1"
)

:: Function to read ini arguments
SET "ini_arguments="
FOR /f "tokens=*" %%a IN ('findstr /n "-server" "%gamaIniPath%"') DO SET "start_line=%%a"
SET "start_line=!start_line:~0,1!"
SET "ini_args="
FOR /f "skip=%start_line% tokens=*" %%a IN (%gamaIniPath%) DO SET "ini_args=!ini_args! %%a"

:: Workspace setup
SET "pathWorkspace=.workspace"

IF "%userWorkspace%"=="" (
    SET "workspaceRootPath=."
    IF %workspaceCreate%==0 (
        SET "workspaceRootPath=%args:~-1%"
        IF NOT EXIST "%workspaceRootPath%" MD "%workspaceRootPath%"
    )

    SET /A count=0
    FOR /f "tokens=*" %%a IN ('dir /b /ad "%workspaceRootPath%\.workspace*" 2^>nul') DO SET /A count+=1
    SET /A count+=1
    SET "pathWorkspace=%workspaceRootPath%\.workspace!count!"
) ELSE (
    SET "pathWorkspace=%userWorkspace%"
)

MD "%pathWorkspace%"

:: Java command execution
"%java%" -cp "%pluginPath%\org.eclipse.equinox.launcher*.jar;!ini_arguments!" ^
    -Xms512m ^
    %memorySetting% ^
    org.eclipse.equinox.launcher.Main ^
    -configuration "%headlessPath%configuration" ^
    -application gama.headless.product ^
    -data "%pathWorkspace%" ^
    %args%

IF ERRORLEVEL 1 (
    echo Error in your command, here's the log:
    TYPE "%pathWorkspace%\.metadata\.log"
    exit /b 1
) ELSE (
    IF %workspaceCreate%==1 (
        RD /s /q "%pathWorkspace%"
    )
)
