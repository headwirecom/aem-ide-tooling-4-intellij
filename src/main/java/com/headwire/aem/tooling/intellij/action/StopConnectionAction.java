package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by schaefa on 6/13/15.
 */
public class StopConnectionAction
    extends AbstractProjectAction
{

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        if(connectionManager != null) {
            connectionManager.stopDebugConnection(dataContext);
        }
    }

    @Override
    protected boolean isEnabled(@NotNull Project project) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        return connectionManager != null && connectionManager.isConnectionIsStoppable();
    }
}
