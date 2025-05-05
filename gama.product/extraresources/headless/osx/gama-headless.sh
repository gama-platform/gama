#!/bin/bash

java="java"

if [ -d "$( dirname "${BASH_SOURCE[0]}" )/../jdk" ]; then
  java="$( dirname "${BASH_SOURCE[0]}" )"/../jdk/Contents/Home/bin/java
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
  memory=$(grep Xmx "$( dirname "${BASH_SOURCE[0]}" )/../Eclipse/Gama.ini" || echo "-Xmx4096m")
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
  start_line=$(grep -n -- '-server' "$( dirname "${BASH_SOURCE[0]}" )/../Eclipse/Gama.ini" | cut -d ':' -f 1)
  tail -n +$start_line "$( dirname "${BASH_SOURCE[0]}" )/../Eclipse/Gama.ini" | tr '\n' ' '
}

echo "******************************************************************"
echo "* GAMA version 0.0.0-SNAPSHOT                                         *"
echo "* http://gama-platform.org                                       *"
echo "* (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners                *"
echo "******************************************************************"
passWork=.workspace
# w/ output folder
if [ $workspaceCreate -eq 0 ]; then
  # create output folder if not existing
  if [ ! -d "${@: -1}" ]; then
      mkdir ${@: -1}
  fi
  # create workspace in output folder
  # `expr` use is to remove whitespace from MacOS's result
  passWork=${@: -1}/.workspace$(find ${@: -1} -name ".workspace*" | expr $(wc -l))
  mkdir -p $passWork

# w/o output folder
else
  # create workspace in current folder
  passWork=.workspace$(find ./ -maxdepth 1 -name ".workspace*" | expr $(wc -l))
fi

ini_arguments=$(read_from_ini)

if ! $java -cp "$( dirname "${BASH_SOURCE[0]}" )"/../Eclipse/plugins/org.eclipse.equinox.launcher*.jar -Xms512m $memory ${ini_arguments[@]} -Djava.awt.headless=true org.eclipse.equinox.launcher.Main -configuration "$( dirname "${BASH_SOURCE[0]}" )"/configuration -application gama.headless.product -data $passWork "$@"; then
    echo "Error in you command, here's the log :"
    cat $passWork/.metadata/.log
    exit 1
fi
