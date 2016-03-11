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

package com.headwire.aem.tooling.intellij.console;

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.ui.ConsoleLogSettingsDialog;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ContextHelpAction;
import com.intellij.notification.impl.NotificationsConfigurationImpl;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ScrollToTheEndToolbarAction;
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.AncestorEvent;

/**
 * Created by schaefa on 5/7/15.
 */
public class ConsoleLogToolWindowFactory
    implements ToolWindowFactory, DumbAware
{
    public static final String TOOL_WINDOW_ID = "AEM Console";

    @Override
    public void createToolWindowContent(@NotNull final Project project, @NotNull ToolWindow toolWindow) {
        ConsoleLog.getProjectComponent(project).initDefaultContent();
    }

    static void createContent(Project project, ToolWindow toolWindow, ConsoleLogConsole console, String title) {
        // update default Event Log tab title
        ContentManager contentManager = toolWindow.getContentManager();
        Content generalContent = contentManager.getContent(0);
        if (generalContent != null && contentManager.getContentCount() == 1) {
            generalContent.setDisplayName("General");
        }

        final Editor editor = console.getConsoleEditor();

        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(false, true) {
            @Override
            public Object getData(@NonNls String dataId) {
                return PlatformDataKeys.HELP_ID.is(dataId) ? ConsoleLog.HELP_ID : super.getData(dataId);
            }
        };
        panel.setContent(editor.getComponent());
        panel.addAncestorListener(new LogShownTracker(project));

        ActionToolbar toolbar = createToolbar(project, editor, console);
        toolbar.setTargetComponent(editor.getContentComponent());
        panel.setToolbar(toolbar.getComponent());

        Content content = ContentFactory.SERVICE.getInstance().createContent(panel, title, false);
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);
    }

    private static ActionToolbar createToolbar(Project project, Editor editor, ConsoleLogConsole console) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new EditNotificationSettings(project));
        group.add(new DisplayBalloons());
        group.add(new ToggleSoftWraps(editor));
        group.add(new ScrollToTheEndToolbarAction(editor));
        group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_MARK_ALL_NOTIFICATIONS_AS_READ));
        group.add(new ConsoleLogConsole.ClearLogAction(console));
        group.add(new ContextHelpAction(ConsoleLog.HELP_ID));

        return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, false);
    }

    private static class DisplayBalloons extends ToggleAction implements DumbAware {
        public DisplayBalloons() {
            super("Show balloons", "Enable or suppress notification balloons", AllIcons.General.Balloon);
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return NotificationsConfigurationImpl.getInstanceImpl().SHOW_BALLOONS;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            NotificationsConfigurationImpl.getInstanceImpl().SHOW_BALLOONS = state;
        }
    }

    private static class EditNotificationSettings extends DumbAwareAction {
        private final Project project;

        public EditNotificationSettings(Project project) {
            super("Settings", "Edit AEM Console Log settings", AllIcons.General.Settings);
            this.project = project;
        }

        @Override
        public void update(AnActionEvent e) {
            SlingServerTreeSelectionHandler selectionHandler = project.getComponent(SlingServerTreeSelectionHandler.class);
            ServerConfiguration serverConfiguration = selectionHandler == null ? null : selectionHandler.getCurrentConfiguration();
            e.getPresentation().setEnabled(project != null && serverConfiguration != null);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            SlingServerTreeSelectionHandler selectionHandler = project.getComponent(SlingServerTreeSelectionHandler.class);
            ServerConnectionManager serverConnectionManager = project.getComponent(ServerConnectionManager.class);
            ServerConfigurationManager configurationManager = project.getComponent(ServerConfigurationManager.class);
            if(selectionHandler != null && serverConnectionManager != null && configurationManager != null) {
                ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                if(serverConfiguration != null) {
                    ConsoleLogSettingsDialog dialog = new ConsoleLogSettingsDialog(project, serverConfiguration);
                    if (dialog.showAndGet()) {
                        // Modules might have changed and so update the tree
                        configurationManager.updateServerConfiguration(serverConfiguration);
                    }
                }
            }
        }
    }

    private static class ToggleSoftWraps extends ToggleUseSoftWrapsToolbarAction {
        private final Editor myEditor;

        public ToggleSoftWraps(Editor editor) {
            super(SoftWrapAppliancePlaces.CONSOLE);
            myEditor = editor;
        }

        @Override
        protected Editor getEditor(AnActionEvent e) {
            return myEditor;
        }
    }

    private static class LogShownTracker extends AncestorListenerAdapter {
        private final Project myProject;

        public LogShownTracker(Project project) {
            myProject = project;
        }

        @Override
        public void ancestorAdded(AncestorEvent event) {
            ToolWindow log = ConsoleLog.getLogWindow(myProject);
            if (log != null && log.isVisible()) {
                ConsoleLog.getLogModel(myProject).logShown();
            }
        }
    }

}
