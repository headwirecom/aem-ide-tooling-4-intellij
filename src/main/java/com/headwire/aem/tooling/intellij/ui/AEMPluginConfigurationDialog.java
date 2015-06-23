package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.config.general.AEMPluginConfiguration;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AEMPluginConfigurationDialog {

    public static final String COMPONENT_NAME = "AEMPluginConfiguration";

    private JPanel contentPane;
    private AEMPluginConfiguration pluginConfiguration;

    private JCheckBox incrementalBuild;
    private JSpinner buildDelay;
    private JLabel buildDelayLabel;

    public AEMPluginConfigurationDialog() {
        incrementalBuild.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                buildDelayLabel.setEnabled(incrementalBuild.isSelected());
                buildDelay.setEnabled(incrementalBuild.isSelected());
            }
        });
    }

    private void setUpDialog(AEMPluginConfiguration pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
        setData(pluginConfiguration);
    }

    public JComponent getRootPane() {
        return contentPane;
    }

    private void clearList(JList list) {
    }

    public void setData(AEMPluginConfiguration data) {
        incrementalBuild.setSelected(data.isIncrementalBuilds());
        buildDelayLabel.setEnabled(incrementalBuild.isSelected());
        buildDelay.setEnabled(incrementalBuild.isSelected());
    }

    public void getData(AEMPluginConfiguration data) {
        data.setIncrementalBuilds(incrementalBuild.isSelected());
        data.setBuildDelayInSeconds(incrementalBuild.isSelected() ? UIUtil.obtainInteger(buildDelay, -1) : -1);
    }

    public boolean isModified(AEMPluginConfiguration data) {
        return incrementalBuild.isSelected() != data.isIncrementalBuilds() ||
            (incrementalBuild.isSelected() &&
            data.getBuildDelayInSeconds() != UIUtil.obtainInteger(buildDelay, -1));
    }

}
