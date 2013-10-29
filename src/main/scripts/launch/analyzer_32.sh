#!/bin/sh
cd lib
rm swt.jar
rm librxtxSerial.so
cp ../os/swt-gtk-linux-x86.jar swt.jar
cp ../os/librxtxSerial_x86_32.so librxtxSerial.so
java -Djava.library.path=. -jar ${project.artifactId}.jar

