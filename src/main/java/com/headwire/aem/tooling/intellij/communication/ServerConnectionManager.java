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

package com.headwire.aem.tooling.intellij.communication;

import com.github.rjeschke.txtmark.Run;
import com.headwire.aem.tooling.intellij.communication.IntelliJDeploymentManager.IntelliJFileWrapper;
import com.headwire.aem.tooling.intellij.communication.IntelliJDeploymentManager.IntelliJModuleWrapper;
import com.headwire.aem.tooling.intellij.config.ModuleProject;
import com.headwire.aem.tooling.intellij.config.ModuleProjectFactory;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;

//AS TODO: We should use Eclipse Stuff here -> find a way to make this IDE independent
import com.headwire.aem.tooling.intellij.eclipse.ServerUtil;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;

//AS TODO: We should use Eclipse Stuff here -> find a way to make this IDE independent
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;

import com.headwire.aem.tooling.intellij.explorer.RunExecutionMonitor;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.io.SlingResource4IntelliJ;
import com.headwire.aem.tooling.intellij.util.BundleStateHelper;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.KillableProcess;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.artifacts.EmbeddedArtifact;
import org.apache.sling.ide.artifacts.EmbeddedArtifactLocator;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.io.ConnectorException;
import org.apache.sling.ide.io.NewResourceChangeCommandFactory;
import org.apache.sling.ide.io.SlingResource;
import org.apache.sling.ide.osgi.OsgiClient;
import org.apache.sling.ide.osgi.OsgiClientException;
import org.apache.sling.ide.serialization.SerializationException;
import org.apache.sling.ide.serialization.SerializationManager;
import org.apache.sling.ide.transport.Command;
import org.apache.sling.ide.transport.Repository;
import org.apache.sling.ide.transport.RepositoryException;
import org.apache.sling.ide.transport.RepositoryInfo;
import org.apache.sling.ide.transport.ResourceProxy;
import org.apache.sling.ide.transport.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.model.MavenExplicitProfiles;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenDataKeys;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;
import org.osgi.framework.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;
import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;

/**
 * Handles the Server Connections for the Plugin, its state and flags
 *
 * Created by schaefa on 5/21/15.
 */
