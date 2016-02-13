package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.ui.BuildSelectionDialog;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by schaefa on 6/26/15.
 */
public class BuildConfigurationAction
    extends AbstractProjectAction
{
    public BuildConfigurationAction() {
        super("build.configuration.action");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        ServerConnectionManager serverConnectionManager = project.getComponent(ServerConnectionManager.class);
        ServerConfigurationManager configurationManager = project.getComponent(ServerConfigurationManager.class);
        if(selectionHandler != null && serverConnectionManager != null && configurationManager != null) {
            ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
            BuildSelectionDialog dialog = new BuildSelectionDialog(project, serverConfiguration);
            if(dialog.showAndGet()) {
                // Modules might have changed and so update the tree
                configurationManager.updateServerConfiguration(serverConfiguration);
            }
        }
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager serverConnectionManager = project.getComponent(ServerConnectionManager.class);
        return serverConnectionManager != null && serverConnectionManager.isConfigurationSelected();
    }
}
