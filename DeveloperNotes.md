#### Developer Notes

##### Introduction

This is a collection of issues and their solution, if found, that were encountered during the development of this
plugin.

##### Maven Project / Module Handling

Maven is part of the IntelliJ Maven Plugin and so not directly available. These are the steps to make it available:

1. Add all JAR files of the Maven Plugin Folder ('intelliJ CE folder'/Contents/plugins/maven/lib) to the Plugin
   development SDK (not as a library)
2. Make the plugin depending on the Maven plugin with this line:

        <depends>org.jetbrains.idea.maven</depends>

3. Then use obtain the MavenProjectsManager with:

        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(myProject);

