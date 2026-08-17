# this script requires graalvm's jdk for building the final executable with 'native-image'

set -e


GAMA_EXPORT_PLUGIN_PATH="gama.export"
GAMA_EXPORT_BINARIES_PATH="${GAMA_EXPORT_PLUGIN_PATH}/binaries"

CLASS_PATH=".:${GAMA_EXPORT_PLUGIN_PATH}/dev-libs/org.apache.commons.commons-compress_1.28.0.jar:${GAMA_EXPORT_PLUGIN_PATH}/dev-libs/org.apache.commons.commons-io_2.21.0.jar"

ZIP_EXTRACTOR_SRC_PATH="${GAMA_EXPORT_PLUGIN_PATH}/dev-src/ZipExtractor.java"
ASM_RUNNER_SRC_PATH="${GAMA_EXPORT_PLUGIN_PATH}/dev-src/runner.asm"


if ! [ -d $GAMA_EXPORT_BINARIES_PATH ]; then
    mkdir $GAMA_EXPORT_BINARIES_PATH
fi

echo "Building the elf64 runner"
nasm -f bin $ASM_RUNNER_SRC_PATH -o $GAMA_EXPORT_BINARIES_PATH/runner

echo "Compiling ZipExtractor.java"
javac -cp $CLASS_PATH -d $GAMA_EXPORT_BINARIES_PATH $ZIP_EXTRACTOR_SRC_PATH

echo "Building a native executable from ZipExtractor.class" 
cd $GAMA_EXPORT_BINARIES_PATH
CLASS_PATH=$(echo $CLASS_PATH | sed "s;${GAMA_EXPORT_PLUGIN_PATH};..;g")
native-image -cp .:$CLASS_PATH ZipExtractor

echo "Done"
