package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;

/**
 * Created by schaefa on 5/14/15.
 */
public class ISlingLaunchpadServer {

    public static final String PROP_INSTALL_LOCALLY = "";

    private ServerConfiguration serverConfiguration;
    private ISlingLaunchpadConfiguration slingLaunchpadConfiguration;

    public ISlingLaunchpadServer(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        this.slingLaunchpadConfiguration = new ISlingLaunchpadConfiguration(this.serverConfiguration);
    }

    public ISlingLaunchpadConfiguration getConfiguration() {
        return slingLaunchpadConfiguration;
    }
}
