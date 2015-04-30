package com.headwire.aem.tooling.intellij.ui;

import javax.swing.*;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import org.jetbrains.annotations.Nullable;

public class ServerConfigurationDialog
        extends DialogWrapper
{
    private JPanel contentPane;
    private JTextField hostName;
    private JTextField configurationPath;
    private JComboBox runtimeEnvironment;
    private JTextField configurationName;
    private JTabbedPane tabbedPane1;
    private JTextField connectionDebugPort;
    private JTextField connectionUserName;
    private JPasswordField connectionPassword;
    private JTextField connectionPort;
    private JTextField connectionContextPath;
    private JSpinner stopConnectionTimeout;
    private JSpinner startConnectionTimeout;
    private JRadioButton neverAutomaticallyPublishContentRadioButton;
    private JRadioButton automaticallyPublishResourcesOnRadioButton;
    private JRadioButton automaticallyPublishContentOnRadioButton;
    private JRadioButton installBundlesViaBundleRadioButton;
    private JRadioButton installBundlesDirectlyFromRadioButton;
    private JButton installButton;
    private TextFieldWithBrowseButton workDirectoryField;

    public ServerConfigurationDialog(@Nullable Project project) {
        super(project);

        setTitle("Edit Server Properties");
        setModal(true);
        setUpDialog();
        init();
    }

    public ServerConfiguration getConfiguration() {
        ServerConfiguration ret = new ServerConfiguration();
        ret.setServerName(configurationName.getText());
        ret.setHostName(hostName.getText());
        ret.setRuntimeEnvironment(runtimeEnvironment.getSelectedIndex());
        ret.setConfigurationPath(configurationPath.getText());

        return ret;
    }

    private void setUpDialog() {

    }

//
//    private void onCancel() {
//// add your code here if necessary
//        dispose();
//    }

//    public static void main(String[] args) {
//        ServerConfigurationDialog dialog = new ServerConfigurationDialog();
//        dialog.pack();
////        dialog.setVisible(true);
//        System.exit(0);
//    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
