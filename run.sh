#!/bin/bash
export PATH=/home/anjan/jdk/bin:$PATH
ant clean build
java -Xmx15G -Dfile.encoding=UTF-8 -classpath bin:lib/mallet.jar program.Main
