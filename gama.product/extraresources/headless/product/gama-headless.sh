#!/bin/bash
# GAMA headless from this product (gama.headless.product, Linux), with the Java options GAMA needs.
#
# Usage: gama-headless.sh [-m <max heap, e.g. 8g>] [-ws <workspace>] [-c <configuration dir>] <headless arguments>
#   -ws  workspace (default: a temporary one, removed at exit)
#   -c   OSGi configuration (default: <product>/headless/configuration, shared by every run, created at the first
#        start); a run of its own: an empty folder, the product's configuration/ is read as its read-only base
#   GAMA_JAVA_OPTS: extra Java options (e.g. -Djava.library.path=/usr/local/lib for the MPI plugin)
# Examples: gama-headless.sh -version
#           gama-headless.sh -compile model.gaml
#           gama-headless.sh -m 8g experiment.xml output_folder

product=$( dirname "$( realpath "${BASH_SOURCE[0]}" )" )
memory=4g
workspace=""
configuration="$product/headless/configuration"
while [[ "$#" -gt 0 ]]; do
	case "$1" in
		-m) memory="$2"; shift 2 ;;
		-ws) workspace="$2"; shift 2 ;;
		-c) configuration="$2"; shift 2 ;;
		*) break ;;
	esac
done
[[ "$#" -gt 0 ]] || set -- -help

java_major=$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -1)
if [[ -z "$java_major" || "$java_major" -lt 25 ]]; then
	echo "gama-headless.sh: Java 25 or later needed (found: ${java_major:-none})" >&2
	exit 1
fi

temporary=""
if [[ -z "$workspace" ]]; then
	temporary=$(mktemp -d)
	workspace="$temporary"
	trap 'rm -rf "$temporary"' EXIT
fi

java -cp "$(ls "$product"/plugins/org.eclipse.equinox.launcher_*.jar | head -1)" \
	-Xms512m -Xmx"$memory" \
	-Djava.awt.headless=true \
	--enable-native-access=ALL-UNNAMED \
	--add-modules=jdk.incubator.vector \
	--enable-preview \
	--sun-misc-unsafe-memory-access=allow \
	--add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
	$GAMA_JAVA_OPTS \
	org.eclipse.equinox.launcher.Main \
	-configuration "$configuration" \
	-application gama.headless.product \
	-data "$workspace" \
	"$@"
