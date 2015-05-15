package com.headwire.aem.tooling.intellij.util;

import org.apache.sling.ide.artifacts.EmbeddedArtifactLocator;
import org.apache.sling.ide.impl.vlt.VltRepositoryFactory;
import org.apache.sling.ide.osgi.OsgiClientFactory;
import org.apache.sling.ide.osgi.impl.HttpOsgiClientFactory;
import org.apache.sling.ide.transport.RepositoryFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Created by schaefa on 5/5/15.
 */
@Deprecated
public class OSGiFactory {

    private static OsgiClientFactory osgiClientFactory = new HttpOsgiClientFactory();
    private static EmbeddedArtifactLocator artifactLocator = new ArtifactsLocatorImpl();
    private static RepositoryFactory repositoryFactory;

    public static OsgiClientFactory getOSGiClientFactory() {
        return osgiClientFactory;
    }

    public static EmbeddedArtifactLocator getArtifactLocator() {
        return new ArtifactsLocatorImpl();
    }

    public static RepositoryFactory getRepositoryFactory() {
        if(repositoryFactory == null) {
            MyVltRepositoryFactory vltRepositoryFactory = new MyVltRepositoryFactory();
            vltRepositoryFactory.bindEventAdmin(
                new EventAdmin() {
                    @Override
                    public void postEvent(Event event) {
                    }

                    @Override
                    public void sendEvent(Event event) {
                    }
                }
            );
            repositoryFactory = vltRepositoryFactory;
        }
        return repositoryFactory;
    }

    private static class MyVltRepositoryFactory extends VltRepositoryFactory {
        @Override
        public void bindEventAdmin(EventAdmin eventAdmin) {
            super.bindEventAdmin(eventAdmin);
        }

        @Override
        public void unbindEventAdmin(EventAdmin eventAdmin) {
            super.unbindEventAdmin(eventAdmin);
        }
    }
}
