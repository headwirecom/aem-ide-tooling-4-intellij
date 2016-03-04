package com.headwire.aem.tooling.intellij.eclipse.wrapper;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import org.apache.sling.ide.osgi.impl.HttpOsgiClientFactory;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.event.EventAdmin;

/**
 * Created by schaefa on 5/14/15.
 */
@Deprecated
public class OsgiClientFactoryWrapper
    extends HttpOsgiClientFactory
    implements ApplicationComponent
{

    public OsgiClientFactoryWrapper() {
        EventAdmin eventAdmin = ServiceManager.getService(EventAdmin.class);
        bindEventAdmin(eventAdmin);
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "OSGi Client Factory";
    }
}
