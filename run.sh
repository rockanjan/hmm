#!/bin/bash
if[ ! -d out/model]; then
    mkdir -p out/model;
fi
if[ ! -d out/decoded]; then
    mkdir -p out/decoded;
fi
export PATH=/home/anjan/jdk/bin:$PATH
ant clean build
java -Xmx1G -Dfile.encoding=UTF-8 -classpath bin:lib/mallet.jar program.Main
