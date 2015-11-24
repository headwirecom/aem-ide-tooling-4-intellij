package com.headwire.aem.tooling.intellij.io;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.apache.sling.ide.log.Logger;

/**
 * Created by schaefa on 5/14/15.
 */
public class IntelliJPluginLogger
    extends ApplicationComponent.Adapter
    implements org.apache.sling.ide.io.PluginLogger
{
    private com.intellij.openapi.diagnostic.Logger delegate;

    public IntelliJPluginLogger() {
        delegate = PluginManager.getLogger();
    }

    @Override
    public void error(String s) {
        delegate.error(s);
    }

    @Override
    public void error(String message, Object...parameters) {
        delegate.error(format(message, parameters));
    }

    public void error(String s, Throwable throwable) {
        delegate.error(s, throwable);
    }

    @Override
    public void warn(String s) {
        delegate.warn(s);
    }

    @Override
    public void warn(String message, Object...parameters) {
        delegate.warn(format(message, parameters));
    }

    @Override
    public void trace(String message) {
        delegate.debug(message);
    }

    @Override
    public void trace(String message, Object...parameters) {
        delegate.debug(format(message, parameters));
    }

    private String format(String template, Object...parameters) {
        String ret = template;
        if(parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                ret = ret.replace("{" + i + "}", String.valueOf(parameters[i]));
            }
        }
        return ret;
    }
}
