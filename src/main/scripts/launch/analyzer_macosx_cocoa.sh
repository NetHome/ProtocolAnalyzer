#!/bin/sh
cd lib
rm swt.jar
cp ../os/swt-cocoa-macosx.jar swt.jar
BASEDIR=`dirname $0`
exec java -d32 \
    -XstartOnFirstThread \
    -classpath $BASEDIR/lib/swt.jar:$BASEDIR/lib \
    -jar ${project.artifactId}.jar
