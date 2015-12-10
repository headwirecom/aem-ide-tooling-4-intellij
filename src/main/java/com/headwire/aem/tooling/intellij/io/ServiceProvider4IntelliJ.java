package com.headwire.aem.tooling.intellij.io;

import org.apache.sling.ide.io.PluginLogger;
import org.apache.sling.ide.io.ProjectUtil;
import org.apache.sling.ide.io.ServiceProvider;

/**
 * Created by schaefa on 11/16/15.
 */
public class ServiceProvider4IntelliJ
    implements ServiceProvider
{
    @Override
    public PluginLogger createPluginLogger() {
        return new IntelliJPluginLogger();
    }
}
