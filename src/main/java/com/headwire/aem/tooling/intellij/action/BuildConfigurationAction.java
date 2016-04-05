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
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.ui.BuildSelectionDialog;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/26/15.
 */
public class BuildConfigurationAction
    extends AbstractProjectAction
{
    public BuildConfigurationAction() {
        super("action.build.configuration");
    }

    @Override
    protected boolean isAsynchronous() {
        return false;
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext, final ProgressHandler progressHandler) {
        SlingServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        ServerConnectionManager serverConnectionManager = ComponentProvider.getComponent(project, ServerConnectionManager.class);
        ServerConfigurationManager configurationManager = ComponentProvider.getComponent(project, ServerConfigurationManager.class);
        if(selectionHandler != null && serverConnectionManager != null && configurationManager != null) {
            ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
            BuildSelectionDialog dialog = new BuildSelectionDialog(project, serverConfiguration);
            if(dialog.showAndGet()) {
                // Modules might have changed and so update the tree
                configurationManager.updateServerConfiguration(serverConfiguration);
            }
        }
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager serverConnectionManager = ComponentProvider.getComponent(project, ServerConnectionManager.class);
        return serverConnectionManager != null && serverConnectionManager.isConfigurationSelected();
    }
}
