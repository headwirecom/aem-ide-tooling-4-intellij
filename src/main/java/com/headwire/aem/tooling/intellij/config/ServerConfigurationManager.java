/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.config;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.EventDispatcher;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.DefaultMode;
/**
 * The Server Configuration Manager responsible for Loading & Saving the Server Configurations into the Workspace File
 * inside the IDEA folder (.idea/workspace.xml) and to provide the configurations to the plugin.
 *
 * Created by schaefa on 3/19/15.
 */
@State(
    name = ServerConfiguration.COMPONENT_NAME,
    storages = {
        @Storage(id = "serverConfigurations", file = StoragePathMacros.WORKSPACE_FILE)
    }
)
public class ServerConfigurationManager
    extends AbstractProjectComponent
    implements PersistentStateComponent<Element>
{

    private static final Logger LOGGER = Logger.getInstance(ServerConfigurationManager.class);
    public static final String LOG_FILTER = "logFilter";
    public static final String NAME = "name";
    public static final String HOST = "host";
    public static final String DESCRIPTION = "description";
    public static final String CONNECTION_PORT = "connectionPort";
    public static final String CONNECTION_DEBUG_PORT = "connectionDebugPort";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String CONTEXT_PATH = "contextPath";
    public static final String START_CONNECTION_TIMEOUT = "startConnectionTimeout";
    public static final String STOP_CONNECTION_TIMEOUT = "stopConnectionTimeout";
    public static final String PUBLISH_TYPE = "publishType";
    public static final String INSTALLATION_TYPE = "installationType";
    //AS TODO: 'default' is just here to be backwards compatible -> delete later
    public static final String DEFAULT = "default";
    public static final String DEFAULT_CONFIGURATION = "defaultConfiguration";
    public static final String BUILD_WITH_MAVEN = "buildWithMaven";
    @Deprecated
    public static final String BUILD_WITH_MAVEN_TIMEOUT_IN_SECONDS = "buildWithMavenTimeoutInSecond";
    public static final String ARTIFACT_ID = "artifactId";
    public static final String SYMBOLIC_NAME = "symbolicName";
    public static final String PART_OF_BUILD = "partOfBuild";
    public static final String LAST_MODIFICATION_TIMESTAMP = "lastModificationTimestamp";

    private MessageManager messageManager;
    private ServerConnectionManager serverConnectionManager;
    private final EventDispatcher<ConfigurationListener> myEventDispatcher = EventDispatcher.create(ConfigurationListener.class);
    private List<ServerConfiguration> serverConfigurationList = new ArrayList<ServerConfiguration>();

    public boolean updateCurrentServerConfiguration() {
        boolean ret = false;
        if(serverConnectionManager == null) {
            serverConnectionManager = myProject.getComponent(ServerConnectionManager.class);
        }
        if(serverConnectionManager != null) {
            // A Server Connection may or may not be connected so the only way to ensure a proper update is to update them all
            for(ServerConfiguration serverConfiguration: serverConfigurationList) {
                // Clear any bindings
                serverConfiguration.unBind();
                serverConnectionManager.bindModules(serverConfiguration);
            }
            ret = true;
        }
        return ret;
    }

    public class ConfigurationChangeListener {
        public void configurationChanged() {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                myEventDispatcher.getMulticaster().configurationLoaded();
                }
            });
        }
    }

    private ConfigurationChangeListener configurationChangeListener = new ConfigurationChangeListener();

    public ServerConfigurationManager(final Project project) {
        super(project);
        messageManager = project.getComponent(MessageManager.class);
    }

    public ServerConfiguration[] getServerConfigurations() {
        return serverConfigurationList.toArray(new ServerConfiguration[] {});
    }

    public int serverConfigurationSize() {
        return serverConfigurationList.size();
    }

    public ServerConfiguration findConnectedServerConfiguration() {
        ServerConfiguration ret = null;
        for(ServerConfiguration serverConfiguration: serverConfigurationList) {
            if(
                serverConfiguration.getServerStatus() == ServerConfiguration.ServerStatus.connected ||
                serverConfiguration.getServerStatus() == ServerConfiguration.ServerStatus.running
            ) {
                ret = serverConfiguration;
                break;
            }
        }
        return ret;
    }

    public ServerConfiguration findServerConfigurationByName(String configurationName) {
        ServerConfiguration ret = null;
        for(ServerConfiguration serverConfiguration: serverConfigurationList) {
            if(serverConfiguration.getName().equals(configurationName)) {
                ret = serverConfiguration;
                break;
            }
        }
        return ret;
    }

    public void addServerConfiguration(ServerConfiguration serverConfiguration) {
        String name = serverConfiguration.getName();
        if(findServerConfigurationByName(name) != null) {
            throw new IllegalArgumentException("Duplicate Name: " + name);
        }
        serverConfigurationList.add(serverConfiguration);
        myEventDispatcher.getMulticaster().configurationLoaded();
    }

    public void removeServerConfiguration(ServerConfiguration serverConfiguration) {
        ServerConfiguration configuration = findServerConfigurationByName(serverConfiguration.getName());
        if(configuration != null) {
            serverConfigurationList.remove(configuration);
        }
        myEventDispatcher.getMulticaster().configurationLoaded();
    }

    /** @param serverConfiguration Update Server Configuration. Attention: the name cannot have changed here **/
    public void updateServerConfiguration(ServerConfiguration serverConfiguration) {
        ServerConfiguration configuration = findServerConfigurationByName(serverConfiguration.getName());
        if(configuration != null && configuration != serverConfiguration) {
            configuration.copy(serverConfiguration);
        }
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                myEventDispatcher.getMulticaster().configurationLoaded();
            }
        });
    }

    /**
     *  @param previous Existing Server Configuration if there is a chance that the name changed
     *  @param current new Server Configuration which can have the name changed
     **/
    public void updateServerConfiguration(ServerConfiguration previous, ServerConfiguration current) {
        ServerConfiguration configuration = findServerConfigurationByName(previous.getName());
        if(configuration != null) {
            configuration.copy(current);
        }
        myEventDispatcher.getMulticaster().configurationLoaded();
    }

    private volatile Boolean myIsInitialized = null;

    public boolean isInitialized() {
        final Boolean initialized = myIsInitialized;
        return initialized == null || initialized.booleanValue();
    }

    // ------ Project Component

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
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
        return "Server Configuration Manager";
    }

    // -------------------- state persistence

    public Element getState() {
        LOGGER.debug("SCM.getState(), start");
        Element root = new Element("state");
        int i = 0;
        for(ServerConfiguration serverConfiguration: serverConfigurationList) {
            Element childNode = new Element("ssc-" + i++);
            childNode.setAttribute(NAME, serverConfiguration.getName());
            childNode.setAttribute(HOST, serverConfiguration.getHost());
            childNode.setAttribute(DESCRIPTION, serverConfiguration.getDescription());
            childNode.setAttribute(CONNECTION_PORT, serverConfiguration.getConnectionPort() + "");
            childNode.setAttribute(CONNECTION_DEBUG_PORT, serverConfiguration.getConnectionDebugPort() + "");
            childNode.setAttribute(USER_NAME, serverConfiguration.getUserName());
            //AS TODO: Can we store that in an encrypted form?
            childNode.setAttribute(PASSWORD, new String(serverConfiguration.getPassword()));
            childNode.setAttribute(CONTEXT_PATH, serverConfiguration.getContextPath());
            childNode.setAttribute(START_CONNECTION_TIMEOUT, serverConfiguration.getStartConnectionTimeoutInSeconds() + "");
            childNode.setAttribute(STOP_CONNECTION_TIMEOUT, serverConfiguration.getStopConnectionTimeoutInSeconds() + "");
            childNode.setAttribute(PUBLISH_TYPE, serverConfiguration.getPublishType() + "");
            childNode.setAttribute(INSTALLATION_TYPE, serverConfiguration.getInstallationType() + "");
            childNode.setAttribute(DEFAULT_CONFIGURATION, serverConfiguration.getDefaultMode() + "");
            childNode.setAttribute(BUILD_WITH_MAVEN, serverConfiguration.isBuildWithMaven() + "");
            childNode.setAttribute(BUILD_WITH_MAVEN_TIMEOUT_IN_SECONDS, serverConfiguration.getMavenBuildTimeoutInSeconds() + "");
            childNode.setAttribute(LOG_FILTER, serverConfiguration.getLogFilter() + "");
            int j = 0;
            for(ServerConfiguration.Module module: serverConfiguration.getModuleList()) {
                Element moduleChildNode = new Element("sscm-" + j++);
//                moduleChildNode.setAttribute(ARTIFACT_ID, module.getArtifactId());
                moduleChildNode.setAttribute(SYMBOLIC_NAME, module.getSymbolicName());
                moduleChildNode.setAttribute(PART_OF_BUILD, module.isPartOfBuild() + "");
                moduleChildNode.setAttribute(LAST_MODIFICATION_TIMESTAMP, module.getLastModificationTimestamp() + "");
                childNode.addContent(moduleChildNode);
            }
            root.addContent(childNode);
        }
        LOGGER.debug("SCM.getState(), end -> returns: " + root);
        return root;
    }

    public void loadState(final Element state) {
        LOGGER.debug("SCM.loadState(), start");
        myIsInitialized = Boolean.FALSE;
        List<Element> elementList = state.getChildren();
        serverConfigurationList.clear();
        for(Element child: elementList) {
            ServerConfiguration serverConfiguration = new ServerConfiguration();
            serverConfiguration.setName(child.getAttributeValue(NAME));
            serverConfiguration.setHost(child.getAttributeValue(HOST));
            serverConfiguration.setDescription(child.getAttributeValue(DESCRIPTION));
            serverConfiguration.setConnectionPort(Util.convertToInt(child.getAttributeValue(CONNECTION_PORT), 0));
            serverConfiguration.setConnectionDebugPort(Util.convertToInt(child.getAttributeValue(CONNECTION_DEBUG_PORT), 0));
            serverConfiguration.setUserName(child.getAttributeValue(USER_NAME));
            serverConfiguration.setPassword(child.getAttributeValue(PASSWORD).toCharArray());
            serverConfiguration.setContextPath(child.getAttributeValue(CONTEXT_PATH));
            serverConfiguration.setStartConnectionTimeoutInSeconds(Util.convertToInt(child.getAttributeValue(START_CONNECTION_TIMEOUT), -1));
            serverConfiguration.setStopConnectionTimeoutInSeconds(Util.convertToInt(child.getAttributeValue(STOP_CONNECTION_TIMEOUT), -1));
            serverConfiguration.setPublishType(Util.convertToEnum(child.getAttributeValue(PUBLISH_TYPE), ServerConfiguration.DEFAULT_PUBLISH_TYPE));
            serverConfiguration.setInstallationType(Util.convertToEnum(child.getAttributeValue(INSTALLATION_TYPE), ServerConfiguration.DEFAULT_INSTALL_TYPE));
            serverConfiguration.setConfigurationChangeListener(configurationChangeListener);
            DefaultMode defaultMode = Util.convertToEnum(child.getAttributeValue(DEFAULT_CONFIGURATION), ServerConfiguration.DEFAULT_MODE);
            //AS TODO: This is only necessary for backward compatibility for previous versions -> remove later
            if(defaultMode == ServerConfiguration.DEFAULT_MODE) {
                String oldDefaultValue = child.getAttributeValue(DEFAULT);
                if ("true".equalsIgnoreCase(oldDefaultValue)) {
                    defaultMode = DefaultMode.run;
                }
            }
            if(defaultMode != DefaultMode.none) {
                // Check if no other configuration is already the default
                for(ServerConfiguration serverConfiguration1 : serverConfigurationList) {
                    if(serverConfiguration1.getDefaultMode() != DefaultMode.none) {
                        messageManager.sendErrorNotification("server.configuration.multiple.default", serverConfiguration.getName(), serverConfiguration1.getName());
                        defaultMode = DefaultMode.none;
                        break;
                    }
                }
            }
            serverConfiguration.setDefaultMode(defaultMode);
            serverConfiguration.setBuildWithMaven(new Boolean(child.getAttributeValue(BUILD_WITH_MAVEN, "true")));
            serverConfiguration.setMavenBuildTimeoutInSeconds(new Integer(child.getAttributeValue(BUILD_WITH_MAVEN_TIMEOUT_IN_SECONDS, ServerConfiguration.DEFAULT_MAVEN_BUILD_TIME_OUT_IN_SECONDS + "")));
            serverConfiguration.setLogFilter(Util.convertToEnum(child.getAttributeValue(LOG_FILTER), ServerConfiguration.DEFAULT_LOG_FILTER));
            for(Element element: child.getChildren()) {
                try {
//                    String artifactId = element.getAttributeValue(ARTIFACT_ID, "No Artifact Id");
                    String symbolicName = element.getAttributeValue(SYMBOLIC_NAME, "");
                    boolean isPartOfBuild = new Boolean(element.getAttributeValue(PART_OF_BUILD, "true"));
                    long lastModificationTimestamp = new Long(element.getAttributeValue(LAST_MODIFICATION_TIMESTAMP, "-1"));
                    ServerConfiguration.Module module = new ServerConfiguration.Module(serverConfiguration, symbolicName, isPartOfBuild, lastModificationTimestamp);
                    serverConfiguration.addModule(module);
                } catch(Exception e) {
                    // Ignore any exceptions to avoid a stall configurations
                }
            }
            LOGGER.debug("SCM.loadState(), add Server Configuration: " + serverConfiguration);
            serverConfigurationList.add(serverConfiguration);
        }
        myIsInitialized = Boolean.TRUE;
        final String title = AEMBundle.message("tree.builder.configurations.loading.name");
        queueLater(
            new Task.Backgroundable(myProject, title, false) {
                public void run(@NotNull final ProgressIndicator indicator) {
                    if (getProject().isDisposed()) {
                        return;
                    }
                    indicator.setIndeterminate(true);
                    indicator.pushState();
                    try {
                        indicator.setText(title);
                        ApplicationManager.getApplication().runReadAction(
                            new Runnable() {
                                public void run() {
                                    if(myEventDispatcher.getMulticaster() != null) {
                                        myEventDispatcher.getMulticaster().configurationLoaded();
                                    }
                                }
                            }
                        );
                    } finally {

                    }
                }
            }
        );
    }

    public void addConfigurationListener(ConfigurationListener myConfigurationListener) {
        //AS TODO: This class is loaded way ahead and so we fire a configuration listener is none are there
        boolean first = myEventDispatcher.getListeners().isEmpty();
        myEventDispatcher.addListener(myConfigurationListener);
        if(first) {
            myEventDispatcher.getMulticaster().configurationLoaded();
        }
    }

    private static void queueLater(final Task task) {
        final Application app = ApplicationManager.getApplication();
        if (app.isDispatchThread()) {
            task.queue();
        } else {
            app.invokeLater(new Runnable() {
                public void run() {
                    task.queue();
                }
            });
        }
    }

}
