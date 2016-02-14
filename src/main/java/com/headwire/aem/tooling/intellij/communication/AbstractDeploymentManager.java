package com.headwire.aem.tooling.intellij.communication;

import org.apache.sling.ide.io.ConnectorException;
import org.apache.sling.ide.io.ExceptionConstants;
import org.apache.sling.ide.io.NewResourceChangeCommandFactory;
import org.apache.sling.ide.io.SlingResource;
import org.apache.sling.ide.serialization.SerializationException;
import org.apache.sling.ide.transport.Command;
import org.apache.sling.ide.transport.Repository;
import org.apache.sling.ide.transport.RepositoryException;
import org.apache.sling.ide.transport.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class contains all the generic code that is common to all plugins
 * dealing with the deployment of a module to Sling
 */
public abstract class AbstractDeploymentManager
{

    public static class ModuleWrapper<T, P> {
        private T module;
        private ProjectWrapper<P> project;

        public ModuleWrapper(T module, ProjectWrapper<P> project) {
            this.module = module;
            this.project = project;
        }

        public T getModule() {
            return module;
        }

        public P getProject() {
            return project.getProject();
        }
    }

    public static class ProjectWrapper<T> {
        private T project;
        public ProjectWrapper(T project) {
            this.project = project;
        }

        public T getProject() {
            return project;
        }
    }

    public static abstract class FileWrapper<T> {
        private T file;
        public FileWrapper(T file) {
            this.file = file;
        }

        public T getFile() {
            return file;
        }

        abstract public FileWrapper<T> getParent();

        abstract public String getPath();

        abstract public void setModificationTimestamp();

        abstract public long getModificationTimestamp();

        abstract public long getTimestamp();
    }

    public enum MessageType {INFO, WARNING, ERROR, DEBUG};
    public enum SynchronizationStatus {updating, upToDate, failed}

    private NewResourceChangeCommandFactory commandFactory;

    public AbstractDeploymentManager(NewResourceChangeCommandFactory resourceChangeCommandFactory) {
        commandFactory = resourceChangeCommandFactory;
    }

    abstract void sendMessage(MessageType messageType, String message, Object...parameters);
    abstract void sendAlert(MessageType messageType, String title, String message);

    abstract Repository obtainRepository(ModuleWrapper module);
    //AS TODO: The ServerConfiguration is IntelliJ Only so we need an IDE independent flag
    abstract void updateModuleStatus(ModuleWrapper module, SynchronizationStatus synchronizationStatus);

    abstract List<String> findContentResources(ModuleWrapper module, String filePath);

    abstract SlingResource obtainSlingResource(ModuleWrapper moduleWrapper, FileWrapper file);

    abstract FileWrapper obtainResourceFile(ModuleWrapper module, String resourcePath);

    abstract void getChangedResourceList(FileWrapper resource, List<FileWrapper> resourceList);

    abstract void setModuleLastModificationTimestamp(ModuleWrapper module, long timestamp);

    abstract String getMessage(String messageId, Object...parameters);

