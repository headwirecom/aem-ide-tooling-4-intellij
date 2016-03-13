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
import com.intellij.execution.RunManagerEx;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/13/15.
 */
public class StartDebugConnectionAction
    extends AbstractProjectAction
{

    public StartDebugConnectionAction() {
        super("debug.configuration.action");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext, @NotNull final ProgressIndicator indicator) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        if(connectionManager != null) {
            doDebug(project, connectionManager);
        }
    }

    public void doDebug(Project project, ServerConnectionManager connectionManager) {
        RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
        if(runManager != null) {
            connectionManager.connectInDebugMode(runManager);
        } else {
            getMessageManager(project).showAlert("debug.configuration.action.failure");
        }
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        return connectionManager != null && connectionManager.isConnectionNotInUse();
    }
}
