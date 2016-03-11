/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.ui.ServerConfigurationDialog;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Created by schaefa on 6/12/15.
 */
public abstract class AbstractEditServerConfigurationAction
    extends AbstractProjectAction
{
    public AbstractEditServerConfigurationAction(@NotNull String textId) {
        super(textId);
    }

    /**
     * Adds or Edits a Server Configuration and makes sure the configuration is valid
     *
     * @param project The Current Project
     * @param source  The Server Configuration that needs to be edited. Null if a new one should be created.
     */
    protected void editServerConfiguration(@Nullable Project project, @Nullable ServerConfiguration source) {
        if(project != null) {
            boolean isOk;
            do {
                isOk = true;
                ServerConfigurationDialog dialog = new ServerConfigurationDialog(project, source);
                if(dialog.showAndGet()) {
                    final ServerConfigurationManager configuration = project.getComponent(ServerConfigurationManager.class);
                    // Check if there is not a name collision due to changed name
                    ServerConfiguration target = dialog.getConfiguration();
                    if(source != null && !source.getName().equals(target.getName())) {
                        // Name has changed => check for name collisions
                        ServerConfiguration other = configuration.findServerConfigurationByName(target.getName());
                        if(other != null) {
                            // Collision found -> alert and retry
                            isOk = false;
                            getMessageManager(project).sendErrorNotification("aem.explorer.cannot.change.configuration", target.getName());
                        }
                    } else {
                        // Verity Content
                        String message = target.verify();
                        if(message != null) {
                            isOk = false;
                            getMessageManager(project).sendErrorNotification("aem.explorer.server.configuration.invalid", AEMBundle.message(message));
                        }
                    }
                    if(isOk) {
                        if(source != null) {
                            configuration.updateServerConfiguration(source, target);
                        } else {
                            configuration.addServerConfiguration(target);
                        }
                    }
                }
            } while(!isOk);
        }
    }

}
