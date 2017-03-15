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

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.general.AEMPluginConfiguration;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.headwire.aem.tooling.intellij.util.ExecutionUtil.WaitableRunner;
import static com.headwire.aem.tooling.intellij.util.ExecutionUtil.runAndWait;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/12/15.
 */
public abstract class AbstractConnectionAction
    extends AbstractProjectAction
{
    public AbstractConnectionAction(@NotNull String textId) {
        super(textId);
    }

    /**
     * Prepares the Deployment by running the Verification and the Purge if the Server Connection changed
     *
     * @param project The Current Project
     */
    protected boolean prepareDeployment(@Nullable Project project, @Nullable final DataContext dataContext, final ProgressHandler progressHandler) {
        boolean isVerified = true;
        if(project != null) {
            isVerified = doVerify(project, dataContext, progressHandler);
            if(isVerified) {
                // Find the Plugin Configuration, obtain the last Server Configuration used, compare and it they do not match purge cache
                final SlingServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
                ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                if(serverConfiguration != null) {
                    AEMPluginConfiguration pluginConfiguration = ComponentProvider.getComponent(project, AEMPluginConfiguration.class);
                    String lastUsedServerConfiguration = pluginConfiguration != null ? pluginConfiguration.getLastUsedServerConfiguration() : "";
                    if(lastUsedServerConfiguration != null && lastUsedServerConfiguration.length() > 0 && !lastUsedServerConfiguration.equals(serverConfiguration.getName())) {
                        doPurge(project, progressHandler);
                        // Set new Server Configuration as last used
                        if(pluginConfiguration != null) {
                            pluginConfiguration.setLastUsedServerConfiguration(serverConfiguration.getName());
                        }
                    } else if(lastUsedServerConfiguration == null || lastUsedServerConfiguration.length() == 0) {
                        // Set this Server Configuration as last used
                        if(pluginConfiguration != null) {
                            pluginConfiguration.setLastUsedServerConfiguration(serverConfiguration.getName());
                        }
                    }
                }
            }
        }
        return isVerified;
    }

    protected void doPurge(@NotNull final Project project, @NotNull final ProgressHandler progressHandler) {
        // First Run the Purge Cache
        ActionManager actionManager = ActionManager.getInstance();
        final ResetConfigurationAction resetConfigurationAction = (ResetConfigurationAction) actionManager.getAction("AEM.Purge.Cache.Action");
        WaitableRunner<Void> runner;
        if(resetConfigurationAction != null) {
            runner = new WaitableRunner<Void>() {
                @Override
                public void run() {
                    resetConfigurationAction.doReset(project, progressHandler);
                }
                @Override
                public void handleException(Exception e) {
                    // Catch and report unexpected exception as debug message to keep it going
                    getMessageManager(project).sendErrorNotification("server.configuration.purge.failed.unexpected", e);
                }
                @Override
                public boolean isAsynchronous() {
                    return AbstractConnectionAction.this.isAsynchronous();
                }
            };
            runAndWait(runner);
        }
    }
}
