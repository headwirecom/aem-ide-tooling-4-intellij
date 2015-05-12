package com.headwire.aem.tooling.intellij.util;

import org.apache.sling.ide.artifacts.EmbeddedArtifact;
import org.apache.sling.ide.artifacts.EmbeddedArtifactLocator;

import java.net.URL;

/**
 * Created by schaefa on 5/5/15.
 */
public class ArtifactsLocatorImpl
    implements EmbeddedArtifactLocator
{
//    private static final String ARTIFACTS_LOCATION = "target/artifacts";
    private static final String ARTIFACTS_LOCATION = "artifacts";

    @Override
    public EmbeddedArtifact loadToolingSupportBundle() {

        String version = "1.0.2"; // TODO - remove version hardcoding
        String artifactId = "org.apache.sling.tooling.support.install";
        String extension = "jar";

        String fileName = artifactId
            + (version != null || version.trim().length() > 0 ? "-" + version : "")
            + "." + extension;
        URL jarUrl = loadResource(
//            bundleContext,
            ARTIFACTS_LOCATION + "/sling-tooling-support-install/" + fileName
        );

        return new EmbeddedArtifact(fileName, version, jarUrl);
    }

    private URL loadResource(
//        BundleContext bundleContext,
        String resourceLocation
    ) {
        URL resourceUrl = getClass().getClassLoader().getResource(resourceLocation);
        if(resourceUrl == null) {
            throw new RuntimeException("Unable to locate bundle resource " + resourceLocation);
        }
        return resourceUrl;
    }
}
