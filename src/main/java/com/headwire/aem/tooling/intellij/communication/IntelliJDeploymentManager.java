package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.config.ModuleProject;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.eclipse.ServerUtil;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.io.SlingResource4IntelliJ;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.io.NewResourceChangeCommandFactory;
import org.apache.sling.ide.io.ServiceFactory;
import org.apache.sling.ide.io.SlingResource;
import org.apache.sling.ide.serialization.SerializationManager;
import org.apache.sling.ide.transport.Repository;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;

/**
 * Created by schaefa on 2/13/16.
 */
public class IntelliJDeploymentManager
    extends AbstractDeploymentManager<Module, Project, VirtualFile>
{

    public class IntelliJFileWrapper
        extends FileWrapper
    {
        public IntelliJFileWrapper(VirtualFile file) {
            super(file);
        }

        public FileWrapper getParent() {
            return new IntelliJFileWrapper(getFile().getParent());
        }

        public String getPath() {
            return getFile().getPath();
        }

        public void setModificationTimestamp() {
            Util.setModificationStamp(getFile());
        }

        public long getModificationTimestamp() {
            return Util.getModificationStamp(getFile());
        }

        public long getTimestamp() {
            return getFile().getTimeStamp();
        }

        @Override
        public void getChangedResourceList(List<FileWrapper> resourceList) {
            VirtualFile rawResource = getFile();
            getChangedResourceList(rawResource, resourceList);
        }

        void getChangedResourceList(VirtualFile resource, List<FileWrapper> resourceList) {
            if(resource.isDirectory()) {
                List<VirtualFile> children = Arrays.asList(resource.getChildren());
                for(VirtualFile child : children) {
                    getChangedResourceList(child, resourceList);
                }
            } else {
                resourceList.add(new IntelliJFileWrapper(resource));
            }
        }
    }

    public class IntelliJModuleWrapper
        extends ModuleWrapper
    {
        public IntelliJModuleWrapper(Module module, Project project) {
            super(module, new ProjectWrapper(project));
        }

        public Repository obtainRepository() {
            return ServerUtil.getConnectedRepository(
                new IServer(getModule().getParent()), new NullProgressMonitor(), messageManager
            );
        }

        public void updateModuleStatus(SynchronizationStatus synchronizationStatus) {
            Module rawModule = getModule();
            if(rawModule != null) {
                ServerConfiguration.SynchronizationStatus status;
                switch(synchronizationStatus) {
                    case updating:
                        status = ServerConfiguration.SynchronizationStatus.updating;
                        break;
                    case upToDate:
                        status = ServerConfiguration.SynchronizationStatus.upToDate;
                        break;
                    case failed:
                        status = ServerConfiguration.SynchronizationStatus.failed;
                        break;
                    default:
                        status = ServerConfiguration.SynchronizationStatus.unsupported;
                }
                rawModule.setStatus(status);
                serverConfigurationManager.updateServerConfiguration(rawModule.getParent());
            }
        }

        public List<String> findContentResources(String filePath) {
            List<String> ret = new ArrayList<String>();
            ModuleProject moduleProject = getModule().getModuleProject();
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

        public SlingResource obtainSlingResource(FileWrapper file) {
            return new SlingResource4IntelliJ(getModule().getSlingProject(), file.getFile());
        }

        public FileWrapper obtainResourceFile(String resourcePath) {
            Module rawModule = getModule();
            VirtualFile baseFile = rawModule.getProject().getBaseDir();
            return new IntelliJFileWrapper(baseFile.getFileSystem().findFileByPath(resourcePath));
        }

        public void setModuleLastModificationTimestamp(long timestamp) {
            Module rawModule = getModule();
            rawModule.setLastModificationTimestamp(timestamp);
        }
    }

    private MessageManager messageManager;
    private ServerConfigurationManager serverConfigurationManager;

    public IntelliJDeploymentManager(@NotNull Project project) {
        super(
            new NewResourceChangeCommandFactory(
                project.getComponent(SerializationManager.class)
            )
        );
        messageManager = project.getComponent(MessageManager.class);
        serverConfigurationManager = project.getComponent(ServerConfigurationManager.class);
    }

    @Override
    void sendMessage(MessageType messageType, String message, Object... parameters) {
        switch(messageType) {
            case DEBUG:
                if(parameters.length > 0) {
                    ServiceFactory.getPluginLogger().trace(message, parameters);
                } else {
                    messageManager.sendDebugNotification(message);
                }
                break;
            case WARNING:
                messageManager.sendNotification(message, NotificationType.WARNING);
                break;
            case INFO:
                messageManager.sendInfoNotification(message, parameters);
                break;
            case ERROR:
                messageManager.sendErrorNotification(message, parameters);
                break;
        }
    }

    @Override
    void sendAlert(MessageType messageType, String title, String message) {
        NotificationType type;
        switch(messageType) {
            case ERROR:
                type = NotificationType.ERROR;
                break;
            case WARNING:
                type = NotificationType.WARNING;
                break;
            default:
                type = NotificationType.INFORMATION;
        }
        String myTitle = AEMBundle.message(title);
        myTitle = myTitle.equals("") ? title : myTitle;
        messageManager.showAlert(type, myTitle, message);
    }

//    @Override
//    Repository obtainRepository(ModuleWrapper module) {
//        return ServerUtil.getConnectedRepository(
//            new IServer(module.getModule().getParent()), new NullProgressMonitor(), messageManager
//        );
//    }
//
//    private Module getModule(ModuleWrapper wrapper) {
//        return (Module) wrapper.getModule();
//    }

//    @Override
//    void updateModuleStatus(ModuleWrapper module, SynchronizationStatus synchronizationStatus) {
//        Module rawModule = module.getModule();
//        if(rawModule != null) {
//            ServerConfiguration.SynchronizationStatus status;
//            switch(synchronizationStatus) {
//                case updating:
//                    status = ServerConfiguration.SynchronizationStatus.updating;
//                    break;
//                case upToDate:
//                    status = ServerConfiguration.SynchronizationStatus.upToDate;
//                    break;
//                case failed:
//                    status = ServerConfiguration.SynchronizationStatus.failed;
//                    break;
//                default:
//                    status = ServerConfiguration.SynchronizationStatus.unsupported;
//            }
//            rawModule.setStatus(status);
//            serverConfigurationManager.updateServerConfiguration(rawModule.getParent());
//        }
//    }

//    @Override
//    List<String> findContentResources(ModuleWrapper module, String filePath) {
//        List<String> ret = new ArrayList<String>();
//        ModuleProject moduleProject = module.getModule().getModuleProject();
//        List<String> contentDirectoryPaths = moduleProject.getContentDirectoryPaths();
//        for(String basePath: contentDirectoryPaths) {
//            messageManager.sendDebugNotification("Content Base Path: '" + basePath + "'");
//            //AS TODO: Paths from Windows have backlashes instead of forward slashes
//            //AS TODO: It is possible that certain files are in forward slashes even on Windows
//            String myFilePath = filePath == null ? null : filePath.replace("\\", "/");
//            String myBasePath = basePath == null ? null : basePath.replace("\\", "/");
//            if(Util.pathEndsWithFolder(basePath, JCR_ROOT_FOLDER_NAME) && (myFilePath == null || myFilePath.startsWith(myBasePath))) {
//                ret.add(basePath);
//                break;
//            }
//        }
//        return ret;
//    }

//    @Override
//    SlingResource obtainSlingResource(ModuleWrapper moduleWrapper, FileWrapper file) {
//        return new SlingResource4IntelliJ(moduleWrapper.getModule().getSlingProject(), file.getFile());
//    }

//    @Override
//    FileWrapper obtainResourceFile(ModuleWrapper module, String resourcePath) {
//        Module rawModule = module.getModule();
//        VirtualFile baseFile = rawModule.getProject().getBaseDir();
//        return new IntelliJFileWrapper(baseFile.getFileSystem().findFileByPath(resourcePath));
//    }

//    @Override
//    void getChangedResourceList(FileWrapper resource, List<FileWrapper> resourceList) {
//        VirtualFile rawResource = resource.getFile();
//        getChangedResourceList(rawResource, resourceList);
//    }
//
//    void getChangedResourceList(VirtualFile resource, List<FileWrapper> resourceList) {
//        if(resource.isDirectory()) {
//            List<VirtualFile> children = Arrays.asList(resource.getChildren());
//            for(VirtualFile child : children) {
//                getChangedResourceList(child, resourceList);
//            }
//        } else {
//            resourceList.add(new IntelliJFileWrapper(resource));
//        }
//    }

//    @Override
//    void setModuleLastModificationTimestamp(ModuleWrapper module, long timestamp) {
//        Module rawModule = module.getModule();
//        rawModule.setLastModificationTimestamp(timestamp);
//    }

    @Override
    String getMessage(String messageId, Object... parameters) {
        return AEMBundle.message(messageId, parameters);
    }
}
