package org.apache.sling.ide.eclipse.core.internal;

import com.intellij.openapi.components.ServiceManager;
import org.apache.sling.ide.artifacts.EmbeddedArtifactLocator;
import org.apache.sling.ide.filter.FilterLocator;
import org.apache.sling.ide.osgi.OsgiClientFactory;
import org.apache.sling.ide.serialization.SerializationManager;
import org.apache.sling.ide.transport.RepositoryFactory;
import org.osgi.service.event.EventAdmin;
import org.apache.sling.ide.log.Logger;

/**
 * This class is a Stub of the class used in the Eclipse Core Module of the Sling Eclipse IDE and
 * is just there to make the integration easier until there is a solution for separating the UI
 * from the Handling Code (support IntelliJ w/o OSGi).
 *
 * Created by schaefa on 5/14/15.
 */
public class Activator {

    public static final int PLUGIN_ID = 1;

    private static Activator instance = new Activator();

    public static Activator getDefault() {
        return instance;
    }

    public RepositoryFactory getRepositoryFactory() {
        return ServiceManager.getService(RepositoryFactory.class);
    }

    public SerializationManager getSerializationManager() {
        return ServiceManager.getService(SerializationManager.class);
    }

    public FilterLocator getFilterLocator() {
        //AS TODO: Implement this
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public OsgiClientFactory getOsgiClientFactory() {
        return ServiceManager.getService(OsgiClientFactory.class);
    }

    public EmbeddedArtifactLocator getArtifactLocator() {
        return ServiceManager.getService(EmbeddedArtifactLocator.class);
    }

    public Logger getPluginLogger() {
        return ServiceManager.getService(Logger.class);
    }

    public EventAdmin getEventAdmin() {
        return ServiceManager.getService(EventAdmin.class);
    }

    public void issueConsoleLog(String installBundle, String s, String s1) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
