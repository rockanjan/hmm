#!/bin/bash
if [ ! -d "out" ]; then
        mkdir -p out/model;
        mkdir out/decoded;
fi
if [ ! -d "out/model" ]; then
    mkdir -p out/model;
fi
if [ ! -d "out/decoded" ]; then
    mkdir -p out/decoded;
fi
#set proper jdk and ant path
ant clean build
java -Xmx15G -Dfile.encoding=UTF-8 -classpath bin:lib/mallet.jar program.Main
