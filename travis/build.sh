#!/bin/bash
set -e

# Remove Copilot plugin from the built
# Tycho doesn't honor optional tag : https://www.eclipse.org/lists/tycho-user/msg03542.html
# Related #836
sed -i '/gama\.feature\.copilot/d' $( dirname $( realpath "${BASH_SOURCE[0]}" ) )/../gama.product/gama.product

echo "Compiling gama.annotations"
cd $( dirname $( realpath "${BASH_SOURCE[0]}" ) )/../gama.annotations
# Disable build cache here: gama.annotations must always be fully installed so that
# Tycho can discover it as an OSGi bundle when resolving gama.processor's dependencies.
# If a cache hit skips the install phase, ~/.m2/repository/.meta/p2-artifacts.properties
# is never updated and gama.processor fails with "osgi.bundle; gama.annotations not found".
mvn clean install -Dmaven.build.cache.enabled=false "$@"

echo "Compiling gama.processor"
cd ../gama.processor
# Same reason as gama.annotations: must always be fully installed so gama.parent
# can discover it as an annotation processor via Tycho's local P2 index.
mvn clean install -Dmaven.build.cache.enabled=false "$@"

echo "Compiling gama.parent"
cd ../gama.parent 
mvn clean install -Dmaven.build.cache.configPath=maven-build-cache-config.xml "$@"
