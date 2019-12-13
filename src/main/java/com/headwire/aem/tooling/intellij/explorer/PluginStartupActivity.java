package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.ContentResourceChangeListener;
import com.headwire.aem.tooling.intellij.console.ConsoleLog;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class PluginStartupActivity
    implements StartupActivity
{
    @Override
    public void runActivity(@NotNull Project project) {
        ConsoleLog consoleLog = ServiceManager.getService(ConsoleLog.class);
        if(consoleLog != null) {
            consoleLog.initComponent();
        }
        ContentResourceChangeListener contentResourceChangeListener = ServiceManager.getService(project, ContentResourceChangeListener.class);
        if(contentResourceChangeListener != null) {
            contentResourceChangeListener.projectOpened();
        }
        AemdcPanel aemdcPanel = ServiceManager.getService(project, AemdcPanel.class);
        if(aemdcPanel != null) {
            aemdcPanel.initComponent();
        }
    }
}
