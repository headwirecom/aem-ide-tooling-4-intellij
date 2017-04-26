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

package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.config.general.AEMPluginConfiguration;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/12/15.
 */
public class ListenToFileSystemToggleConfigurationAction
    extends AnAction implements Toggleable
{

    public ListenToFileSystemToggleConfigurationAction() {
        super(
            AEMBundle.message("action.toggle.listen.to.file.system.text"),
            AEMBundle.message("action.toggle.listen.to.file.system.description"),
            null
        );
    }

    // The next two methods were copied from IntelliJ's ToggleAction but we need to have access to actionPerformed
    // which is final to handle the right click
    public final void actionPerformed(@NotNull AnActionEvent event) {
        if((event.getModifiers() & MouseEvent.ALT_MASK) != 0) {
            ShowSettingsUtil.getInstance().showSettingsDialog(event.getProject(), AEMPluginConfiguration.DISPLAY_NAME);
        } else {
            boolean state = !this.isSelected(event);
            this.setSelected(event, state);
            Presentation presentation = event.getPresentation();
            presentation.putClientProperty("selected", state);
        }
    }

    public void update(@NotNull AnActionEvent event) {
        boolean selected = this.isSelected(event);
        Presentation presentation = event.getPresentation();
        presentation.putClientProperty("selected", selected);
    }

    public boolean isSelected(AnActionEvent event) {
        AEMPluginConfiguration pluginConfiguration = ComponentProvider.getComponent(event.getProject(), AEMPluginConfiguration.class);
        if(pluginConfiguration != null) {
            return pluginConfiguration.isListenToFileSystemEvents();
        } else {
            return true;
        }
    }

    public void setSelected(AnActionEvent event, boolean value) {
        AEMPluginConfiguration pluginConfiguration = ComponentProvider.getComponent(event.getProject(), AEMPluginConfiguration.class);
        if(pluginConfiguration != null) {
            pluginConfiguration.setListenToFileSystemEvents(value);
        }
    }

}
