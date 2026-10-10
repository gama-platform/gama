# stop if any error is raised
set -e 

IFS=$'\n'

# dependencies :
# jgrapht-core, jgrapht-io, jgrapht-opt, org.apache.commons.text
# org.apache.commons.lang3, antlr4-runtime, com.google.guava

# define the dependencies directory
GAMA_DEPENDENCIES_PATH="../gama.dependencies/"
M2_PATH=~/.m2/repository

function removeFileIfExist()
{
    local filename="$1"
    if [ -f $filename ]; then
        rm $filename
    fi
}

function findLibrary()
{
    local search_path="$1"
    local library="$2"
    find $search_path -type f -name "*${library}*.jar"
}

# resolving exact plugins paths dynamically
JGRAPHT_CORE=($(findLibrary $GAMA_DEPENDENCIES_PATH jgrapht-core))
JGRAPHT_IO=($(findLibrary $GAMA_DEPENDENCIES_PATH jgrapht-io))
JGRAPHT_OPT=($(findLibrary $GAMA_DEPENDENCIES_PATH jgrapht-opt))
APACHE_COMMONS_TEXT=($(findLibrary $M2_PATH org.apache.commons.text-))
APACHE_COMMONS_LANG3=($(findLibrary $M2_PATH org.apache.commons.lang3-))
ANTLR4=($(findLibrary $GAMA_DEPENDENCIES_PATH antlr4-runtime))

# building the java classpath
JGRAPHT_PATH="${JGRAPHT_CORE}:${JGRAPHT_IO}:${JGRAPHT_OPT}"
APACHE_COMMONS_PATH="${APACHE_COMMONS_TEXT}:${APACHE_COMMONS_LANG3}"
GUAVA_PATH=($(findLibrary $M2_PATH "com.google.guava-"))

CLASSPATH=".:${JGRAPHT_PATH}:${APACHE_COMMONS_PATH}:${ANTLR4}:${GUAVA_PATH}:src"

removeFileIfExist "./src/gama/ui/devtools/dependencies/Main.class"
removeFileIfExist "./src/gama/ui/devtools/dependencies/GraphToHtml.class"
removeFileIfExist "./src/gama/ui/devtools/dependencies/DependencyGraphBuilder.class"

# graph building
javac -cp $CLASSPATH ./src/gama/ui/devtools/dependencies/DependencyGraphBuilder.java \
    ./src/gama/ui/devtools/dependencies/GraphToHtml.java \
    ./src/gama/ui/devtools/dependencies/Main.java 

java -cp $CLASSPATH gama.ui.devtools.dependencies.Main
# java -cp $CLASSPATH gama.ui.devtools.dependencies.GraphToHtml graph.dot graph.html

# build classic 2d graph representation
# dot -Tpng graph.dot -o graph.png
