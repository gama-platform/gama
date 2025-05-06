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
  if [[ ${javaVersion:2} == 23 ]]; then
    echo "You should use Java 23 to run GAMA"
    echo "Found you using version : $javaVersion"
    exit 1
  fi
fi

memory="0"

for arg do
  shift
  case $arg in
    -m)
    memory="${1}"
    shift
    ;;
    *)
    set -- "$@" "$arg"
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
if [[ -z "$@" ]]; then
    set -- "$@" "-help"
    workspaceCreate=1
else
    case "$@" in
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
echo "* (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners                *"
echo "******************************************************************"

# Create Workspace
# ======================
pathWorkspace=.workspace
workspaceRootPath="./"

# Create ws in output folder
if [ $workspaceCreate -eq 0 ]; then
  # create workspace in output folder
  workspaceRootPath="${@: -1}"
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
mkdir -p $pathWorkspace

ini_arguments=$(read_from_ini)

if ! $java -cp "${pluginPath}"/org.eclipse.equinox.launcher*.jar \
        -Xms512m \
        $memory \
        ${ini_arguments[@]} \
        org.eclipse.equinox.launcher.Main \
        -configuration "${headlessPath}"/configuration \
        -application gama.headless.product \
        -data $pathWorkspace \
        "$@"; then
    echo "Error in you command, here's the log :"
    cat $pathWorkspace/.metadata/.log
    exit 1
fi
