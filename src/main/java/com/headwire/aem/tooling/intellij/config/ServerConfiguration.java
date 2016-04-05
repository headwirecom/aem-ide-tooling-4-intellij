/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.config;

import com.headwire.aem.tooling.intellij.io.SlingProject4IntelliJ;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.io.SlingProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas Schaefer (Headwire.com) on 3/19/15.
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
    public static final int DEFAULT_CONNECTION_PORT = 4502;
    public static final int DEFAULT_DEBUG_CONNECTION_PORT = 30303;
    public static final String DEFAULT_USER_NAME = "admin";
    public static final String DEFAULT_CONTEXT_PATH = "/";
    public static final int DEFAULT_START_CONNECTION_TIMEOUT_IN_SECONDS = 30;
    public static final int DEFAULT_STOP_CONNECTION_TIMEOUT_IN_SECONDS = 15;
    public static final PublishType DEFAULT_PUBLISH_TYPE = PublishType.automaticallyOnChange;
    public static final InstallationType DEFAULT_INSTALL_TYPE = InstallationType.installViaBundleUpload;
    public static final DefaultMode DEFAULT_MODE = DefaultMode.none;
    public static final ServerStatus DEFAULT_SERVER_STATUS = ServerStatus.notConnected;
    public static final SynchronizationStatus DEFAULT_SERVER_SYNCHRONIZATION_STATUS = SynchronizationStatus.notChecked;
    public static final boolean DEFAULT_BUILD_WITH_MAVEN = true;
    @Deprecated //AS TODO: Remove later as soon as the Cancel Build Action is implemented
    public static final int DEFAULT_MAVEN_BUILD_TIME_OUT_IN_SECONDS = 0;
    public static final LogFilter DEFAULT_LOG_FILTER = LogFilter.error;

    protected static final String COMPONENT_NAME = "ServerConfiguration";

    public enum PublishType {never, automaticallyOnChange, automaticallyOnBuild};
    public enum InstallationType {installViaBundleUpload, installFromFilesystem};
    public enum SynchronizationStatus {
        /** Module was not checked against Sling server **/
        notChecked("not running"),
        /** Module Deployment or Synchronization failed **/
        failed,
        /** Module Deployment maybe compromised **/
        compromised,
        /** Bundle was successfully deployed **/
        bundleDeployed("deployed"),
        /** Content Module is synchronized with Sling server **/
        upToDate("synchronized"),
        /** Content Module is out of data (needs synchronization) **/
        outdated("out of date"),
        /** Bundle isn't deployed yet **/
        notDeployed("not deployed"),
        /** Module is not part of the build **/
        excluded,
        /** Module is not supported but is still part of the build **/
        unsupported,
        /** **/
        checking,
        /** **/
        updating("synchronizing")
        ;

        private String name;

        SynchronizationStatus() {
            this.name = name();
        }

        SynchronizationStatus(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    }

    public enum ServerStatus {
//        notConnected("not connected"), connecting, connected, disconnecting, disconnected, failed, upToDate("synchronized"), outdated("out dated");
        notConnected("not connected"), connecting, connected, disconnecting, disconnected, checking, running, failed;

        private String name;

        ServerStatus() {
            this.name = name();
        }

        ServerStatus(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    };

    public enum LogFilter {debug, info, warning, error};

    public enum DefaultMode {none, run, debug};

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
    private DefaultMode defaultMode = DEFAULT_MODE;
    private boolean buildWithMaven = DEFAULT_BUILD_WITH_MAVEN;
    @Deprecated //AS TODO: Remove later as soon as the Cancel Build Action is implemented
    private int mavenBuildTimeoutInSeconds = DEFAULT_MAVEN_BUILD_TIME_OUT_IN_SECONDS;
    private LogFilter logFilter = DEFAULT_LOG_FILTER;

    // Don't store Server Status as it is reset when the Configuration is loaded again
    //AS TODO: Not sure about this -> Check if that works
    private transient ServerStatus serverStatus = DEFAULT_SERVER_STATUS;
    private transient SynchronizationStatus synchronizationStatus = DEFAULT_SERVER_SYNCHRONIZATION_STATUS;
//    private transient boolean bound = false;
    private transient boolean booted = false;
    // Modules must be stored because they carry the info if a project is part of the deployment build
    private List<Module> moduleList = new ArrayList<Module>();

    private transient ServerConfigurationManager.ConfigurationChangeListener configurationChangeListener;

    public ServerConfiguration() {}

    /** Copy Constructor **/
    public ServerConfiguration(ServerConfiguration source) {
        if(source != null) {
            copy(source);
        }
    }

    void copy(ServerConfiguration source) {
        name = source.name;
        host = source.host;
        description = source.description;
        defaultMode = source.defaultMode;
        buildWithMaven = source.buildWithMaven;
        mavenBuildTimeoutInSeconds = source.mavenBuildTimeoutInSeconds;
        connectionPort = source.connectionPort;
        connectionDebugPort = source.connectionDebugPort;
        userName = source.userName;
        password = source.password;
        contextPath = source.contextPath;
        startConnectionTimeout = source.startConnectionTimeout;
        stopConnectionTimeout = source.stopConnectionTimeout;
        publishType = source.publishType;
        installationType = source.installationType;
        serverStatus = source.serverStatus;
        if(source.configurationChangeListener != null) {
            configurationChangeListener = source.configurationChangeListener;
        }
    }

    public String verify() {
        String ret = null;
        // First make sure all mandatory fields are provided
        if(StringUtils.isBlank(name)) {
            ret = "server.configuration.missing.name";
        } else if(StringUtils.isBlank(host)) {
            ret = "server.configuration.missing.host";
        } else if(connectionPort <= 0) {
            ret = "server.configuration.invalid.port";
        } else if(connectionDebugPort <= 0 || connectionPort == connectionDebugPort) {
            ret = "server.configuration.invalid.debug.port";
        } else if(mavenBuildTimeoutInSeconds < 0) {
            ret = "server.configuration.invalid.maven.build.timeout";
        } else if(StringUtils.isBlank(userName)) {
            ret = "server.configuration.missing.user.name";
        } else if(StringUtils.isBlank(contextPath)) {
            ret = "server.configuration.missing.context.path";
        } else if(!contextPath.startsWith("/")) {
            ret = "server.configuration.invalid.context.path";
        }
        return ret;
    }

    public void setConfigurationChangeListener(ServerConfigurationManager.ConfigurationChangeListener configurationChangeListener) {
        this.configurationChangeListener = configurationChangeListener;
    }

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
        this.contextPath = StringUtils.isNotBlank(contextPath) ? contextPath : DEFAULT_CONTEXT_PATH;
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
        if(configurationChangeListener != null) { configurationChangeListener.configurationChanged(); }
    }

    public SynchronizationStatus getSynchronizationStatus() {
        return synchronizationStatus;
    }

    public void setSynchronizationStatus(SynchronizationStatus synchronizationStatus) {
        this.synchronizationStatus = synchronizationStatus != null ? synchronizationStatus : DEFAULT_SERVER_SYNCHRONIZATION_STATUS;
        if(configurationChangeListener != null) { configurationChangeListener.configurationChanged(); }
    }

    public LogFilter getLogFilter() {
        return logFilter;
    }

    public void setLogFilter(LogFilter logFilter) {
        this.logFilter = logFilter;
    }

    public DefaultMode getDefaultMode() {
        return defaultMode;
    }

    public void setDefaultMode(DefaultMode defaultMode) {
        this.defaultMode = defaultMode;
    }

    public boolean isBuildWithMaven() {
        return buildWithMaven;
    }

    public void setBuildWithMaven(boolean buildWithMaven) {
        this.buildWithMaven = buildWithMaven;
    }

    public int getMavenBuildTimeoutInSeconds() {
        return mavenBuildTimeoutInSeconds;
    }

    public void setMavenBuildTimeoutInSeconds(int mavenBuildTimeoutInSeconds) {
        this.mavenBuildTimeoutInSeconds = mavenBuildTimeoutInSeconds >= 0 ?
            mavenBuildTimeoutInSeconds :
            DEFAULT_MAVEN_BUILD_TIME_OUT_IN_SECONDS;
    }

    public boolean isBooted() {
        return booted;
    }

    public void setBooted(boolean booted) {
        this.booted = booted;
    }

    @Nullable
    @Override
    public ServerConfiguration getState() {
        return this;
    }

    public List<Module> getModuleList() { return moduleList; }

    @Deprecated
    public Module obtainModuleBySymbolicName(String symbolicName) {
        Module ret = null;
        if(symbolicName != null) {
            for(Module module : moduleList) {
                //AS TODO: Need to figure out who is calling this mehod to know if this is related to OSGi
                if(symbolicName.equals(module.getSymbolicName())) {
                    ret = module;
                    break;
                }
            }
        }
        return ret;
    }

    public Module obtainModuleByName(String moduleName) {
        Module ret = null;
        if(moduleName != null) {
            for(Module module : moduleList) {
                //AS TODO: Need to figure out who is calling this mehod to know if this is related to OSGi
                if(moduleName.equals(module.getName())) {
                    ret = module;
                    break;
                }
            }
        }
        return ret;
    }

    public boolean addModule(Module module) {
        boolean ret = false;
        Module existing = obtainModuleBySymbolicName(name);
        if(existing == null) {
            moduleList.add(module);
            ret = true;
        }
        return ret;
    }

    public boolean removeModule(Module module) {
        boolean ret = false;
        if(moduleList.contains(module)) {
            ret = moduleList.remove(module);
        }
        return ret;
    }

    public Module addModule(Project project, UnifiedModule unifiedModule) {
        Module ret = obtainModuleBySymbolicName(name);
        if(ret == null) {
            ret = new Module(this, project, unifiedModule);
            moduleList.add(ret);
        }
        return ret;
    }

    @Override
    public void loadState(ServerConfiguration serverConfiguration) {
        XmlSerializerUtil.copyBean(serverConfiguration, this);
    }

    public boolean isBound() {
        boolean ret = !moduleList.isEmpty();
        for(Module module: moduleList) {
            ret = ret && module.isBound();
        }
        return ret;
    }

    public void unBind() {
//        bound = false;
        for(Module module: moduleList) {
            module.unBind();
        }
    }

    public static class Module {
        private ServerConfiguration parent;
        private String moduleName;
        private boolean partOfBuild = true;
        private long lastModificationTimestamp;
        private transient Project project;
        private transient SlingProject slingProject;
        private transient UnifiedModule unifiedModule;
        private transient SynchronizationStatus status = SynchronizationStatus.notChecked;
        private transient ServerConfigurationManager.ConfigurationChangeListener configurationChangeListener;
        private transient VirtualFile metaInfFolder;
        private transient VirtualFile filterFile;
        private transient Filter filter;

        public Module(@NotNull ServerConfiguration parent, @NotNull String moduleName, boolean partOfBuild, long lastModificationTimestamp) {
            this.parent = parent;
            this.moduleName = moduleName;
            setPartOfBuild(partOfBuild);
            this.lastModificationTimestamp = lastModificationTimestamp;
            this.configurationChangeListener = parent.configurationChangeListener;
        }

        private Module(@NotNull ServerConfiguration parent, @NotNull Project project, @NotNull UnifiedModule unifiedModule) {
            this.parent = parent;
            this.moduleName = unifiedModule.getModule().getName();
            this.configurationChangeListener = parent.configurationChangeListener;
            rebind(project, unifiedModule);
        }

        public static String getSymbolicName(UnifiedModule project) {
            return project.getSymbolicName();
        }

        public boolean isPartOfBuild() {
            return partOfBuild;
        }

        public void setPartOfBuild(boolean partOfBuild) {
            if(this.partOfBuild) {
                if(!partOfBuild) {
                    this.partOfBuild = false;
                    status = SynchronizationStatus.excluded;
                }
            } else {
                if(partOfBuild) {
                    this.partOfBuild = true;
                    status = SynchronizationStatus.notChecked;
                }
            }
        }

        public ServerConfiguration getParent() {
            return parent;
        }

        public String getSymbolicName() {
            return unifiedModule == null ? "No Project" : unifiedModule.getSymbolicName();
        }

        public String getName() {
            return unifiedModule == null ? moduleName : unifiedModule.getName();
        }

        public long getLastModificationTimestamp() {
            return lastModificationTimestamp;
        }

        public void setLastModificationTimestamp(long lastModificationTimestamp) {
            if(lastModificationTimestamp > this.lastModificationTimestamp) {
                this.lastModificationTimestamp = lastModificationTimestamp;
            }
        }

        public String getVersion() {
            return unifiedModule == null ? "No Project" : unifiedModule.getVersion();
        }

        public Project getProject() {
            return project;
        }

        public SlingProject getSlingProject() {
            return slingProject;
        }

        public UnifiedModule getUnifiedModule() {
            return unifiedModule;
        }

        public SynchronizationStatus getStatus() {
            return status;
        }

        public VirtualFile getMetaInfFolder() {
            return metaInfFolder;
        }

        public void setMetaInfFolder(VirtualFile metaInfFolder) {
            this.metaInfFolder = metaInfFolder;
        }

        public VirtualFile getFilterFile() {
            return filterFile;
        }

        public void setFilterFile(VirtualFile filterFile) {
            this.filterFile = filterFile;
        }

        public Filter getFilter() {
            return filter;
        }

        public void setFilter(Filter filter) {
            this.filter = filter;
        }

        public boolean isOSGiBundle() {
            return unifiedModule != null && unifiedModule.isOSGiBundle();
        }

        public boolean isSlingPackage() {
            return unifiedModule != null && unifiedModule.isContent();
        }

        public boolean isBound() {
            return unifiedModule != null;
        }

        public void unBind() {
            // First remove this Module from the Module Context
            if(unifiedModule != null) {
                unifiedModule.removeServerConfigurationModule(this);
            }
            unifiedModule = null;
        }

        public boolean rebind(@NotNull Project project, @NotNull UnifiedModule unifiedModule) {
            boolean ret = false;
            // Check if the Module Name match and issue a warning if not otherwise proceed
            String moduleName = unifiedModule.getName();
            if(!this.moduleName.equals(moduleName)) {
                //AS TODO: Warn about the mismatch in the Modules
            }
            this.project = project;
            this.unifiedModule = unifiedModule;
            this.unifiedModule.addServerConfigurationModule(this);
            if(!isOSGiBundle() && !isSlingPackage()) {
                setStatus(SynchronizationStatus.unsupported);
            } else {
                setStatus(SynchronizationStatus.notChecked);
            }
            if(configurationChangeListener != null) {
                configurationChangeListener.configurationChanged();
            }
            this.slingProject = new SlingProject4IntelliJ(this);
            ret = true;
            String metaInfPath = unifiedModule.getMetaInfPath();
            if(metaInfPath != null) {
                VirtualFile metaInfFolder = project.getBaseDir().getFileSystem().findFileByPath(metaInfPath);
                if(metaInfFolder != null) {
                    setMetaInfFolder(metaInfFolder);
                }
            }
            return ret;
        }

        public void setStatus(SynchronizationStatus status) {
            if(status != null) {
                this.status = status;
                if(configurationChangeListener != null) { configurationChangeListener.configurationChanged(); }
            }
        }

        @Override
        public String toString() {
            return "Module: " +
                "module name = '" + moduleName + '\'' +
                ", last modification timestamp = " + lastModificationTimestamp +
                ", part of build = " + partOfBuild +
                "";
        }
    }
}
