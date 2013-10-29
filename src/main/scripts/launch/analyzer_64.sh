#!/bin/sh
cd lib
rm swt.jar
rm librxtxSerial.so
cp ../os/swt-gtk-linux-x86_64.jar swt.jar
cp ../os/librxtxSerial_x86_64.so librxtxSerial.so
java -Djava.library.path=. -jar ${project.artifactId}.jar
