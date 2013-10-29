cp ../lib/rxtx/Linux/i686-unknown-linux-gnu/librxtxSerial.so ../../../
mvn install:install-file -Dfile=../lib/swt/swt-gtk-linux-x86.jar -DgroupId=org.eclipse -DartifactId=swt -Dversion=3.7 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
