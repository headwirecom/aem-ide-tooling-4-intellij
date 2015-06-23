package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.config.general.AEMPluginConfiguration;

import javax.swing.*;

public class AEMPluginConfigurationDialog {

    public static final String COMPONENT_NAME = "AEMPluginConfiguration";

    private JPanel contentPane;
    private AEMPluginConfiguration pluginConfiguration;

    private JCheckBox incrementalBuild;
    private JSpinner deployDelay;
    private JLabel deployDelayLabel;

    public AEMPluginConfigurationDialog() {
    }

//    private void setUpDialog(AEMPluginConfiguration pluginConfiguration) {
//        this.pluginConfiguration = pluginConfiguration;
//        setData(pluginConfiguration);
//    }

    public JComponent getRootPane() {
        return contentPane;
    }

    public void setData(AEMPluginConfiguration data) {
        incrementalBuild.setSelected(data.isIncrementalBuilds());
        deployDelay.setValue(data.getDeployDelayInSeconds());
    }

    public void getData(AEMPluginConfiguration data) {
        data.setIncrementalBuilds(incrementalBuild.isSelected());
        data.setDeployDelayInSeconds(UIUtil.obtainInteger(deployDelay, -1));
    }

    public boolean isModified(AEMPluginConfiguration data) {
        return incrementalBuild.isSelected() != data.isIncrementalBuilds() ||
            UIUtil.obtainInteger(deployDelay, -1) != data.getDeployDelayInSeconds() ;
    }

}
