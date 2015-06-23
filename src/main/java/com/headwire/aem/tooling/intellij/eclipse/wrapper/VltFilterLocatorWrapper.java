package com.headwire.aem.tooling.intellij.eclipse.wrapper;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import org.apache.sling.ide.impl.vlt.VaultFsLocator;
import org.apache.sling.ide.impl.vlt.filter.VltFilterLocator;
import org.jetbrains.annotations.NotNull;

/**
 * Created by schaefa on 6/19/15.
 */
public class VltFilterLocatorWrapper
    extends VltFilterLocator
    implements ApplicationComponent
{
    public VltFilterLocatorWrapper() {
        VaultFsLocator fsLocator = ServiceManager.getService(VaultFsLocator.class);
        bindVaultFsLocator(fsLocator);
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
        return "VLT Filter Locator";
    }

}
