package com.headwire.aem.tooling.intellij.util;

import org.apache.sling.ide.artifacts.EmbeddedArtifactLocator;
import org.apache.sling.ide.osgi.OsgiClientFactory;
import org.apache.sling.ide.osgi.impl.HttpOsgiClientFactory;

/**
 * Created by schaefa on 5/5/15.
 */
public class OSGiFactory {

    private static OsgiClientFactory osgiClientFactory = new HttpOsgiClientFactory();
    private static EmbeddedArtifactLocator artifactLocator = new ArtifactsLocatorImpl();

    public static OsgiClientFactory getOSGiClientFactory() {
        return osgiClientFactory;
    }

    public static EmbeddedArtifactLocator getArtifactLocator() {
        return new ArtifactsLocatorImpl();
    }
}
