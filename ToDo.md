### ToDo list for the IntelliJ Plugin

#### Refinements

* Add a Cancel Action to the Toolbar to kill a long running build (**Done**)
* Improve the Progress Indicator (**1. Iteration DONE**)
* Sort / Order the Modules to handle build dependencies
* When a Server Configuration is created its modules should be created
  as it is done inside verify. There is not need to verify as that should
  only be done when a connection is established
* Clean up the Debug Messages and improve the overall message handling
* Remove deprecated classes from the project after there is a replacement
* 

#### Eclipse Independence Tasks

* Merge the current fork with the Sling GIT
* Change the Sling code to remove dependency onto the deprecated
  com.headwire.aem.tooling.intellij.eclipse classes
* Merge my Deployment Manager with the Sling GIT and find a common solution
* We should have a way to track the actions that are done when things
  are deployed so that users view what files were handled with what
  action. This could be done with a class that collects files and the
  actions performed on them. This must be done in Resource Changed
  Command Factory
* 

#### Plugin Tasks

* Create a ZIP / Sling Content Module for IntelliJ to create a content package
* Add the OSGi settings into the Sling Facet to support any type of OSGi package
* Add more tests to the Verification in order to make sure that a build
  is going through. OSGi settings, sling content folders, filters etc
* Test the Plugin with AEM 6.2 beta (**done**)
* Find a better way to handle the various types of modules and how to
  discover them from any other type (there should be only one code that
  correlates them by using a Module Manager)
* All Actions should be executed in the background. Add this to the base
  Action class (**DONE: Dialog Actions are still executed in the Dispatcher
  Thread**)
* 

Andreas Schaefer, 3/17/2016