package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IResource;
import com.headwire.aem.tooling.intellij.eclipse.ResourceChangeCommandFactory;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.util.ServerException;
import com.headwire.aem.tooling.intellij.eclipse.ServerUtil;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.impl.vlt.VaultFsLocatorImpl;
import org.apache.sling.ide.serialization.SerializationException;
import org.apache.sling.ide.serialization.SerializationManager;
import org.apache.sling.ide.transport.Command;
import org.apache.sling.ide.transport.Repository;
import org.apache.sling.ide.transport.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenResource;
import org.jetbrains.idea.maven.project.MavenProject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Created by schaefa on 5/12/15.
 */
public class ContentResourceChangeListener {

    public static final String CONTENT_SOURCE_TO_ROOT_PATH = "/content/jcr_root";

    private Project project;
    private ServerTreeSelectionHandler selectionHandler;
    private ResourceChangeCommandFactory commandFactory;
    private MessageManager messageManager;

    public ContentResourceChangeListener(@NotNull Project project, @NotNull ServerTreeSelectionHandler selectionHandler) {
        this.project = project;
        this.selectionHandler = selectionHandler;
        SerializationManager serializationManager = Activator.getDefault().getSerializationManager();
        commandFactory = new ResourceChangeCommandFactory(
            serializationManager
        );
        messageManager = ServiceManager.getService(project, MessageManager.class);

        // Create the Listener on File Changes
        VirtualFileManager.getInstance().addVirtualFileListener(
            new VirtualFileAdapter() {

                @Override
                public void contentsChanged(@NotNull VirtualFileEvent event){
                    handleFileChange(event, Type.CHANGED);
                }

                @Override
                public void fileCreated(@NotNull VirtualFileEvent event) {
                    handleFileChange(event, Type.CREATED);
                }

                @Override
                public void fileDeleted(@NotNull VirtualFileEvent event) {
                    handleFileChange(event, Type.DELETED);
                }

                @Override
                public void fileMoved(@NotNull VirtualFileMoveEvent event) {
                    handleFileChange(event, Type.MOVED);
                }

                @Override
                public void fileCopied(@NotNull VirtualFileCopyEvent event) {
                    handleFileChange(event, Type.COPIED);
                }
            },
            project
        );
    }

    private enum Type {CHANGED, CREATED, DELETED, MOVED, COPIED};

    private void handleFileChange(VirtualFileEvent event, Type type) {
        final VirtualFile file = event.getFile();
        String path = file.getPath();
        Module currentModule = null;
        // Check if that relates to any Content Packages and if so then publish it
        List<Module> moduleList = selectionHandler.getModuleDescriptorListOfCurrentConfiguration();
        for(Module module: moduleList) {
            if(module.isSlingPackage()) {
                MavenProject mavenProject = module.getProject();
                List<MavenResource> sourcePathList = mavenProject.getResources();
                for(MavenResource sourcePath: sourcePathList) {
                    String basePath = sourcePath.getDirectory();
                    if(basePath.endsWith(CONTENT_SOURCE_TO_ROOT_PATH) && path.startsWith(basePath)) {
                        // This file belongs to this module so we are good to publish it
                        currentModule = module;
                        messageManager.sendDebugNotification("Found File: '" + file.getPath() + "' in module: '" + currentModule.getName() + "");
                        break;
                    }
                }
                if(currentModule != null) { break; }
            }
        }
        if(currentModule != null) {
            Repository repository = null;
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
//                ensureParentIsPublished(resourceDelta.getModuleResource(), repository, allResources,
//                    handledPaths);
//                addedOrUpdatedResources.add(resourceDelta.getModuleResource());
                    execute(command);
                    // Add a property that can be used later to avoid a re-sync if not needed
                    file.putUserData(Util.MODIFICATION_DATE_KEY, (new Date()).getTime());
                    messageManager.sendDebugNotification("Successfully Updated File: " + file.getPath());
                }
            } catch(CoreException e) {
                e.printStackTrace();
            } catch(SerializationException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            } catch(ServerException e) {
                e.printStackTrace();
            }
        }
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
        IResource resource = new IResource(module, file);
        return commandFactory.newCommandForAddedOrUpdated(repository, resource);
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

}
