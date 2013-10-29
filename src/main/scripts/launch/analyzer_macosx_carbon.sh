#!/bin/sh
cd lib
rm swt.jar
cp ../os/swt-carbon-macosx.jar swt.jar
BASEDIR=`dirname $0`
exec java \
    -XstartOnFirstThread \
    -classpath $BASEDIR/lib/swt.jar:$BASEDIR/lib \
    -jar ${project.artifactId}.jar
