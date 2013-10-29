cd lib
del swt.jar
del rxtxSerial.dll
copy ..\os\swt-win32-win32-x86_64.jar swt.jar
copy ..\os\rxtxSerial_64.dll rxtxSerial.dll
start javaw -jar ${project.artifactId}.jar
