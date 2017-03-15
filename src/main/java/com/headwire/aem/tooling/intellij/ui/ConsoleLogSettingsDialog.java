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

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ConsoleLogSettingsDialog extends DialogWrapper {
    private JPanel contentPane;
    private JRadioButton errorLogLevel;
    private JRadioButton warningLogLevel;
    private JRadioButton infoLogLevel;
    private JRadioButton debugLogLevel;
    private ServerConfiguration serverConfiguration;

    public ConsoleLogSettingsDialog(@NotNull Project project, @NotNull ServerConfiguration serverConfiguration) {
        super(project);

        setTitle(AEMBundle.message("dialog.log.configuration.title"));
        setModal(true);
        setUpDialog(serverConfiguration);
        init();
    }

    private void setUpDialog(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        ServerConfiguration.LogFilter logFilter = serverConfiguration.getLogFilter();
        switch(logFilter) {
            case debug:
                debugLogLevel.setSelected(true);
                break;
            case info:
                infoLogLevel.setSelected(true);
                break;
            case warning:
                warningLogLevel.setSelected(true);
                break;
            case error:
            default:
                errorLogLevel.setSelected(true);
                break;
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    protected void doOKAction() {
        if(infoLogLevel.isSelected()) {
            serverConfiguration.setLogFilter(ServerConfiguration.LogFilter.info);
        } else if(warningLogLevel.isSelected()) {
            serverConfiguration.setLogFilter(ServerConfiguration.LogFilter.warning);
        } else if(debugLogLevel.isSelected()) {
            serverConfiguration.setLogFilter(ServerConfiguration.LogFilter.debug);
        } else {
            serverConfiguration.setLogFilter(ServerConfiguration.LogFilter.error);
        }
        super.doOKAction();
    }
}
