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

import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerAdapter;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.w3c.dom.Document;
import org.xhtmlrenderer.event.DefaultDocumentListener;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.DelegatingUserAgent;
import org.xhtmlrenderer.swing.ImageResourceLoader;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
import org.xhtmlrenderer.util.GeneralUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by Andreas Schaefer (Headwire.com) on 3/19/15.
 */
public class SlingPluginToolWindowFactory
    implements ToolWindowFactory
{
    private static final Logger LOGGER = Logger.getInstance(SlingPluginToolWindowFactory.class);

    public void createToolWindowContent(final Project project, final ToolWindow toolWindow) {
        // Plugin is now placed into a Tabbed Pane to add additional views to it
        JBTabbedPane master = new JBTabbedPane(SwingConstants.BOTTOM);
        SlingPluginExplorer explorer = new SlingPluginExplorer(project);
        LOGGER.debug("CTWC: Explorer: '" + explorer + "'");
        master.insertTab("Plugin", null, explorer, "Plugin Windows", 0);

        WebContentFXPanel info = new WebContentFXPanel();
        master.insertTab("Info", null, info, "Plugin Info", 1);

        final AemdcPanel aemdcPanel = ComponentProvider.getComponent(project, AemdcPanel.class);
        LOGGER.debug("AEMDC Panel found: '{}'", aemdcPanel);
        aemdcPanel.setContainer(master);

        final ContentManager contentManager = toolWindow.getContentManager();
        final Content content = contentManager.getFactory().createContent(master, null, false);
        contentManager.addContent(content);
        Disposer.register(project, explorer);

        final ToolWindowManagerAdapter listener = new ToolWindowManagerAdapter() {
            boolean wasVisible = false;

            @Override
            public void stateChanged() {
                if (toolWindow.isDisposed()) {
                    return;
                }
                boolean visible = toolWindow.isVisible();
                // If the Plugin became visible then we let the AEMDC Panel know to recrate the JFX Panel
                // to avoid the double buffering
                if(!wasVisible && visible) {
                    aemdcPanel.reset();
                }
                wasVisible = visible;
            }
        };
        final ToolWindowManagerEx manager = ToolWindowManagerEx.getInstanceEx(project);
        manager.addToolWindowManagerListener(listener, project);
    }
}
