package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by schaefa on 6/13/15.
 */
public abstract class AbstractProjectAction
    extends AnAction
    implements DumbAware

{
    /** Map that contains the Toolbar Locks per Project **/
    private static Map<Project, Boolean> lockMap = new HashMap<Project, Boolean>();

    private MessageManager messageManager;

    public AbstractProjectAction(@NotNull String textId) {
        super(
            AEMBundle.message(textId + ".text"),
            AEMBundle.message(textId + ".description"),
            null
        );
    }

    public AbstractProjectAction() {
    }

    public void update(AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        event.getPresentation().setEnabled(
            project != null &&
            isEnabled(project, dataContext) &&
            !isLocked(project)
        );
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        if(project != null && !isLocked(project)) {
            lock(project);
            try {
                execute(project, dataContext);
            } finally {
                if(!isAsynchronous()) {
                    unlock(project);
                }
            }
        }
    }

    @NotNull
    protected MessageManager getMessageManager(@NotNull Project project) {
        return project.getComponent(MessageManager.class);
    }

    protected abstract void execute(@NotNull Project project, @NotNull DataContext dataContext);

    protected abstract boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext);

    /**
     * This method indicates if an Action is executing tasks in the background. If true then
     * the action must unlock the toolbar when the task is done.
     *
     * @return True if code is executed in the background and therefore the unlock must be postponed
     *         where it becomes the Action responsibility to unlock it when done.
     */
    protected boolean isAsynchronous() {
        return false;
    }

    private synchronized boolean isLocked(Project project) {
        Boolean ret = lockMap.get(project);
        return ret == null ? false : ret;
    }

    /**
     * Locks the Toolbar for the given Project
     *
     * @param project Project the toolbar will be locked on
     */
    protected synchronized void lock(@NotNull  Project project) {
        lockMap.put(project, true);
    }

    /**
     * Unlocks the Toolbar for the given Project
     *
     * @param project Project the toolbar will be unlocked from
     */
    protected synchronized void unlock(@NotNull Project project) {
        lockMap.put(project, false);
    }

    protected ServerTreeSelectionHandler getSelectionHandler(@Nullable Project project) {
        return project == null ? null : project.getComponent(ServerTreeSelectionHandler.class);
    }

    protected ServerConnectionManager getConnectionManager(@Nullable Project project) {
        return project == null ? null : project.getComponent(ServerConnectionManager.class);
    }

    protected ServerConfigurationManager getConfigurationManager(@Nullable Project project) {
        return project == null ? null : project.getComponent(ServerConfigurationManager.class);
    }
}
