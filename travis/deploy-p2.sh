#!/bin/bash

echo "Publishing module gama.annotations on p2 site"
cd $( dirname $( realpath "${BASH_SOURCE[0]}" ) )/../gama.annotations
mvn deploy --settings ../travis/settings.xml -DskipTests=true -B "$@"

echo "Publishing module gama.processor on p2 site"
cd ../gama.processor 
mvn deploy --settings ../travis/settings.xml -DskipTests=true -B "$@"

echo "Publishing module gama.parent on p2 site"
cd ../gama.parent 
mvn deploy --settings ../travis/settings.xml -DskipTests=true -B "$@"
