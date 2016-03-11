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

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration.ServerStatus;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
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
 * Created by schaefa on 6/12/15.
 */
public class StartRunConnectionAction extends AbstractProjectAction {

    public StartRunConnectionAction() {
        super("check.configuration.action");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        doRun(project, dataContext);
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        return connectionManager != null && connectionManager.isConnectionNotInUse();
    }

    protected boolean isAsynchronous() {
        return true;
    }

    public void doRun(final Project project, final DataContext dataContext) {
        final SlingServerTreeSelectionHandler selectionHandler = project.getComponent(SlingServerTreeSelectionHandler.class);
        final ServerConnectionManager serverConnectionManager = project.getComponent(ServerConnectionManager.class);
        final String title = AEMBundle.message("check.configuration.action.text");
        final String description = AEMBundle.message("check.configuration.action.description");

        // First Run the Verifier
        ActionManager actionManager = ActionManager.getInstance();
        VerifyConfigurationAction verifyConfigurationAction = (VerifyConfigurationAction) actionManager.getAction("AEM.Verify.Configuration.Action");
        boolean verifiedOk = true;
        if(verifyConfigurationAction != null) {
            try {
                verifiedOk = verifyConfigurationAction.doVerify(project, SimpleDataContext.getSimpleContext(VerifyConfigurationAction.VERIFY_CONTENT_WITH_WARNINGS, false, dataContext));
            } catch(Exception e) {
                // Catch and report unexpected exception as debug message to keep it going
                getMessageManager(project).sendDebugNotification("Verification failed due to unexpected exception: " + e);
            }
        }
        if(verifiedOk) {
            ProgressManager.getInstance().run(
                // The Task is moved to the background to free up the Dispatcher Thread and the toolbar is unlock when the background task ends
                new Task.Backgroundable(project, title, false) {
                    @Nullable
                    public NotificationInfo getNotificationInfo() {
                        return new NotificationInfo("Sling", "Sling Deployment Checks", "");
                    }

                    public void run(@NotNull final ProgressIndicator indicator) {
                        indicator.setIndeterminate(false);
                        indicator.pushState();
                        try {
                            indicator.setText(description);
                            indicator.setFraction(0.0);
                            ApplicationManager.getApplication().runReadAction(new Runnable() {
                                public void run() {
                                    if (!serverConnectionManager.checkSelectedServerConfiguration(true, false)) {
                                        return;
                                    }
                                    ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                                    //AS TODO: this is not showing if the check is short but if it takes longer it will update
                                    indicator.setFraction(0.1);
                                    serverConnectionManager.updateServerStatus(serverConfiguration.getName(), ServerConfiguration.ServerStatus.checking);
                                    indicator.setFraction(0.2);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }

                                    indicator.setFraction(0.3);
                                    OsgiClient osgiClient = serverConnectionManager.obtainSGiClient();
                                    if (osgiClient != null) {
                                        indicator.setFraction(0.4);
                                        ServerConnectionManager.BundleStatus status = serverConnectionManager.checkAndUpdateSupportBundle(false);
                                        if (status != ServerConnectionManager.BundleStatus.failed) {
                                            // If a Module is selected then check only this one
                                            indicator.setFraction(0.6);
                                            ServerConfiguration.Module module = selectionHandler.getCurrentModuleConfiguration();
                                            indicator.setFraction(0.7);
                                            if (module != null) {
                                                // Handle Module only
                                                serverConnectionManager.checkModule(osgiClient, module);
                                            } else {
                                                // Handle entire Project
                                                serverConnectionManager.checkModules(osgiClient);
                                            }
                                            indicator.setFraction(1.0);
                                            serverConnectionManager.updateServerStatus(serverConfiguration.getName(), ServerConfiguration.ServerStatus.running);
                                        }
                                    } else {
                                        serverConnectionManager.updateServerStatus(serverConfiguration.getName(), ServerStatus.failed);
                                    }
                                }
                            });
                        } finally {
                            indicator.popState();
                            unlock(project);
                        }
                    }
                }
            );
        } else {
            // If verification failed we need to unlock here
            unlock(project);
        }
    }
}
