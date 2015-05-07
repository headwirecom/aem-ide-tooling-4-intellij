package com.headwire.aem.tooling.intellij.console;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ContextHelpAction;
import com.intellij.notification.impl.NotificationsConfigurable;
import com.intellij.notification.impl.NotificationsConfigurationImpl;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.ToggleUseSoftWrapsToolbarAction;
import com.intellij.openapi.editor.impl.softwrap.SoftWrapAppliancePlaces;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.AncestorEvent;

/**
 * Created by schaefa on 5/6/15.
 *
 * @deprecated This is no longer used -> remove it later
 */
public class SlingServerReportView {

//    public static final String TOOL_WINDOW_ID = "AEM.Console";

    public void initToolWindow(@NotNull ToolWindow toolWindow, Project project) {
        // update default Event Log tab title
        ContentManager contentManager = toolWindow.getContentManager();
        Content generalContent = contentManager.getContent(0);
        if (generalContent != null && contentManager.getContentCount() == 1) {
            generalContent.setDisplayName("General");
        }

        ConsoleLogModel model = new ConsoleLogModel(project, project);
        final ConsoleLogConsole console = new ConsoleLogConsole(model);
        final Editor editor = console.getConsoleEditor();

//        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(false, true) {
//            @Override
//            public Object getData(@NonNls String dataId) {
//                return PlatformDataKeys.HELP_ID.is(dataId) ? EventLog.HELP_ID : super.getData(dataId);
//            }
//        };
        SlingServerConsolePanel panel = new SlingServerConsolePanel(false, true);
//        panel.setContent(panel);
        panel.setContent(editor.getComponent());
//        panel.addAncestorListener(new LogShownTracker(project));

        ActionToolbar toolbar = createToolbar(project
//            , editor, console
        );
        toolbar.setTargetComponent(panel);
        toolbar.setTargetComponent(editor.getContentComponent());
        panel.setToolbar(toolbar.getComponent());

        String title = "AEM Console";
        Content content = ContentFactory.SERVICE.getInstance().createContent(panel, title, false);
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);
/*
        // Create panels
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content allTodosContent = contentFactory.createContent(null, IdeBundle.message("title.project"), false);
        myAllTodos = new TodoPanel(myProject, state.all, false, allTodosContent) {
            @Override
            protected TodoTreeBuilder createTreeBuilder(JTree tree, DefaultTreeModel treeModel, Project project) {
                AllTodosTreeBuilder builder = new AllTodosTreeBuilder(tree, treeModel, project);
                builder.init();
                return builder;
            }
        };
        allTodosContent.setComponent(myAllTodos);
        Disposer.register(this, myAllTodos);

        Content currentFileTodosContent = contentFactory.createContent(null, IdeBundle.message("title.todo.current.file"), false);
        CurrentFileTodosPanel currentFileTodos = new CurrentFileTodosPanel(myProject, state.current, currentFileTodosContent) {
            @Override
            protected TodoTreeBuilder createTreeBuilder(JTree tree, DefaultTreeModel treeModel, Project project) {
                CurrentFileTodosTreeBuilder builder = new CurrentFileTodosTreeBuilder(tree, treeModel, project);
                builder.init();
                return builder;
            }
        };
        Disposer.register(this, currentFileTodos);
        currentFileTodosContent.setComponent(currentFileTodos);

        myChangeListTodosContent = contentFactory
            .createContent(null, IdeBundle.message("changelist.todo.title",
                    ChangeListManager.getInstance(myProject).getDefaultChangeList().getName()),
                false);
        ChangeListTodosPanel changeListTodos = new ChangeListTodosPanel(myProject, state.current, myChangeListTodosContent) {
            @Override
            protected TodoTreeBuilder createTreeBuilder(JTree tree, DefaultTreeModel treeModel, Project project) {
                ChangeListTodosTreeBuilder builder = new ChangeListTodosTreeBuilder(tree, treeModel, project);
                builder.init();
                return builder;
            }
        };
        Disposer.register(this, changeListTodos);
        myChangeListTodosContent.setComponent(changeListTodos);

        Content scopeBasedTodoContent = contentFactory.createContent(null, "Scope Based", false);
        ScopeBasedTodosPanel scopeBasedTodos = new ScopeBasedTodosPanel(myProject, state.current, scopeBasedTodoContent);
        Disposer.register(this, scopeBasedTodos);
        scopeBasedTodoContent.setComponent(scopeBasedTodos);

        myContentManager = toolWindow.getContentManager();

        myContentManager.addContent(allTodosContent);
        myContentManager.addContent(currentFileTodosContent);
        myContentManager.addContent(scopeBasedTodoContent);

        if (ProjectLevelVcsManager.getInstance(myProject).hasActiveVcss()) {
            myVcsListener.myIsVisible = true;
            myContentManager.addContent(myChangeListTodosContent);
        }
        for (Content content : myNotAddedContent) {
            myContentManager.addContent(content);
        }

        myChangeListTodosContent.setCloseable(false);
        allTodosContent.setCloseable(false);
        currentFileTodosContent.setCloseable(false);
        scopeBasedTodoContent.setCloseable(false);
        Content content = myContentManager.getContent(state.selectedIndex);
        myContentManager.setSelectedContent(content == null ? allTodosContent : content);

        myPanels.add(myAllTodos);
        myPanels.add(changeListTodos);
        myPanels.add(currentFileTodos);
        myPanels.add(scopeBasedTodos);
*/
    }

    //AS TODO: This was taken from IntelliJ EventLog (com.intellij.notification.EventLogToolWindowFactory)
    private static ActionToolbar createToolbar(Project project
//                                               ,Editor editor,
//                                               EventLogConsole console
    ) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new EditNotificationSettings(project));
        group.add(new DisplayBalloons());
//        group.add(new ToggleSoftWraps(editor));
//        group.add(new ScrollToTheEndToolbarAction(editor));
        group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_MARK_ALL_NOTIFICATIONS_AS_READ));
//        group.add(new EventLogConsole.ClearLogAction(console));
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
        private final Project myProject;

        public EditNotificationSettings(Project project) {
            super("Settings", "Edit notification settings", AllIcons.General.Settings);
            myProject = project;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            ShowSettingsUtil.getInstance().editConfigurable(myProject, new NotificationsConfigurable());
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
//                EventLog.getLogModel(myProject).logShown();
            }
        }
    }
}
