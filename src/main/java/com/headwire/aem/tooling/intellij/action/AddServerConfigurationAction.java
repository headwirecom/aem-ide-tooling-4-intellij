package com.headwire.aem.tooling.intellij.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by schaefa on 6/12/15.
 */
public class AddServerConfigurationAction
    extends AbstractEditServerConfigurationAction
{
    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext) {
        editServerConfiguration(project, null);
    }

    @Override
    protected boolean isEnabled(@Nullable Project project) {
        return true;
    }
}
