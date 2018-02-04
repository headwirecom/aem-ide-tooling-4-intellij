### AEM Tooling for IntelliJ IDE Plugin

#### Requirements

This plugin requires to run IntelliJ IDEA **2016.2** or higher.

#### Releases:

Release of 1.0.3.1 is out. Check out in the release section.

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

**0.7.3**
* Working on integrating and testing latest Sling Tooling IDE code from Trunk
* Fixed an issue with Import From Server (Context Menu) failing
* Fixed an issue with finding a folder in Windows
* Fixed an issue with creating folders locally during import from server
* Added the AEM Developer Companion to the Plugin
* Added a Info page to the plugin
* Fixed an issue with the modification timestamp

**0.7.2**:
* Fixed the issue with the deadlock during Project Load. An automatic start connection during
  that phase led to a deadlock inside the Event Dispatcher. This means that you can use the
  Start Connection on Load again.

**0.7.1**:
* Fixed an issue where the Plugin tried to connect to the repository during incremental deployment
  even when the connection was stopped
* Fixed an issue when the Plugin tried to compile even though there was not configuration
  (non AEM / Sling project)
* Added the 'Filter is generated' flag to the Sling Module Facet. With that it is possible to verify
  a project without having a filter.xml file. It is important though that the filter.xml file is
  available in the designated META-INF/vault folder at the time of the deployment.

**0.7.0**:
* Forced Deployment will Purge Cache first so that both have the same effect locally
* Implemented Install Button in Server Configuration to install Support Bundle
* Made General Plugin Configuration persistent.
* Added a check to the Run Connection so that if the user changes the Server Configuration the
  cache will be automatically purged so that the next deployed will push all of it.
* The Debug Connection is now also doing a Verification and Server Configuration change check
  like the Run Connection.
* The Jar File validator on the Facet is now working properly.
* Exported the Background task execution into its own utility method.
* Added a Dialog to the Possible Symbolic Name Mismatch so that further warnings can be ignored.
* When a new module is created the module is automatically verified. This will also create the
  list of modules and updates the tree.
* Fixed a NPE when the Plugin Preferences Configuration is not created

**0.6.4.9**:
* Dropped the SNAPSHOT from the version.  
* Started to work on a better Module handling to unify
  all the various modules (IntelliJ Project Modules, Maven Project and Server Configuration Module).  
* Added to support for an override of the Bundle Symbolic Name as Felix is renaming them sometimes and
  porting all OSGi configuration for non-Maven OSGi bundles to the Facet to work with any OSGi
  configurations and not just Osmorc.  
* Made the Server Configuration Verification Action available for any selected Server Configuration
  and let it run in the Foreground to allow for user interactions.  
* Moving most of the static text into the Resource Bundle including Debug messages.
* Fixed an issue with the Sling Module Builder which was renamed.  
* Fixed an issue where in the latest IntelliJ the Plugin Settings will not show in the settings.  
* Fixed an issue with the Debug Mode as it cannot run in the background.  
* Updated the Documentation to this release and added new topics like Sling Module Facets etc.  

**0.6.4.8-SNAPSHOT**: Make it work with IntelliJ 2016.1.0. Added the cancel action to the toolbar
                      to stop background actions. Fixed an issue with IntelliJ 14 which failed to find
                      components / services if on application level.

**0.6.4.7-SNAPSHOT**: All but Dialog Actions were move into the background which is done in the base
                      class so the Actions don't need to do anything special. Added a better way to
                      handle process indicators that also deals with no indicator and handles the nesting
                      easier as it automates the pops.

**0.6.4.6-SNAPSHOT**: Fixed an issue with the entire Module build which led to deadlocks. Also removed some
                      debug statements that could disable the Plugin. For now the usage of non-Maven based
                      project is discouraged as IntelliJ has no concept of a ZIP file based module.

**0.6.4.5-SNAPSHOT**: Non-Maven OSGi modules should build now. There is a fix for any OSGi module as the
                      deployment of Maven OSGi modules failed silently to deploy. This is fixed and if the
                      build file is not present an alert is shown.
                      There is also a fix for Maven modules where the Module Name and the Maven Artifact Id
                      did not match as it is now looking at the parent folder to find a match.

**0.6.4.4-SNAPSHOT**: Added support for non-Maven based project. In order to support these types of projects
                      you need to use two facets. For OSGi modules you need to use the "Osmorc" plugin
                      to setup the OSGi Facet and for Content Modules you need to setup the Sling Content
                      Facet and specify the Content and META-INF root folder.
                      For now only the IntelliJ Plugin Osmorc is supported for non-Maven OSGi bundles.

**0.6.4.3-SNAPSHOT**: This release makes the deployment wait until the Maven Build for OSGi bundles is over.
                      In addition the toolbar is locked while any action is executed so that background tasks
                      like the Maven Build cannot be intercepted. Finally the build also addresses an issue
                      with AEM DAM Workflows could interfere with deployments of DAM Assets. The plugin will
                      place the push of the /renditions/original nodes to the end.

**0.6.4.2-SNAPSHOT**: This release contains a fix for the Maven Build execution for OSGi Bundles. Now the build
                      is executed in the background and the action waits until it is finished before installing
                      the OSGi Bundle in AEM.

**0.6.4.1-SNAPSHOT**: This releases contains the refactored Resource Change Command Factory which is made
IDE independent. The UI and handling did not change. This release also contains a fix for mysterious failures to
upload Renditions files.

**0.6 Beta 3**: Fixed an issue with the Console Log Settings to being disabled if no configuration is selected.
Fixed issues with Windows paths. Fixed an issue with Force Deploy doesn't apply to parent folders.
Fixed an issue with Windows paths and File Changes.

**0.6 Beta 2**: Checking a Selected Configuration is renamed to **Run** as it indicates a persistence of the connection
and even this isn't the case it is persistent for deployments.
Also renamed the Reset Current Configuration to **Purge Plugin Cache Data**.
Adjusted some of the Icon's Enabled status to work as intended.

**0.6 Beta 1**: Added the support to create new Projects through Maven Archetypes (AEM or Sling). The Builder
is geared towards that and only shows the available Archetypes and let the user set the
required properties. The rest remains the same as creating a Project through the Maven Builder
and using archetypes there

**0.5-BETA-2** is available which now tries to perform a Maven build for OSGi Modules during the deployment. This feature
can be switched off (on is the default) in the Server Connection Configuration of the Plugin. If the Edit Button is
disabled make sure you stop the connection first.

**Attention**: There is a little problem with IntelliJ where the first Maven build from the AEM Plugin will fail. There
is an Alert that pops up and it will suggest that you try once more. During the initial tests that did solve the issue.

#### Introduction

This projects creates an IntelliJ IDE plugin for version 14 and up providing the same features as the Eclipse AEM Tooling provided by the Adobe / Sling team.

#### Installation

Please have a look at the AEM Tooling Plugin for IntelliJ IDEA documentation that you can find the the **documentation** folder right here. It will explain how to install the Plugin and for OSGi deployment the Felix SCR Annotation plugin as well as using the plugin.

### Notes

This README uses Google Analytics for tracking site visits using: [![Analytics](https://ga-beacon.appspot.com/UA-72395016-3/headwirecom/aem-ide-tooling-4-intellij/readme)](https://github.com/igrigorik/ga-beacon)
