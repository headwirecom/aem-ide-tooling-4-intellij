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
    extends AbstractDeploymentManager
{

    public static class IntelliJFileWrapper
        extends FileWrapper<VirtualFile>
    {
        public IntelliJFileWrapper(VirtualFile file) {
            super(file);
        }

        public FileWrapper<VirtualFile> getParent() {
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
    }

    public static class IntelliJModuleWrapper
        extends ModuleWrapper<Module, Project>
    {
        public IntelliJModuleWrapper(Module module, Project project) {
            super(module, new ProjectWrapper(project));
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
        messageManager.showAlert(type, title, message);
    }

    @Override
    Repository obtainRepository(ModuleWrapper module) {
        Module rawModule = (Module) module.getModule();
        return ServerUtil.getConnectedRepository(
            new IServer(getModule(module).getParent()), new NullProgressMonitor(), messageManager
        );
    }

    private Module getModule(ModuleWrapper wrapper) {
        return (Module) wrapper.getModule();
    }

    @Override
    void updateModuleStatus(ModuleWrapper module, SynchronizationStatus synchronizationStatus) {
        Module rawModule = getModule(module);
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

    @Override
    List<String> findContentResources(ModuleWrapper module, String filePath) {
        List<String> ret = new ArrayList<String>();
        ModuleProject moduleProject = getModule(module).getModuleProject();
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

    @Override
    SlingResource obtainSlingResource(ModuleWrapper moduleWrapper, FileWrapper file) {
        return new SlingResource4IntelliJ(getModule(moduleWrapper).getSlingProject(), (VirtualFile) file.getFile());
    }

    @Override
    FileWrapper obtainResourceFile(ModuleWrapper module, String resourcePath) {
        Module rawModule = getModule(module);
        VirtualFile baseFile = rawModule.getProject().getBaseDir();
        return new IntelliJFileWrapper(baseFile.getFileSystem().findFileByPath(resourcePath));
    }

    @Override
    void getChangedResourceList(FileWrapper resource, List<FileWrapper> resourceList) {
        VirtualFile rawResource = (VirtualFile) resource.getFile();
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

    @Override
    void setModuleLastModificationTimestamp(ModuleWrapper module, long timestamp) {
        Module rawModule = getModule(module);
        rawModule.setLastModificationTimestamp(timestamp);
    }

    @Override
    String getMessage(String messageId, Object... parameters) {
        return AEMBundle.message(messageId, parameters);
    }
}
