## AEM Tooling Common Codebase Merge Ideas

### Introduction

During the development of the initial AEM Tooling Plugin for IntelliJ I used the Eclipse code base and try to wrap IntelliJ around that. I did either stubbing of Eclipse classes or took Eclipse classes and infused it with IntelliJ classes.
Since then the Eclipse Plugin released a new version and that would require to redo the entire thing, check all the dependencies and adjust it. Also the development of the plugin took way longer than expected because the Eclipse code depended on many modules that were not directly related to the tooling (Remote Server module etc).

To streamline the development of both IDEs I propose that all code that deals with the AEM is separated into two parts, one that is IDE specific and the rest that is independent of the IDE as well as the OS (file paths etc). Changes affecting the other side of the two worlds should be manifested in the Interface
This way code changes in the IDE / OS independent part should only affect the IDEs' in the interface and changes in the IDE should not affect the IDE / OS independent part.

### Suggestion

Anything in the project that isn't related to Eclipse shouldn't be using any eclipse classes (not sure if that is already done this way).
The eclipse code base that deals with AEM should be split into a part that deals with AEM and the part that deals with Eclipse. Any classes in the Interface of the AEM part should be independent of Eclipse so that it can be used from IntelliJ without any code adjustments.
In addition all file paths in the AEM part should be handled in an OS independent way to avoid re-mappings on inconsistencies.
Finally any non-Eclipse specific code should not use OSGi as IntelliJ is not based on OSGi and I don't it will ever be. Classes like VftRepositoryFactory I had to wrap in order to bind the Event Admin. Eclipse can wrap that class and use OSGi as IntelliJ will wrap it and use the Service Manager instead.

One of the classes that gave me most grief where the Resource Change Command Factory, ServerUtil etc. Dealing with the **I** classes (IServer, IModule, IResource etc) was quite a challenge and keeping up with them is not doable in the long haul.

### Problems

I know that extracting the AEM code into its own module will introduce another layer into Eclipse but I think it would make it easier to develop the AEM tooling for other IDEs and it makes it easier to develop the AEM side as there is not dependent on Eclipse.

This also means that IDE logging and language independence must be made IDE independent. 

Andreas Schaefer, 10/23/2015