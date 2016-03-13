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

package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.eclipse.ProjectUtil;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.util.Constants;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.filter.FilterResult;
import org.apache.sling.ide.transport.Repository;
import org.apache.sling.ide.transport.ResourceProxy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/12/15.
 */
public class VerifyConfigurationAction extends AbstractProjectAction {

    public static final String VERIFY_CONTENT_WITH_WARNINGS = "VerifyContentWithWarnings";

    public VerifyConfigurationAction() {
        super("verify.configuration.action");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext, @NotNull final ProgressIndicator indicator) {
        DataContext wrappedDataContext = SimpleDataContext.getSimpleContext(VERIFY_CONTENT_WITH_WARNINGS, true, dataContext);
        doVerify(project, wrappedDataContext);
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager serverConnectionManager = project.getComponent(ServerConnectionManager.class);
        return serverConnectionManager != null && serverConnectionManager.isConnectionInUse();
    }

    public boolean doVerify(final Project project, final DataContext dataContext) {
        MessageManager messageManager = getMessageManager(project);
        boolean ret = true;
        int exitNow = Messages.OK;
        SlingServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        ServerConnectionManager serverConnectionManager = project.getComponent(ServerConnectionManager.class);
        ServerConfigurationManager serverConfigurationManager = getConfigurationManager(project);
        if(selectionHandler != null && serverConnectionManager != null && messageManager != null) {
            ServerConfiguration source = selectionHandler.getCurrentConfiguration();
            if(source != null) {
                try {
                    messageManager.sendInfoNotification("server.configuration.start.verification", source.getName());
                    // Before we can verify we need to ensure the Configuration is properly bound to Maven
                    List<ServerConfiguration.Module> unboundModules = null;
                    try {
                        unboundModules = serverConnectionManager.bindModules(source);
                    } catch(IllegalArgumentException e) {
                        messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.verification.failed.due.to.bind.exception", source.getName(), e.getMessage());
                        return false;
                    }
                    if(unboundModules != null && !unboundModules.isEmpty()) {
                        ret = false;
                        for(ServerConfiguration.Module module : unboundModules) {
                            exitNow = messageManager.showAlertWithOptions(NotificationType.WARNING, "server.configuration.unresolved.module", module.getName());
                            if(exitNow == 1) {
                                source.removeModule(module);
                                if(serverConfigurationManager != null) {
                                    serverConfigurationManager.updateServerConfiguration(source);
                                }
                            } else if(exitNow == Messages.CANCEL) {
                                return false;
                            }
                        }
                    }
                    // Verify each Module to see if all prerequisites are met
                    Repository repository = ServerConnectionManager.obtainRepository(source, messageManager);
                    if(repository != null) {
                        for(ServerConfiguration.Module module : source.getModuleList()) {
                            if(module.isSlingPackage()) {
                                // Check if the Filter is available for Content Modules
                                Filter filter = null;
                                try {
                                    filter = ProjectUtil.loadFilter(module);
                                    if(filter == null) {
                                        ret = false;
                                        exitNow = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.filter.file.not.found", module.getName());
                                        module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                                        if(exitNow == Messages.CANCEL) {
                                            return false;
                                        }
                                    }
                                } catch(CoreException e) {
                                    ret = false;
                                    exitNow = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.filter.file.failure", module.getName(), e.getMessage());
                                    module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                                    if(exitNow == Messages.CANCEL) {
                                        return false;
                                    }
                                }
                                // Check if the Content Modules have a Content Resource
                                List<String> resourceList = serverConnectionManager.findContentResources(module);
                                if(resourceList.isEmpty()) {
                                    ret = false;
                                    exitNow = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.content.folder.not.found", module.getName());
                                    module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                                    if(exitNow == Messages.CANCEL) {
                                        return false;
                                    }
                                }
                                // Check if Content Module Folders all have a .content.xml
                                Object temp = dataContext.getData(VERIFY_CONTENT_WITH_WARNINGS);
                                boolean verifyWithWarnings = !(temp instanceof Boolean) || ((Boolean) temp);
                                if(verifyWithWarnings && filter != null) {
                                    // Get the Content Root /jcr_root)
                                    for(String contentPath : resourceList) {
                                        VirtualFile rootFile = project.getProjectFile().getFileSystem().findFileByPath(contentPath);
                                        if(rootFile != null) {
                                            // Loop over all folders and check if .content.xml file is there
                                            Result childResult = checkFolderContent(repository, messageManager, serverConnectionManager, module, null, rootFile, filter);
                                            if(childResult.isCancelled) {
                                                return false;
                                            } else if(!childResult.isOk) {
                                                ret = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        ret = false;
                    }
                } finally {
                    messageManager.sendInfoNotification("server.configuration.end.verification", source.getName());
                }
            }
        }
        return ret;
    }

    private Result checkFolderContent(Repository repository, MessageManager messageManager, ServerConnectionManager serverConnectionManager, ServerConfiguration.Module module, File rootDirectory, VirtualFile parentDirectory, Filter filter) {
        int exitNow = Messages.OK;
        Result ret = new Result();
        // Loop over all files in the given folder
        for(VirtualFile child: parentDirectory.getChildren()) {
            // We only need to check Folders
            if(child.isDirectory()) {
                // If the Root Directory is null then just started -> Create Root Directory File and work on its children
                if(rootDirectory == null) {
                    rootDirectory = new File(parentDirectory.getPath());
                } else {
                    // Get Relative Paths
                    String relativeParentPath = parentDirectory.getPath().substring(rootDirectory.getPath().length());
                    String relativeChildPath = child.getPath().substring(rootDirectory.getPath().length());
                    List<ResourceProxy> childNodes = null;
                    FilterResult filterResult = null;
                    if(filter != null) {
                        // Check if the Resource is part of the Filter
                        filterResult = filter.filter(relativeChildPath);
                    }
                    // We don't need to check anything if it is part of one of the filter entries
                    if(filterResult != FilterResult.ALLOW) {
                        if(filterResult == FilterResult.DENY) {
                            ret.failed();
                            // File is not part of a filter entry so it will never be deployed
                            exitNow = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.content.folder.filtered.out", relativeChildPath, module.getName());
                            module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                            if(exitNow == Messages.CANCEL) {
                                return ret.doExit();
                            }
                        } else if(child.findChild(Constants.CONTENT_FILE_NAME) == null) {
                            boolean isOk = false;
                            if(repository != null && childNodes == null) {
                                // First check if there are only folders as children and if all of them are inside the filters
                                boolean isGood = true;
                                for(VirtualFile grandChild: child.getChildren()) {
                                    filterResult = filter.filter(relativeChildPath + "/" + grandChild.getName());
                                    if(filterResult != FilterResult.ALLOW) {
                                        isGood = false;
                                        break;
                                    }
                                }
                                if(isGood) {
                                    isOk = true;
                                } else {
                                    // .content.xml file not found in the filter -> check if that folder exists on the Server
                                    childNodes = serverConnectionManager.getChildrenNodes(repository, relativeParentPath);
                                    for(ResourceProxy childNode : childNodes) {
                                        String path = childNode.getPath();
                                        boolean found = path.equals(relativeChildPath);
                                        if(found) {
                                            isOk = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(!isOk) {
                                ret.failed();
                                exitNow = messageManager.showAlertWithOptions(NotificationType.ERROR, "server.configuration.content.folder.configuration.not.found", relativeChildPath, module.getName());
                                module.setStatus(ServerConfiguration.SynchronizationStatus.compromised);
                                if(exitNow == Messages.CANCEL) {
                                    return ret.doExit();
                                }
                            }
                        }
                    }
                }
                // Check the children of the current folder
                Result childResult = checkFolderContent(repository, messageManager, serverConnectionManager, module, rootDirectory, child, filter);
                if(childResult.isCancelled) {
                    return ret.doExit();
                } else if(!childResult.isOk) {
                    ret.failed();
                }
            }
        }
        return ret;
    }

    private static class Result {
        private boolean isCancelled = false;
        private boolean isOk = true;

        public Result doExit() {
            isCancelled = true;
            isOk = false;
            return this;
        }

        public Result failed() {
            isOk = false;
            return this;
        }
    }
}
