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

package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;

import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.io.ServiceProvider4IntelliJ;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import org.apache.sling.ide.io.ServiceFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.BorderLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

/**
 * This is the Class that sets up the Plugin with all its features. These are its components:
 * 1) Toolbar which uses Actions to perform its features
 * 2) Sling Server Tree (list of servers and the state of the project moodules on there)
 * 3) Sling Server Configuration (persistet configuration of remote AEM / Sling Servers)
 *
 * Created by Andreas Schaefer (Headwire.com) on 3/19/15.
 */
public class SlingPluginExplorer
    extends SimpleToolWindowPanel
    implements DataProvider, Disposable
{

    private Logger logger = Logger.getInstance(this.getClass());

    private Project project;
    private Tree tree;
    private ServerConnectionManager serverConnectionManager;
    private ServerConfigurationManager serverConfigurationManager;
    private MessageManager messageManager;

    public SlingPluginExplorer(final Project project) {
        super(true, true);
        logger.info("Info message for SSE");
        logger.debug("Debug message for SSE");
        this.project = project;
        serverConfigurationManager = ComponentProvider.getComponent(project, ServerConfigurationManager.class);

        SlingServerTreeManager slingServerTreeManager = ComponentProvider.getComponent(project, SlingServerTreeManager.class);
        if(slingServerTreeManager == null) {
            messageManager.showAlert("Failure", "Failure to find Server Tree Manager");
        }
        tree = slingServerTreeManager.getTree();
        setToolbar(createToolbarPanel(slingServerTreeManager));
        setContent(ScrollPaneFactory.createScrollPane(tree));
        ToolTipManager.sharedInstance().registerComponent(tree);
        RunExecutionMonitor.getInstance(project);
        messageManager = ComponentProvider.getComponent(this.project, MessageManager.class);
        tree.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent containerEvent) {
                messageManager.sendDebugNotification("debug.tree.container.listener.component.added", containerEvent);
            }

            @Override
            public void componentRemoved(ContainerEvent containerEvent) {
                //AS TODO: Cannot send a Debug Notification as the Project is torn down
//                messageManager.sendDebugNotification("Container Remove Event: " + containerEvent);
            }
        });
        // Set the Service Provider instance for IntelliJ
        ServiceFactory.setServiceProvider(new ServiceProvider4IntelliJ());
    }

    public void dispose() {
        if(project != null) {
            RunExecutionMonitor.disposeInstance(project);
        }
        project = null;
    }

    private JPanel createToolbarPanel(SlingServerTreeManager treeManager) {
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(actionManager.getAction("AEM.Toolbar"));
        treeManager.adjustToolbar(group);

        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true);

        final JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
        return buttonsPanel;
    }

    @Nullable
    public Object getData(@NonNls String dataId) {
        return super.getData(dataId);
    }
}
