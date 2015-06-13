package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.explorer.SlingServerExplorer;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.ui.ServerConfigurationDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by schaefa on 6/9/15.
 */
public class EditServerConfigurationAction
    extends AbstractEditServerConfigurationAction
{

//    private ServerConnectionManager serverConnectionManager;
//    private ServerTreeSelectionHandler selectionHandler;
//
//    public EditServerConfigurationAction(@NotNull Project project) {
//        selectionHandler = new ServerTreeSelectionHandler(myTree);
//        serverConnectionManager = new ServerConnectionManager(project, selectionHandler);
//    }

    public EditServerConfigurationAction() {
//        super(
//            AEMBundle.message("edit.configuration.action.name"),
//            AEMBundle.message("edit.configuration.action.description"),
//            IconUtil.getAddIcon()
//        );
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        Project project = event.getProject();
        if(project != null) {
            ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
            if(serverConnectionManager != null) {
                event.getPresentation().setEnabled(serverConnectionManager.isConfigurationEditable());
            }
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if(project != null) {
            ServerTreeSelectionHandler selectionHandler = ServiceManager.getService(project, ServerTreeSelectionHandler.class);
            if(selectionHandler != null) {
                ////        SlingServerExplorer explorer = ServiceManager.getService(project, SlingServerExplorer.class);
                //        SlingServerExplorer explorer = (SlingServerExplorer) project.getPicoContainer().getComponentInstanceOfType(SlingServerExplorer.class);
                //        if(explorer != null) {
                //            ServerTreeSelectionHandler selectionHandler = explorer.getSelectionHandler();
                ServerConfiguration source = selectionHandler.getCurrentConfiguration();
                if(source != null) {
                    editServerConfiguration(e.getProject(), source);
                }

            }
        }
    }
}
