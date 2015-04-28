package com.headwire.aem.tooling.intellij.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Created by schaefa on 3/19/15.
 */
//@State(
//name = "SlingServerConfiguration",
//    storages = {
//        @Storage(file = StoragePathMacros.WORKSPACE_FILE),
//        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/slingServerConfiguration.xml", scheme = StorageScheme.DIRECTORY_BASED)
//    }
//)
public class ServerConfiguration
    implements PersistentStateComponent<ServerConfiguration>
{

    protected static final String COMPONENT_NAME = "ServerConfiguration";

    private String serverName = "";
    private String hostName = "";
    private int runtimeEnvironment = 0;
    private String configurationPath = "";
    private String serverStatus = "";
    private String description = "";

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = StringUtils.isNotBlank(serverName) ? serverName : "";
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = StringUtils.isNotBlank(hostName) ? hostName : "";
    }

    public int getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    public void setRuntimeEnvironment(int runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public String getConfigurationPath() {
        return configurationPath;
    }

    public void setConfigurationPath(String configurationPath) {
        this.configurationPath = StringUtils.isNotBlank(configurationPath) ? configurationPath : "";
    }

    public String getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(String serverStatus) {
        this.serverStatus = StringUtils.isNotBlank(serverStatus) ? serverStatus : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.isNotBlank(description) ? description : "No Description";
    }

    @Nullable
    @Override
    public ServerConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(ServerConfiguration serverConfiguration) {
        XmlSerializerUtil.copyBean(serverConfiguration, this);
    }
}
