package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.eclipse.ProjectUtil;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.sling.ide.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenResource;

import java.util.List;

/**
 * Created by schaefa on 6/12/15.
 */
public class VerifyConfigurationAction extends AbstractProjectAction {
    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        doVerify(project);
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        return serverConnectionManager != null && serverConnectionManager.isConfigurationSelected();
    }

    public void doVerify(final Project project) {
        ServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        if(selectionHandler != null && serverConnectionManager != null) {
            ServerConfiguration source = selectionHandler.getCurrentConfiguration();
            if(source != null) {
                // Before we can verify we need to ensure the Configuration is properly bound to Maven
                serverConnectionManager.checkBinding(source);
                // Verify each Module to see if all prerequisites are met
                for(ServerConfiguration.Module module: source.getModuleList()) {
                    if(module.isSlingPackage()) {
                        // Check if the Filter is available for Content Modules
                        try {
                            Filter filter = ProjectUtil.loadFilter(module);
                            if(filter == null) {
                                MessageManager messageManager = ServiceManager.getService(module.getProject(), MessageManager.class);
                                messageManager.showAlertWithArguments("server.configuration.filter.file.not.found", module.getName());
                                module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                            }
                        } catch(CoreException e) {
                            MessageManager messageManager = ServiceManager.getService(module.getProject(), MessageManager.class);
                            messageManager.showAlertWithArguments("server.configuration.filter.file.failure", module.getName(), e.getMessage());
                            module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                        }
                        // Check if the Content Modules have a Content Resource
                        List<MavenResource> resourceList = serverConnectionManager.findContentResources(module);
                        if(resourceList.isEmpty()) {
                            MessageManager messageManager = ServiceManager.getService(module.getProject(), MessageManager.class);
                            messageManager.showAlertWithArguments("server.configuration.content.folder.not.", module.getName());
                            module.setStatus(ServerConfiguration.SynchronizationStatus.failed);
                        }
                    }
                }
            }
        }
    }
}
