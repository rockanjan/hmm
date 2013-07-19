#!/bin/bash
export PATH=/home/anjan/jdk/bin:$PATH
ant clean build
java -Xmx7G -Dfile.encoding=UTF-8 -classpath /home/anjan/workspace/HMM/bin:/home/anjan/workspace/HMM/lib/mallet.jar program.Main
