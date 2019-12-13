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

import com.headwire.aem.tooling.intellij.config.general.AEMPluginConfiguration;

import javax.swing.*;

public class AEMPluginConfigurationDialog {

    public static final String COMPONENT_NAME = "AEMPluginConfiguration";

    private JPanel contentPane;

    private JCheckBox incrementalBuild;
    private JSpinner deployDelay;
    private JLabel deployDelayLabel;
    private JCheckBox listenToFileSystemEvents;

    public AEMPluginConfigurationDialog() {
    }

    public JComponent getRootPane() {
        return contentPane;
    }

    public void setData(AEMPluginConfiguration data) {
        incrementalBuild.setSelected(data.isIncrementalBuilds());
        deployDelay.setValue(data.getDeployDelayInSeconds());
        listenToFileSystemEvents.setSelected(data.isListenToFileSystemEvents());
    }

    public void getData(AEMPluginConfiguration data) {
        data.setIncrementalBuilds(incrementalBuild.isSelected());
        data.setDeployDelayInSeconds(UIUtil.obtainInteger(deployDelay, -1));
        data.setListenToFileSystemEvents(listenToFileSystemEvents.isSelected());
    }

    public boolean isModified(AEMPluginConfiguration data) {
        return incrementalBuild.isSelected() != data.isIncrementalBuilds() ||
            listenToFileSystemEvents.isSelected() != data.isListenToFileSystemEvents() ||
            UIUtil.obtainInteger(deployDelay, -1) != data.getDeployDelayInSeconds() ;
    }
}
