package com.headwire.aem.tooling.intellij.config;

import com.intellij.openapi.components.PersistentStateComponent;
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

    public static final String DEFAULT_DESCRIPTION = "No Description";
    public static final String DEFAULT_NAME = "Default Configuration";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_CONNECTION_PORT = 4052;
    public static final int DEFAULT_DEBUG_CONNECTION_PORT = 30303;
    public static final String DEFAULT_USER_NAME = "admin";
    public static final String DEFAULT_CONTEXT_PATH = "/";
    public static final int DEFAULT_START_CONNECTION_TIMEOUT_IN_SECONDS = 30;
    public static final int DEFAULT_STOP_CONNECTION_TIMEOUT_IN_SECONDS = 15;
    public static final PublishType DEFAULT_PUBLISH_TYPE = PublishType.automaticallyOnChange;
    public static final InstallationType DEFAULT_INSTALL_TYPE = InstallationType.installViaBundleUpload;
    public static final ServerStatus DEFAULT_SERVER_STATUS = ServerStatus.notConnected;

    protected static final String COMPONENT_NAME = "ServerConfiguration";

    public enum PublishType {never, automaticallyOnChange, getAutomaticallyOnBuild};
    public enum InstallationType {installViaBundleUpload, installFromFilesystem};
    public enum ServerStatus {
        notConnected("not connected"), connecting, connected, disconnecting;

        private String name;

        ServerStatus() {
            this.name = this.name();
        }

        ServerStatus(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    };

    private String name = "";
    private String host = "";
    private String description = "";
    private int connectionPort = DEFAULT_CONNECTION_PORT;
    private int connectionDebugPort = DEFAULT_DEBUG_CONNECTION_PORT;
    private String userName = DEFAULT_USER_NAME;
    private char[] password = DEFAULT_USER_NAME.toCharArray();
    private String contextPath = DEFAULT_CONTEXT_PATH;
    private int startConnectionTimeout = DEFAULT_START_CONNECTION_TIMEOUT_IN_SECONDS;
    private int stopConnectionTimeout = DEFAULT_STOP_CONNECTION_TIMEOUT_IN_SECONDS;
    private PublishType publishType = DEFAULT_PUBLISH_TYPE;
    private InstallationType installationType = DEFAULT_INSTALL_TYPE;

    private ServerStatus serverStatus = DEFAULT_SERVER_STATUS;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.isNotBlank(name) ? name : DEFAULT_NAME;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = StringUtils.isNotBlank(host) ? host : DEFAULT_HOST;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.isNotBlank(description) ? description : DEFAULT_DESCRIPTION;
    }

    public int getConnectionPort() {
        return connectionPort;
    }

    public void setConnectionPort(int connectionPort) {
        this.connectionPort = connectionPort > 0 ? connectionPort : DEFAULT_CONNECTION_PORT;
    }

    public int getConnectionDebugPort() {
        return connectionDebugPort;
    }

    public void setConnectionDebugPort(int connectionDebugPort) {
        this.connectionDebugPort = connectionDebugPort > 0 ? connectionDebugPort : DEFAULT_DEBUG_CONNECTION_PORT;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = StringUtils.isNotBlank(userName) ? userName : DEFAULT_USER_NAME;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password == null || password.length == 0 ? DEFAULT_USER_NAME.toCharArray() : password;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = StringUtils.isNotBlank(contextPath) ? description : DEFAULT_CONTEXT_PATH;
    }

    public int getStartConnectionTimeoutInSeconds() {
        return startConnectionTimeout;
    }

    public void setStartConnectionTimeoutInSeconds(int startConnectionTimeoutInSeconds) {
        this.startConnectionTimeout = startConnectionTimeoutInSeconds > 0 ?
            startConnectionTimeoutInSeconds :
            DEFAULT_START_CONNECTION_TIMEOUT_IN_SECONDS;
    }

    public int getStopConnectionTimeoutInSeconds() {
        return stopConnectionTimeout;
    }

    public void setStopConnectionTimeoutInSeconds(int stopConnectionTimeoutInSeconds) {
        this.stopConnectionTimeout = stopConnectionTimeoutInSeconds > 0 ?
            stopConnectionTimeoutInSeconds :
            DEFAULT_STOP_CONNECTION_TIMEOUT_IN_SECONDS;
    }

    public PublishType getPublishType() {
        return publishType;
    }

    public void setPublishType(PublishType publishType) {
        this.publishType = publishType != null ? publishType : DEFAULT_PUBLISH_TYPE;
    }

    public InstallationType getInstallationType() {
        return installationType;
    }

    public void setInstallationType(InstallationType installationType) {
        this.installationType = installationType != null ? installationType : DEFAULT_INSTALL_TYPE;
    }

    public ServerStatus getServerStatus() { return serverStatus; }

    public void setServerStatus(ServerStatus serverStatus) {
        this.serverStatus = serverStatus != null ? serverStatus : DEFAULT_SERVER_STATUS;
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
