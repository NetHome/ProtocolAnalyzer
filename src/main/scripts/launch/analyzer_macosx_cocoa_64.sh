#!/bin/sh
cd lib
rm swt.jar
cp ../os/swt-cocoa-macosx-x86_64.jar swt.jar
BASEDIR=`dirname $0`
exec java \
    -XstartOnFirstThread \
    -classpath $BASEDIR/lib/swt.jar:$BASEDIR/lib \
    -jar ${project.artifactId}.jar
