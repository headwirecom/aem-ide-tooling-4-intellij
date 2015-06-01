package com.headwire.aem.tooling.intellij.communication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import org.jetbrains.annotations.NotNull;

import static com.headwire.aem.tooling.intellij.communication.ServerConnectionManager.FileChangeType;

/**
 * Created by schaefa on 5/12/15.
 */
public class ContentResourceChangeListener {

    private ServerConnectionManager serverConnectionManager;

    public ContentResourceChangeListener(@NotNull Project project, @NotNull ServerConnectionManager serverConnectionManager) {
        this.serverConnectionManager = serverConnectionManager;

        // Create the Listener on File Changes
        VirtualFileManager.getInstance().addVirtualFileListener(
            new VirtualFileAdapter() {

                @Override
                public void contentsChanged(@NotNull VirtualFileEvent event){
                    handleFileChange(event, FileChangeType.CHANGED);
                }

                @Override
                public void fileCreated(@NotNull VirtualFileEvent event) {
                    handleFileChange(event, FileChangeType.CREATED);
                }

                @Override
                public void fileDeleted(@NotNull VirtualFileEvent event) {
                    handleFileChange(event, FileChangeType.DELETED);
                }

                @Override
                public void fileMoved(@NotNull VirtualFileMoveEvent event) {
                    // Move is not handled but rather split into a delete and a add
                    VirtualFile oldFile = event.getOldParent().findChild(event.getFileName());
                    VirtualFile newFile = event.getNewParent().findChild(event.getFileName());
                    if(oldFile != null && newFile != null) {
                        VirtualFileEvent event2 = new VirtualFileEvent(
                            event.getRequestor(), oldFile, oldFile.getName(), event.getOldParent()
                        );
                        handleFileChange(event2, FileChangeType.DELETED);
                        VirtualFileEvent event3 = new VirtualFileEvent(
                            event.getRequestor(), oldFile, newFile.getName(), event.getNewParent()
                        );
                        handleFileChange(event2, FileChangeType.CREATED);
                    } else {
                        //AS TODO: Report Failure to find Old or New File
                    }
                }

                @Override
                public void fileCopied(@NotNull VirtualFileCopyEvent event) {
                    handleFileChange(event, FileChangeType.COPIED);
                }
            },
            project
        );
    }

//    private enum Type {CHANGED, CREATED, DELETED, MOVED, COPIED};

