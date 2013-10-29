cp ../lib/rxtx/Linux/x86_64-unknown-linux-gnu/librxtxSerial.so ../../../
mvn install:install-file -Dfile=../lib/swt/swt-gtk-linux-x86_64.jar -DgroupId=org.eclipse -DartifactId=swt -Dversion=3.7 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
