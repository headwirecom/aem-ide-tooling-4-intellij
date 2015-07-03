package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by schaefa on 6/12/15.
 */
public class ResetConfigurationAction extends AbstractProjectAction {

    public ResetConfigurationAction() {
        super("reset.configuration.action");
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        doReset(project);
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        return serverConnectionManager != null && serverConnectionManager.isConfigurationSelected();
    }

    public void doReset(final Project project) {
        ServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        if(selectionHandler != null && serverConnectionManager != null) {
            ServerConfiguration source = selectionHandler.getCurrentConfiguration();
            if(source != null) {
                // Before we can verify we need to ensure the Configuration is properly bound to Maven
                serverConnectionManager.checkBinding(source);
                // Verify each Module to see if all prerequisites are met
                getMessageManager(project).sendInfoNotification("action.reset.configuration.begin");
                for(ServerConfiguration.Module module: source.getModuleList()) {
                    if(module.isSlingPackage()) {
                        getMessageManager(project).sendInfoNotification("action.reset.configuration.start", module.getName());
                        // Check if the Content Modules have a Content Resource
                        List<String> resourceList = serverConnectionManager.findContentResources(module);
                        for(String contentPath: resourceList) {
                            VirtualFile mavenResourceDirectory = project.getBaseDir().getFileSystem().findFileByPath(contentPath);
                            if(mavenResourceDirectory != null) {
                                Util.resetModificationStamp(mavenResourceDirectory, true);
                            }
                        }
                        getMessageManager(project).sendInfoNotification("action.reset.configuration.end", module.getName());
                    }
                }
                getMessageManager(project).sendInfoNotification("action.reset.configuration.finish");
            }
        }
    }
}