    private void handleFileChange(VirtualFileEvent event, FileChangeType type) {
        final VirtualFile file = event.getFile();
        serverConnectionManager.handleFileChange(file, type);
//        String path = file.getPath();
//        Module currentModule = null;
//        String basePath = null;
//        // Check if that relates to any Content Packages and if so then publish it
//        List<Module> moduleList = selectionHandler.getModuleDescriptorListOfCurrentConfiguration();
//        for(Module module: moduleList) {
//            if(module.isSlingPackage()) {
//                MavenProject mavenProject = module.getMavenProject();
//                List<MavenResource> sourcePathList = mavenProject.getResources();
//                for(MavenResource sourcePath: sourcePathList) {
//                    basePath = sourcePath.getDirectory();
//                    if(basePath.endsWith(CONTENT_SOURCE_TO_ROOT_PATH) && path.startsWith(basePath)) {
//                        // This file belongs to this module so we are good to publish it
//                        currentModule = module;
//                        messageManager.sendDebugNotification("Found File: '" + file.getPath() + "' in module: '" + currentModule.getName() + "");
//                        break;
//                    }
//                }
//                if(currentModule != null) { break; }
//            }
//        }
//        if(currentModule != null) {
//            Repository repository = null;
////            List<IModuleResource> addedOrUpdatedResources = new ArrayList<IModuleResource>();
//            try {
//                repository = ServerUtil.getConnectedRepository(
////                    currentModule.getParent()
//                    new IServer(currentModule.getParent()), new NullProgressMonitor()
//                );
//                messageManager.sendDebugNotification("Got Repository: " + repository);
//                Command<?> command = addFileCommand(repository, currentModule, file);
//                messageManager.sendDebugNotification("Got Command: " + command);
//                if (command != null) {
////AS TODO: Adjust and Re-enable later
////                    IModuleResource[] allResources = getResources(module);
//                    Set<String> handledPaths = new HashSet<String>();
//                    ensureParentIsPublished(
////                        resourceDelta.getModuleResource(),
//                        currentModule,
//                        basePath,
////                        relativePath,
//                        file,
//                        repository,
////                        allResources,
//                        handledPaths
//                    );
////                    addedOrUpdatedResources.add(resourceDelta.getModuleResource());
//
//                    execute(command);
//                    // Add a property that can be used later to avoid a re-sync if not needed
//                    file.putUserData(Util.MODIFICATION_DATE_KEY, (new Date()).getTime());
//                    messageManager.sendDebugNotification("Successfully Updated File: " + file.getPath());
//                } else {
//                    messageManager.sendDebugNotification("Failed to obtain File Command for Module: " + currentModule + " and file: " + file);
//                }
//            } catch(CoreException e) {
//                e.printStackTrace();
//            } catch(SerializationException e) {
//                e.printStackTrace();
//            } catch(IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
//    /**
//     * Ensures that the parent of this resource has been published to the repository
//     *
//     * <p>
//     * Note that the parents explicitly do not have their child nodes reordered, this will happen when they are
//     * published due to a resource change
//     * </p>
//     *
//     * AS NOTE: Taken from SlingLaunchpadBehaviour.class from Eclipse Sling IDE Project
//     *
//     * @ param moduleResource the current resource
//     * @param repository the repository to publish to
//     * @ param allResources all of the module's resources
//     * @param handledPaths the paths that have been handled already in this publish operation, but possibly not
//     *            registered as published
//     * @throws IOException
//     * @throws SerializationException
//     * @throws CoreException
//     */
//    private void ensureParentIsPublished(
////        IModuleResource moduleResource,
//        Module module,
//        String basePath,
//        VirtualFile file,
//        Repository repository,
////        IModuleResource[] allResources,
//        Set<String> handledPaths
//    )
//        throws CoreException, SerializationException, IOException {
//
//        Logger logger = Activator.getDefault().getPluginLogger();
//
////        IPath currentPath = moduleResource.getModuleRelativePath();
////
////        logger.trace("Ensuring that parent of path {0} is published", currentPath);
//
//        VirtualFile parentFile = file.getParent();
//        messageManager.sendDebugNotification("Check Parent File: " + parentFile);
//        String parentFilePath = parentFile.getPath();
//        if(parentFile.getPath().equals(basePath)) {
//            logger.trace("Path {0} can not have a parent, skipping", parentFile.getPath());
//            return;
//        }
//
////        IPath parentPath = currentPath.removeLastSegments(1);
////        String parentPath = relativePath.substring(relativePath.lastIndexOf("/"));
//
//        // already published by us, a parent of another resource that was published in this execution
//        if (handledPaths.contains(parentFile.getPath())) {
//            logger.trace("Parent path {0} was already handled, skipping", parentFile.getPath());
//            return;
//        }
//
////        for (IModuleResource maybeParent : allResources) {
////            if (maybeParent.getModuleRelativePath().equals(parentPath)) {
//                // handle the parent's parent first, if needed
//                ensureParentIsPublished(module, basePath, parentFile, repository, handledPaths);
//                // create this resource
//                execute(addFileCommand(repository, module, parentFile));
//                handledPaths.add(parentFile.getPath());
//                logger.trace("Ensured that resource at path {0} is published", parentFile.getPath());
//                return;
////            }
////        }
////
////        throw new IllegalArgumentException("Resource at " + moduleResource.getModuleRelativePath()
////            + " has parent path " + parentPath + " but no resource with that path is in the module's resources.");
//
//    }
//
//    private Command<?> addFileCommand(
//        Repository repository, Module module, VirtualFile file
//    ) throws
//        CoreException,
//        SerializationException, IOException {
//
////        IResource res = getResource(resource);
////
////        if (res == null) {
////            return null;
////        }
//
////        return commandFactory.newCommandForAddedOrUpdated(repository, res);
//        IResource resource = new IResource(module, file);
//        return commandFactory.newCommandForAddedOrUpdated(repository, resource);
//    }
//
//    private Command<?> addFileCommand(Repository repository, IModuleResource resource) throws CoreException,
//        SerializationException, IOException {
//
//        IResource res = getResource(resource);
//
//        if (res == null) {
//            return null;
//        }
//
//        return commandFactory.newCommandForAddedOrUpdated(repository, res);
//    }
//
//    private Command<?> reorderChildNodesCommand(Repository repository, IModuleResource resource) throws CoreException,
//        SerializationException, IOException {
//
//        IResource res = getResource(resource);
//
//        if (res == null) {
//            return null;
//        }
//
//        return commandFactory.newReorderChildNodesCommand(repository, res);
//    }
//
//    private IResource getResource(IModuleResource resource) {
//
//        IResource file = (IFile) resource.getAdapter(IFile.class);
//        if (file == null) {
//            file = (IFolder) resource.getAdapter(IFolder.class);
//        }
//
//        if (file == null) {
//            // Usually happens on server startup, it seems to be safe to ignore for now
//            Activator.getDefault().getPluginLogger()
//                .trace("Got null {0} and {1} for {2}", IFile.class.getSimpleName(),
//                    IFolder.class.getSimpleName(), resource);
//            return null;
//        }
//
//        return file;
//    }
//
//    private Command<?> removeFileCommand(Repository repository, IModuleResource resource)
//        throws SerializationException, IOException, CoreException {
//
//        IResource deletedResource = getResource(resource);
//
//        if (deletedResource == null) {
//            return null;
//        }
//
//        return commandFactory.newCommandForRemovedResources(repository, deletedResource);
//    }
//
//    private void execute(Command<?> command) throws CoreException {
//        if (command == null) {
//            return;
//        }
//        Result<?> result = command.execute();
//
//        if (!result.isSuccess()) {
//            // TODO - proper error logging
//            throw new CoreException(
////                new Status(Status.ERROR, Activator.PLUGIN_ID, "Failed publishing path="
////                + command.getPath() + ", result=" + result.toString())
//                "Failed to publish path: " + command.getPath() + ", result: " + result.toString()
//            );
//        }
//
//    }

}
