#!/bin/bash

#
#	SCRIPT watchdog
#

# check if 2 param
oldVersion="0.0.0"
newVersion=$1

# Set path
path="$( dirname $( realpath "${BASH_SOURCE[0]}" ) )/.."

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
find $path -type f -name "*.xml" -exec sed -i "s/$oldVersion.qualifier/$newVersion/g" {} \;
find $path -type f -name "*.product" -exec sed -i "s/$oldVersion.qualifier/$newVersion/g" {} \;
find $path -type f -name "MANIFEST.MF" -exec sed -i "s/$oldVersion.qualifier/$newVersion/g" {} \;
echo "Update -SNAPSHOT"
find $path -type f -name "*.xml" -exec sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" {} \;

echo "Finish updating meta-data from .product"
find $path -type f -name "*.product" -exec sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" {} \;
find $path -type f -name "*.product" -exec sed -i "s/$oldVersion/$newVersion/g" {} \;

echo "Update sites url"
find $path -type f -name "feature.xml" -exec sed -i "s/$oldVersion Update/$newVersion Update/g" {} \;
find $path -type f -name "feature.xml" -exec sed -i "s/org\/$oldVersion/org\/$newVersion/g" {} \;
find $path -type f -name "pom.xml" -exec sed -i "s/$oldVersion<\/url>/$newVersion<\/url>/g" {} \;

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