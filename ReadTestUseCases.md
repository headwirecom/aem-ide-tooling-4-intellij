## Test Use Cases for IntelliJ AEM Tooling Plugin

### Introduction

Due to the limitation of Integration Tests inside IntelliJ IDEA most of the testing needs to be done by hand. In order
to provide a consistent testing and results this document is describing the test use cases, its prerequisites and
expected results to pass. The tests are grouped based on their features.

### Overview

### Development Tests

#### Introduction

These tests are just here to document how we test the Application during development. They are not necessarily meant
for production test but rather to ensure code quality.

#### Create New Server Connection

This test is creating a new Server Connection.

1. **Preparation**
    1. Make sure that IntelliJ does not have the Plugin installed
    2. Make sure that you have a Debug Configurations Setup as described in the **Readme.md** file in section **IntelliJ Debug Setup**.
2. Start IntelliJ IDEA CE with the Plugin (Debug 'AEM Tooling Plugin')
3. Open the AEM Plugin window on the right (AEM)
4. Click on the Tree Node Triangle on the Top named **Server Configurations**
5. Click on the + button in the toolbar on top of the window to create a new server configuration
6. **Test**: an empty Dialog should show that completely empty
7. Enter values for all fields including the tabs at the bottom
8. Click the OK Button
9. Click on the Top Tree Node Triangle (named **Server Configurations**) to close it and once more to reopen
10. **Test**: The new entry should show in the tree
11. Click on the new Entry
12. **Test**: The Edit Icon (3rd from the Left) should become active
13. Click on the Edit Icon
14. **Test**: the same Dialog should show all values except the password.

### Project Setup

These tests make sure that a project can be properly setup, loaded and configured.

#### Create New Project

#### 1.1 Load Existing Project without Configuration and Add Default AEM Connection

This Test is loading a Project without an AEM Connection configured and will add an AEM Connection which must trigger
the connection test and the installation of the Support Bundle.

1. **Preparation**:
    1. Extract the src/test/resources/test-project-without-tooling.zip file
    2. Make sure that an AEM Server (6.0) is available and that it is a plain installation (if not delete the
       **crx-quickstart** folder and restart).
    3. Make sure that AEM is not started
    4. Make sure that AEM is running as Author on port 4502 and debug port on 30303
2. Start IntelliJ CE with the Plugin
3. Open the AEM Plugin window on the right (AEM Tooling)
4. **Test**: There should be only the root entry called **Servers** in the tree
5. Click the Add Button (+ sign) on top of the Plugin Window
6. **Test**: There should be a configuration dialog where a connection can be defined or an existing server configuration
             can be selected (there should one called **Default AEM Author**)
7. Select the **Default AEM Author** configuration and click the **OK** button
8. **Test**: There should be an error dialog indicating that an AEM Server is not found
9. Start the AEM server and wait until it is up
10. Hit the **Retry** button on the Error Dialog
11. **Test**: An new Dialog asking the user if the **Support Bundle** should be installed
12. Click the OK button
13. **Test**: The Server Name should be appear under the **Servers** called **Default AEM Server** with an indication that
              the plugin is connected to the server
14. **Test**: Open the Bundle page in the OSGi web console (/system/console/bundles) and make sure the **Support Bundle**
              is installed and active

#### Load Existing Project with Configuration

This test is loading a Project with an AEM Connection Configured and when connecting it should trigger the connection
test and the installation of the Support Bundle.

1. **Preparation**:
    1. Run the previous test **1.1** and make sure it ran through successfully
    2. Shut down (if not already done) the AEM Server
    3. Remove the **crx-quickstart** folder and restart to bring up a clean AEM Server
    4. Make sure that AEM is not started
    5. Make sure that AEM is running as Author on port 4502 and debug port on 30303
2. Start IntelliJ CE with the Plugin
3. Open the AEM Plugin window on the right (AEM Tooling)
4. **Test**: There must be the **Default AEM Author** listed under the root **Servers** node
5. Click the Connect Button on top of the Plugin Window
8. **Test**: There should be an error dialog indicating that an AEM Server is not found
9. Start the AEM server and wait until it is up
10. Hit the **Retry** button on the Error Dialog
11. **Test**: An new Dialog asking the user if the **Support Bundle** should be installed
12. Click the OK button
13. **Test**: The Server Name should remain under the **Servers** called **Default AEM Server** with an indication that
              the plugin is connected to the server
14. **Test**: Open the Bundle page in the OSGi web console (/system/console/bundles) and make sure the **Support Bundle**
              is installed and active

