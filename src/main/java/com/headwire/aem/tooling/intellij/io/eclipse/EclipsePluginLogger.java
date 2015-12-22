package com.headwire.aem.tooling.intellij.io.eclipse;

import com.intellij.ide.plugins.PluginManager;
import org.apache.sling.ide.io.PluginLogger;

/**
 * Created by schaefa on 12/22/15.
 */
public class EclipsePluginLogger
    implements PluginLogger
{
    private org.apache.sling.ide.log.Logger delegate;

    public EclipsePluginLogger() {
        delegate = org.apache.sling.ide.eclipse.core.internal.Activator.getDefault().getPluginLogger();
        //AS TODO: This will not work
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
        delegate.trace(message);
    }

    @Override
    public void trace(String message, Object...parameters) {
        delegate.trace(format(message, parameters));
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
