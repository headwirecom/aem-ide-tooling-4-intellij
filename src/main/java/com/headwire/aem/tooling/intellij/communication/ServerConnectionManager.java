package com.headwire.aem.tooling.intellij.communication;

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
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.util.BundleStateHelper;
import com.headwire.aem.tooling.intellij.util.OSGiFactory;
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
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.wm.ToolWindowId;
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
import org.apache.sling.ide.transport.RepositoryInfo;
import org.apache.sling.ide.transport.ResourceProxy;
import org.apache.sling.ide.transport.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.model.MavenResource;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.osgi.framework.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Handles the Server Connections for the Plugin, its state and flags
 *
 * Created by schaefa on 5/21/15.
 */
public class ServerConnectionManager {

//    public enum ConnectionState { connecting, connected, disconnecting, disconnected, failed };
//
//    public enum SynchronizationState {
//        unknown, updating("synchronizing"), updated("synchronized"), outdated;
//
//        private String name;
//
//        SynchronizationState() {
//            this.name = name();
//        }
//
//        SynchronizationState(String name) {
//            this.name = name;
//        }
//
//        public String getName() { return name; }
//    }
//

    private static List<ServerConfiguration.ServerStatus> CONFIGURATION_IN_USE = Arrays.asList(
        ServerConfiguration.ServerStatus.connecting,
        ServerConfiguration.ServerStatus.connected,
        ServerConfiguration.ServerStatus.disconnecting
    );

    public static final String CONTENT_SOURCE_TO_ROOT_PATH = "/content/jcr_root";

    private Project project;
    private ServerTreeSelectionHandler selectionHandler;
    private MessageManager messageManager;
    private ServerConfigurationManager serverConfigurationManager;
    private ResourceChangeCommandFactory commandFactory;

    public ServerConnectionManager(@NotNull Project project, @NotNull ServerTreeSelectionHandler serverTreeSelectionHandler) {
        this.project = project;
        selectionHandler = serverTreeSelectionHandler;
        messageManager = MessageManager.getInstance(project);
        serverConfigurationManager = ServerConfigurationManager.getInstance(project);
        commandFactory = new ResourceChangeCommandFactory(ServiceManager.getService(SerializationManager.class));
    }

    // ----- Server State Flags

