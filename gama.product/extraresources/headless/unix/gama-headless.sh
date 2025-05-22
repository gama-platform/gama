#!/bin/bash

if [[ "$OSTYPE" == "darwin"* ]]; then
    headlessPath=$( dirname "${BASH_SOURCE[0]}" )
    gamaIniPath="${headlessPath}/../Eclipse/Gama.ini"
    pluginPath="${headlessPath}/../Eclipse/plugins"
else
    # Assuming Linux
    headlessPath=$( dirname $( realpath "${BASH_SOURCE[0]}" ) )
    gamaIniPath="${headlessPath}/../Gama.ini"
    pluginPath="${headlessPath}/../plugins"
fi

java="java"

if [ -d "${headlessPath}/../jdk" ]; then
  java="${headlessPath}"/../jdk/
    [[ "$OSTYPE" == "darwin"* ]] && java+="Contents/Home/" # DMG path
    java+="bin/java"
else
  javaVersion=$(java -version 2>&1 | head -n 1 | cut -d "\"" -f 2)
  # Check if good Java version before everything
  if [[ ${javaVersion:2} == 21 ]]; then
    echo "You should use Java 21 to run GAMA"
    echo "Found you using version : $javaVersion"
    exit 1
  fi
fi

memory="0"
userWorkspace=""
args=""

while [[ "$#" -gt 0 ]]; do
    case "$1" in
        -m)
            memory="$2"
            shift 2
            ;;
        -ws)
            userWorkspace="$2"
            shift 2
            ;;
        *)
            args+="$1 "
            shift
            ;;
    esac
done

if [[ $memory == "0" ]]; then
  memory=$(grep Xmx "${gamaIniPath}" || echo "-Xmx4096m")
else
  memory=-Xmx$memory
fi

workspaceCreate=0

# Run `-help` if no parameter given
if [[ -z "$args" ]]; then
    args+="-help"
    workspaceCreate=1
else
    case "$args" in
    *-help*|*-version*|*-validate*|*-test*|*-xml*|*-batch*|*-write-xmi*|*-socket*)
        workspaceCreate=1
        ;;
    esac
fi

function read_from_ini {
  start_line=$(grep -n -- '-server' "${gamaIniPath}" | cut -d ':' -f 1)
  tail -n +$start_line "${gamaIniPath}" | tr '\n' ' '
}

echo "******************************************************************"
echo "* GAMA version 0.0.0-SNAPSHOT                                         *"
echo "* http://gama-platform.org                                       *"
echo "* (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners                *"
echo "******************************************************************"

# Create Workspace
# ======================
pathWorkspace=.workspace

if [[ -z $userWorkspace ]]; then
    # No user workspace specified
    workspaceRootPath="./"

    if [ $workspaceCreate -eq 0 ]; then
      # create workspace in output folder
      workspaceRootPath="${args: -1}"
      if [ ! -d "$workspaceRootPath" ]; then
          mkdir $workspaceRootPath
      fi
    fi

    # Set new ws folder for new run and create it
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # `expr` use is to remove whitespace from MacOS's result
        pathWorkspace="${workspaceRootPath}/.workspace$(find ${workspaceRootPath} -name ".workspace*" | expr $(wc -l))"
    else
        pathWorkspace="${workspaceRootPath}/.workspace$(find ${workspaceRootPath} -maxdepth 1 -name ".workspace*" | wc -l)"
    fi

else
    # User gave a userspace
    pathWorkspace="$userWorkspace"
    # Prevent cleaning workspace after running
    workspaceCreate=0
fi

mkdir -p "$pathWorkspace"

ini_arguments=$(read_from_ini)

if ! $java -cp "${pluginPath}"/org.eclipse.equinox.launcher*.jar \
        -Xms512m \
        $memory \
        ${ini_arguments[@]} \
        org.eclipse.equinox.launcher.Main \
        -configuration "${headlessPath}"/configuration \
        -application gama.headless.product \
        -data "$pathWorkspace" \
        $args; then
    if [ $workspaceCreate -eq 1 ]; then
        # create workspace in output folder
        echo "GAMA encountered an error and crashed, please check again your command..."
        rm -fr workspaceRootPath $pathWorkspace
    else
        echo "Error in you command, here's the log :"
        cat $pathWorkspace/.metadata/.log
    fi
    exit 1
else
    if [ $workspaceCreate -eq 1 ]; then
        # create workspace in output folder
        rm -fr workspaceRootPath $pathWorkspace
    fi
fi
