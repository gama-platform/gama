# this variable has to be set -> MAVEN_CACHE_PATH=~/.m2/repository (default)

set -e

# 1. hash every pom.xml, build.properties, and MANIFEST.MF
# 2. hash the resulting file "hash - filename"
# 3. compare the resulting hash with what's stored in the cache
# 4. if it doesn't match, remove the cache because it might lead 
# to build inconsistencies throughout builds and machines
# 5. update the hash stored in the cache with the new one

current_hash=$(find . -type f \
     \( -name pom.xml -o -name MANIFEST.MF -o -name build.properties \) \
     -not -path "*/target/*" \
     -exec sha256sum {} \; | sha256sum | sed 's/\s.*//')

stored_hash=$(cat ${MAVEN_CACHE_PATH}/cachehash.sha256sum || echo "no cache found") 

echo -e "computed hash \t: $current_hash"
echo -e "stored hash \t: $stored_hash"

if ! [ $current_hash == $stored_hash ]; then
    echo "cache miss, cleaning the current cache"
    rm -Rf ${MAVEN_CACHE_PATH}/*
    else
        echo cache hit
fi

echo "$current_hash" > ${MAVEN_CACHE_PATH}/cachehash.sha256sum
