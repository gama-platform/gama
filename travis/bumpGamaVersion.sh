#!/bin/bash

# check if 2 param
oldVersion="0.0.0"
inputVersion=$1

# Flip workflow bool parameter "isRelease" to "isSnapshot"
isSnapshot=true
if [ "$2" = true ]; then
    flipped_value=false
fi

month=$(echo $inputVersion | awk -F'.' '{print $2}' | awk '{print int($1)}')
id=$(echo $inputVersion | awk -F'.' '{print $3}' | awk '{print int($1)}')
versionToTag=$(echo $inputVersion | awk -F'.' -v month="$month" -v id="$id" '{$2=month; $3=id; print}' | sed 's/\ /\./g') # Remove leading zero(s) after dot character, enforcing OSGi version format

# Set path
path="$( dirname $( realpath "${BASH_SOURCE[0]}" ) )/.."

echo "[SNAPSHOT: $isSnapshot] Tagging GAMA packages release as $versionToTag"

#
#	Should I clean maven ?
#

cd $path/gama.annotations && mvn clean -B
cd $path/gama.processor && mvn clean -B
cd $path/gama.parent && mvn clean -B

#
#	UPDATING MAVEN
#

echo "Update .qualifier"
newVersion=$versionToTag
if [ $isSnapshot ]; then
	newVersion="$versionToTag.qualifier"
fi
find $path -type f -name "*.xml" -exec sed -i "s/$oldVersion.qualifier/$newVersion/g" {} \;
find $path -type f -name "*.product" -exec sed -i "s/$oldVersion.qualifier/$newVersion/g" {} \;
find $path -type f -name "*.product" -exec sed -i "s/$oldVersion.qualifier/$versionToTag/g" {} \;
find $path -type f -name "MANIFEST.MF" -exec sed -i "s/$oldVersion.qualifier/$newVersion/g" {} \;

echo "Update -SNAPSHOT"
newVersion=$versionToTag
if [ $isSnapshot ]; then
	newVersion="$versionToTag-SNAPSHOT"
fi
find $path -type f -name "*.xml" -exec sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" {} \;

echo "Finish updating meta-data from .product"
find $path -type f -name "*.product" -exec sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" {} \;

echo "Update sites url"
find $path -type f -name "feature.xml" -exec sed -i "s/$oldVersion Update/$newVersion Update/g" {} \;

if [ ! $isSnapshot ]; then
	find $path -type f -name "feature.xml" -exec sed -i "s/org\/$oldVersion/org\/$versionToTag/g" {} \;
	find $path -type f -name "pom.xml" -exec sed -i "s/$oldVersion<\/url>/$versionToTag<\/url>/g" {} \;
fi

#
#	UPDATING JAVA HEADERS
#
echo "Update JAVA header copyright"
find $path -type f -name "*.java" -exec sed -i "s/(c) 2007-$(( $(date "+%Y") - 1 )) UMI 209/(c) 2007-$( date "+%Y" ) UMI 209/g" {} \;

echo "Update everything in gama.product/extraresources"
find $path/gama.product/extraresources -not -wholename "*/samples/*" -type f -exec sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" {} \;


#
#	EXTRA Forgotten
#

echo "Update extra individual files"

sed -i "s/V$oldVersion-SNAPSHOT http/V$newVersion http/g" $path/gama.ui.application/plugin.xml

sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" $path/gama.core/src/gama/core/runtime/GAMA.java
sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" $path/gama.annotations/src/gama/annotations/precompiler/doc/utils/Constants.java

sed -i "s/$oldVersion/$newVersion/g" $path/gama.product/extraresources/Info.plist

#
#	Meta-Data generator
#

echo "Set GAMA meta-data in config.ini"
sed -i "s/gama.version\" value=\"SNAPSHOT/gama.version\" value=\"$newVersion/g" $path/gama.product/gama.product
sed -i "s/gama.commit\" value=\"SNAPSHOT/gama.commit\" value=\"$(git rev-parse HEAD)/g" $path/gama.product/gama.product
sed -i "s/gama.branch\" value=\"SNAPSHOT/gama.branch\" value=\"$(git rev-parse --abbrev-ref HEAD)/g" $path/gama.product/gama.product
sed -i "s/gama.date\" value=\"SNAPSHOT/gama.date\" value=\"$(date)/g" $path/gama.product/gama.product
sed -i "s/gama.jdk\" value=\"SNAPSHOT/gama.jdk\" value=\"$JDK_EMBEDDED_VERSION/g" $path/gama.product/gama.product