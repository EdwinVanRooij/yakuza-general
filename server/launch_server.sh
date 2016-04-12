#!/bin/sh

export CLASSPATH=".:../Source/libs/*" 

java -Dwzpath=wz/ \
-Xmx800m net.server.Server