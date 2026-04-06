#!/bin/bash

# check if 2 param
oldVersion="0.0.0"
inputVersion=$1

# Flip workflow bool parameter "isRelease" to "isSnapshot"
isSnapshot=true
if [ "$2" = true ]; then
    isSnapshot=false
fi

month=$(echo $inputVersion | awk -F'.' '{print $2}' | awk '{print int($1)}')
id=$(echo $inputVersion | awk -F'.' '{print $3}' | awk '{print int($1)}')
versionToTag=$(echo $inputVersion | awk -F'.' -v month="$month" -v id="$id" '{$2=month; $3=id; print}' | sed 's/\ /\./g') # Remove leading zero(s) after dot character, enforcing OSGi version format

# Set path
path="$( dirname $( realpath "${BASH_SOURCE[0]}" ) )/.."

# Read Tycho version from the annotations POM (single source of truth)
TYCHO_VERSION=$(grep -m1 '<tycho.version>' "$path/gama.annotations/pom.xml" | sed 's/.*<tycho.version>\(.*\)<\/tycho.version>.*/\1/' | tr -d '[:space:]')
TYCHO_VERSIONS_MOJO="org.eclipse.tycho:tycho-versions-plugin:${TYCHO_VERSION}:set-version"

echo "[SNAPSHOT: $isSnapshot] Tagging GAMA packages release as $versionToTag"

#
#	Build Maven/OSGi version strings
#
newVersion=$versionToTag
if [ $isSnapshot = "true" ]; then
	newVersion="$versionToTag-SNAPSHOT"
fi

cd $path/gama.annotations && mvn clean -B
cd $path/gama.processor && mvn clean -B
cd $path/gama.parent && mvn clean -B

#
#	UPDATING MAVEN & OSGi VERSIONS via Tycho
#
#	tycho-versions:set-version updates pom.xml (<version>), META-INF/MANIFEST.MF
#	(Bundle-Version:), feature.xml (version=) and *.product (version=) in one pass.
#	It handles both Maven (-SNAPSHOT) and OSGi (.qualifier) notations automatically.
#

echo "Update versions with Tycho (annotations)"
cd $path/gama.annotations && mvn -B ${TYCHO_VERSIONS_MOJO} -DnewVersion="$newVersion"

echo "Update versions with Tycho (processor)"
cd $path/gama.processor && mvn -B ${TYCHO_VERSIONS_MOJO} -DnewVersion="$newVersion"

echo "Update versions with Tycho (reactor)"
cd $path/gama.parent && mvn -B ${TYCHO_VERSIONS_MOJO} -DnewVersion="$newVersion"

#
#	Update sites url (feature descriptions — not covered by tycho-versions)
#
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
#	UPDATING INSTALLERS
#
echo "Update installer files"
# Windows
sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" $path/gama.product/extraresources/installer/windows/windows_installer_script.iss
# Linux
sed -i "s/$oldVersion/$newVersion/g" $path/gama.product/extraresources/installer/unix/gama-platform.desktop
sed -i "s/$oldVersion/$newVersion/g" $path/gama.product/extraresources/installer/unix/DEBIAN/control
# MacOS
sed -i "s/$oldVersion/$newVersion/g" $path/gama.product/extraresources/Info.plist

#
#	EXTRA Forgotten
#
echo "Update extra individual files"

sed -i "s/V$oldVersion-SNAPSHOT http/V$newVersion http/g" $path/gama.ui.application/plugin.xml

sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" $path/gama.core/src/gama/core/runtime/GAMA.java
sed -i "s/$oldVersion-SNAPSHOT/$newVersion/g" $path/gama.annotations/src/gama/annotations/precompiler/doc/utils/Constants.java

#
#	Meta-Data generator
#
echo "Set GAMA meta-data in config.ini"
sed -i "s/gama.version\" value=\"SNAPSHOT/gama.version\" value=\"$newVersion/g" $path/gama.product/gama.product
sed -i "s/gama.commit\" value=\"SNAPSHOT/gama.commit\" value=\"$(git rev-parse HEAD)/g" $path/gama.product/gama.product
sed -i "s/gama.branch\" value=\"SNAPSHOT/gama.branch\" value=\"$(git rev-parse --abbrev-ref HEAD)/g" $path/gama.product/gama.product
sed -i "s/gama.date\" value=\"SNAPSHOT/gama.date\" value=\"$(date)/g" $path/gama.product/gama.product
sed -i "s/gama.jdk\" value=\"SNAPSHOT/gama.jdk\" value=\"$JDK_EMBEDDED_VERSION/g" $path/gama.product/gama.product

if [ $isSnapshot = "false" ]; then
	echo "Update p2 repositories to gama stable"
	sed -i "s/\/SNAPSHOT/\/$versionToTag/g" $path/gama.product/gama.product

	sed -i "s/\/gama_updates\/$oldVersion/\/gama_updates\/$versionToTag/g" $path/gama.p2site/pom.xml
fi
