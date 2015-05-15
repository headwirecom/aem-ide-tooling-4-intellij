package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;

/**
 * Created by schaefa on 5/14/15.
 */
public class IServer {

    public static final int PUBLISH_INCREMENTAL = 1;

    private ServerConfiguration serverConfiguration;

    public IServer(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public Object loadAdapter(Class clazz, IProgressMonitor monitor) {
        if(clazz == SlingLaunchpadServer.class) {
            return new ISlingLaunchpadServer(serverConfiguration);
        }
        throw new UnsupportedOperationException("Not yet implemented");
//        return null;
    }

    public String getHost() {
        return serverConfiguration.getHost();
    }

    public int getAttribute(String propAutoPublishSetting, int autoPublishResource) {
        throw new UnsupportedOperationException("Not yet implemented");
//        return 0;
    }

    public void publish(int publishIncremental, IProgressMonitor monitorOrNull) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public IModule[] getModules() {
        throw new UnsupportedOperationException("Not yet implemented");
//        return new IModule[0];
    }
}
