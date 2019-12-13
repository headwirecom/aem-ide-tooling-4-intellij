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

package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/12/15.
 */
public class ResetConfigurationAction extends AbstractProjectAction {

    public ResetConfigurationAction() {
        super("action.purge.cache");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext, final ProgressHandler progressHandler) {
        doReset(project, progressHandler);
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        return serverConnectionManager != null && serverConnectionManager.isConfigurationSelected();
    }

    public void doReset(final Project project, final ProgressHandler progressHandler) {
        SlingServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        if(selectionHandler != null && serverConnectionManager != null) {
            ServerConfiguration source = selectionHandler.getCurrentConfiguration();
            if(source != null) {
                // Before we can verify we need to ensure the Configuration is properly bound to Maven
                serverConnectionManager.checkBinding(source, progressHandler);
                // Verify each Module to see if all prerequisites are met
                getMessageManager(project).sendInfoNotification("purge.cache.begin");
                for(ServerConfiguration.Module module: source.getModuleList()) {
                    if(module.isSlingPackage()) {
                        getMessageManager(project).sendInfoNotification("purge.cache.start", module.getName());
                        // Check if the Content Modules have a Content Resource
                        List<String> resourceList = serverConnectionManager.findContentResources(module);
                        for(String contentPath: resourceList) {
                            VirtualFile contentResourceDirectory = project.getBaseDir().getFileSystem().findFileByPath(contentPath);
                            if(contentResourceDirectory != null) {
                                Util.resetModificationStamp(contentResourceDirectory, true);
                            }
                        }
                        getMessageManager(project).sendInfoNotification("purge.cache.end", module.getName());
                    }
                }
                getMessageManager(project).sendInfoNotification("purge.cache.finish");
            }
        }
    }
}
