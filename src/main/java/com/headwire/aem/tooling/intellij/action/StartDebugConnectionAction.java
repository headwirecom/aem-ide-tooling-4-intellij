package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.intellij.execution.RunManagerEx;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by schaefa on 6/13/15.
 */
public class StartDebugConnectionAction
    extends AbstractProjectAction
{

    public StartDebugConnectionAction() {
        super("debug.configuration.action");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
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
