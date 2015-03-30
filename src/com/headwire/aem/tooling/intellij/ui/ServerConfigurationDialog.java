package com.headwire.aem.tooling.intellij.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private JTextField serverName;
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
        ret.setServerName(serverName.getText());
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
