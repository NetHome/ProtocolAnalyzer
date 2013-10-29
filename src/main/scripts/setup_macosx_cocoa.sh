cp ../lib/rxtx/Mac_OS_X/librxtxSerial.jnilib ../../../
mvn install:install-file -Dfile=../lib/swt/swt-cocoa-macosx.jar -DgroupId=org.eclipse -DartifactId=swt -Dversion=3.7 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
