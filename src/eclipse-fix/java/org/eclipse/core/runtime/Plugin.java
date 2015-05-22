package org.eclipse.core.runtime;

import org.osgi.framework.BundleContext;

/**
 * Created by schaefa on 4/28/15.
 */
@Deprecated
public class Plugin {

    BundleContext bundleContext;

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void start(BundleContext context) throws Exception {
        bundleContext = context;
    }

    public void stop(BundleContext context) throws Exception {
        bundleContext = null;
    }
}
