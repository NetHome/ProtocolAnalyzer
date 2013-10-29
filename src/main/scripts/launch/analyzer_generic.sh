#!/bin/sh
cd lib
java -Djava.library.path=. -jar ${project.artifactId}.jar
