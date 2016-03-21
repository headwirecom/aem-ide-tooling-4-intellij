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
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.apache.sling.ide.osgi.OsgiClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/12/15.
 */
public class StartRunConnectionAction extends AbstractProjectAction {

    public StartRunConnectionAction() {
        super("check.configuration.action");
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
        final SlingServerTreeSelectionHandler selectionHandler = ComponentProvider.getComponent(project, SlingServerTreeSelectionHandler.class);
        final ServerConnectionManager serverConnectionManager = ComponentProvider.getComponent(project, ServerConnectionManager.class);
        if(selectionHandler != null && serverConnectionManager != null) {
            final String description = AEMBundle.message("check.configuration.action.description");

            // First Run the Verifier
            ActionManager actionManager = ActionManager.getInstance();
            VerifyConfigurationAction verifyConfigurationAction = (VerifyConfigurationAction) actionManager.getAction("AEM.Verify.Configuration.Action");
            boolean verifiedOk = true;
            ProgressHandler progressHandlerSubTask = progressHandler.startSubTasks(9, "Start Run Connection");
            if(verifyConfigurationAction != null) {
                try {
                    progressHandlerSubTask.next("Do Verify");
                    verifiedOk = verifyConfigurationAction.doVerify(project, SimpleDataContext.getSimpleContext(VerifyConfigurationAction.VERIFY_CONTENT_WITH_WARNINGS, false, dataContext), progressHandlerSubTask);
                } catch(Exception e) {
                    // Catch and report unexpected exception as debug message to keep it going
                    getMessageManager(project).sendDebugNotification("Verification failed due to unexpected exception: " + e);
                }
            }
            if(verifiedOk) {
                progressHandlerSubTask.next("Get Current Server Configuration");
                if(!serverConnectionManager.checkSelectedServerConfiguration(true, false)) {
                    return;
                }
                ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                //AS TODO: this is not showing if the check is short but if it takes longer it will update
                progressHandlerSubTask.next("Update Server Status");
                serverConnectionManager.updateServerStatus(serverConfiguration.getName(), ServerConfiguration.ServerStatus.checking);
                progressHandlerSubTask.next("Update Server Status, Wait");
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e1) {
                    e1.printStackTrace();
                }
                progressHandlerSubTask.next("Obtain OSGi Client");
                OsgiClient osgiClient = serverConnectionManager.obtainSGiClient();
                if(osgiClient != null) {
                    progressHandlerSubTask.next("Check and Update Support Bundle");
                    ServerConnectionManager.BundleStatus status = serverConnectionManager.checkAndUpdateSupportBundle(false);
                    if(status != ServerConnectionManager.BundleStatus.failed) {
                        // If a Module is selected then check only this one
                        progressHandlerSubTask.next("Check and Update Support Bundle");
                        ServerConfiguration.Module module = selectionHandler.getCurrentModuleConfiguration();
                        progressHandlerSubTask.next("Check Module(s)");
                        if(module != null) {
                            // Handle Module only
                            serverConnectionManager.checkModule(osgiClient, module);
                        } else {
                            // Handle entire Project
                            serverConnectionManager.checkModules(osgiClient);
                        }
                        progressHandlerSubTask.next("Update Server Status (Running)");
                        serverConnectionManager.updateServerStatus(serverConfiguration.getName(), ServerConfiguration.ServerStatus.running);
                    }
                } else {
                    progressHandlerSubTask.next("Update Server Status (Failed)");
                    serverConnectionManager.updateServerStatus(serverConfiguration.getName(), ServerStatus.failed);
                }
            }
        }
    }
}
