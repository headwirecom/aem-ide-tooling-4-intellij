package com.headwire.aem.tooling.intellij.eclipse.wrapper;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import org.apache.sling.ide.impl.vlt.VaultFsLocator;
import org.apache.sling.ide.impl.vlt.serialization.VltSerializationManager;
import org.jetbrains.annotations.NotNull;

/**
* Created by schaefa on 5/14/15.
*/
@Deprecated
class SerializationManagerWrapper
    extends VltSerializationManager
    implements ApplicationComponent
{

    public SerializationManagerWrapper() {
        VaultFsLocator locator = ServiceManager.getService(VaultFsLocator.class);
        bindVaultFsLocator(locator);
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
        return "Serialization Manager Wrapper";
    }
}
