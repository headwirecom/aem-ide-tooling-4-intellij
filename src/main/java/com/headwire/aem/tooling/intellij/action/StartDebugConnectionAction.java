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
    @Override
    protected void execute(@NotNull Project project, DataContext dataContext) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        if(connectionManager != null) {
            RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
            if(runManager != null) {
                connectionManager.connectInDebugMode(runManager);
            } else {
                //AS TODO: Create Alert to show that problems
            }
        }
    }

    @Override
    protected boolean isEnabled(@Nullable Project project) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        return connectionManager != null && connectionManager.isConnectionNotInUse();
    }
}
