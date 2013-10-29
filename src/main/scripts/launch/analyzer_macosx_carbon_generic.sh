#!/bin/sh
cd lib
BASEDIR=`dirname $0`
exec java \
    -XstartOnFirstThread \
    -classpath $BASEDIR/lib/swt.jar:$BASEDIR/lib \
    -jar ${project.artifactId}.jar
