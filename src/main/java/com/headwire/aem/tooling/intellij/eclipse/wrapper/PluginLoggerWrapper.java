package com.headwire.aem.tooling.intellij.eclipse.wrapper;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.apache.sling.ide.log.Logger;

/**
 * Created by schaefa on 5/14/15.
 */
@Deprecated
public class PluginLoggerWrapper
    extends ApplicationComponent.Adapter
    implements Logger
{
    private com.intellij.openapi.diagnostic.Logger delegate;

    public PluginLoggerWrapper() {
        delegate = PluginManager.getLogger();
    }
    @Override
    public void error(String s) {
        delegate.error(s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        delegate.error(s, throwable);
    }

    @Override
    public void warn(String s) {
        delegate.warn(s);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        delegate.warn(s, throwable);
    }

    @Override
    public void trace(String s, Object... objects) {
        delegate.debug(s, objects);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        delegate.debug(s, throwable);
    }

    @Override
    public void tracePerformance(String s, long l, Object... objects) {
        delegate.debug(s, objects);
    }
}
