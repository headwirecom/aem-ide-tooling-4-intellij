package org.apache.sling.ide.impl.vlt;

import org.apache.sling.ide.log.Logger;
import org.eclipse.core.runtime.MyBundleContext;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertNotNull;

/**
 * This test here is to verify that the "eclipse-fix" is working in IntelliJ using the
 *
 * Created by schaefa on 4/28/15.
 */
public class ActivatorTest {

    @Test
    public void testActivator() throws Exception {
        Activator activator = new Activator();

//        // Check if that works with just a null bundle context
//        BundleContext bundleContext = new MyBundleContext();
//        activator.start(bundleContext);
        // Obtain the Default instance
        Activator test = Activator.getDefault();
        // Obtain the Plugin Logger
        Logger pluginLogger = test.getPluginLogger();

        assertNotNull("Logger was not specified", pluginLogger);
    }
}
