#### Developer Notes

##### Introduction

This is a collection of issues and their solution, if found, that were encountered during the development of this plugin.

#### Checkout the Eclipse AEM Tooling Plugin

The GIT repo can be found here:

    https://github.com/Adobe-Marketing-Cloud/aem-eclipse-developer-tools.git

The project depends on the Sling IDE codebase that can be found here:

    svn checkout http://svn.apache.org/repos/asf/sling/trunk/tooling/ide

The Sling IDE plugin depends on this codebase which must be downloaded and installed first:

    svn checkout https://svn.apache.org/repos/asf/sling/trunk/tooling/support

#### IntelliJ Debug Setup

Like in the Eclipse Debug Setup we could use the Remote Debugging feature of Java but because this is IntelliJ to IntelliJ
debugging we can use the built-in **Plugin** setup. Do the following:

1. Open Menu -> Run -> Edit Configurations
2. Add a new **Plugin** setup
3. Give it a name like **AEM Tooling Plugin** and save it
4. Open Menu -> Run -> **Debug 'AEM Tooling Plugin'**
5. Wait for IntelliJ to come up (IntelliJ starts an IntelliJ CE in another process)
6. Load or select the AEM Eclipse Tooling Test Project
7. Debug the plugin

#### Eclipse Debug Setup

In order to test / debug the Eclipse AEM Tooling from IntelliJ IDE these are the steps to setup it on the Mac OS X:

1. Install Eclipse J2EE Release (Luna or later)
2. Load the Eclipse AEM Tooling project into IntelliJ
3. Open Menu -> Run -> Edit Configurations
4. Create a new Remote, name it 'Eclipse Remote' and choose a port (here we use the default of 5005)
5. Edit eclipse/Eclipse.app/Contents/MacOS/eclipse.ini
6. Add this at the end of the file: '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005' without
   the quotes. Adjust the port (address) according to your configuration inside IntelliJ
7. Start the Debugger with Menu -> Run -> Debug 'Eclipse Remote'

Make sure the Debugger is connecting to the remote Eclipse installation.

#### Eclipse AEM Tooling Example Project

This project is used as a comparison between the current version of the Eclipse AEM Tooling and the IntelliJ IDE plugin
to see if there a features lacking or do not work correctly.

##### Loading in Eclipse

Do test this project it is best to set the workspace to its parent directory:

    eclipse.aem.tooling.workspace

and then the project should show up in the Project Explorer with a server connecting to localhost:4502 (30303 for debug).

##### Loading in IntelliJ

The project should be opened through the project folder

    eclipse.aem.tooling.workspace/aem-tooling-test-project

##### Maven Project / Module Handling

Maven is part of the IntelliJ Maven Plugin and so not directly available. These are the steps to make it available:

1. Add all JAR files of the Maven Plugin Folder ('intelliJ CE folder'/Contents/plugins/maven/lib) to the Plugin
   development SDK (not as a library)
2. Make the plugin depending on the Maven plugin with this line:

        <depends>org.jetbrains.idea.maven</depends>

3. Then use obtain the MavenProjectsManager with:

        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(myProject);

