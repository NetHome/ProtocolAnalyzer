cd lib
del swt.jar
del rxtxSerial.dll
copy ..\os\swt-win32-x86.jar swt.jar
copy ..\os\rxtxSerial_32.dll rxtxSerial.dll
start javaw -jar ${project.artifactId}.jar
