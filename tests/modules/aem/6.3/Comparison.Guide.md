## Comparison Guide

This document describes items regarding the comparison of the content on AEM and the project used to
upload to AEM.

There are 3 areas here:
1. Items added to the project that are not needed but that speed up / ease the comparison (like missing .content.xml)
1. Items that differ but are ok and why
1. Items missing in the AEM package

### Base Component

1. Added items:

* /.content.xml
* /apps/.content.xml
* /etc/.content.xml
* /etc/clientlibs/.content.xml
* /etc/designs/.content.xml

2. Items that differ

* /apps/base/components/content/text/_cq_editConfig
* /apps/base/components/content/text/_cq_editConfig/.content.xml

Both morph into /apps/base/components/content/text/_cq_editConfig.xml

3. Items missing in AEM

* /apps/base/install/.vltignore

For obvious reasons

* /brackets.json

No idea. Need to get an answer from Ruben.

Andy Schaefer, 1/9/2016