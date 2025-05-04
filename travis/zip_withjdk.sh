#!/bin/bash

#
#	Prepare utils variables
#

set -e
archivePath="$GITHUB_WORKSPACE/gama.application"
JDK_MAJOR=$(echo $JDK_EMBEDDED_VERSION | cut -d '.' -f 1)

#
#	Download latest JDK
#

echo "=== Download latest JDK"
echo "Downloading from https://api.github.com/repos/adoptium/temurin$JDK_MAJOR-binaries/releases/tags/jdk-$JDK_EMBEDDED_VERSION"

wget -q $(curl https://api.github.com/repos/adoptium/temurin$JDK_MAJOR-binaries/releases/tags/jdk-$JDK_EMBEDDED_VERSION | grep "/OpenJDK${JDK_MAJOR}U-jdk_x64_linux.*.gz\"" | cut -d ':' -f 2,3 | tr -d \") -O "jdk_linux-21.tar.gz"
wget -q $(curl https://api.github.com/repos/adoptium/temurin$JDK_MAJOR-binaries/releases/tags/jdk-$JDK_EMBEDDED_VERSION | grep "/OpenJDK${JDK_MAJOR}U-jdk_x64_window.*.zip\"" | cut -d ':' -f 2,3 | tr -d \") -O "jdk_win32-21.zip"
wget -q $(curl https://api.github.com/repos/adoptium/temurin$JDK_MAJOR-binaries/releases/tags/jdk-$JDK_EMBEDDED_VERSION | grep "/OpenJDK${JDK_MAJOR}U-jdk_x64_mac.*.gz\"" | cut -d ':' -f 2,3 | tr -d \") -O "jdk_macosx-21.tar.gz"
wget -q $(curl https://api.github.com/repos/adoptium/temurin$JDK_MAJOR-binaries/releases/tags/jdk-$JDK_EMBEDDED_VERSION | grep "/OpenJDK${JDK_MAJOR}U-jdk_aarch64_mac.*.gz\"" | cut -d ':' -f 2,3 | tr -d \") -O "jdk_macosx_aarch-21.tar.gz"

#
#	Prepare downloaded JDK
#
echo "=== Prepare downloaded JDK"
for os in "linux" "macosx" "macosx_aarch" "win32"; do
	mkdir jdk_$os

	echo "unzip jdk $os"
    if [[ -f "jdk_$os-21.tar.gz" ]]; then
    tar -zxf jdk_$os-21.tar.gz -C jdk_$os/
	else
		unzip -q jdk_$os-21.zip -d jdk_$os
	fi

	mv jdk_$os/jdk-2* jdk_$os/jdk
done


#
# Modify .ini file to use custom JDK
#
echo "=== Creating zip archive of release using custom JDK"
for targetPlatform in "linux.gtk.x86_64" "win32.win32.x86_64" "macosx.cocoa.x86_64" "macosx.cocoa.aarch64"; do

	#
	# Get OS (first attribute in the path)
	# + Add suffix if build for ARM64 system
	os="$(echo $targetPlatform | cut -d '.' -f 1)$(if [[ "$targetPlatform" == *'aarch64'* ]]; then echo '_aarch'; fi )"

	echo "Add custom JDK for $os"

	unzip -q $archivePath-$targetPlatform.zip -d $RUNNER_TMP

	#
	# Specific sub-path for Eclipse in MacOS
	jdkLocation=$RUNNER_TMP
	if [[ "$os" == "macosx"* ]]; then
		jdkLocation=$RUNNER_TMP/Gama.app/Contents
		# Create symlink of jdk next to GAMA's executable
		# Fix #229
		ln -sf ../jdk $RUNNER_TMP/Gama.app/Contents/MacOS/jdk
	fi

	#
	# Add JDK to build
	sudo cp -R jdk_$os/jdk $jdkLocation
	#
	# Add custom jar signing certificate in custom JDK
	if [[ -f "$GITHUB_WORKSPACE/sign.maven" ]]; then
		keytool -export -alias gama-platform -file ~/GamaPlatform.cer -keystore ~/gama.keystore -storepass "$GAMA_KEYSTORE_STOREPASS"
		# Using default 'changeit' JDK storepass
		sudo find $RUNNER_TMP -name "cacerts" -exec keytool -importcert -noprompt -file ~/GamaPlatform.cer -keystore {} -alias gama-platform -storepass "changeit" \;
	fi


	#
	# Specific sub-path for Eclipse in MacOS
	folderEclipse=$jdkLocation
	if [[ "$os" == "macosx"* ]]; then
		folderEclipse=$jdkLocation/Eclipse
	fi
	#
	# Make GAMA use embedded JDK
	sed -i '1s/^/-vm\n/' $folderEclipse/Gama.ini
	if [[ "$os" == "macosx"* ]]; then
		sed -i '2s/^/.\/jdk\/Contents\/Home\/bin\/java\n/' $folderEclipse/Gama.ini
	elif [[ "$os" == "win32"* ]]; then
		sed -i '2s/^/.\/jdk\/bin\/javaw\n/' $folderEclipse/Gama.ini
	else
		sed -i '2s/^/.\/jdk\/bin\/java\n/' $folderEclipse/Gama.ini
	fi

	#
	# Create final zip archives
	cd $RUNNER_TMP
	sudo zip -9 -qyr "${archivePath}-${targetPlatform}_withJDK.zip" . && echo "Successfully compressed ${archivePath}-${targetPlatform}_withJDK.zip" || echo "Failed compressing ${archivePath}-${targetPlatform}_withJDK.zip"

	cd $GITHUB_WORKSPACE && sudo rm -fr $RUNNER_TMP

done

echo DONE
