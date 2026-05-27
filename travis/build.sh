#!/bin/bash
set -e

echo "Compiling gama.annotations"
cd $( dirname $( realpath "${BASH_SOURCE[0]}" ) )/../gama.annotations
mvn clean install -Dmaven.build.cache.configPath=maven-build-cache-config.xml "$@"

echo "Compiling gama.processor"
cd ../gama.processor 
mvn clean install -Dmaven.build.cache.configPath=maven-build-cache-config.xml "$@"

echo "Compiling gama.parent"
cd ../gama.parent 
mvn clean install -Dmaven.build.cache.configPath=maven-build-cache-config.xml "$@"
