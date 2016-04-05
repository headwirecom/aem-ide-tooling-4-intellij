/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.ui;

import javax.swing.*;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.DefaultMode;

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
    private JCheckBox buildWithMaven;
    private JCheckBox defaultDebugConfiguration;
    private JCheckBox defaultRunConfiguration;
    @Deprecated //AS TODO: Remove later as soon as the Cancel Build Action is implemented
    private JTextField mavenBuildTimeoutInSeconds;

    private ServerConfiguration serverConfiguration;

    public ServerConfigurationDialog(@Nullable Project project) {
        this(project, null);
    }

    public ServerConfigurationDialog(@Nullable Project project, @Nullable ServerConfiguration serverConfiguration) {
        super(project);

        setTitle((serverConfiguration == null ? "Create" :"Edit") + " Server Connection Properties");
        setModal(true);
        setUpDialog(serverConfiguration == null ? getConfiguration() : serverConfiguration);
        init();
        // Implement the toggle if one is selected while the other is selected
        defaultRunConfiguration.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if (defaultRunConfiguration.isSelected()) {
                        defaultDebugConfiguration.setSelected(false);
                    }
                }
            }
        );
        defaultDebugConfiguration.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if(defaultDebugConfiguration.isSelected()) {
                        defaultRunConfiguration.setSelected(false);
                    }
                }
            }
        );
    }

    public ServerConfiguration getConfiguration() {
        // Use the Copy Constructor to return a Copy to be able to undo changes even after the configuration has changed
        ServerConfiguration ret = serverConfiguration != null ? new ServerConfiguration(serverConfiguration) : new ServerConfiguration(null);
        ret.setName(name.getText());
        ret.setHost(host.getText());
        ret.setDescription(description.getText());
        DefaultMode defaultMode = DefaultMode.none;
        if(defaultRunConfiguration.isSelected()) {
            defaultMode = DefaultMode.run;
        } else if(defaultDebugConfiguration.isSelected()) {
            defaultMode = DefaultMode.debug;
        }
        ret.setDefaultMode(defaultMode);
        ret.setBuildWithMaven(buildWithMaven.isSelected());
        ret.setMavenBuildTimeoutInSeconds(UIUtil.obtainInteger(mavenBuildTimeoutInSeconds, ServerConfiguration.DEFAULT_MAVEN_BUILD_TIME_OUT_IN_SECONDS));
        ret.setConnectionPort(UIUtil.obtainInteger(connectionPort, 0));
        ret.setConnectionDebugPort(UIUtil.obtainInteger(connectionDebugPort, 0));
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
        ret.setStartConnectionTimeoutInSeconds(UIUtil.obtainInteger(startConnectionTimeout, -1));
        ret.setStopConnectionTimeoutInSeconds(UIUtil.obtainInteger(stopConnectionTimeout, -1));
        ServerConfiguration.PublishType publishType =
            neverAutomaticallyPublishContentRadioButton.isSelected() ? ServerConfiguration.PublishType.never :
                automaticallyPublishOnChangeRadioButton.isSelected() ? ServerConfiguration.PublishType.automaticallyOnChange :
                    automaticallyPublishOnBuildRadioButton.isSelected() ? ServerConfiguration.PublishType.automaticallyOnBuild :
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
            switch (configuration.getDefaultMode()) {
                case run:
                    defaultRunConfiguration.setSelected(true);
                    break;
                case debug:
                    defaultDebugConfiguration.setSelected(true);
                    break;
            }
            buildWithMaven.setSelected(serverConfiguration.isBuildWithMaven());
            mavenBuildTimeoutInSeconds.setText(serverConfiguration.getMavenBuildTimeoutInSeconds() + "");
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
                case automaticallyOnBuild:
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


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
