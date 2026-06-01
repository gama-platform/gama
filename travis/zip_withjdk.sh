#!/bin/bash
set -e

##########################
# building download URLs #
##########################

# the JDK_EMBEDDED_VERSION variable has to be defined before running this script
# the format must either fit "25.0.3+9" or "25.0.3_9"

JDK_EMBEDDED_VERSION=$(echo $JDK_EMBEDDED_VERSION | tr "+" "_") # replace '+' with '_'
JDK_EMBEDDED_VERSION_URL=$(echo $JDK_EMBEDDED_VERSION | sed "s;_;%2B;g") # replace '_' with '%2B'
JDK_MAJOR=$(echo $JDK_EMBEDDED_VERSION | cut -d '.' -f 1)

RELEASE_URL_PREFIX="https://github.com/adoptium/temurin${JDK_MAJOR}-binaries/releases/download/jdk-${JDK_EMBEDDED_VERSION_URL}"

######################
# defining constants #
######################

OS_LINUX="linux"
OS_MACOS="mac"
OS_WINDOWS="windows"

ARCH_x64="x64"
ARCH_AARCH64="aarch64"

TARGET_PLATFORMS=("linux.gtk.x86_64" "win32.win32.x86_64" "macosx.cocoa.x86_64" "macosx.cocoa.aarch64")

archivePath="$GITHUB_WORKSPACE/gama.application"

function download_and_prepare() {
    local os="$1"
    local arch="$2"
    local output_folder="jdk_${os}_${arch}"

    # infering archive_type from os
    local archive_type=""
    if [ $os == $OS_WINDOWS ]; then 
        archive_type="zip"
    else
        archive_type="tar.gz"
    fi

    # building archive_file name from os, arch and archive type
    local archive_file="${output_folder}.${archive_type}"

    # building the release and sha256 URLs, references are here : https://github.com/adoptium/temurin25-binaries/releases
    local RELEASE_URL="${RELEASE_URL_PREFIX}/OpenJDK${JDK_MAJOR}U-jdk_${arch}_${os}_hotspot_${JDK_EMBEDDED_VERSION}.${archive_type}"
    local SHA256_URL="${RELEASE_URL}.sha256.txt"

    echo "== Downloading jdk-$JDK_EMBEDDED_VERSION for $os $arch"

    # actual downloading
    wget -q $RELEASE_URL -O $archive_file || (echo $archive_file cannot download $archive_file - aborting the script && exit 1)
    wget -q $SHA256_URL -O "${archive_file}.sha256.txt" || (echo $archive_file cannot download the sha256sum of $archive_file - aborting the script && exit 1)

    # comparing hashes, logging and aborting if hashes are not equals
    # the sed command removes the file's name at the end of the sha256 file
    echo Checking the hash of the downloaded archive
    echo "$(cat $archive_file.sha256.txt | sed "s; .*;;g")  $archive_file" | sha256sum -c - || (echo $archive_file checksum does not match - aborting the script && exit 1)

    echo Unziping the archive
    mkdir $output_folder

    if [ $archive_type == "zip" ]; then
	    unzip -q $archive_file -d $output_folder
    elif [ $archive_type == "tar.gz" ]; then
        tar -zxf $archive_file -C $output_folder
    else
        echo Unknown archive type && exit 1
    fi

    mv $output_folder/jdk-* "$output_folder/jdk"
}

echo "=== Downloading latest JDK from $(echo $RELEASE_URL_PREFIX | sed 's;download/;tag/;g')"

download_and_prepare $OS_LINUX $ARCH_x64
download_and_prepare $OS_WINDOWS $ARCH_x64
download_and_prepare $OS_MACOS $ARCH_x64
download_and_prepare $OS_MACOS $ARCH_AARCH64

######################################
# Modify .ini file to use proper JDK #
######################################

