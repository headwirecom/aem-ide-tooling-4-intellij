package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.ImportRepositoryContentManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IPath;
import com.headwire.aem.tooling.intellij.eclipse.stub.IProject;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.serialization.SerializationException;
import org.apache.sling.ide.serialization.SerializationManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;

/**
 * Created by schaefa on 6/18/15.
 */
public class ImportFromServerAction extends AbstractProjectAction {

    @Override
    public boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        boolean ret = false;
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        if(serverConnectionManager.isConfigurationSelected()) {
            // Now check if a file is selected
            VirtualFile[] virtualFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
            if(virtualFiles.length == 0) {
                ret = true;
            } else if(virtualFiles.length == 1) {
                // We only support the Import from one file
                String path = virtualFiles[0].getPath();
                ret = path.indexOf("/" + JCR_ROOT_FOLDER_NAME) > 0;
            }
        }
        return ret;
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        VirtualFile[] virtualFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        if(virtualFiles != null) {
            switch(virtualFiles.length) {
                case 0:
                    //AS TODO: Alert User about unselected folder
                    break;
                case 1:
                    doImport(project, virtualFiles[0]);
                    break;
                default:
                    //AS TODO: Alert user about too many selected folders
            }
        }
    }

    private void doImport(Project project, final VirtualFile file) {
        final ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        final ServerTreeSelectionHandler selectionHandler = ServiceManager.getService(project, ServerTreeSelectionHandler.class);
        if(!serverConnectionManager.checkSelectedServerConfiguration(true, false)) {
            return;
        }
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        serverConnectionManager.checkBinding(serverConfiguration);
        List<ServerConfiguration.Module> moduleList = selectionHandler.getModuleDescriptorListOfCurrentConfiguration();
        ServerConfiguration.Module currentModuleLookup = null;
        for(ServerConfiguration.Module module: moduleList) {
            if(module.isSlingPackage()) {
                String contentPath = serverConnectionManager.findContentResource(module, file.getPath());
                if(contentPath != null) {
                    // This file belongs to this module so we are good to publish it
                    currentModuleLookup = module;
//                    basePath = mavenResource.getDirectory();
//                    messageManager.sendDebugNotification("Found File: '" + path + "' in module: '" + currentModule.getName() + "");
                    break;
                }
            }
        }
        if(currentModuleLookup != null) {
            final ServerConfiguration.Module currentModule = currentModuleLookup;
//            final String title = AEMBundle.message("deploy.configuration.action.name");
//
//            ProgressManager.getInstance().
//                new Task.Modal(project, title, false) {
//                    @Nullable
//                    public NotificationInfo getNotificationInfo() {
//                        return new NotificationInfo("Sling", "Sling Deployment Checks", "");
//                    }
//
//                    public void run(@NotNull final ProgressIndicator indicator) {
//                        //AS TODO: Check if there is a new version of IntelliJ CE that would allow to use
//                        //AS TODO: the ProgressAdapter.
//                        //AS TODO: Or create another Interface / Wrapper to make it IDE independent
//                        indicator.setIndeterminate(false);
//                        indicator.pushState();
                        ApplicationManager.getApplication().runWriteAction(
                            new Runnable() {
                                public void run() {
                                    try {
                                        final String description = AEMBundle.message("deploy.configuration.action.description");

//                                        indicator.setText(description);
//                                        indicator.setFraction(0.0);

                                        IServer server = new IServer(currentModule.getParent());
                                        String path = file.getPath();
                                        String modulePath = currentModule.getModuleProject().getModuleDirectory();
                                        String relativePath = path.substring(modulePath.length());
                                        if(relativePath.startsWith("/")) {
                                            relativePath = relativePath.substring(1);
                                        }
                                        IPath projectRelativePath = new IPath(relativePath);
                                        IProject iProject = new IProject(currentModule);
                                        SerializationManager serializationManager = ServiceManager.getService(SerializationManager.class);


                                        try {
                                            ImportRepositoryContentManager importManager = new ImportRepositoryContentManager(
                                                server,
                                                projectRelativePath,
                                                iProject,
                                                serializationManager
                                            );
                                            importManager.doImport(new NullProgressMonitor());
                                        } catch(InvocationTargetException e) {
                                            e.printStackTrace();
                                        } catch(InterruptedException e) {
                                            e.printStackTrace();
                                        } catch(SerializationException e) {
                                            e.printStackTrace();
                                        } catch(CoreException e) {
                                            e.printStackTrace();
                                        }
                                    } finally {
//                                        indicator.popState();
                                    }
                                }
                            }
                        );
//                    }
//                }
//            );
        }
    }
}