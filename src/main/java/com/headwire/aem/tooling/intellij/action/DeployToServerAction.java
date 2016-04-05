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
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/13/15.
 */
public class DeployToServerAction
        extends AbstractProjectAction
{

    public DeployToServerAction(@NotNull String textId) {
        super(textId);
    }

    public DeployToServerAction() {
        this("action.deploy.configuration");
    }

    protected boolean isForced() {
        return false;
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext, final ProgressHandler progressHandler) {
        doDeploy(dataContext, project, isForced(), progressHandler);
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        return connectionManager != null && connectionManager.isConnectionInUse();
    }

    private void doDeploy(final DataContext dataContext, final Project project, final boolean forceDeploy, final ProgressHandler progressHandler) {
        ProgressHandler aProgressHandler = progressHandler.startSubTasks(5, progressHandler.getTitle());
        final ServerConnectionManager connectionManager = getConnectionManager(project);
        final SlingServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        final String description = AEMBundle.message("action.deploy.configuration.description");
        aProgressHandler.next(description);
        // There is no Run Connection to be made to the AEM Server like with DEBUG (no HotSwap etc).
        // So we just need to setup a connection to the AEM Server to handle OSGi Bundles and Sling Packages
        ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
        aProgressHandler.next("Update Status");
        //AS TODO: this is not showing if the check is short but if it takes longer it will update
        connectionManager.updateStatus(serverConfiguration, ServerConfiguration.SynchronizationStatus.updating);
        aProgressHandler.next("Update Status, wait");
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e1) {
            e1.printStackTrace();
        }
        aProgressHandler.next("Get Bundle Status");
        // First Check if the Install Support Bundle is installed
        ServerConnectionManager.BundleStatus bundleStatus = connectionManager.checkAndUpdateSupportBundle(false);
        ServerConfiguration.Module module = selectionHandler.getCurrentModuleConfiguration();
        aProgressHandler.next("Build and Deploy " + (module != null ? "All" : "Single") + " Module");
        if(module != null) {
            // Deploy only the selected Module
            connectionManager.deployModule(dataContext, module, forceDeploy, aProgressHandler);
        } else {
            // Deploy all Modules of the Project
            connectionManager.deployModules(dataContext, forceDeploy, aProgressHandler);
        }
        progressHandler.next("Done");
    }
}
