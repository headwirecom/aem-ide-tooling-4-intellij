package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by schaefa on 6/9/15.
 */
public class EditServerConfigurationAction
    extends AbstractEditServerConfigurationAction
{

    public EditServerConfigurationAction() {
        super("edit.configuration.action");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        SlingServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        if(selectionHandler != null) {
            ServerConfiguration source = selectionHandler.getCurrentConfiguration();
            if(source != null) {
                editServerConfiguration(project, source);
            }
        }
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager serverConnectionManager = getConnectionManager(project);
        return serverConnectionManager != null && serverConnectionManager.isConfigurationEditable();
    }
}
