copy ..\lib\rxtx\Windows\i368-mingw32\rxtxSerial.dll ..\..\..\
mvn install:install-file -Dfile=..\lib\swt\swt-win32-x86.jar -DgroupId=org.eclipse -DartifactId=swt -Dversion=3.7 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
