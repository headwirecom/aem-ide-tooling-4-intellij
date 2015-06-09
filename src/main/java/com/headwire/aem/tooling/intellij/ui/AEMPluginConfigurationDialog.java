package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.config.general.AEMPluginConfiguration;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

public class AEMPluginConfigurationDialog {

    public static final String COMPONENT_NAME = "AEMPluginConfiguration";

    private JPanel contentPane;
    private AEMPluginConfiguration pluginConfiguration;

    private JCheckBox incrementalBuild;

    public AEMPluginConfigurationDialog() {
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
    }

    public void getData(AEMPluginConfiguration data) {
        data.setIncrementalBuilds(incrementalBuild.isSelected());
    }

    public boolean isModified(AEMPluginConfiguration data) {
        return incrementalBuild.isSelected() != data.isIncrementalBuilds();
    }

}
