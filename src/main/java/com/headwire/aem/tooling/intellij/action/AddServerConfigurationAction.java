package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Created by schaefa on 6/12/15.
 */
public class AddServerConfigurationAction
    extends AbstractEditServerConfigurationAction
{
    public AddServerConfigurationAction() {
//        super(
//            AEMBundle.message("add.configuration.action.name"),
//            AEMBundle.message("add.configuration.action.description"),
//            IconUtil.getAddIcon()
//        );
    }

    public void update(AnActionEvent event) {
        event.getPresentation().setEnabled(true);
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if(project != null) {
            editServerConfiguration(project, null);
        }
    }

}
