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
import com.headwire.aem.tooling.intellij.config.ServerConfiguration.ServerStatus;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.sling.ide.osgi.OsgiClient;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/12/15.
 */
public class StartRunConnectionAction extends AbstractConnectionAction {

    public StartRunConnectionAction() {
        super("action.check.configuration");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext, final ProgressHandler progressHandler) {
        doRun(project, dataContext, progressHandler);
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        return connectionManager != null && connectionManager.isConnectionNotInUse();
    }

    public void doRun(final Project project, final DataContext dataContext, final ProgressHandler progressHandler) {
        final SlingServerTreeSelectionHandler selectionHandler = ServiceManager.getService(project, SlingServerTreeSelectionHandler.class);
        final ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        if(selectionHandler != null && serverConnectionManager != null) {
            final String description = AEMBundle.message("action.check.configuration.description");

            final ProgressHandler progressHandlerSubTask = progressHandler.startSubTasks(9, "progress.start.run.connection");

            boolean verified = prepareDeployment(project, dataContext, progressHandlerSubTask);
            if(verified) {
                progressHandlerSubTask.next("progress.get.current.server.configuration");
                if(!serverConnectionManager.checkSelectedServerConfiguration(true, false)) {
                    return;
                }
                ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                //AS TODO: this is not showing if the check is short but if it takes longer it will update
                progressHandlerSubTask.next("progress.update.server.status");
                serverConnectionManager.updateServerStatus(serverConfiguration.getName(), ServerConfiguration.ServerStatus.checking);
                progressHandlerSubTask.next("progress.update.server.status.wait.cycle");
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e1) {
                    e1.printStackTrace();
                }
                progressHandlerSubTask.next("progress.obtain.osgi.client");
                OsgiClient osgiClient = serverConnectionManager.obtainOSGiClient();
                if(osgiClient != null) {
                    progressHandlerSubTask.next("progress.check.support.bundle");
                    ServerConnectionManager.BundleStatus status = serverConnectionManager.checkAndUpdateSupportBundle(false);
                    if(status != ServerConnectionManager.BundleStatus.failed) {
                        // If a Module is selected then check only this one
                        progressHandlerSubTask.next("progress.obtain.module");
                        ServerConfiguration.Module module = selectionHandler.getCurrentModuleConfiguration();
                        progressHandlerSubTask.next("progress.check.modules");
                        if(module != null) {
                            // Handle Module only
                            serverConnectionManager.checkModule(osgiClient, module);
                        } else {
                            // Handle entire Project
                            serverConnectionManager.checkModules(osgiClient);
                        }
                        progressHandlerSubTask.next("progress.update.server.status.to.running");
                        serverConnectionManager.updateServerStatus(serverConfiguration.getName(), ServerConfiguration.ServerStatus.running);
                    }
                } else {
                    progressHandlerSubTask.next("progress.update.server.status.to.failed");
                    serverConnectionManager.updateServerStatus(serverConfiguration.getName(), ServerStatus.failed);
                }
            }
        }
    }
}
