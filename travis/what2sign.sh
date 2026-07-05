#!/bin/bash
#
#	Generate list of jars containings .so\|.dylib\|.jnilib to sign for MacOS release
#	Can automatically parse 4 releases at once
#

# set the list separator to newline
IFS=$'\n'
# exit on error
set -e

# init global variables
haveLib=false
NEED_TO_SIGN_FILENAME="needToSign.txt"
currentAppJar=()
alreadySawJar=()

# Add a jar to the list of jars to sign
function addJarInFile(){
	if [[ $(tail -n 1 $NEED_TO_SIGN_FILENAME) != "$1" ]]; then
		echo "$1" >> $NEED_TO_SIGN_FILENAME
	fi

	if (( $# != 1 )); then
		echo "[$1] $2" >> $NEED_TO_SIGN_FILENAME
	fi
}

# List .jar files in the input directory and its subdirectories, without counting global duplicates
function getJarToCheck(){
	local target_directory="$1"

	# List all the jar files contained in the target directory
	currentAppJar=($(find "$target_directory" -name "*.jar"))

	# Remove already checked lines
	currentAppJar=($(grep -v -x -f <(printf "%s\n" "${alreadySawJar[@]}") <(printf "%s\n" "${currentAppJar[@]}") || true))
	
	alreadySawJar=("${alreadySawJar[@]}" "${currentAppJar[@]}")
}

# Check if archive contains *.so, *.dynlib or *.jnilib files
function haveSomethingToSign(){
	if [ $(jar tf "$1" | grep '\.so\|\.dylib\|\.jnilib' | wc -l) -gt 0 ]; then
    	# 0 = true
		return 0 
	else
		return 1
	fi
}

function parseApp(){
	getJarToCheck "$1"

    for jarfile in "${currentAppJar[@]}"
	do
		if haveSomethingToSign "$jarfile"; then
			echo "==> Need to sign $jarfile <=="
			addJarInFile $jarfile
		else
			# Checking the sub .jar files if they exist
			if [ $(jar tf "$jarfile" | grep '\.jar' | wc -l) -gt 0 ]; then
				nestedJar=($(jar tf "$jarfile" | grep '\.jar'))
				for sub_jarfile in "${nestedJar[@]}"
				do
					echo "[$(echo $jarfile | rev | cut -d "/" -f 1 | rev)] Check in $sub_jarfile"
					jar xf "$jarfile" "$sub_jarfile"
					if haveSomethingToSign "$sub_jarfile"; then
						echo "==> Need to sign $sub_jarfile <=="
						addJarInFile "$jarfile" "$sub_jarfile"
					fi
				done
			fi
		fi 
    done
}

function unzipAndParse(){
	local filename="$1"

	echo "Unzipping $filename ..."
	unzip -q "$filename"
	parseApp "./Gama.app"
	
	# Clean-up the created directories
	find . -mindepth 1 -maxdepth 1 -type d -exec rm -fr {} \;
	echo "xxx"
}

touch $NEED_TO_SIGN_FILENAME
for gama in ./gama.application-macosx*.zip; do
	unzipAndParse "$gama"
done

# Remove duplicated lines
tmp=($(awk '!a[$0]++' $NEED_TO_SIGN_FILENAME))
printf "%s\n" ${tmp[@]} > $NEED_TO_SIGN_FILENAME