    public boolean isConnectionInUse() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        return serverConfiguration != null && CONFIGURATION_IN_USE.contains(serverConfiguration.getServerStatus());
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
            // We only support Maven Modules as of now
            //AS TODO: are we support Facets as well -> check later
            MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
            messageManager.sendDebugNotification("Maven Projects Manager: '" + mavenProjectsManager);
            boolean allSynchronized = true;
            List<MavenProject> mavenProjects = mavenProjectsManager.getNonIgnoredProjects();
            for(MavenProject mavenProject : mavenProjects) {
                MavenId mavenId = mavenProject.getMavenId();
                String moduleName = mavenProject.getName();
                String artifactId = mavenId.getArtifactId();
                String version = mavenId.getVersion();
                // Check if this Module is listed in the Module Sub Tree of the Configuration. If not add it.
                messageManager.sendDebugNotification("Maven Module: '" + moduleName + "', artifact id: '" + artifactId + "', version: '" + version + "'");
                // Ignore the Unnamed Projects
                if(moduleName == null) {
                    continue;
                }
                // Change any dashes to dots
                version = version.replaceAll("-", ".");
                Version localVersion = new Version(version);
                ServerConfiguration.Module module = serverConfiguration.obtainModuleBySymbolicName(ServerConfiguration.Module.getSymbolicName(mavenProject));
                if(module == null) {
                    module = serverConfiguration.addModule(project, mavenProject);
                } else {
                    // If the module already exists then it could be from the Storage so we need to re-bind with the maven project
                    module.rebind(project, mavenProject);
                }
                try {
                    if(module.isOSGiBundle()) {
                        Version remoteVersion = osgiClient.getBundleVersion(module.getSymbolicName());
                        messageManager.sendDebugNotification("Check OSGi Module: '" + moduleName + "', artifact id: '" + artifactId + "', version: '" + remoteVersion + "' vs. '" + localVersion + "'");
                        boolean moduleUpToDate = remoteVersion != null && remoteVersion.compareTo(localVersion) >= 0;
                        Object state = BundleStateHelper.getBundleState(module);
                        messageManager.sendDebugNotification("Bundle State of Module: '" + module.getName() + "', state: '" + state + "'");
                        if(moduleUpToDate) {
                            // Mark as synchronized
                            module.setStatus(ServerConfiguration.SynchronizationStatus.upToDate);
                        } else {
                            // Mark as out of date
                            module.setStatus(ServerConfiguration.SynchronizationStatus.outdated);
                            allSynchronized = false;
                        }
                    } else if(module.isSlingPackage()) {
                        //AS TODO: Handle Sling Package
                        //AS TODO: We need to go through that module and check if they are synchronized
                    }
                } catch(OsgiClientException e1) {
                    // Mark connection as failed
                    module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                    allSynchronized = false;
                }
            }
            if(allSynchronized) {
                markConfigurationAsSynchronized(serverConfiguration.getName());
            } else {
                markConfigurationAsOutDated(serverConfiguration.getName());
            }
        } else {
            messageManager.sendDebugNotification("Cannot check modules if no Server Configuration is selected");
        }
    }

    public enum BundleStatus { upToDate, outDated, failed };
    public BundleStatus checkAndUpdateSupportBundle(boolean onlyCheck) {
        BundleStatus ret = BundleStatus.failed;

        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        if(serverConfiguration != null) {
            try {
                OsgiClient osgiClient = obtainSGiClient();
                EmbeddedArtifactLocator artifactLocator = OSGiFactory.getArtifactLocator();
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
            messageManager.sendDebugNotification("Cannot check support bundle if no Server Configuration is selected");
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
                messageManager.sendInfoNotification("aem.explorer.begin.connecting.sling.repository");
                Repository repository = ServerUtil.connectRepository(
                    new IServer(serverConfiguration), new NullProgressMonitor()
                );
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
            } catch(URISyntaxException e) {
                messageManager.sendErrorNotification("aem.explorer.server.uri.bad", serverConfiguration.getName(), e);
            } catch(CoreException e) {
                messageManager.sendErrorNotification("aem.explorer.cannot.connect.repository", serverConfiguration.getName(), e);
            }
        } else {
            messageManager.sendDebugNotification("Cannot check support bundle if no Server Configuration is selected");
        }
        return ret;
    }

    public void deployModules() {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        if(serverConfiguration != null) {
            List<Module> moduleList = serverConfiguration.getModuleList();
            for(ServerConfiguration.Module module: moduleList) {
                if(module.isPartOfBuild()) {
                    if(module.isOSGiBundle()) {
                        InputStream contents = null;
                        // Check if this is a OSGi Bundle
                        if(module.getMavenProject().getPackaging().equalsIgnoreCase("bundle")) {
                            try {
                                //                    sendInfoNotification("aem.explorer.begin.installing.support.bundle", embeddedVersion);
                                File buildDirectory = new File(module.getMavenProject().getBuildDirectory());
                                if(buildDirectory.exists() && buildDirectory.isDirectory()) {
                                    File buildFile = new File(buildDirectory, module.getMavenProject().getFinalName() + ".jar");
                                    messageManager.sendDebugNotification("Build File Name: " + buildFile.toURL());
                                    if(buildFile.exists()) {
                                        EmbeddedArtifact bundle = new EmbeddedArtifact(module.getSymbolicName(), module.getVersion(), buildFile.toURL());
                                        contents = bundle.openInputStream();
                                        obtainSGiClient().installBundle(contents, bundle.getName());
                                        module.setStatus(ServerConfiguration.SynchronizationStatus.upToDate);
                                    }
                                }
                            } catch(MalformedURLException e) {
                                module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                                messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.bad.url", e);
                            } catch(OsgiClientException e) {
                                module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                                messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.client", e);
                            } catch(IOException e) {
                                module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                                messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.io", e);
                            } finally {
                                IOUtils.closeQuietly(contents);
                            }
                        }
                    } else if(module.isSlingPackage()) {
                        //AS TODO: Add the synchronization of the entire module
                        publishModule(module);
                    } else {
                        module.setStatus(ServerConfiguration.SynchronizationStatus.unsupported);
                        messageManager.sendDebugNotification("Module: '" + module.getName() + "' is not a supported package");
                    }
                } else {
                    module.setStatus(ServerConfiguration.SynchronizationStatus.unsupported);
                    messageManager.sendDebugNotification("Module: '" + module.getName() + "' is not Part of the Build");
                }
            }
        } else {
            messageManager.sendDebugNotification("Cannot check support bundle if no Server Configuration is selected");
        }
    }

    public void connectInDebugMode(RunManagerEx runManager) {
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        // Create Remote Connection to Server using the IntelliJ Run / Debug Connection
        //AS TODO: It is working but the configuration is listed and made persistent. That is not too bad because
        //AS TODO: after changes a reconnect will update the configuration.
        RemoteConfigurationType remoteConfigurationType = new RemoteConfigurationType();
        RunConfiguration runConfiguration = remoteConfigurationType.getFactory().createTemplateConfiguration(project);
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
//            myRunManager.setSelectedConfiguration(configuration);
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
        if(processHandler.detachIsDefault()) {
            processHandler.detachProcess();
        } else {
            processHandler.destroyProcess();
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
            //AS TODO: Update Bundle Status
            // Mark any Bundles inside the Tree as disconnected
        }
    }

    private void markConfigurationAsOutDated(String configurationName) {
        ServerConfiguration configuration = serverConfigurationManager.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setSynchronizationStatus(ServerConfiguration.SynchronizationStatus.outdated);
            serverConfigurationManager.updateServerConfiguration(configuration);
            //AS TODO: Update Bundle Status
            // Mark any Bundles inside the Tree as disconnected
        }
    }

    // Publishing Stuff --------------------------


    public enum FileChangeType {CHANGED, CREATED, DELETED, MOVED, COPIED};

    public void publishModule(Module module) {
        Repository repository = null;
//            List<IModuleResource> addedOrUpdatedResources = new ArrayList<IModuleResource>();
        try {
            repository = ServerUtil.getConnectedRepository(
//                    currentModule.getParent()
                new IServer(module.getParent()), new NullProgressMonitor()
            );
            messageManager.sendDebugNotification("Got Repository: " + repository);
            List<MavenResource> resourceList = findMavenSources(module);
            Set<String> allResourcesUpdatedList = new HashSet<String>();
            MavenProject mavenProject = module.getMavenProject();
            VirtualFile baseFile = mavenProject.getDirectoryFile();
            for(MavenResource resource: resourceList) {
                String resourceDirectoryPath = resource.getDirectory();
                VirtualFile resourceFile = baseFile.getFileSystem().findFileByPath(resourceDirectoryPath);
                messageManager.sendDebugNotification("Resource File to deploy: " + resourceFile);
                List<VirtualFile> changedResources = new ArrayList<VirtualFile>();
                getChangedResourceList(resourceFile, changedResources);
                //AS TODO: Create a List of Changed Resources
                for(VirtualFile changedResource : changedResources) {
                    Command<?> command = addFileCommand(repository, module, changedResource);
                    if(command != null) {
                        ensureParentIsPublished(module, resourceFile.getPath(), changedResource, repository, allResourcesUpdatedList);
                        allResourcesUpdatedList.add(changedResource.getPath());
                    }
                    messageManager.sendDebugNotification("Publish file: " + changedResource);
                    messageManager.sendDebugNotification("Publish for module: " + module.getName());
                    execute(command);
                }
            }
            // reorder the child nodes at the end, when all create/update/deletes have been processed
//            for (IModuleResource resource : addedOrUpdatedResources) {
//                execute(reorderChildNodesCommand(repository, resource));
//            }
        } catch(CoreException e) {
            e.printStackTrace();
        } catch(SerializationException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
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

    public void handleFileChange(VirtualFile file, FileChangeType type) {
        String path = file.getPath();
        Module currentModule = null;
        String basePath = null;
        // Check if that relates to any Content Packages and if so then publish it
        List<Module> moduleList = selectionHandler.getModuleDescriptorListOfCurrentConfiguration();
        for(Module module: moduleList) {
            if(module.isSlingPackage()) {
                MavenResource mavenResource = findMavenSource(module, path);
                if(mavenResource != null) {
                    // This file belongs to this module so we are good to publish it
                    currentModule = module;
                    basePath = mavenResource.getDirectory();
                    messageManager.sendDebugNotification("Found File: '" + file.getPath() + "' in module: '" + currentModule.getName() + "");
                    break;
                }
            }
        }
        if(currentModule != null) {
            Repository repository = null;
//            List<IModuleResource> addedOrUpdatedResources = new ArrayList<IModuleResource>();
            try {
                repository = ServerUtil.getConnectedRepository(
//                    currentModule.getParent()
                    new IServer(currentModule.getParent()), new NullProgressMonitor()
                );
                messageManager.sendDebugNotification("Got Repository: " + repository);
                Command<?> command = addFileCommand(repository, currentModule, file);
                messageManager.sendDebugNotification("Got Command: " + command);
                if (command != null) {
//AS TODO: Adjust and Re-enable later
//                    IModuleResource[] allResources = getResources(module);
                    Set<String> handledPaths = new HashSet<String>();
                    ensureParentIsPublished(
//                        resourceDelta.getModuleResource(),
                        currentModule,
                        basePath,
//                        relativePath,
                        file,
                        repository,
//                        allResources,
                        handledPaths
                    );
//                    addedOrUpdatedResources.add(resourceDelta.getModuleResource());

                    execute(command);
                    // Add a property that can be used later to avoid a re-sync if not needed
                    file.putUserData(Util.MODIFICATION_DATE_KEY, file.getModificationStamp());
                    messageManager.sendDebugNotification("Successfully Updated File: " + file.getPath());
                } else {
                    messageManager.sendDebugNotification("Failed to obtain File Command for Module: " + currentModule + " and file: " + file);
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
    private void ensureParentIsPublished(
//        IModuleResource moduleResource,
        Module module,
        String basePath,
        VirtualFile file,
        Repository repository,
//        IModuleResource[] allResources,
        Set<String> handledPaths
    )
        throws CoreException, SerializationException, IOException {

        Logger logger = Activator.getDefault().getPluginLogger();

//        IPath currentPath = moduleResource.getModuleRelativePath();
//
//        logger.trace("Ensuring that parent of path {0} is published", currentPath);

        VirtualFile parentFile = file.getParent();
        messageManager.sendDebugNotification("Check Parent File: " + parentFile);
        String parentFilePath = parentFile.getPath();
        if(parentFile.getPath().equals(basePath)) {
            logger.trace("Path {0} can not have a parent, skipping", parentFile.getPath());
            return;
        }

//        IPath parentPath = currentPath.removeLastSegments(1);
//        String parentPath = relativePath.substring(relativePath.lastIndexOf("/"));

        // already published by us, a parent of another resource that was published in this execution
        if (handledPaths.contains(parentFile.getPath())) {
            logger.trace("Parent path {0} was already handled, skipping", parentFile.getPath());
            return;
        }

//        for (IModuleResource maybeParent : allResources) {
//            if (maybeParent.getModuleRelativePath().equals(parentPath)) {
        // handle the parent's parent first, if needed
        ensureParentIsPublished(module, basePath, parentFile, repository, handledPaths);
        // create this resource
        execute(addFileCommand(repository, module, parentFile));
        handledPaths.add(parentFile.getPath());
        logger.trace("Ensured that resource at path {0} is published", parentFile.getPath());
        return;
//            }
//        }
//
//        throw new IllegalArgumentException("Resource at " + moduleResource.getModuleRelativePath()
//            + " has parent path " + parentPath + " but no resource with that path is in the module's resources.");

    }

    private Command<?> addFileCommand(
        Repository repository, Module module, VirtualFile file
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
        return commandFactory.newCommandForAddedOrUpdated(repository, resource);
    }

    private Command<?> addFileCommand(Repository repository, IModuleResource resource) throws CoreException,
        SerializationException, IOException {

        IResource res = getResource(resource);

        if (res == null) {
            return null;
        }

        return commandFactory.newCommandForAddedOrUpdated(repository, res);
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

    private Command<?> removeFileCommand(Repository repository, IModuleResource resource)
        throws SerializationException, IOException, CoreException {

        IResource deletedResource = getResource(resource);

        if (deletedResource == null) {
            return null;
        }

        return commandFactory.newCommandForRemovedResources(repository, deletedResource);
    }

    private void execute(Command<?> command) throws CoreException {
        if (command == null) {
            return;
        }
        Result<?> result = command.execute();

        if (!result.isSuccess()) {
            // TODO - proper error logging
            throw new CoreException(
//                new Status(Status.ERROR, Activator.PLUGIN_ID, "Failed publishing path="
//                + command.getPath() + ", result=" + result.toString())
                "Failed to publish path: " + command.getPath() + ", result: " + result.toString()
            );
        }

    }

    private MavenResource findMavenSource(Module module, String filePath) {
        List<MavenResource> resourceList = findMavenSources(module, filePath);
        return resourceList.isEmpty() ? null : resourceList.get(0);
    }

    private List<MavenResource> findMavenSources(Module module) {
        return findMavenSources(module, null);
    }

    private List<MavenResource> findMavenSources(Module module, String filePath) {
        List<MavenResource> ret = new ArrayList<MavenResource>();
        MavenProject mavenProject = module.getMavenProject();
        List<MavenResource> sourcePathList = mavenProject.getResources();
        for(MavenResource sourcePath: sourcePathList) {
            String basePath = sourcePath.getDirectory();
            if(basePath.endsWith(CONTENT_SOURCE_TO_ROOT_PATH) && (filePath == null || filePath.startsWith(basePath))) {
                ret.add(sourcePath);
                break;
            }
        }
        return ret;
    }

}
