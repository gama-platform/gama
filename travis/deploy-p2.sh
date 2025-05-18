#!/bin/bash

echo "Publishing p2 site public OVH server"
cd $( dirname $( realpath "${BASH_SOURCE[0]}" ) )/../gama.p2site
mvn clean install --settings ../travis/settings.xml -DskipTests=true -B "$@"
