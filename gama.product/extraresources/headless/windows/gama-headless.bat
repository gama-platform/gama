echo off
cls
setLocal EnableDelayedExpansion
set inputFile=""
set outputFile=""

REM memory is defined in the ../Gama.ini file
set "memory=-1m"
SET userWorkspace=""

set workDir=.work%RANDOM%
SETLOCAL enabledelayedexpansion


:TOP

	IF (%1) == () GOTO NEXT_CODE

	if %1 EQU -m (
		set memory=%2
		SHIFT
	) else IF "%~1"=="-ws" (
        SET "userWorkspace=%~2"
        SHIFT
    ) else (
		set param=%param% %1
	)
	SHIFT

GOTO TOP

:NEXT_CODE
echo ******************************************************************
echo * GAMA version 0.0.0-SNAPSHOT                                         *
echo * http://gama-platform.org                                       *
echo * (c) 2007-2025 UMI 209 UMMISCO IRD/SU and Partners              *
echo ******************************************************************

set FILENAME="..\plugins\"
FOR /F %%e in ('dir /b %FILENAME%') do (
 	SET result=%%e
	if "!result:~0,29!" == "org.eclipse.equinox.launcher_" (
		goto END
	)
)
:END
@echo !result!

set "result=..\plugins\%result%"

echo %result%
echo %JAVA_HOME%

REM We don't want to use the options before the `-server` options in the GAMA.ini file
REM because they are not compatible with the headless mode

set "ini_arguments="
set "skip_until_line=-server"
set "skipping=true"

for /f "usebackq delims=" %%a in (..\GAMA.ini) do (
	set "line=%%a"

	if !skipping!==true (
		if !skip_until_line!==%%a (
			set "skipping=false"
			set "ini_arguments=!ini_arguments!!line! "
		)
	) else (
		if "!line:~0,4!"=="-Xmx" (
			if "!memory!"=="-1m" ( set "memory=!line:~4!" )
		) else (
			set "ini_arguments=!ini_arguments!!line! "
		)
	)
)

@echo Will run with these options:
@echo %ini_arguments%

IF "%userWorkspace%"=="" (
    set workDir=%workDir%
) ELSE (
    set workDir=%userWorkspace%
)
@echo workDir = %workDir%
@echo memory = %memory%

if exist ..\jdk\ (
	echo "JDK"
	call ..\jdk\bin\java -cp !result! -Xms512m -Xmx%memory% !ini_arguments! -Djava.awt.headless=true org.eclipse.equinox.launcher.Main -configuration ./configuration -application gama.headless.product -data "%workDir%" !param!
) else (
	echo "JAVA_HOME"
  	call "%JAVA_HOME%\bin\java.exe" -cp !result! -Xms512m -Xmx%memory% !ini_arguments! -Djava.awt.headless=true org.eclipse.equinox.launcher.Main -configuration ./configuration -application gama.headless.product -data "%workDir%" !param!
)