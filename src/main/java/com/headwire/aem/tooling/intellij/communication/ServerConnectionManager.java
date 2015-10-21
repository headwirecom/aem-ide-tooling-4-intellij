package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.config.ModuleProject;
import com.headwire.aem.tooling.intellij.config.ModuleProjectFactory;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.eclipse.ResourceChangeCommandFactory;
import com.headwire.aem.tooling.intellij.eclipse.ServerUtil;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IFile;
import com.headwire.aem.tooling.intellij.eclipse.stub.IFolder;
import com.headwire.aem.tooling.intellij.eclipse.stub.IModuleResource;
import com.headwire.aem.tooling.intellij.eclipse.stub.IResource;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.IStatus;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.eclipse.stub.Status;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.BundleStateHelper;
import com.headwire.aem.tooling.intellij.util.Constants;
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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.artifacts.EmbeddedArtifact;
import org.apache.sling.ide.artifacts.EmbeddedArtifactLocator;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.log.Logger;
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
import org.jetbrains.idea.maven.project.MavenProject;
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
//import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_PATH_INDICATOR;
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
    private ResourceChangeCommandFactory commandFactory;

    private static boolean firstRun = true;

    public ServerConnectionManager(@NotNull Project project) {
        super(project);
        messageManager = ServiceManager.getService(myProject, MessageManager.class);
        serverConfigurationManager = ServiceManager.getService(myProject, ServerConfigurationManager.class);
        commandFactory = new ResourceChangeCommandFactory(ServiceManager.getService(SerializationManager.class));
    }

    public void init(@NotNull ServerTreeSelectionHandler serverTreeSelectionHandler) {
        selectionHandler = serverTreeSelectionHandler;
    }

    // ----- Server State Flags

    public boolean isConfigurationEditable() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        return serverConfiguration != null &&
//            ( CONFIGURATION_CHECKED.contains(serverConfiguration.getServerStatus()) ||
              !CONFIGURATION_IN_USE.contains(serverConfiguration.getServerStatus())
            ;
    }

    public boolean isConfigurationChecked() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        return serverConfiguration != null && CONFIGURATION_CHECKED.contains(serverConfiguration.getServerStatus());
    }

    public boolean isConnectionInUse() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        return serverConfiguration != null && CONFIGURATION_IN_USE.contains(serverConfiguration.getServerStatus());
    }

    public boolean isConnectionIsStoppable() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        return serverConfiguration != null &&
