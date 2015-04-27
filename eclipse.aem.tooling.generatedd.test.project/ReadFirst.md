## Eclipse AEM Tooling Generated Test Project

This is the test project from the Eclipse AEM Tooling and should be used to make sure that the IntelliJ Plugin
provides the same features as the Eclipse counterpart.

### Build and Deployment

Go to this directory and build the project using maven with

    mvn clean install -P autoDeployBundle

to deploy an OSGi Bundle like **core**.

    mvn clean install -P autoInstallPackage

to deploy an AEM package like **ui.content**.

#### Tests

In order to make sure that everything is working as expected make sure that the Bundles are deployed, installed and
activate (in the OSGi Web Console under /system/console/bundles). Also make sure the services and components are all
there.
For the Packages make sure they are listed in the Package Manager and that they are deploy fine. In doubt go ahead
and re-install them in the Package Manager.

#### HotSwap

Hot Swap will only work if changes are only made to the content of a method and not to any structure of the class. Any
exceptions from the remote JVM that says something like **Structural Changes** or **Add / Delete Method** not supported
then you hit one of these issues.
There is a problem with changes to the OSGi services as the **Maven SCR Plugin** does inject methods (bind / unbind) for
**@Reference** annotations. This will yield a **Delete Method not supported**. There are two solutions for it:

**1)** Write the necessary bind / unbind method. So if you have this:


    @Reference
    MyService myService;

    // Add these now
    public void bindMyService(MyService myService) {
        this.myService = myService;
    }

    public void unbindMyService(MyService myService) {
        this.myService = null;
    }


**2)** Install the Felix SCR Annotation Processor (http://plugins.jetbrains.com/plugin/7009) plugin and restart


