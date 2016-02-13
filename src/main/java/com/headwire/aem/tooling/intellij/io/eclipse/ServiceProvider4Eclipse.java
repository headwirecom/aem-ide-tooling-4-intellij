package com.headwire.aem.tooling.intellij.io.eclipse;

import com.headwire.aem.tooling.intellij.io.IntelliJPluginLogger;
import org.apache.sling.ide.io.PluginLogger;
import org.apache.sling.ide.io.ServiceProvider;
import org.apache.sling.ide.serialization.SerializationManager;

/**
 * Created by schaefa on 11/16/15.
 */
public class ServiceProvider4Eclipse
    implements ServiceProvider
{
    @Override
    public PluginLogger createPluginLogger() {
        return new EclipsePluginLogger();
    }

    @Override
    public SerializationManager getSerializationManager() {
        return null;
    }
}
