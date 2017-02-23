## Tests for IntelliJ Plugin

### Introduction

Lately I found some dormat / hidden bugs in the IntelliJ plugin that cause some grief to find.
More problematic is the fact that user might had issues using the plugin. Stepping through the
Plugin and making sure that it works it somewhat difficult and time consuming and so we need
a more efficoent way to test for correct behavior and easily spot regressions.
 
### Concept

The basic idea is to have AEM projects that both can build an CRX Package as well as be deployed
through the IntelliJ plugin.

This is the basic procedure:

1. Use Maven to build a CRX Package but do not install it
1. Upload the CRX Package to AEM
1. Load the Project in IntelliJ
1. Force Deploy the Package in IntelliJ
1. Go to the Package Manager
1. Select the Package and build it
1. Download the Package
1. Extract the Package
1. Compare the Package with the Project

### Tests

We need tests for all AEMs we support. In addition when Sling 9 comes out we can use the same
concept but instead of the CRX Package we use Composum.

The reason to have a test for each AEM version is due to the dependencies. 6.3 has different
version / dependencies as 6.2 etc.

#### Test Modules
 
 We need tests to cover /apps (components, JSPs, slightly etc), /content (pages, DAM etc)
 and /etc (css, clinent libs etc).
 
 As long as we don't have automated tests we must combine the various "tests" into one module
 so that upload, deployment, extraction and compirson can be done quickly.
 
 The test should list all scenarios that are tested and also note any accepted differences.
 
 
 Andreas Schaefer, 1/6/2017