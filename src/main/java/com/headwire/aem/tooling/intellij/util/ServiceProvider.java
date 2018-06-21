package com.headwire.aem.tooling.intellij.util;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
//import org.apache.sling.ide.impl.vlt.VaultFsLocator;
//import org.apache.sling.ide.impl.vlt.serialization.VltSerializationManager;
//
//import static com.headwire.aem.tooling.intellij.util.AccessUtil.setPrivateFieldValue;

public class ServiceProvider {

    private static final Logger LOGGER = Logger.getInstance(ServiceProvider.class);

    public static <T> T getService(Class<T> clazz) {
        T ret = ServiceManager.getService(clazz);
//        if(clazz == VltSerializationManager.class) {
//            VaultFsLocator locator = ServiceManager.getService(VaultFsLocator.class);
//            try {
//                setPrivateFieldValue(VltSerializationManager.class, ret,"fsLocator", locator);
//            } catch (IllegalAccessException e) {
//                LOGGER.warn("Failed to access 'fsLocator' on VltSerializationManager", e);
//            } catch (NoSuchFieldException e) {
//                LOGGER.warn("Failed to find 'fsLocator' on VltSerializationManager", e);
//            }
//        } else if()
        return ret;
    }
}
