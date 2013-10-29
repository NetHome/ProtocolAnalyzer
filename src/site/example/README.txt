This is an example of a protocol decoder plugin. Any jars containing plugins
that are placed in this directory will be loaded by ProtocolAnalyzer at startup
and the plugin decoders will be used by the ProtocolAnalyzer.

The source code of the DemoDecoder contains comments describing how to write
your own protocol decoder plugins.

To build the example decoder, simply issue the following commands from the 
command line:

javac -classpath ../../lib/utils-${utils.version}.jar DemoDecoder.java
jar cf plugin.jar DemoDecoder.class


The first line will compile the java source into a .class-file and the second line will place that
.class-file in a .jar-file called plugin.jar. When the ProtocolAnalyzer starts it will scan this
library, find this .jar-file and load it and use the decoder it finds in it.

Note! if you have installed ProtocolAnalyzer in the Program Files folder on windows, you may not have
write permissions to this folder. The simplest solution then is to uninstall it and reinstall it
somewhere else.