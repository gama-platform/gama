date=$(date +'%d/%m/%y %R')
echo "date=$date" >> "$GITHUB_OUTPUT"

# Add naming file depending on if current file is pre-release or not
fileNameExtension=""
if [[ "$IS_STABLE_RELEASE" == "false" ]]; then
    timestamp="$(echo $date | cut -d' ' -f1 | sed 's|/|.|g')"

    fileNameExtension=_${timestamp}_${COMMIT_SHA}
fi

mkdir $TEMP/files
mv *.zip *.deb *.dmg *.exe $TEMP/files
cd $TEMP/files

# Windows
for file in *.exe *win32*.zip; do
if [[ "$(echo $file | awk -F'_' '{print $NF}')" == *"withJDK"* ]]; then
    mv -v "$file" "GAMA_${RELEASE_VERSION}_Windows_with_JDK${fileNameExtension}.$(echo $file | awk -F'.' '{print $NF}')"
else
    mv -v "$file" "GAMA_${RELEASE_VERSION}_Windows${fileNameExtension}.$(echo $file | awk -F'.' '{print $NF}')"
fi
done

# Mac
for file in *.dmg; do
prefix="${fileNameExtension}.dmg"
if [[ "$(echo $file | awk -F'_' '{print $NF}')" == *"withJDK"* ]]; then
    prefix="_with_JDK${prefix}"
fi

if [[ "$file" == *"aarch64"* ]]; then
    prefix="_Silicon${prefix}"
else 
    prefix="_Intel${prefix}"
fi

prefix="GAMA_${RELEASE_VERSION}_MacOS${prefix}"

mv -v $file $prefix
done

# Linux
for file in *.deb gama-platform*.zip; do
if [[ "$(echo $file | awk -F'_' '{print $1}')" == *"jdk"* ]]; then
    mv -v "$file" "GAMA_${RELEASE_VERSION}_Linux_with_JDK${fileNameExtension}.$(echo $file | awk -F'.' '{print $NF}')"
else
    mv -v "$file" "GAMA_${RELEASE_VERSION}_Linux${fileNameExtension}.$(echo $file | awk -F'.' '{print $NF}')"
fi
done