public class ServerConnectionManager
    extends AbstractProjectComponent
{

    private static List<ServerConfiguration.ServerStatus> CONFIGURATION_CHECKED = Arrays.asList(
        ServerConfiguration.ServerStatus.checking,
        ServerConfiguration.ServerStatus.running
    );

    private static List<ServerConfiguration.ServerStatus> CONFIGURATION_IN_USE = Arrays.asList(
        ServerConfiguration.ServerStatus.connecting,
        ServerConfiguration.ServerStatus.connected,
        ServerConfiguration.ServerStatus.disconnecting,
        ServerConfiguration.ServerStatus.checking,
        ServerConfiguration.ServerStatus.running
    );

    private ServerTreeSelectionHandler selectionHandler;
    private MessageManager messageManager;
    private ServerConfigurationManager serverConfigurationManager;
//    private NewResourceChangeCommandFactory commandFactory;
    private IntelliJDeploymentManager deploymentManager;

    private static boolean firstRun = true;

    public ServerConnectionManager(@NotNull Project project) {
        super(project);
        messageManager = myProject.getComponent(MessageManager.class);
        serverConfigurationManager = myProject.getComponent(ServerConfigurationManager.class);
        deploymentManager = new IntelliJDeploymentManager(project);
    }

    public void init(@NotNull ServerTreeSelectionHandler serverTreeSelectionHandler) {
        selectionHandler = serverTreeSelectionHandler;
    }

    // ----- Server State Flags

    public boolean isConfigurationEditable() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        return serverConfiguration != null &&
              !CONFIGURATION_IN_USE.contains(serverConfiguration.getServerStatus())
            ;
    }

    public boolean isConnectionInUse() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        return serverConfiguration != null && CONFIGURATION_IN_USE.contains(serverConfiguration.getServerStatus());
    }

    public boolean isConnectionIsStoppable() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        return serverConfiguration != null &&
              CONFIGURATION_IN_USE.contains(serverConfiguration.getServerStatus())
            ;
    }

    public boolean isConnectionNotInUse() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        return serverConfiguration != null && !CONFIGURATION_IN_USE.contains(serverConfiguration.getServerStatus());
    }

    public boolean isConfigurationSelected() {
        return selectionHandler.getCurrentConfiguration() != null;
    }

    public void checkModules(OsgiClient osgiClient) {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        if(serverConfiguration != null) {
            ServerConfiguration.SynchronizationStatus status = ServerConfiguration.SynchronizationStatus.upToDate;
            boolean allSynchronized = true;
            if(checkBinding(serverConfiguration)) {
                int moduleCount = serverConfiguration.getModuleList().size();
                float steps = (float) (0.9 / moduleCount);
                for(Module module : serverConfiguration.getModuleList()) {
                    boolean sync = checkModule(osgiClient, module);
                    // Any out of sync marks the project out of sync
                    if(!sync) {
                        status = ServerConfiguration.SynchronizationStatus.outdated;
                    }
                }
            } else {
                status = ServerConfiguration.SynchronizationStatus.failed;
            }
            updateStatus(serverConfiguration.getName(), status);
        } else {
            messageManager.sendNotification("aem.explorer.check.modules.no.configuration.selected", NotificationType.WARNING);
        }
    }

    public boolean checkModule(@NotNull OsgiClient osgiClient, @NotNull Module module) {
        boolean ret = true;
        try {
            if(module.isPartOfBuild()) {
                // Check Binding
                if(checkBinding(module.getParent())) {
                    ModuleProject moduleProject = module.getModuleProject();
                    if(moduleProject != null) {
                        String moduleName = moduleProject.getName();
                        String artifactId = moduleProject.getArtifactId();
                        String version = moduleProject.getVersion();
                        version = checkBundleVersion(version);
                        updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.checking);
                        if(module.isOSGiBundle()) {
                            Version remoteVersion = osgiClient.getBundleVersion(module.getSymbolicName());
                            Version localVersion = new Version(version);
                            messageManager.sendDebugNotification("Check OSGi Module: '" + moduleName + "', artifact id: '" + artifactId + "', version: '" + remoteVersion + "' vs. '" + localVersion + "'");
                            boolean moduleUpToDate = remoteVersion != null && remoteVersion.compareTo(localVersion) >= 0;
                            Object state = BundleStateHelper.getBundleState(module);
                            messageManager.sendDebugNotification("Bundle State of Module: '" + module.getName() + "', state: '" + state + "'");
                            if(remoteVersion == null) {
                                // Mark as not deployed yet
                                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.notDeployed);
                                ret = false;
                            } else if(moduleUpToDate) {
                                // Mark as synchronized
                                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.upToDate);
                            } else {
                                // Mark as out of date
                                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.outdated);
                                ret = false;
                            }
                        } else if(module.isSlingPackage()) {
                            long lastModificationTimestamp = getLastModificationTimestamp(module);
                            long moduleModificationTimestamp = module.getLastModificationTimestamp();
                            if(lastModificationTimestamp > moduleModificationTimestamp) {
                                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.outdated);
                                ret = false;
                            } else {
                                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.upToDate);
                            }
                        } else {
                            updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.unsupported);
                        }
                    } else {
                        updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.failed);
                        ret = false;
                    }
                } else {
                    updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.failed);
                    ret = false;
                }
            } else {
                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.excluded);
            }
        } catch(OsgiClientException e1) {
            // Mark connection as failed
            updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.failed);
            ret = false;
        }
        return ret;
    }

    private String checkBundleVersion(String version) {
        String ret = "";
        // Versions need to be in this format n.n.n(-|_)aaaaa where n is a number and a are alphanumeric characters
        // If a version number is missing we need to add and superfluous numbers need to be removed
        int separator = version.indexOf('-');
        if(separator < 0) {
            separator = version.indexOf("-");
        }
        String qualifier = "";
        if(separator >= 0) {
            qualifier = "." + (separator < version.length() - 1 ? version.substring(separator + 1) : "");
            version = version.substring(0, separator);
        }
        String[] tokens = version.split("\\.");
        ArrayList<String> tokenList = new ArrayList<String>(Arrays.asList(tokens));
        while(tokenList.size() < 3) {
            tokenList.add("0");
        }
        while(tokenList.size() > 3) {
            tokenList.remove(tokenList.size() - 1);
        }
        // Build version
        for(String token: tokenList) {
            ret += token + ".";
        }
        ret = ret.substring(0, ret.length() - 1);
        ret += qualifier;

        return ret;
    }

    /**
     * Binding is the process of connecting the Project's Modules with the Maven Modules (its sub projects)
     *
     * @param serverConfiguration The Server Connection that is checked and bound if not already done
     * @return True if the the connection was successfully bound otherwise false
     */
    public boolean checkBinding(@NotNull ServerConfiguration serverConfiguration) {
        if(!serverConfiguration.isBound()) {
            List<Module> moduleList = bindModules(serverConfiguration);
            return moduleList.isEmpty();
        }
        return true;
    }

    public List<Module> bindModules(@NotNull ServerConfiguration serverConfiguration) {
        List<ModuleProject> moduleProjects = ModuleProjectFactory.getProjectModules(myProject);
        List<Module> moduleList = new ArrayList<Module>(serverConfiguration.getModuleList());
        for(ModuleProject moduleProject : moduleProjects) {
            String moduleName = moduleProject.getName();
            String artifactId = moduleProject.getArtifactId();
            String version = moduleProject.getVersion();
            // Check if this Module is listed in the Module Sub Tree of the Configuration. If not add it.
            messageManager.sendDebugNotification("Check Binding for Maven Module: '" + moduleName + "', artifact id: '" + artifactId + "', version: '" + version + "'");
            // Ignore the Unnamed Projects
            if(moduleName == null) {
                continue;
            }
            ServerConfiguration.Module module = serverConfiguration.obtainModuleBySymbolicName(ServerConfiguration.Module.getSymbolicName(moduleProject));
            if(module == null) {
                module = serverConfiguration.addModule(myProject, moduleProject);
            } else if(!module.isBound()) {
                // If the module already exists then it could be from the Storage so we need to re-bind with the maven project
                module.rebind(myProject, moduleProject);
                moduleList.remove(module);
            } else {
                moduleList.remove(module);
            }
        }
        return moduleList;
    }

    public enum BundleStatus { upToDate, outDated, failed };
    public BundleStatus checkAndUpdateSupportBundle(boolean onlyCheck) {
        BundleStatus ret = BundleStatus.failed;

        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        if(serverConfiguration != null) {
            try {
                OsgiClient osgiClient = obtainSGiClient();
                EmbeddedArtifactLocator artifactLocator = myProject.getComponent(EmbeddedArtifactLocator.class);
                Version remoteVersion = osgiClient.getBundleVersion(EmbeddedArtifactLocator.SUPPORT_BUNDLE_SYMBOLIC_NAME);

                messageManager.sendInfoNotification("aem.explorer.version.installed.support.bundle", remoteVersion);

                final EmbeddedArtifact supportBundle = artifactLocator.loadToolingSupportBundle();
                final Version embeddedVersion = new Version(supportBundle.getVersion());

                if(remoteVersion == null || remoteVersion.compareTo(embeddedVersion) < 0) {
                    ret = BundleStatus.outDated;
                    if(!onlyCheck) {
                        InputStream contents = null;
                        try {
                            messageManager.sendInfoNotification("aem.explorer.begin.installing.support.bundle", embeddedVersion);
                            contents = supportBundle.openInputStream();
                            osgiClient.installBundle(contents, supportBundle.getName());
                            ret = BundleStatus.upToDate;
                        } finally {
                            IOUtils.closeQuietly(contents);
                        }
                        remoteVersion = embeddedVersion;
                    }
                } else {
                    ret = BundleStatus.upToDate;
                }
                messageManager.sendInfoNotification("aem.explorer.finished.connection.to.remote");
            } catch(IOException e) {
                messageManager.sendErrorNotification("aem.explorer.cannot.read.installation.support.bundle", serverConfiguration.getName(), e);
            } catch(OsgiClientException e) {
                messageManager.sendErrorNotification("aem.explorer.osgi.client.problem", serverConfiguration.getName(), e);
            }
        } else {
            messageManager.sendNotification("\n" + "aem.explorer.check.support.bundle.no.configuration.selected", NotificationType.WARNING);
        }
        return ret;
    }

    public OsgiClient obtainSGiClient() {
        OsgiClient ret = null;

        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        if(serverConfiguration != null) {
            serverConfiguration.setServerStatus(ServerConfiguration.ServerStatus.connecting);
            try {
                boolean success = false;
                Result<ResourceProxy> result = null;
                Repository repository = obtainRepository(serverConfiguration, messageManager);
                if(repository != null) {
                    Command<ResourceProxy> command = repository.newListChildrenNodeCommand("/");
                    result = command.execute();
                    success = result.isSuccess();

                    messageManager.sendInfoNotification("aem.explorer.connected.sling.repository", success);
                    if(success) {
                        serverConfiguration.setServerStatus(ServerConfiguration.ServerStatus.connected);
                        RepositoryInfo repositoryInfo = ServerUtil.getRepositoryInfo(
                            new IServer(serverConfiguration), new NullProgressMonitor()
                        );
                        ret = Activator.getDefault().getOsgiClientFactory().createOsgiClient(repositoryInfo);
                    }
                }
            } catch(URISyntaxException e) {
                messageManager.sendErrorNotification("aem.explorer.server.uri.bad", serverConfiguration.getName(), e);
            }
        } else {
            messageManager.sendErrorNotification("aem.explorer.cannot.connect.repository.missing.configuration", serverConfiguration.getName());
        }
        return ret;
    }

    @Nullable
    public static Repository obtainRepository(@NotNull ServerConfiguration serverConfiguration, @NotNull MessageManager messageManager) {
        Repository ret = null;
        messageManager.sendInfoNotification("aem.explorer.begin.connecting.sling.repository");
        try {
            ret = ServerUtil.connectRepository(new IServer(serverConfiguration), new NullProgressMonitor());
            // Check if the Connection is still alive by fetching the root nodes
            getChildrenNodes(ret, "/");
        } catch(CoreException e) {
            // Show Alert and exit
            //AS TODO: Seriously the RepositoryUtils class is throwing a IllegalArgumentException is it cannot connect to a Repo
            if(e.getCause().getClass() == IllegalArgumentException.class) {
                messageManager.showAlertWithArguments("aem.explorer.cannot.connect.repository.refused", serverConfiguration.getName());
            } else {
                messageManager.showAlertWithArguments("aem.explorer.cannot.connect.repository", serverConfiguration.getName(), e);
            }
        }
        return ret;
    }

    public static List<ResourceProxy> getChildrenNodes(Repository repository, String path) {
        List<ResourceProxy> ret = new ArrayList<ResourceProxy>();
        if(path != null && path.length() > 0) {
            if(path.charAt(0) != '/') {
                path = "/" + path;
            }
            try {
                Command<ResourceProxy> command = repository.newListChildrenNodeCommand(path);
                Result<ResourceProxy> result = command.execute();
                boolean success = result.isSuccess();
                if(success) {
                    ResourceProxy resourceProxy = result.get();
                    for(ResourceProxy childResourceProxy: resourceProxy.getChildren()) {
                        ret.add(childResourceProxy);
                    }
                } else {
                    result.get();
                }
            } catch(RepositoryException e) {
                //AS TODO: Throw Proper Exception
            }
        }
        return ret;
    }

    public void deployModules(final DataContext dataContext, boolean force, ProgressIndicator indicator) {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        if(serverConfiguration != null) {
            indicator.setText2("Check Bindings");
            checkBinding(serverConfiguration);
            List<Module> moduleList = serverConfiguration.getModuleList();
            double i = 0;
            for(ServerConfiguration.Module module: moduleList) {
                deployModule(dataContext, module, force, indicator);
                indicator.setFraction(i / moduleList.size());
                i += 1;
            }
        } else {
            messageManager.sendNotification("aem.explorer.deploy.modules.no.configuration.selected", NotificationType.WARNING);
        }
    }

    public void deployModule(@NotNull final DataContext dataContext, @NotNull ServerConfiguration.Module module, boolean force, ProgressIndicator indicator) {
        messageManager.sendInfoNotification("aem.explorer.begin.connecting.sling.repository");
        indicator.setText2("Deploy Module: " + module.getName());
        checkBinding(module.getParent());
        if(module.isPartOfBuild()) {
            if(module.isOSGiBundle()) {
                publishBundle(dataContext, module);
            } else if(module.isSlingPackage()) {
                //AS TODO: Add the synchronization of the entire module
                deploymentManager.publishModule(
                    deploymentManager.new IntelliJModuleWrapper(module, myProject),
                    force
                );
            } else {
                messageManager.sendDebugNotification("Module: '" + module.getName() + "' is not a supported package");
                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.unsupported);
            }
        } else {
            messageManager.sendDebugNotification("Module: '" + module.getName() + "' is not Part of the Build");
            updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.excluded);
        }
    }

    public void connectInDebugMode(RunManagerEx runManager) {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        // Create Remote Connection to Server using the IntelliJ Run / Debug Connection
        //AS TODO: It is working but the configuration is listed and made persistent. That is not too bad because
        //AS TODO: after changes a reconnect will update the configuration.
        RemoteConfigurationType remoteConfigurationType = new RemoteConfigurationType();
        RunConfiguration runConfiguration = remoteConfigurationType.getFactory().createTemplateConfiguration(myProject);
        RemoteConfiguration remoteConfiguration = (RemoteConfiguration) runConfiguration;
        // Server means if you are listening. If not you are attaching.
        remoteConfiguration.SERVER_MODE = false;
        remoteConfiguration.USE_SOCKET_TRANSPORT = true;
        remoteConfiguration.HOST = serverConfiguration.getHost();
        remoteConfiguration.PORT = serverConfiguration.getConnectionDebugPort() + "";
        // Set a Name of the Configuration so that it is properly listed.
        remoteConfiguration.setName(serverConfiguration.getName());
        RunnerAndConfigurationSettings configuration = new RunnerAndConfigurationSettingsImpl(
            (RunManagerImpl) runManager,
            runConfiguration,
            false
        );
        runManager.setTemporaryConfiguration(configuration);
        //AS TODO: Make sure that this is the proper way to obtain the DEBUG Executor
        Executor executor = ExecutorRegistry.getInstance().getExecutorById(ToolWindowId.DEBUG);
        ExecutionUtil.runConfiguration(configuration, executor);
        // Update the Modules with the Remote Sling Server
        OsgiClient osgiClient = obtainSGiClient();
        if(osgiClient != null) {
            BundleStatus status = checkAndUpdateSupportBundle(false);
            if(status != BundleStatus.failed) {
                checkModules(osgiClient);
            }
        }
    }

    public void stopDebugConnection(@NotNull DataContext dataContext) {
        ProcessHandler processHandler = getHandler(dataContext);
        if(processHandler instanceof KillableProcess && processHandler.isProcessTerminating()) {
            ((KillableProcess) processHandler).killProcess();
            return;
        }

        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        if(serverConfiguration != null) {
            serverConfiguration.setServerStatus(ServerConfiguration.ServerStatus.disconnecting);
        }
        if(processHandler != null) {
            if(processHandler.detachIsDefault()) {
                processHandler.detachProcess();
            } else {
                processHandler.destroyProcess();
            }
        }
        if(serverConfiguration != null) {
            serverConfiguration.setServerStatus(ServerConfiguration.ServerStatus.disconnected);
        }
    }

    @Nullable
    private ProcessHandler getHandler(@NotNull DataContext dataContext) {
        final RunContentDescriptor contentDescriptor = LangDataKeys.RUN_CONTENT_DESCRIPTOR.getData(dataContext);
        if(contentDescriptor != null) {
            // toolwindow case
            return contentDescriptor.getProcessHandler();
        } else {
            // main menu toolbar
            final Project project = CommonDataKeys.PROJECT.getData(dataContext);
            final RunContentDescriptor selectedContent =
                project == null ? null : ExecutionManager.getInstance(project).getContentManager().getSelectedContent();
            return selectedContent == null ? null : selectedContent.getProcessHandler();
        }
    }

    private void updateStatus(String configurationName, ServerConfiguration.SynchronizationStatus synchronizationStatus) {
        ServerConfiguration configuration = serverConfigurationManager.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setSynchronizationStatus(synchronizationStatus);
            serverConfigurationManager.updateServerConfiguration(configuration);
        }
    }

    public void updateServerStatus(String configurationName, ServerConfiguration.ServerStatus serverStatus) {
        ServerConfiguration configuration = serverConfigurationManager.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setServerStatus(serverStatus);
            serverConfigurationManager.updateServerConfiguration(configuration);
        }
    }

    public void updateStatus(ServerConfiguration configuration, ServerConfiguration.SynchronizationStatus synchronizationStatus) {
        if(configuration != null) {
            configuration.setSynchronizationStatus(synchronizationStatus);
            serverConfigurationManager.updateServerConfiguration(configuration);
        }
    }

    private void updateModuleStatus(Module module, ServerConfiguration.SynchronizationStatus synchronizationStatus) {
        if(module != null) {
            module.setStatus(synchronizationStatus);
            serverConfigurationManager.updateServerConfiguration(module.getParent());
        }
    }

    // Publishing Stuff --------------------------

    public void publishBundle(@NotNull final DataContext dataContext, final @NotNull Module module) {
        messageManager.sendInfoNotification("aem.explorer.deploy.module.prepare", module);
        InputStream contents = null;
        // Check if this is a OSGi Bundle
        final ModuleProject moduleProject = module.getModuleProject();
        if(moduleProject.isOSGiBundle()) {
            try {
                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.updating);
                boolean mavenDoneSuccessfully = true;
                if(module.getParent().isBuildWithMaven()) {
                    mavenDoneSuccessfully = false;
                    List<String> goals = MavenDataKeys.MAVEN_GOALS.getData(dataContext);
                    if (goals == null) {
                        goals = new ArrayList<String>();
                    }
                    if (goals.isEmpty()) {
                        goals.add("package");
                    }
                    messageManager.sendInfoNotification("aem.explorer.deploy.module.maven.goals", goals);
                    final MavenProjectsManager projectsManager = MavenActionUtil.getProjectsManager(dataContext);
                    if (projectsManager == null) {
                        messageManager.showAlert("Maven Failure", "Could not find Maven Project Manager, need to build manually");
                    } else {
                        final ToolWindow tw = ToolWindowManager.getInstance(module.getProject()).getToolWindow(ToolWindowId.RUN);
                        final boolean isShown = tw != null && tw.isVisible();
                        String workingDirectory = moduleProject.getModuleDirectory();
                        MavenExplicitProfiles explicitProfiles = projectsManager.getExplicitProfiles();
                        final MavenRunnerParameters params = new MavenRunnerParameters(true, workingDirectory, goals, explicitProfiles.getEnabledProfiles(), explicitProfiles.getDisabledProfiles());
                        // This Monitor is used to know when the Maven build is done
                        RunExecutionMonitor rem = RunExecutionMonitor.getInstance(myProject);
                        // We need to tell the Monitor that we are going to start a Maven Build so that the Countdown Latch
                        // is ready
                        rem.startMavenBuild();
                        try {
                            ApplicationManager.getApplication().invokeLater(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            MavenRunConfigurationType.runConfiguration(module.getProject(), params, null);
                                        } catch(RuntimeException e) {
                                            // Ignore it
                                            String message = e.getMessage();
                                        }
                                        if(isShown) {
                                            tw.hide(null);
                                        }
                                    }
                                },
                                ModalityState.NON_MODAL
                            );
                        } catch (IllegalStateException e) {
                            if (firstRun) {
                                firstRun = false;
                                messageManager.showAlert("aem.explorer.deploy.module.maven.first.run.failure");
                            }
                        } catch(RuntimeException e) {
                            messageManager.sendDebugNotification("Maven Build failed with an unexpectected exception");
                            messageManager.sendUnexpectedException(e);
                        }
                        // Now we can wait for the process to end
//                        switch(RunExecutionMonitor.getInstance(myProject).waitFor(module.getParent().getMavenBuildTimeoutInSeconds())) {
                        switch(RunExecutionMonitor.getInstance(myProject).waitFor()) {
                            case done:
                                messageManager.sendInfoNotification("aem.explorer.deploy.module.maven.done");
                                mavenDoneSuccessfully = true;
                                break;
                            case timedOut:
                                messageManager.sendInfoNotification("aem.explorer.deploy.module.maven.timedout");
                                messageManager.showAlert("aem.explorer.deploy.module.maven.timedout");
                                break;
                            case interrupted:
                                messageManager.sendInfoNotification("aem.explorer.deploy.module.maven.interrupted");
                                messageManager.showAlert("aem.explorer.deploy.module.maven.interrupted");
                                break;
                        }
                    }
                }
                if(mavenDoneSuccessfully) {
                    File buildDirectory = new File(module.getModuleProject().getBuildDirectoryPath());
                    if(buildDirectory.exists() && buildDirectory.isDirectory()) {
                        File buildFile = new File(buildDirectory, module.getModuleProject().getBuildFileName() + ".jar");
                        messageManager.sendDebugNotification("Build File Name: " + buildFile.toURL());
                        if(buildFile.exists()) {
                            EmbeddedArtifact bundle = new EmbeddedArtifact(module.getSymbolicName(), module.getVersion(), buildFile.toURL());
                            contents = bundle.openInputStream();
                            obtainSGiClient().installBundle(contents, bundle.getName());
                            module.setStatus(ServerConfiguration.SynchronizationStatus.upToDate);
                        }
                    }
                    updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.upToDate);
                    messageManager.sendInfoNotification("aem.explorer.deploy.module.success", module);
                }
            } catch(MalformedURLException e) {
                module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.bad.url", e);
                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.failed);
            } catch(OsgiClientException e) {
                module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.client", e);
                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.failed);
            } catch(IOException e) {
                module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.io", e);
                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.failed);
            } finally {
                IOUtils.closeQuietly(contents);
            }
        } else {
            messageManager.sendNotification("aem.explorer.deploy.module.unsupported.maven.packaging", NotificationType.WARNING);
        }
    }

    public enum FileChangeType {CHANGED, CREATED, DELETED, MOVED, COPIED};

    public long getLastModificationTimestamp(Module module) {
        long ret = -1;

        List<String> resourceList = findContentResources(module);
        Set<String> allResourcesUpdatedList = new HashSet<String>();
        ModuleProject moduleProject = module.getModuleProject();
        VirtualFile baseFile = module.getProject().getBaseDir();
        for(String resource: resourceList) {
            VirtualFile resourceFile = baseFile.getFileSystem().findFileByPath(resource);
            messageManager.sendDebugNotification("LMT Resource File to check: " + resourceFile);
            List<VirtualFile> changedResources = new ArrayList<VirtualFile>();
            getChangedResourceList(resourceFile, changedResources);
            //AS TODO: Create a List of Changed Resources
            for(VirtualFile changedResource : changedResources) {
                long fileTimestamp = Util.getModificationStamp(changedResource);
                if(fileTimestamp > 0) {
                    ret = Math.max(ret, fileTimestamp);
                    long parentLastModificationTimestamp =
                        getParentLastModificationTimestamp(module, resourceFile.getPath(), changedResource, allResourcesUpdatedList);
                    ret = Math.max(ret, parentLastModificationTimestamp);
                }
            }
        }

        return ret;
    }

    private long getParentLastModificationTimestamp(
        Module module,
        String basePath,
        VirtualFile file,
        Set<String> handledPaths
    ) {
        long ret = -1;
        VirtualFile parentFile = file.getParent();
        messageManager.sendDebugNotification("PLMT Check Parent File: " + parentFile);
        if(parentFile.getPath().equals(basePath)) {
            return ret;
        }
        // already published by us, a parent of another resource that was published in this execution
        if (handledPaths.contains(parentFile.getPath())) {
            return ret;
        }
        long parentLastModificationTimestamp = getParentLastModificationTimestamp(module, basePath, parentFile, handledPaths);
        ret = Math.max(parentLastModificationTimestamp, ret);
        long timestamp = file.getTimeStamp();
        long fileTimestamp = Util.getModificationStamp(file);
        if(fileTimestamp > 0) {
            ret = Math.max(timestamp, ret);
        }
        return ret;
    }

    /**
     * Builds up the list of resource files recursively of the given resource
     *
     * @param resource The resource we start with. If it is a directory we call this method with that directory
     * @param resourceList The list we add all resource files. At the end this list contains all files inside
     *                     the original resource.
     */
    private void getChangedResourceList(VirtualFile resource, List<VirtualFile> resourceList) {
        if(resource.isDirectory()) {
            List<VirtualFile> children = Arrays.asList(resource.getChildren());
            for(VirtualFile child : children) {
                getChangedResourceList(child, resourceList);
            }
        } else {
            resourceList.add(resource);
        }
    }

    public void handleFileChanges(List<FileChange> fileChangeList) {
        Map<String, Module> resourcePathToModuleMap = new HashMap<String, Module>();
        for(FileChange fileChange: fileChangeList) {
            String filePath = fileChange.getFile().getPath();
            // First go over the map to see if we already found the module
            boolean found = false;
            for(Map.Entry<String, Module> entry: resourcePathToModuleMap.entrySet()) {
                Module module = entry.getValue();
                String path = entry.getKey();
                if(filePath.startsWith(path)) {
                    fileChange.setModule(module);
                    fileChange.setResourcePath(path);
                    found = true;
                    break;
                }
            }
            if(!found) {
                // If not found we loop over the modules and see if one is
                List<Module> moduleList = selectionHandler.getModuleDescriptorListOfCurrentConfiguration();
                for(Module module: moduleList) {
                    if(module.isSlingPackage()) {
                        String contentPath = findContentResource(module, filePath);
                        if(contentPath != null) {
                            // This file belongs to this module so we are good to publish it
                            fileChange.setModule(module);
                            fileChange.setResourcePath(contentPath);
                            resourcePathToModuleMap.put(contentPath, module);
                            break;
                        }
                    } else if(module.isOSGiBundle()) {
                        // Here we are not interested in a source file but rather in the Artifact. If it is the artifact then
                        // we mark the module as outdated
                        ModuleProject moduleProject = module.getModuleProject();
                        if(filePath.startsWith(moduleProject.getBuildDirectoryPath())) {
                            // Check if it is the build file
                            String fileName = fileChange.getFile().getName();
                            String artifactId = moduleProject.getArtifactId();
                            String version = moduleProject.getVersion();
                            //AS TODO: Can't we use the getBuildFileName() (aka MavenProject.getFinalName())
                            if(fileName.equals(artifactId + "-" + version + ".jar")) {
                                messageManager.sendInfoNotification("server.update.file.change.prepare", filePath, fileChange.getFileChangeType());
                                module.setStatus(ServerConfiguration.SynchronizationStatus.outdated);
                            }
                        }
                    }
                }
            }
        }
        if(!resourcePathToModuleMap.isEmpty()) {
            Module module = resourcePathToModuleMap.values().iterator().next();
            Repository repository =  ServerUtil.getConnectedRepository(
                new IServer(module.getParent()), new NullProgressMonitor(), messageManager
            );
            if(repository != null) {
                for(FileChange fileChange : fileChangeList) {
                    try {
                        VirtualFile file = fileChange.getFile();
                        String path = file.getPath();
                        FileChangeType type = fileChange.getFileChangeType();
                        String basePath = fileChange.getResourcePath();
                        Module currentModule = fileChange.getModule();
                        messageManager.sendInfoNotification("server.update.file.change.prepare", path, type);
                        messageManager.sendDebugNotification("Got Repository: " + repository);
                        Command<?> command = null;
                        switch(type) {
                            case CHANGED:
                            case CREATED:
                                command = deploymentManager.addFileCommand(
                                    repository,
                                    deploymentManager.new IntelliJModuleWrapper(currentModule, myProject),
                                    deploymentManager.new IntelliJFileWrapper(file),
                                    false
                                );
                                break;
                            case DELETED:
                                command = deploymentManager.removeFileCommand(
                                    repository,
                                    deploymentManager.new IntelliJModuleWrapper(currentModule, myProject),
                                    deploymentManager.new IntelliJFileWrapper(file)
                                );
                                break;
                        }
                        messageManager.sendDebugNotification("Got Command: " + command);
                        if(command != null) {
                            Set<String> handledPaths = new HashSet<String>();
                            deploymentManager.ensureParentIsPublished(
                                deploymentManager.new IntelliJModuleWrapper(currentModule, myProject),
                                //AS Make sure the basepath is in forward slash notation
                                basePath.replace("\\", "/"),
                                deploymentManager.new IntelliJFileWrapper(file),
                                repository,
                                handledPaths,
                                true
                            );
                            deploymentManager.execute(command);
                            // Add a property that can be used later to avoid a re-sync if not needed
                            Util.setModificationStamp(file);
                            messageManager.sendInfoNotification("server.update.file.change.success", path);
                        } else {
                            messageManager.sendInfoNotification("server.update.file.change.failed", path, currentModule);
                        }
                    } catch(ConnectorException e) {
                        e.printStackTrace();
                    } catch(SerializationException e) {
                        e.printStackTrace();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String findContentResource(Module module, String filePath) {
        List<String> resourceList = findContentResources(module, filePath);
        return resourceList.isEmpty() ? null : resourceList.get(0);
    }

    public List<String> findContentResources(Module module) {
        return findContentResources(module, null);
    }

    public List<String> findContentResources(Module module, String filePath) {
        List<String> ret = new ArrayList<String>();
        ModuleProject moduleProject = module.getModuleProject();
        List<String> contentDirectoryPaths = moduleProject.getContentDirectoryPaths();
        for(String basePath: contentDirectoryPaths) {
            messageManager.sendDebugNotification("Content Base Path: '" + basePath + "'");
            //AS TODO: Paths from Windows have backlashes instead of forward slashes
            //AS TODO: It is possible that certain files are in forward slashes even on Windows
            String myFilePath = filePath == null ? null : filePath.replace("\\", "/");
            String myBasePath = basePath == null ? null : basePath.replace("\\", "/");
            if(Util.pathEndsWithFolder(basePath, JCR_ROOT_FOLDER_NAME) && (myFilePath == null || myFilePath.startsWith(myBasePath))) {
                ret.add(basePath);
                break;
            }
        }
        return ret;
    }

    /**
     * Checks if the current selected and connected Server Connection are the same (to avoid accidental deployments)
     * and also checks if automatic builds are supported
     * @param showAlert Displays an alert if the current selected and connected server connection are different
     * @param automaticBuild If true it will check if the publish type is set to automatically on change
     * @return True if the connection are in sync and if automatic build are support (if it is set to be checked)
     */
    public boolean checkSelectedServerConfiguration(boolean showAlert, boolean automaticBuild) {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        ServerConfiguration connectedServerConfiguration = serverConfigurationManager.findConnectedServerConfiguration();
        if(connectedServerConfiguration != null && serverConfiguration != connectedServerConfiguration) {
            if(showAlert) {
                messageManager.showAlert("aem.explorer.check.connection.out.of.sync");
            }
            return false;
        } else if(serverConfiguration != null && automaticBuild && serverConfiguration.getPublishType() != ServerConfiguration.PublishType.automaticallyOnChange) {
            return false;
        } else {
            return true;
        }
    }
}
