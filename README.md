Protocol Analyzer
=================
The Protocol Analyzer is a small tool that can catch, analyze and decode “slow” pulse based protocols.
It can sample messages either from the audio input or via a CUL RF-receiver. See http://wiki.nethome.nu for details.

The Protocol Analyzer supports plugins for writing new protocol decoders, so if that is what you want to do, 
you do not need to check out and build this source. All you have to do is to download the binary package
and look in the Plugin folder to get information on how to write plugins for new protocols.


Development Environment
-----------------------

Dependencies: Java SE SDK 1.6, Maven 2, Coders-module, Utils-module.

Coders and Utils are located in two separate repositories, so you need to 
download and build them first so they are installed in your local maven repository.

The project is built using Maven, which makes it quite easy to load it into any
standard development IDE such as Eclipse or IntelliJ. Since the project uses
the SWT window library and rxtx serial library which are both platform
dependant, you have to run a platform setup script to configure the build
environment for your development platform before you can build it.
These scripts are located under src/main/scripts. So to configure the
environment for a 64 bits Windows 7 platform you would have to:

    cd src/main/scripts
    setup_win64.bat

On Linux and MAC you also have to make the script executable before running it.
For example:

    cd src/main/scripts
    chmod +x setup_macosx_cocoa64.sh
    ./setup_macosx_cocoa64.sh

This will install the correct versions of the swt library in your local Maven
repository and copy the correct version of the rxtx runtime libraries to the
root of the project so you can run the application from your IDE.

How to Build
------------

To just build the jar file of the project, you issue:

    mvn package

from the root of the project.
To build the entire deployment package you issue:

    mvn install

To also build the installer, use the profile "installer":

    mvn install -Pinstaller

Open from IntelliJ
------------------

IntelliJ can read Maven project files, so the project can be opened directly.
All you have to do is to create a run-configuration. Select:

run->edit configurations

and create a new application. Select the main class as:

nu.nethome.tools.protocol_analyzer.Main

And the configuration should be able to build and run the application.

Open from Eclipse
-----------------

Maven can create an eclipse project by issuing the command:

    mvn eclipse:eclipse

After that you can import the project into your workspace. You will have to
create a variable called M2_REPO which points to your local maven repository,
which on windows is located under your personal folder in a folder called

.m2\repository

For example: C:\Users\Stefan\.m2\repository
