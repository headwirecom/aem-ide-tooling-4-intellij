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
import com.headwire.aem.tooling.intellij.explorer.AemdcPanel;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/12/15.
 */
public class AemDcPanelAction extends AbstractProjectAction {

    public AemDcPanelAction() {
        super("action.aem.dc.panel");
    }

    @Override
    protected boolean isAsynchronous() {
        return false;
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext, final ProgressHandler progressHandler) {
    }

    // Had to override this method in order to gain access to the Mouse / Keyboard Modifiers
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getProject();
        if(project != null && !isLocked(project)) {
            lock(project);
            try {
                toggle(project, new ProgressHandlerImpl(AEMBundle.message("action.aem.dc.panel.text")), (event.getModifiers() & MouseEvent.ALT_MASK) != 0);
            } finally {
                unlock(project);
            }
        }
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        return serverConnectionManager != null && serverConnectionManager.isConfigurationSelected();
    }

    public void toggle(final Project project, final ProgressHandler progressHandler, boolean showDialog) {
        SlingServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        if(selectionHandler != null && serverConnectionManager != null) {
            ServerConfiguration source = selectionHandler.getCurrentConfiguration();
            if(source != null) {
                // Before we can verify we need to ensure the Configuration is properly bound to Maven
                serverConnectionManager.checkBinding(source, progressHandler);
                AemdcPanel aemdcPanel = ServiceManager.getService(project, AemdcPanel.class);
                if(aemdcPanel != null) {
                    if(!showDialog) {
                        aemdcPanel.display(!aemdcPanel.isShown());
                    } else {
                        aemdcPanel.showDialog();
                    }
                }
            }
        }
    }
}