//            ( CONFIGURATION_CHECKED.contains(serverConfiguration.getServerStatus()) ||
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
//                        Version localVersion = new Version(version);
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
     * @return True if the the connection was successfully bound otherwise flase
     */
    public boolean checkBinding(@NotNull ServerConfiguration serverConfiguration) {
        if(!serverConfiguration.isBound()) {
            List<Module> moduleList = bindModules(serverConfiguration);
            return moduleList.isEmpty();
        }
        return true;
    }

    public List<Module> bindModules(@NotNull ServerConfiguration serverConfiguration) {
        MavenProjectsManager mavenProjectsManager = ServiceManager.getService(myProject, MavenProjectsManager.class);
        List<MavenProject> mavenProjects = mavenProjectsManager.getNonIgnoredProjects();

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
//                EmbeddedArtifactLocator artifactLocator = OSGiFactory.getArtifactLocator();
                EmbeddedArtifactLocator artifactLocator = ServiceManager.getService(EmbeddedArtifactLocator.class);
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

    @Nullable
    public static void disconnectRepository(@NotNull ServerConfiguration serverConfiguration, @NotNull MessageManager messageManager) {
//        messageManager.sendInfoNotification("aem.explorer.begin.connecting.sling.repository");
        try {
            ServerUtil.stopRepository(new IServer(serverConfiguration), new NullProgressMonitor());
        } catch(CoreException e) {
            messageManager.sendDebugNotification("Failed to disconnect: " + e.getMessage());
//            // Show Alert and exit
//            //AS TODO: Seriously the RepositoryUtils class is throwing a IllegalArgumentException is it cannot connect to a Repo
//            if(e.getCause().getClass() == IllegalArgumentException.class) {
//                messageManager.showAlertWithArguments("aem.explorer.cannot.connect.repository.refused", serverConfiguration.getName());
//            } else {
//                messageManager.showAlertWithArguments("aem.explorer.cannot.connect.repository", serverConfiguration.getName(), e);
//            }
        }
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

    public void deployModules(final DataContext dataContext, boolean force) {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        if(serverConfiguration != null) {
            checkBinding(serverConfiguration);
            List<Module> moduleList = serverConfiguration.getModuleList();
            for(ServerConfiguration.Module module: moduleList) {
                deployModule(dataContext, module, force);
            }
        } else {
            messageManager.sendNotification("aem.explorer.deploy.modules.no.configuration.selected", NotificationType.WARNING);
        }
    }

    public void deployModule(@NotNull final DataContext dataContext, @NotNull ServerConfiguration.Module module, boolean force) {
        messageManager.sendInfoNotification("aem.explorer.begin.connecting.sling.repository");
        checkBinding(module.getParent());
        if(module.isPartOfBuild()) {
            if(module.isOSGiBundle()) {
                publishBundle(dataContext, module);
            } else if(module.isSlingPackage()) {
                //AS TODO: Add the synchronization of the entire module
                publishModule(module, force);
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

    private boolean canBeStopped(@Nullable ProcessHandler processHandler) {
        return processHandler != null && !processHandler.isProcessTerminated()
            && (!processHandler.isProcessTerminating()
            || processHandler instanceof KillableProcess && ((KillableProcess) processHandler).canKillProcess());
    }

    private void markConfigurationAsSynchronized(String configurationName) {
        ServerConfiguration configuration = serverConfigurationManager.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setSynchronizationStatus(ServerConfiguration.SynchronizationStatus.upToDate);
            serverConfigurationManager.updateServerConfiguration(configuration);
        }
    }

    private void markConfigurationAsOutDated(String configurationName) {
        ServerConfiguration configuration = serverConfigurationManager.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setSynchronizationStatus(ServerConfiguration.SynchronizationStatus.outdated);
            serverConfigurationManager.updateServerConfiguration(configuration);
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

    public void updateServerStatus(ServerConfiguration configuration, ServerConfiguration.ServerStatus serverStatus) {
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
                if(module.getParent().isBuildWithMaven()) {
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
                        final MavenRunnerParameters params = new MavenRunnerParameters(
                            true,
                            workingDirectory,
                            goals,
                            explicitProfiles.getEnabledProfiles(),
                            explicitProfiles.getDisabledProfiles());
                        try {
                            MavenRunConfigurationType.runConfiguration(module.getProject(), params, null);
                            if (!isShown) {
                                ApplicationManager.getApplication().invokeLater(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            tw.hide(null);
                                        }
                                    },
                                    ModalityState.NON_MODAL
                                    //                                    ApplicationManager.getApplication().getCurrentModalityState()
                                );
                            }
                        } catch (IllegalStateException e) {
                            if (firstRun) {
                                firstRun = false;
                                messageManager.showAlert("aem.explorer.deploy.module.maven.first.run.failure");
                            }
                        }
                        messageManager.sendInfoNotification("aem.explorer.deploy.module.maven.done");
                    }
                }
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

    public void publishModule(Module module, boolean force) {
        Repository repository = null;
        long lastModificationTimestamp = -1;
        if(force) {
            messageManager.sendInfoNotification("aem.explorer.deploy.module.by.force.prepare", module);
        } else {
            messageManager.sendInfoNotification("aem.explorer.deploy.module.prepare", module);
        }
        try {
            repository = ServerUtil.getConnectedRepository(
                new IServer(module.getParent()), new NullProgressMonitor(), messageManager
            );
            if(repository != null) {
                messageManager.sendDebugNotification("Got Repository: " + repository);
                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.updating);
                List<String> resourceList = findContentResources(module);
                Set<String> allResourcesUpdatedList = new HashSet<String>();
                ModuleProject moduleProject = module.getModuleProject();
                VirtualFile baseFile = module.getProject().getBaseDir();
                for(String resource : resourceList) {
                    VirtualFile resourceFile = baseFile.getFileSystem().findFileByPath(resource);
                    messageManager.sendDebugNotification("Resource File to deploy: " + resourceFile);
                    List<VirtualFile> changedResources = new ArrayList<VirtualFile>();
                    getChangedResourceList(resourceFile, changedResources);
                    //AS TODO: Create a List of Changed Resources
                    for(VirtualFile changedResource : changedResources) {
                        try {
                            Command<?> command = addFileCommand(repository, module, changedResource, force);
                            if(command != null) {
                                long parentLastModificationTimestamp = ensureParentIsPublished(module, resourceFile.getPath(), changedResource, repository, allResourcesUpdatedList, force);
                                lastModificationTimestamp = Math.max(parentLastModificationTimestamp, lastModificationTimestamp);
                                allResourcesUpdatedList.add(changedResource.getPath());

                                messageManager.sendDebugNotification("Publish file: " + changedResource);
                                messageManager.sendDebugNotification("Publish for module: " + module.getName());
                                execute(command);

                                // save the modification timestamp to avoid a redeploy if nothing has changed
                                Util.setModificationStamp(changedResource);
                                lastModificationTimestamp = Math.max(changedResource.getTimeStamp(), lastModificationTimestamp);
                            } else {
                                // We do not update the file but we need to find the last modification timestamp
                                // We need to obtain the command to see if it is deployed
                                command = addFileCommand(repository, module, changedResource, true);
                                if(command != null) {
                                    long parentLastModificationTimestamp = getParentLastModificationTimestamp(module, resourceFile.getPath(), changedResource, allResourcesUpdatedList);
                                    lastModificationTimestamp = Math.max(lastModificationTimestamp, parentLastModificationTimestamp);
                                    allResourcesUpdatedList.add(changedResource.getPath());
                                    long timestamp = changedResource.getTimeStamp();
                                    lastModificationTimestamp = Math.max(lastModificationTimestamp, timestamp);
                                }
                            }
                        } catch(CoreException e) {
                            Status status = e.getStatus();
                            if(status != null) {
                                // The Core Exception is used to end the processing of publishing a file. In case of an error it will stop the entire processing
                                // and in case of a warning it will proceed
                                NotificationType type = status.getStatus() == IStatus.ERROR ? NotificationType.ERROR : NotificationType.WARNING;
                                messageManager.showAlert(type, AEMBundle.message("aem.explorer.deploy.exception.title"), status.getMessage());
                                if(type == NotificationType.ERROR) {
                                    return;
                                }
                            } else {
                                messageManager.showAlert(NotificationType.ERROR, AEMBundle.message("aem.explorer.deploy.exception.title"), e.getCause().getMessage());
                                return;
                            }
                        }
                    }
                }
                // reorder the child nodes at the end, when all create/update/deletes have been processed
                //AS TODO: This needs to be resolved -> done but needs to be verified
                for(String resourcePath : allResourcesUpdatedList) {
                    VirtualFile file = baseFile.getFileSystem().findFileByPath(resourcePath);
                    if(file != null) {
                        execute(reorderChildNodesCommand(repository, module, file));
                    } else {
                        messageManager.sendErrorNotification("aem.explorer.deploy.failed.to.reorder.missing.resource", resourcePath);
                    }
                }
                module.setLastModificationTimestamp(lastModificationTimestamp);
                updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.upToDate);
                if(force) {
                    messageManager.sendInfoNotification("aem.explorer.deploy.module.by.force.success", module);
                } else {
                    messageManager.sendInfoNotification("aem.explorer.deploy.module.success", module);
                }
            }
        } catch(CoreException e) {
            messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.client", module, e);
            updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.failed);
        } catch(SerializationException e) {
            messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.client", module, e);
            updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.failed);
        } catch(IOException e) {
            messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.io", module, e);
            updateModuleStatus(module, ServerConfiguration.SynchronizationStatus.failed);
        }
    }

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
                    long parentLastModificationTimestamp = getParentLastModificationTimestamp(module, resourceFile.getPath(), changedResource, allResourcesUpdatedList);
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

    private void getChangedResourceList(VirtualFile resourceFile, List<VirtualFile> changedResources) {
        if(!resourceFile.isDirectory()) {
            changedResources.add(resourceFile);
        } else {
            for(VirtualFile child : resourceFile.getChildren()) {
                getChangedResourceList(child, changedResources);
            }
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
                                command = addFileCommand(repository, currentModule, file, false);
                                break;
                            case DELETED:
                                command = removeFileCommand(repository, currentModule, file);
                                break;
                        }
                        messageManager.sendDebugNotification("Got Command: " + command);
                        if(command != null) {
                            Set<String> handledPaths = new HashSet<String>();
                            ensureParentIsPublished(
                                currentModule,
                                basePath,
                                file,
                                repository,
                                handledPaths,
                                false
                            );
                            execute(command);
                            // Add a property that can be used later to avoid a re-sync if not needed
                            Util.setModificationStamp(file);
                            messageManager.sendInfoNotification("server.update.file.change.success", path);
                        } else {
                            messageManager.sendInfoNotification("server.update.file.change.failed", path, currentModule);
                        }
                    } catch(CoreException e) {
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

    /**
     * Ensures that the parent of this resource has been published to the repository
     *
     * <p>
     * Note that the parents explicitly do not have their child nodes reordered, this will happen when they are
     * published due to a resource change
     * </p>
     *
     * AS NOTE: Taken from SlingLaunchpadBehaviour.class from Eclipse Sling IDE Project
     *
     * @ param moduleResource the current resource
     * @param repository the repository to publish to
     * @ param allResources all of the module's resources
     * @param handledPaths the paths that have been handled already in this publish operation, but possibly not
     *            registered as published
     * @throws IOException
     * @throws SerializationException
     * @throws CoreException
     */
    private long ensureParentIsPublished(
        Module module,
        String basePath,
        VirtualFile file,
        Repository repository,
        Set<String> handledPaths,
        boolean force
    )
        throws CoreException, SerializationException, IOException {

        long ret = -1;
        Logger logger = Activator.getDefault().getPluginLogger();

//        IPath currentPath = moduleResource.getModuleRelativePath();
//
//        logger.trace("Ensuring that parent of path {0} is published", currentPath);

        VirtualFile parentFile = file.getParent();
        messageManager.sendDebugNotification("Check Parent File: " + parentFile);
        String parentFilePath = parentFile.getPath();
        if(parentFilePath.equals(basePath)) {
            logger.trace("Path {0} can not have a parent, skipping", parentFilePath);
            return ret;
        }

//        IPath parentPath = currentPath.removeLastSegments(1);
//        String parentPath = relativePath.substring(relativePath.lastIndexOf("/"));

        // already published by us, a parent of another resource that was published in this execution
        if (handledPaths.contains(parentFile.getPath())) {
            logger.trace("Parent path {0} was already handled, skipping", parentFile.getPath());
            return ret;
        }

//        for (IModuleResource maybeParent : allResources) {
//            if (maybeParent.getModuleRelativePath().equals(parentPath)) {
        // handle the parent's parent first, if needed
        long lastParentModificationTimestamp = ensureParentIsPublished(module, basePath, parentFile, repository, handledPaths, force);

        try {
            // create this resource
            Command command = addFileCommand(repository, module, parentFile, force);
            execute(command);
        } catch(CoreException e) {
            Status status = e.getStatus();
            if(status != null) {
                throw new CoreException(
                    new Status(
                        status.getStatus(), status.getComponentId(), status.getActionId(),
                        AEMBundle.message(
                            ( status.getActionId() == Constants.COMMAND_EXECUTION_FAILURE ?
                                "aem.explorer.deploy.create.parent.failed.message" :
                                "aem.explorer.deploy.create.parent.unsuccessful.message" ),
                            status.getMessage(), e.getCause().getMessage()),
                        e
                    )
                );
            } else {
                throw e;
            }
        }

        // save the modification timestamp to avoid a redeploy if nothing has changed
        Util.setModificationStamp(parentFile);

        handledPaths.add(parentFile.getPath());
        logger.trace("Ensured that resource at path {0} is published", parentFile.getPath());
        return Math.max(lastParentModificationTimestamp, parentFile.getTimeStamp());
//            }
//        }
//
//        throw new IllegalArgumentException("Resource at " + moduleResource.getModuleRelativePath()
//            + " has parent path " + parentPath + " but no resource with that path is in the module's resources.");

    }

    private Command<?> addFileCommand(
        Repository repository, Module module, VirtualFile file, boolean forceDeploy
    ) throws
        CoreException,
        SerializationException, IOException {

//        IResource res = getResource(resource);
//
//        if (res == null) {
//            return null;
//        }

//        return commandFactory.newCommandForAddedOrUpdated(repository, res);
        IResource resource = file.isDirectory() ? new IFolder(module, file) : new IFile(module, file);
        return commandFactory.newCommandForAddedOrUpdated(repository, resource, forceDeploy);
    }

    private Command<?> addFileCommand(Repository repository, IModuleResource resource) throws CoreException,
        SerializationException, IOException {

        IResource res = getResource(resource);

        if (res == null) {
            return null;
        }

        return commandFactory.newCommandForAddedOrUpdated(repository, res, false);
    }

    private Command<?> reorderChildNodesCommand(Repository repository, Module module, VirtualFile file) throws CoreException,
        SerializationException, IOException {

//        IResource res = getResource(resource);
//
//        if (res == null) {
//            return null;
//        }

        IResource resource = file.isDirectory() ? new IFolder(module, file) : new IFile(module, file);
        return commandFactory.newReorderChildNodesCommand(repository, resource);
    }

    private Command<?> reorderChildNodesCommand(Repository repository, IModuleResource resource) throws CoreException,
        SerializationException, IOException {

        IResource res = getResource(resource);

        if (res == null) {
            return null;
        }

        return commandFactory.newReorderChildNodesCommand(repository, res);
    }

    private IResource getResource(IModuleResource resource) {

        IResource file = (IFile) resource.getAdapter(IFile.class);
        if (file == null) {
            file = (IFolder) resource.getAdapter(IFolder.class);
        }

        if (file == null) {
            // Usually happens on server startup, it seems to be safe to ignore for now
            Activator.getDefault().getPluginLogger()
                .trace("Got null {0} and {1} for {2}", IFile.class.getSimpleName(),
                    IFolder.class.getSimpleName(), resource);
            return null;
        }

        return file;
    }

    private Command<?> removeFileCommand(
//        Repository repository, IModuleResource resource
        Repository repository, Module module, VirtualFile file
    )
        throws SerializationException, IOException, CoreException {

//        IResource deletedResource = getResource(resource);
//
//        if (deletedResource == null) {
//            return null;
//        }

        IResource resource = file.isDirectory() ? new IFolder(module, file) : new IFile(module, file);
        return commandFactory.newCommandForRemovedResources(repository, resource);
    }

    private void execute(Command<?> command) throws CoreException {
        if (command == null) {
            return;
        }
        Result<?> result = command.execute();

        if (!result.isSuccess()) {
            try {
                result.get();
            } catch(RepositoryException e) {
                // Got the Repository Exception form the call
                Throwable cause = e.getCause();
                if(cause != null) {
                    throw new CoreException(
                        new Status(
                            Status.ERROR, Constants.SERVER_CONNECTION_MANAGER, Constants.COMMAND_EXECUTION_FAILURE,
                            command.getPath(),
                            e
                        )
                    );
                }
            }
            throw new CoreException(
                new Status(
                    Status.ERROR, Constants.SERVER_CONNECTION_MANAGER, Constants.COMMAND_EXECUTION_UNSUCCESSFUL,
                    AEMBundle.message("aem.explorer.deploy.command.execution.unsuccessful.message", command.getPath()),
                    null
                )
            );
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
//        List<MavenResource> sourcePathList = moduleProject.getResources();
//        for(MavenResource sourcePath: sourcePathList) {
//            String basePath = sourcePath.getDirectory();
        for(String basePath: contentDirectoryPaths) {
            messageManager.sendDebugNotification("Content Base Path: '" + basePath + "'");
            //AS TODO: Paths from Windows have backlashes instead of forward slashes
            if(Util.pathEndsWithFolder(basePath, JCR_ROOT_FOLDER_NAME) && (filePath == null || filePath.startsWith(basePath))) {
//            if(basePath.endsWith(JCR_ROOT_PATH_INDICATOR) && (filePath == null || filePath.startsWith(basePath))) {
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
