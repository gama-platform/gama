CACHE_DIR=~/.m2/build-cache/v1.1/org.gama

# exit on error
set -e

if [ -d "$CACHE_DIR" ]; then
    rm -Rf $CACHE_DIR && echo Gama\'s cache has been cleaned
else
    echo Error cleaning the cache, the folder $CACHE_DIR does not exist
    exit 1
fi