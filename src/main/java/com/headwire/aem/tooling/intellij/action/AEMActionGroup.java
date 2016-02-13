package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

/**
 * Created by schaefa on 6/18/15.
 */
public class AEMActionGroup extends DefaultActionGroup implements DumbAware {
    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        boolean available = isAvailable(e);
        e.getPresentation().setEnabled(available);
        e.getPresentation().setVisible(available);
    }

    protected boolean isAvailable(AnActionEvent e) {
        boolean ret = false;
        Project project = e.getProject();
        if(project != null) {
            final ServerTreeSelectionHandler selectionHandler = project.getComponent(ServerTreeSelectionHandler.class);
            ret = selectionHandler.getCurrentConfiguration() != null;
        }
        return ret;
    }
}