echo "=== Creating zip archive of release using custom JDK"
for targetPlatform in ${TARGET_PLATFORMS[@]}; do

	##########################################
	# Get OS (first attribute in the path),  #
	# arch (last attribute in the path)      #
    # and archive type on the file system    #
    ##########################################

	os="$(echo $targetPlatform | cut -d '.' -f 1)"
    arch="$(echo $targetPlatform | cut -d '.' -f 3)"
    archive_type=$(find -name "gama.application-${targetPlatform}*" | sed "s;.*${targetPlatform}\.\(.*\);\1;g")

    ################################
    # normalizing arch and os name #
    ################################

    if [ $arch == "x86_64" ]; then arch=$ARCH_x64; fi

    case "$os" in
        "win32")
            os=$OS_WINDOWS;;
        "macosx")
            os=$OS_MACOS;;
        "linux")
            :;; # do nothing because os name is already normalized
        *)
            echo "Unknown artifact $os" && exit 1;;
    esac

    ###################################
    # Unzip the build in a tmp folder # 
    ###################################

	echo "== Adding custom JDK for $os $arch"
    echo "Unziping the build"

    if [ $archive_type == "zip" ]; then
	    unzip -q $archivePath-$targetPlatform.zip -d $RUNNER_TMP
    elif [ $archive_type == "tar.gz" ]; then
        tar -zxf $archivePath-$targetPlatform.tar.gz -C $RUNNER_TMP
    else
        echo Unknown archive type && exit 1
    fi

	##################################################
	# Specific sub-path for JDK and Eclipse in MacOS #
    ##################################################

	jdkLocation=$RUNNER_TMP
	folderEclipse=$jdkLocation

	if [ $os == $OS_MACOS ]; then
		jdkLocation=$RUNNER_TMP/Gama.app/Contents
		# Create symlink of jdk next to GAMA's executable
		# Fix #229
		ln -sf ../jdk $RUNNER_TMP/Gama.app/Contents/MacOS/jdk
        folderEclipse=$jdkLocation/Eclipse
	fi

	#########################
	# Copy JDK inside build #
    #########################

    echo Copying the jdk inside the build
	sudo cp -R jdk_${os}_${arch}/jdk $jdkLocation

	####################################################
	# Add custom jar signing certificate in custom JDK #
	####################################################

	if [[ -f "$GITHUB_WORKSPACE/sign.maven" ]]; then
		keytool -export -alias gama-platform -file ~/GamaPlatform.cer -keystore ~/gama.keystore -storepass "$GAMA_KEYSTORE_STOREPASS"
		# Using default 'changeit' JDK storepass
		sudo find $RUNNER_TMP -name "cacerts" -exec keytool -importcert -noprompt -file ~/GamaPlatform.cer -keystore {} -alias gama-platform -storepass "changeit" \;
	fi

	##############################
	# Make GAMA use embedded JDK #
    # by editing .ini file       #
    ##############################

    echo Editing Gama.ini

	sed -i '1s/^/-vm\n/' $folderEclipse/Gama.ini

	if [ $os == $OS_MACOS ]; then
		sed -i '2s/^/.\/jdk\/Contents\/Home\/bin\/java\n/' $folderEclipse/Gama.ini

	elif [ $os == $OS_WINDOWS ]; then
		sed -i '2s/^/.\/jdk\/bin\/javaw\n/' $folderEclipse/Gama.ini
        
	else
		sed -i '2s/^/.\/jdk\/bin\/java\n/' $folderEclipse/Gama.ini
	fi

	############################
	# Create final zip archive #
    ############################

    # zip
    echo "Ziping build with JDK"
	cd $RUNNER_TMP
	sudo zip -9 -qyr "${archivePath}-${targetPlatform}_withJDK.zip" . && echo "Successfully compressed ${archivePath}-${targetPlatform}_withJDK.zip" || echo "Failed compressing ${archivePath}-${targetPlatform}_withJDK.zip"

    # clean the tmp folder
	cd $GITHUB_WORKSPACE
    sudo rm -Rf "${RUNNER_TMP:?}"/*
    
done

echo DONE
