package com.headwire.aem.tooling.intellij.ui;

import javax.swing.*;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.util.Util;
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
    private JRadioButton automaticallyPublishOnChangeRadioButton;
    private JRadioButton automaticallyPublishOnBuildRadioButton;
    private JRadioButton installBundlesViaBundleRadioButton;
    private JRadioButton installBundlesDirectlyFromRadioButton;
    private JButton installButton;
    private JTextField name;
    private JTextField description;

    private ServerConfiguration serverConfiguration;

    public ServerConfigurationDialog(@Nullable Project project) {
        this(project, null);
//        super(project);
//
//        setTitle("Create Server Connection Properties");
//        setModal(true);
//        setUpDialog(null);
//        init();
    }

    public ServerConfigurationDialog(@Nullable Project project, @Nullable ServerConfiguration serverConfiguration) {
        super(project);

        setTitle((serverConfiguration == null ? "Create" :"Edit") + " Server Connection Properties");
        setModal(true);
        setUpDialog(serverConfiguration == null ? getConfiguration() : serverConfiguration);
        init();
    }

    public ServerConfiguration getConfiguration() {
        // Use the Copy Constructor to return a Copy to be able to undo changes even after the configuration has changed
        ServerConfiguration ret = serverConfiguration != null ? new ServerConfiguration(serverConfiguration) : new ServerConfiguration();
        ret.setName(name.getText());
        ret.setHost(host.getText());
        ret.setDescription(description.getText());
        ret.setConnectionPort(obtainInteger(connectionPort, 0));
        ret.setConnectionDebugPort(obtainInteger(connectionDebugPort, 0));
        ret.setUserName(connectionUserName.getText());
        char[] password = connectionPassword.getPassword();
        // If password is already set and we did not add anything then we don't changes it. If empty we set it anyway
        if(ret.getPassword() != null) {
            if(password != null && password.length > 0) {
                ret.setPassword(password);
            }
        } else {
            ret.setPassword(password);
        }
        ret.setContextPath(connectionContextPath.getText());
        ret.setStartConnectionTimeoutInSeconds(obtainInteger(startConnectionTimeout, -1));
        ret.setStopConnectionTimeoutInSeconds(obtainInteger(stopConnectionTimeout, -1));
        ServerConfiguration.PublishType publishType =
            neverAutomaticallyPublishContentRadioButton.isSelected() ? ServerConfiguration.PublishType.never :
                automaticallyPublishOnChangeRadioButton.isSelected() ? ServerConfiguration.PublishType.automaticallyOnChange :
                    automaticallyPublishOnBuildRadioButton.isSelected() ? ServerConfiguration.PublishType.getAutomaticallyOnBuild :
                        null;
        ret.setPublishType(publishType);
        ServerConfiguration.InstallationType installationType =
            installBundlesViaBundleRadioButton.isSelected() ? ServerConfiguration.InstallationType.installViaBundleUpload :
                installBundlesDirectlyFromRadioButton.isSelected() ? ServerConfiguration.InstallationType.installViaBundleUpload :
                        null;
        ret.setInstallationType(installationType);

        return ret;
    }

    private void setUpDialog(ServerConfiguration configuration) {
        serverConfiguration = configuration;
        if(serverConfiguration != null) {
            name.setText(serverConfiguration.getName());
            host.setText(serverConfiguration.getHost());
            description.setText(serverConfiguration.getDescription());
            connectionPort.setText(serverConfiguration.getConnectionPort() + "");
            connectionDebugPort.setText(serverConfiguration.getConnectionDebugPort() + "");
            connectionUserName.setText(serverConfiguration.getUserName());
            connectionContextPath.setText(serverConfiguration.getContextPath());
            startConnectionTimeout.setValue(serverConfiguration.getStartConnectionTimeoutInSeconds());
            stopConnectionTimeout.setValue(serverConfiguration.getStopConnectionTimeoutInSeconds());
            switch(serverConfiguration.getPublishType()) {
                case never:
                    neverAutomaticallyPublishContentRadioButton.setSelected(true);
                    break;
                case automaticallyOnChange:
                    automaticallyPublishOnChangeRadioButton.setSelected(true);
                    break;
                case getAutomaticallyOnBuild:
                    automaticallyPublishOnBuildRadioButton.setSelected(true);
                    break;
                default:
                    automaticallyPublishOnChangeRadioButton.setSelected(true);
                    break;
            }
            switch(serverConfiguration.getInstallationType()) {
                case installViaBundleUpload:
                    installBundlesViaBundleRadioButton.setSelected(true);
                    break;
                case installFromFilesystem:
                    installBundlesDirectlyFromRadioButton.setSelected(true);
                    break;
                default:
                    installBundlesViaBundleRadioButton.setSelected(true);
                    break;
            }
        }
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
            ret = Util.convertToInt(textField.getText(), defaultValue);
        }
        return ret;
    }

    private int obtainInteger(JSpinner spinner, int defaultValue) {
        int ret = defaultValue;
        if(spinner != null) {
            ret = Util.convertToInt(spinner.getValue() + "", defaultValue);
        }
        return ret;
    }
}
