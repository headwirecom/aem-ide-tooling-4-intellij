/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.config.UnifiedModule;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.eclipse.ServerUtil;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.io.SlingResource4IntelliJ;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.io.NewResourceChangeCommandFactory;
import org.apache.sling.ide.io.ServiceFactory;
import org.apache.sling.ide.io.SlingResource;
import org.apache.sling.ide.serialization.SerializationManager;
import org.apache.sling.ide.transport.Repository;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;

/**
 * Created by Andreas Schaefer (Headwire.com) on 2/13/16.
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

        public String toString() {
            return "File Wrapper for: " + getPath();
        }
    }

    public class IntelliJModuleWrapper
        extends ModuleWrapper
    {
        public IntelliJModuleWrapper(Module module, Project project) {
            super(module, new ProjectWrapper(project));
        }

        public String getName() {
            return getModule().getName();
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
            UnifiedModule unifiedModule = getModule().getUnifiedModule();
            List<String> contentDirectoryPaths = unifiedModule.getContentDirectoryPaths();
            for(String basePath: contentDirectoryPaths) {
                messageManager.sendDebugNotification("debug.content.base.path", basePath);
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
                ServiceManager.getService(SerializationManager.class)
            )
        );
        messageManager = ServiceManager.getService(project, MessageManager.class);
        serverConfigurationManager = ServiceManager.getService(project, ServerConfigurationManager.class);
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

    @Override
    String getMessage(String messageId, Object... parameters) {
        return AEMBundle.message(messageId, parameters);
    }
}
