package org.apache.sling.ide.eclipse.core.internal;

import com.intellij.openapi.application.ApplicationManager;
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
        return ApplicationManager.getApplication().getComponent(RepositoryFactory.class);
    }

    public SerializationManager getSerializationManager() {
        return ApplicationManager.getApplication().getComponent(SerializationManager.class);
    }

    public FilterLocator getFilterLocator() {
        return ApplicationManager.getApplication().getComponent(FilterLocator.class);
    }

    public OsgiClientFactory getOsgiClientFactory() {
        return ApplicationManager.getApplication().getComponent(OsgiClientFactory.class);
    }

    public EmbeddedArtifactLocator getArtifactLocator() {
        return ApplicationManager.getApplication().getComponent(EmbeddedArtifactLocator.class);
    }

    public Logger getPluginLogger() {
        return ApplicationManager.getApplication().getComponent(Logger.class);
    }

    public EventAdmin getEventAdmin() {
        return ApplicationManager.getApplication().getComponent(EventAdmin.class);
    }

    public void issueConsoleLog(String installBundle, String s, String s1) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
