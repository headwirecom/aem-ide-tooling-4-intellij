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

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration.SupportInstallationType;
import com.headwire.aem.tooling.intellij.util.ArtifactsLocatorImpl;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.*;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.bouncycastle.util.Arrays;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.DefaultMode;

public class ServerConfigurationDialog
    extends DialogWrapper
{
    private static final String DUMMY_PWD = "~~~~~~~~~~~~~~~~~~~";
    private JPanel contentPane;
    private JTextField host;
    private JTabbedPane tabbedPane1;
    private JTextField connectionDebugPort;
    private JTextField connectionUserName;
    private JPasswordField connectionPassword;
    private JTextField connectionPort;
    private JTextField connectionContextPath;
    private JSpinner bundleDeploymentWaitPeriodInSeconds;
    private JSpinner bundleDeploymentTries;
    private JRadioButton neverAutomaticallyPublishContentRadioButton;
    private JRadioButton automaticallyPublishOnChangeRadioButton;
    private JRadioButton automaticallyPublishOnBuildRadioButton;
    private JRadioButton installSupportBundleAutomaticallyRadioButton;
    private JRadioButton installSupportBundleManuallyRadioButton;
    private JButton installButton;
    private JTextField name;
    private JTextField description;
    private JCheckBox buildWithMaven;
    private JCheckBox defaultDebugConfiguration;
    private JCheckBox defaultRunConfiguration;
    @Deprecated //AS TODO: Remove later as soon as the Cancel Build Action is implemented
    private JTextField mavenBuildTimeoutInSeconds;
    private JList supportBundleList;

    private ServerConfiguration serverConfiguration;
    private ServerConnectionManager serverConnectionManager;
    private MessageManager messageManager;

    public ServerConfigurationDialog(@Nullable Project project) {
        this(project, null);
    }

    public ServerConfigurationDialog(@Nullable Project project, @Nullable final ServerConfiguration serverConfiguration) {
        super(project);

        setTitle((serverConfiguration == null ? "Create" :"Edit") + " Server Connection Properties");
        setModal(true);
        setUpDialog(serverConfiguration == null ? getConfiguration() : serverConfiguration);
        init();
        serverConnectionManager = ComponentProvider.getComponent(project, ServerConnectionManager.class);
        messageManager = ComponentProvider.getComponent(project, MessageManager.class);
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
        installButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // First Check if the Install Support Bundle is installed
                    if(serverConnectionManager != null) {
                        serverConnectionManager.checkAndUpdateSupportBundle(false);
                    } else if(messageManager != null) {
                        messageManager.showAlert("server.configuration.configuration.no.connection");
                    }
                }
            }
        );
        installSupportBundleAutomaticallyRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                supportBundleList.setEnabled(true);
            }
        });
        installSupportBundleManuallyRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                supportBundleList.setEnabled(false);
            }
        });
        supportBundleList.setCellRenderer(new StringListCellRenderer());
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
        if(password == null) { password = new char[0]; }
        // If it is the dummy password then ignore it
        if(!Arrays.areEqual(password, DUMMY_PWD.toCharArray())) {
            ret.setPassword(password);
        }
        ret.setContextPath(connectionContextPath.getText());
        ret.setBundleDeploymentRetries(UIUtil.obtainInteger(bundleDeploymentTries, -1));
        ret.setBundleDeploymentWaitPeriodInSeconds(UIUtil.obtainInteger(bundleDeploymentWaitPeriodInSeconds, -1));
        ServerConfiguration.PublishType publishType =
            neverAutomaticallyPublishContentRadioButton.isSelected() ? ServerConfiguration.PublishType.never :
                automaticallyPublishOnChangeRadioButton.isSelected() ? ServerConfiguration.PublishType.automaticallyOnChange :
                    automaticallyPublishOnBuildRadioButton.isSelected() ? ServerConfiguration.PublishType.automaticallyOnBuild :
                        null;
        ret.setPublishType(publishType);
        SupportInstallationType installationType =
            installSupportBundleAutomaticallyRadioButton.isSelected() ? SupportInstallationType.installAutomatically :
                installSupportBundleManuallyRadioButton.isSelected() ? SupportInstallationType.installManually :
                        null;
        ret.setInstallationType(installationType);
        //AS TODO: Add a check there to make sure a value is selected
        ret.setSupportBundleVersion(supportBundleList.getSelectedValue() + "");

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
            bundleDeploymentTries.setValue(serverConfiguration.getBundleDeploymentRetries());
            bundleDeploymentWaitPeriodInSeconds.setValue(serverConfiguration.getBundleDeploymentWaitPeriodInSeconds());
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
                case installAutomatically:
                    installSupportBundleAutomaticallyRadioButton.setSelected(true);
                    supportBundleList.setEnabled(true);
                    break;
                case installManually:
                    installSupportBundleManuallyRadioButton.setSelected(true);
                    supportBundleList.setEnabled(false);
                    break;
                default:
                    installSupportBundleAutomaticallyRadioButton.setSelected(true);
                    break;
            }
            supportBundleList.setModel(new StringListModel(ArtifactsLocatorImpl.PROVIDED_VERSIONS));
            // Select the current version
            String bundleVersion = configuration.getSupportBundleVersion();
            if(bundleVersion == null || bundleVersion.isEmpty()) {
                bundleVersion = ArtifactsLocatorImpl.DEFAULT_TOOLING_SUPPORT_BUNDLE_VERSION;
            }
            supportBundleList.setSelectedValue(bundleVersion, true);
            if(supportBundleList.getSelectedIndex() < 0) {
                // Nothing was selected so select the default
                supportBundleList.setSelectedValue(ArtifactsLocatorImpl.DEFAULT_TOOLING_SUPPORT_BUNDLE_VERSION, true);
            }
            // Preset the Dialog with dummy value if password is set
            char[] pwd = configuration.getPassword();
            if(pwd != null && pwd.length > 0) {
                connectionPassword.setText(DUMMY_PWD);
            }
        }
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public static class StringListModel extends AbstractListModel {
        private List<String> data;

        public StringListModel(String[] data) {
            this(java.util.Arrays.asList(data));
        }

        public StringListModel(List<String> data) {
            this.data = data;
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int i) {
            return data.get(i);
        }

        public List<String> getData() {
            return data;
        }

        public boolean addString(String line) {
            boolean ret = false;
            int index = data.indexOf(line);
            if(index < 0) {
                data.add(line);
                index = data.indexOf(line);
                fireIntervalAdded(line, index, index);
                ret = true;
            }
            return ret;
        }

        public boolean removeString(String line) {
            boolean ret = false;
            int index = data.indexOf(line);
            if(index >= 0) {
                data.remove(index);
                fireIntervalRemoved(line, index, index);
                ret = true;
            }
            return ret;
        }

        public void clear() {
            data.clear();
        }
    }

    public static class StringListCellRenderer
        extends ColoredListCellRenderer
    {
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            append(
                value.toString(),
                SimpleTextAttributes.REGULAR_ATTRIBUTES
            );
        }
    }

}
