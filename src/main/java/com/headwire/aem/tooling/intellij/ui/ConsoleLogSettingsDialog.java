package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ConsoleLogSettingsDialog extends DialogWrapper {
    private JPanel contentPane;
    private JRadioButton errorLogLevel;
    private JRadioButton warningLogLevel;
    private JRadioButton infoLogLevel;
    private JRadioButton debugLogLevel;
    private ServerConfiguration serverConfiguration;

    public ConsoleLogSettingsDialog(@NotNull Project project, @NotNull ServerConfiguration serverConfiguration) {
        super(project);

        setTitle(AEMBundle.message("log.configuration.dialog.title"));
        setModal(true);
        setUpDialog(serverConfiguration);
        init();
    }

    private void setUpDialog(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        ServerConfiguration.LogFilter logFilter = serverConfiguration.getLogFilter();
        switch(logFilter) {
            case debug:
                debugLogLevel.setSelected(true);
                break;
            case info:
                infoLogLevel.setSelected(true);
                break;
            case warning:
                warningLogLevel.setSelected(true);
                break;
            case error:
            default:
                errorLogLevel.setSelected(true);
                break;
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    protected void doOKAction() {
        if(infoLogLevel.isSelected()) {
            serverConfiguration.setLogFilter(ServerConfiguration.LogFilter.info);
        } else if(warningLogLevel.isSelected()) {
            serverConfiguration.setLogFilter(ServerConfiguration.LogFilter.warning);
        } else if(debugLogLevel.isSelected()) {
            serverConfiguration.setLogFilter(ServerConfiguration.LogFilter.debug);
        } else {
            serverConfiguration.setLogFilter(ServerConfiguration.LogFilter.error);
        }
        super.doOKAction();
    }
}
