AEM Document Creator Instruction
================================

In order for "aemdc" to work it needs an "aemdc-config.properties" file
in the root of the project. After that properties files is created aemdc
can generate components etc for you.

As of know there are some limitations:

1) There is only one content package supported
2) There is only one /apps, /conf, /designs folder supported
3) There is only one OSGi bundle and one package supported
4) You need to have a set of "aemdc-files" available