    public void publishModule(ModuleWrapper module, boolean force) {
        Repository repository = null;
        long lastModificationTimestamp = -1;
        if(force) {
            sendMessage(MessageType.INFO, "aem.explorer.deploy.module.by.force.prepare", module);
        } else {
            sendMessage(MessageType.INFO, "aem.explorer.deploy.module.prepare", module);
        }
        try {
            repository = obtainRepository(module);
            if(repository != null) {
                sendMessage(MessageType.DEBUG, "Got Repository: " + repository);
                updateModuleStatus(module, SynchronizationStatus.updating);
                List<String> resourceList = findContentResources(module, null);
                Set<String> allResourcesUpdatedList = new HashSet<String>();
                for(String resource : resourceList) {
                    FileWrapper resourceFile = obtainResourceFile(module, resource);
                    sendMessage(MessageType.DEBUG, "Resource File to deploy: " + resourceFile);
                    List<FileWrapper> changedResources = new ArrayList<FileWrapper>();
                    getChangedResourceList(resourceFile, changedResources);
                    //AS TODO: Create a List of Changed Resources
                    for(FileWrapper changedResource : changedResources) {
                        try {
                            Command<?> command = addFileCommand(repository, module, changedResource, force);
                            if(command != null) {
                                long parentLastModificationTimestamp = ensureParentIsPublished(module, resourceFile.getPath(), changedResource, repository, allResourcesUpdatedList, force);
                                lastModificationTimestamp = Math.max(parentLastModificationTimestamp, lastModificationTimestamp);
                                allResourcesUpdatedList.add(changedResource.getPath());

                                sendMessage(MessageType.DEBUG, "Publish file: " + changedResource);
                                sendMessage(MessageType.DEBUG, "Resource File to deploy: " + resourceFile);
                                execute(command);

                                // save the modification timestamp to avoid a redeploy if nothing has changed
                                changedResource.setModificationTimestamp();
                                lastModificationTimestamp = Math.max(changedResource.getTimestamp(), lastModificationTimestamp);
                            } else {
                                // We do not update the file but we need to find the last modification timestamp
                                // We need to obtain the command to see if it is deployed
                                command = addFileCommand(repository, module, changedResource, true);
                                if(command != null) {
                                    long parentLastModificationTimestamp = getParentLastModificationTimestamp(resourceFile.getPath(), changedResource, allResourcesUpdatedList);
                                    lastModificationTimestamp = Math.max(lastModificationTimestamp, parentLastModificationTimestamp);
                                    allResourcesUpdatedList.add(changedResource.getPath());
                                    long timestamp = changedResource.getTimestamp();
                                    lastModificationTimestamp = Math.max(lastModificationTimestamp, timestamp);
                                }
                            }
                        } catch(ConnectorException e) {
                            if(e.getId()  != ConnectorException.UNKNOWN) {
                                // The Connector Exception is used to end the processing of publishing a file. In case of an error it will stop the entire processing
                                // and in case of a warning it will proceed
                                MessageType type = e.getId() < 0 ? MessageType.ERROR : MessageType.WARNING;
                                sendAlert(type, "aem.explorer.deploy.exception.title", e.getMessage());
                                if(e.getId() < 0) {
                                    return;
                                }
                                throw e;
                            } else {
                                sendAlert(MessageType.ERROR, "aem.explorer.deploy.exception.title", e.getCause().getMessage());
                                return;
                            }
                        }
                    }
                }
                // reorder the child nodes at the end, when all create/update/deletes have been processed
                //AS TODO: This needs to be resolved -> done but needs to be verified
                for(String resourcePath : allResourcesUpdatedList) {
                    FileWrapper file = obtainResourceFile(module, resourcePath);
                    if(file != null) {
                        execute(reorderChildNodesCommand(repository, module, file));
                    } else {
                        sendMessage(MessageType.ERROR, "aem.explorer.deploy.failed.to.reorder.missing.resource", resourcePath);
                    }
                }
                setModuleLastModificationTimestamp(module, lastModificationTimestamp);
                updateModuleStatus(module, SynchronizationStatus.upToDate);
                if(force) {
                    sendMessage(MessageType.INFO, "aem.explorer.deploy.module.by.force.success", module);
                } else {
                    sendMessage(MessageType.INFO, "aem.explorer.deploy.module.success", module);
                }
            }
        } catch(ConnectorException e) {
            sendMessage(MessageType.ERROR, "aem.explorer.deploy.module.failed.client", module, e);
            updateModuleStatus(module, SynchronizationStatus.failed);
            throw new RuntimeException(e);
        } catch(SerializationException e) {
            sendMessage(MessageType.ERROR, "aem.explorer.deploy.module.failed.client", module, e);
            updateModuleStatus(module, SynchronizationStatus.failed);
        } catch(IOException e) {
            sendMessage(MessageType.ERROR, "aem.explorer.deploy.module.failed.io", module, e);
            updateModuleStatus(module, SynchronizationStatus.failed);
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
     * @throws ConnectorException
     */
    protected long ensureParentIsPublished(
        ModuleWrapper module,
        String basePath,
        FileWrapper file,
        Repository repository,
        Set<String> handledPaths,
        boolean force
    )
        throws ConnectorException, SerializationException, IOException {

        long ret = -1;

        FileWrapper parentFile = file.getParent();
        sendMessage(MessageType.DEBUG, "Check Parent File: " + parentFile);
        String parentFilePath = parentFile.getPath();
        if(parentFilePath.equals(basePath)) {
            sendMessage(MessageType.DEBUG, "Path {0} can not have a parent, skipping", parentFilePath);
            return ret;
        }

        // already published by us, a parent of another resource that was published in this execution
        if (handledPaths.contains(parentFile.getPath())) {
            sendMessage(MessageType.DEBUG, "Parent path {0} was already handled, skipping", parentFile.getPath());
            return ret;
        }

        // handle the parent's parent first, if needed
        long lastParentModificationTimestamp = ensureParentIsPublished(module, basePath, parentFile, repository, handledPaths, force);

        Command command = null;
        try {
            // create this resource
            command = addFileCommand(repository, module, parentFile, force);
            execute(command);
        } catch(ConnectorException e) {
            ConnectorException rethrow = e;
            if(e.getId() != ConnectorException.UNKNOWN) {
                rethrow = new ConnectorException(
                    getMessage(
                        (e.getId() == ExceptionConstants.COMMAND_EXECUTION_FAILURE ?
                            "aem.explorer.deploy.create.parent.failed.message" :
                            "aem.explorer.deploy.create.parent.unsuccessful.message" ),
                        e.getMessage(),
                        e.getCause().getMessage()),
                    e
                );
            }
            throw rethrow;
        }

        // save the modification timestamp to avoid a redeploy if nothing has changed
        parentFile.setModificationTimestamp();

        handledPaths.add(parentFile.getPath());
        sendMessage(MessageType.DEBUG, "Ensured that resource at path {0} is published", parentFile.getPath());
        return Math.max(lastParentModificationTimestamp, parentFile.getModificationTimestamp());
    }

    protected long getParentLastModificationTimestamp(
        String basePath,
        FileWrapper file,
        Set<String> handledPaths
    ) {
        long ret = -1;
        FileWrapper parentFile = file.getParent();
        sendMessage(MessageType.DEBUG, "PLMT Check Parent File: " + parentFile);
        if(parentFile.getPath().equals(basePath)) {
            return ret;
        }
        // already published by us, a parent of another resource that was published in this execution
        if (handledPaths.contains(parentFile.getPath())) {
            return ret;
        }
        long parentLastModificationTimestamp = getParentLastModificationTimestamp(basePath, parentFile, handledPaths);
        ret = Math.max(parentLastModificationTimestamp, ret);
        long timestamp = file.getTimestamp();
        long fileTimestamp = file.getModificationTimestamp();
        if(fileTimestamp > 0) {
            ret = Math.max(timestamp, ret);
        }
        return ret;
    }

    protected Command<?> addFileCommand(
        Repository repository, ModuleWrapper module, FileWrapper file, boolean forceDeploy
    ) throws
        ConnectorException,
        SerializationException, IOException
    {
        SlingResource resource = obtainSlingResource(module, file);
        return commandFactory.newCommandForAddedOrUpdated(repository, resource, forceDeploy);
    }

    protected Command<?> reorderChildNodesCommand(
        Repository repository, ModuleWrapper module, FileWrapper file
    ) throws ConnectorException,
        SerializationException, IOException
    {
        SlingResource resource = obtainSlingResource(module, file);
        return commandFactory.newReorderChildNodesCommand(repository, resource);
    }

    protected void execute(Command<?> command) throws ConnectorException {
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
                    throw new ConnectorException(
                        ExceptionConstants.COMMAND_EXECUTION_FAILURE,
                        command.getPath(),
                        e
                    );
                }
            }
            throw new ConnectorException(
                ExceptionConstants.COMMAND_EXECUTION_UNSUCCESSFUL,
                getMessage("aem.explorer.deploy.command.execution.unsuccessful.message", command.getPath())
            );
        }
    }

}
