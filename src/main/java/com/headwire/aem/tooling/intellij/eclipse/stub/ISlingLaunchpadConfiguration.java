package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;

/**
 * Created by schaefa on 5/14/15.
 */
public class ISlingLaunchpadConfiguration {

    private ServerConfiguration serverConfiguration;

    public ISlingLaunchpadConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public int getPort() {
        return serverConfiguration.getConnectionPort();
    }

    public String getContextPath() {
        return serverConfiguration.getContextPath();
    }

    public String getUsername() {
        return serverConfiguration.getUserName();
    }

    public String getPassword() {
        return new String(serverConfiguration.getPassword());
    }
}
