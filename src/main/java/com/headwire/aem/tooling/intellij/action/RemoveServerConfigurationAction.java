package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by schaefa on 6/13/15.
 */
public class RemoveServerConfigurationAction
    extends AbstractProjectAction
{
    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConfigurationManager configurationManager = getConfigurationManager(project);
        ServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        if(configurationManager != null && selectionHandler != null) {
            configurationManager.removeServerConfiguration(selectionHandler.getCurrentConfiguration());
        }
    }

    @Override
    protected boolean isEnabled(@NotNull Project project) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        return connectionManager != null && connectionManager.isConfigurationEditable();
    }
}
