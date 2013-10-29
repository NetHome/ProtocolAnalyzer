copy ..\lib\rxtx\Windows\win-x64\rxtxSerial.dll ..\..\..\
mvn install:install-file -Dfile=..\lib\swt\swt-win32-win32-x86_64.jar -DgroupId=org.eclipse -DartifactId=swt -Dversion=3.7 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
