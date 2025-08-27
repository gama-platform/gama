#!/bin/bash

echo "[I] Checking to add forgotten features to category =================="

for file in $( dirname $( realpath "${BASH_SOURCE[0]}" ) )/../*"feature"*; do
	file=$(basename $file)
    isInFile=$( cat $( dirname $( realpath "${BASH_SOURCE[0]}" ) )/../gama.p2site/category.xml | grep -c ${file})

    if [[ -f "$file/pom.xml" && $isInFile -eq 0  ]]; then

		echo "[I] Adding forgotten feature $file xxxxxx"

		# Get feature's version
        version=$(sed '/<parent>/,/<\/parent>/d;/<version>/!d;s/ *<\/\?version> *//g' "$file/pom.xml")
        if [ -z $version ]; then
            version=$(sed '/<version>/!d;s/ *<\/\?version> *//g' "$file/pom.xml" | sed 's/^[[:space:]]*//')
        fi

        # Tweak package version to feature pattern
        q=$".qualifier"
        version=${version/-SNAPSHOT/$q}

        # Prepare category's xml entry 
        temp="<feature  url=\"features/"$file"_$version.jar\" id=\"$file\" version=\"$version\"> <category name=\"gama.optional\"/> </feature>"
        temp=$(echo $temp|tr -d '\r' |tr -d '\n')

        # Add in category
        sed -i "\$i\\$temp" $( dirname $( realpath "${BASH_SOURCE[0]}" ) )/../gama.p2site/category.xml 
    else
        if [[ ! -f "$file/pom.xml" ]]; then
        	echo "[W] Skipping $file xxxxxx"
            echo "[W]  No pom.xml properly set"
        fi
    fi;
done

echo "[I] Publishing p2 site public OVH server ============================"
cd $( dirname $( realpath "${BASH_SOURCE[0]}" ) )/../gama.p2site
mvn clean install --settings ../travis/settings.xml -DskipTests=true "$@"