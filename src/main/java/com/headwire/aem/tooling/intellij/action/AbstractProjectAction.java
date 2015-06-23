package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by schaefa on 6/13/15.
 */
public abstract class AbstractProjectAction
    extends AnAction
    implements DumbAware

{
    protected MessageManager messageManager;

    public AbstractProjectAction() {
        messageManager = ServiceManager.getService(MessageManager.class);
    }

    public void update(AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        event.getPresentation().setEnabled(
            project != null && isEnabled(project, dataContext)
        );
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        if(project != null) {
            execute(project, dataContext);
        }
    }

    protected abstract void execute(@NotNull Project project, @NotNull DataContext dataContext);

    protected abstract boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext);

    protected ServerTreeSelectionHandler getSelectionHandler(@Nullable Project project) {
        return project == null ? null : ServiceManager.getService(project, ServerTreeSelectionHandler.class);
    }

    protected ServerConnectionManager getConnectionManager(@Nullable Project project) {
        return project == null ? null : ServiceManager.getService(project, ServerConnectionManager.class);
    }

    protected ServerConfigurationManager getConfigurationManager(@Nullable Project project) {
        return project == null ? null : ServiceManager.getService(project, ServerConfigurationManager.class);
    }
}
