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

import com.headwire.aem.tooling.intellij.communication.ImportRepositoryContentManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IPath;
import com.headwire.aem.tooling.intellij.eclipse.stub.IProject;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.serialization.SerializationException;
import org.apache.sling.ide.serialization.SerializationManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/18/15.
 */
public class ImportFromServerAction extends AbstractProjectAction {

    @Override
    public boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        boolean ret = false;
        ServerConnectionManager serverConnectionManager = ComponentProvider.getComponent(project, ServerConnectionManager.class);
        if(serverConnectionManager.isConfigurationSelected()) {
            // Now check if a file is selected
            VirtualFile[] virtualFiles = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
            if(virtualFiles == null || virtualFiles.length == 0) {
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
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext, final ProgressHandler progressHandler) {
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

    private void doImport(final Project project, final VirtualFile file) {
        final ServerConnectionManager serverConnectionManager = ComponentProvider.getComponent(project, ServerConnectionManager.class);
        final SlingServerTreeSelectionHandler selectionHandler = ComponentProvider.getComponent(project, SlingServerTreeSelectionHandler.class);
        if(!serverConnectionManager.checkSelectedServerConfiguration(true, false)) {
            return;
        }
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        serverConnectionManager.checkBinding(serverConfiguration, new ProgressHandlerImpl("Do Import from Server"));
        List<ServerConfiguration.Module> moduleList = selectionHandler.getModuleDescriptorListOfCurrentConfiguration();
        ServerConfiguration.Module currentModuleLookup = null;
        for(ServerConfiguration.Module module: moduleList) {
            if(module.isSlingPackage()) {
                String contentPath = serverConnectionManager.findContentResource(module, file.getPath());
                if(contentPath != null) {
                    // This file belongs to this module so we are good to publish it
                    currentModuleLookup = module;
                    break;
                }
            }
        }
        if(currentModuleLookup != null) {
            final ServerConfiguration.Module currentModule = currentModuleLookup;
                ApplicationManager.getApplication().runWriteAction(
                    new Runnable() {
                        public void run() {
                            try {
                                final String description = AEMBundle.message("action.deploy.configuration.description");
                                IServer server = new IServer(currentModule.getParent());
                                String path = file.getPath();
                                String modulePath = currentModule.getUnifiedModule().getModuleDirectory();
                                String relativePath = path.substring(modulePath.length());
                                if(relativePath.startsWith("/")) {
                                    relativePath = relativePath.substring(1);
                                }
                                IPath projectRelativePath = new IPath(relativePath);
                                IProject iProject = new IProject(currentModule);
                                SerializationManager serializationManager = ComponentProvider.getComponent(project, SerializationManager.class);


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
                            }
                        }
                    }
                );
        }
    }
}
