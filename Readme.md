### AEM Tooling for IntelliJ IDE Plugin

#### Requirements

This plugin requires to run IntelliJ IDEA **2016.2** or higher.

#### Releases:

1.0.3.4 is out with a fix for the 'Import From' action and ignoring
an error from the Code Smell IntelliJ feature that I used to check
code before building it. For IntelliJ 2018.2 this renders that feature
mute.
 
**Attention**: due to changes inside IntelliJ APIs some stored data might be lost during
the update. If your configuration is lost then just recreate it, if your modules are gone
then just hit **verify** to rebuild them.

New Releases can be found in the **Code -> Release** section on
GitHub: [AEM Intellij Plugin](https://github.com/headwirecom/aem-ide-tooling-4-intellij)

#### For Devs

The plugin has a dependency on the IntelliJ Maven and Git4Idea plugin (see
META-INF/plugin.xml) and they **have** to be added to the **IntelliJ SDK**
and **not as a library**.
As of now do the following:
1. Open the Project Structure Dialog
2. Go down to the SDKs
3. Select your SDK which has to be an IntelliJ Community Editon SDK
4. Select **Classpath** on the right
5. Click the **+** sign at the bottom
6. Go to **content/plugins** and there add Git4Idea and Maven
    1. maven/lib/maven.jar
    2. maven/lib/maven-server-api.jar
    3. git4idea/lib/git4idea.jar

**Attention**: if you upgrade to a newer SDK you have to repeat these steps.

If this is added as a Library then one will encounter Class Cast Exception due
to duplicate classes.

#### Current Branch:

The current development can be found under **feature/'version'** branch.
The **develop** branch is the latest pre-released code.
The **master** is the latest release code and it the same as the highest
**release/v'version'** branch.

#### Updates:

**1.0.3.5-beta**
* Migrated the Plugin to work with IntelliJ Plugin services rather than components so that it works with the latest releases of IntelliJ.
* Updated to the latest Aemdc code base

**1.0.3.4**:
* A fix for the Import From User Action that fails because I cannot write from a
  User Action.
* An exception during code change from the Code Smell Detector is ignored if it is based on
  the 'cannot run under write action' failure. There is not fix in sight from JetBrains or
  help on how to make this work so for now it is just ignored.
* Upgraded the Tooling Support Bundle to version 1.0.4.
  
**1.0.3.3**:
* Working on issues with latest IntelliJ Releases.
* All threading is centralized in single class and with it fixed some issues with threading

**1.0.3.2**:
* Fixed an issue on Windows with Log4j's LocationAwareLogger.

**1.0.3.1**:
* Book keeping release

**1.0.3**:
* Fix an issue with Mac OS X High Sierra to load a class

**1.0.2**:
* Fixed a possible deadlock scenario when a Bundle is not deployed or has a symbolic name mismatch. Now the Dialog will show up after the Debug Connection is established.
* 'Import from' is now working correctly for .content.xml files (beforehand it was ignored).
* Support of the Sling Tooling Support Bundle version 1.0.5-SNAPSHOT which is required for Sling 9 and up.
  Also the Configuration allows the user to configure the selection
  of the support versions even though for most parts the latest should work just fine.
* Added a Configurable Bundle Deployment Retries and Wait Period so that a user can configure the deployment based on its connection speed to the server.

**1.0.1**:
* Fixed an issue where the module file (.iml) is not found by IntelliJ right after the project is created
* Issue 40: Cleaned up Symbolic Name Handling
* Issue 36: Fixed the issue with the 'Import from..'
* Added the proper handling of renaming and removing files. Added a toggle to enable / disabled file systems syncs

**1.0**:
* Cleaning up Code
* Improving Documentation
* Issue 28: AEMDC Dialog Enhancements
* Issue 30: Fix issues related to Symbolic Names
* Fixed issues with relative and Windows OS paths
* Added feedback to the Password field inside the Server Connection Configuration
* Rearranged the AEMDC Dialog and improve user interaction
* Added Tooltips to AEMDC Dialog
* Increased the IntelliJ Version the plugin can run on to 2016.2 or higher
* Fixed issues with exception handling and reporting

#### Introduction

This projects creates an IntelliJ IDE plugin for version 14 and up providing the same features as the Eclipse AEM Tooling provided by the Adobe / Sling team.

#### Installation

Please have a look at the AEM Tooling Plugin for IntelliJ IDEA documentation that you can find the the **documentation** folder right here. It will explain how to install the Plugin and for OSGi deployment the Felix SCR Annotation plugin as well as using the plugin.

### Notes

This README uses Google Analytics for tracking site visits using: [![Analytics](https://ga-beacon.appspot.com/UA-72395016-3/headwirecom/aem-ide-tooling-4-intellij/readme)](https://github.com/igrigorik/ga-beacon)
