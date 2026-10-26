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

# Build identity in each headless product (build_info.txt: printed by -version and at the start of every run).
# From git, or from travis/build_commit.txt when built from a git archive (no .git; filled by export-subst).
ROOT=$( realpath "$( dirname $( realpath "${BASH_SOURCE[0]}" ) )/.." )
if git -C "$ROOT" rev-parse HEAD > /dev/null 2>&1; then
	COMMIT=$(git -C "$ROOT" rev-parse HEAD)
	BRANCH=$(git -C "$ROOT" rev-parse --abbrev-ref HEAD)
	DESCRIBE=$(git -C "$ROOT" describe --tags --always)
	# gama.product/gama.product is edited by this script (copilot feature): not a source change
	MODIFIED=$(git -C "$ROOT" status --porcelain --untracked-files=no | grep -cv ' gama.product/gama.product$' || true)
	SOURCE="git checkout"
else
	COMMIT=$(sed -n 's/^commit: //p' "$ROOT/travis/build_commit.txt")
	# refs as "HEAD -> <branch>, origin/<branch>, ...": the checked-out branch, else the first ref
	REFS=$(sed -n 's/^refs: //p' "$ROOT/travis/build_commit.txt")
	BRANCH=$(echo "$REFS" | sed -n 's/.*HEAD -> \([^,]*\).*/\1/p')
	BRANCH=${BRANCH:-${REFS%%,*}}
	DESCRIBE=$(sed -n 's/^describe: //p' "$ROOT/travis/build_commit.txt")
	# %(describe) needs git >= 2.35 on the archiving side: left as is by older ones
	case "$DESCRIBE" in *'%('*) DESCRIBE="" ;; esac
	MODIFIED=0
	SOURCE="git archive"
fi
COMMIT=${COMMIT:-unknown}
BRANCH=${BRANCH:-unknown}
DESCRIBE=${DESCRIBE:-unknown}
STATE=$([ "$MODIFIED" = 0 ] && echo clean || echo "$MODIFIED modified file(s)")
BUILT=$(date -Iseconds)
for PRODUCT in "$ROOT"/gama.product/target/products/gama.headless.product/*/*/*/; do
	[ -d "$PRODUCT/plugins" ] || continue
	printf 'build: %s@%s %s, built %s\nbranch: %s\ncommit: %s\ndescribe: %s\nmodified: %s\nsource: %s\nbuilt: %s\n' \
		"$BRANCH" "${COMMIT:0:9}" "$STATE" "$BUILT" "$BRANCH" "$COMMIT" "$DESCRIBE" "$MODIFIED" "$SOURCE" "$BUILT" \
		> "$PRODUCT/build_info.txt"
	echo "Build identity: $(sed -n 's/^build: //p' "$PRODUCT/build_info.txt") -> $PRODUCT"
done
