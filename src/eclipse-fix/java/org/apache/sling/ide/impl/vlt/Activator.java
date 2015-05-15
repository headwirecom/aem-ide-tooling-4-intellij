package org.apache.sling.ide.impl.vlt;

import com.intellij.openapi.components.ServiceManager;
import org.apache.sling.ide.log.Logger;

/**
 * Created by schaefa on 5/15/15.
 */
public class Activator {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.apache.sling.ide.impl-vlt"; //$NON-NLS-1$

    private static Activator instance = new Activator();

    public static Activator getDefault() {
        return instance;
    }

    public Logger getPluginLogger() {
        return ServiceManager.getService(Logger.class);
    }
}
