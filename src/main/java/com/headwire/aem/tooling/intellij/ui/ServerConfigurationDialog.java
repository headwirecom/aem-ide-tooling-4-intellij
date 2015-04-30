package com.headwire.aem.tooling.intellij.ui;

import javax.swing.*;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

public class ServerConfigurationDialog
        extends DialogWrapper
{
    private JPanel contentPane;
    private JTextField host;
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
    private JTextField name;
    private JTextField description;

    public ServerConfigurationDialog(@Nullable Project project) {
        super(project);

        setTitle("Create Server Connection Properties");
        setModal(true);
        setUpDialog(null);
        init();
    }

    public ServerConfigurationDialog(@Nullable Project project, @Nullable ServerConfiguration serverConfiguration) {
        super(project);

        setTitle("Edit Server Connection Properties");
        setModal(true);
        setUpDialog(serverConfiguration);
        init();
    }

    public ServerConfiguration getConfiguration() {
        ServerConfiguration ret = new ServerConfiguration();
        ret.setName(name.getText());
        ret.setHost(host.getText());
        ret.setDescription(description.getText());
        ret.setConnectionPort(obtainInteger(connectionPort, 0));
        ret.setDebugConnectionPort(obtainInteger(connectionDebugPort, 0));
        ret.setUserName(connectionUserName.getText());
        ret.setPassword(connectionPassword.getPassword());
        ret.setContextPath(connectionContextPath.getText());
        ret.setStartConnectionTimeoutInSeconds(obtainInteger(startConnectionTimeout, -1));
        ret.setStopConnectionTimeoutInSeconds(obtainInteger(stopConnectionTimeout, -1));

        
        ret.setName(name.getText());
        ret.setName(name.getText());
        ret.setName(name.getText());
        ret.setServerName(configurationName.getText());
        ret.setHostName(host.getText());
        ret.setRuntimeEnvironment(runtimeEnvironment.getSelectedIndex());
        ret.setConfigurationPath(configurationPath.getText());

        return ret;
    }

    private void setUpDialog(ServerConfiguration serverConfiguration) {
//        if(serverConfiguration != null) {
//            name.setText(serverConfiguration.);
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

    private int obtainInteger(JTextField textField, int defaultValue) {
        int ret = defaultValue;
        if(textField != null) {
            String value = textField.getText();
            if(StringUtils.isNotBlank(value)) {
                try {
                    ret = Integer.parseInt(value);
                } catch(NumberFormatException e) {
                    // Ignore
                }
            }
        }
        return ret;
    }

    private int obtainInteger(JSpinner spinner, int defaultValue) {
        int ret = defaultValue;
        if(spinner != null) {
            String value = spinner.getValue() + "";
            if(StringUtils.isNotBlank(value)) {
                try {
                    ret = Integer.parseInt(value);
                } catch(NumberFormatException e) {
                    // Ignore
                }
            }
        }
        return ret;
    }
}
