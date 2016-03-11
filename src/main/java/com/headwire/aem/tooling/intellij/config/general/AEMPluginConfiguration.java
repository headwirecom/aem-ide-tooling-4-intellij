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

package com.headwire.aem.tooling.intellij.config.general;

import com.headwire.aem.tooling.intellij.ui.AEMPluginConfigurationDialog;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.IconLoader;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * A component created just to be able to configure the plugin.
 */
@State(
        name = AEMPluginConfigurationDialog.COMPONENT_NAME,
        storages = {
                @Storage(id = "other", file = "$APP_CONFIG$/aemPluginConfigurations.config")
        }
)
public class AEMPluginConfiguration
    extends ApplicationComponent.Adapter
    implements Configurable, PersistentStateComponent<Element> {

    private boolean incrementalBuilds = true;
    private int deployDelayInSeconds = -1;

    private AEMPluginConfigurationDialog configDialog;

    public AEMPluginConfiguration() {
    }

    @NotNull
    public String getComponentName() {
        return "AEM Plugin Configuration";
    }

    public boolean isIncrementalBuilds() {
        return incrementalBuilds;
    }

    public void setIncrementalBuilds(boolean incrementalBuilds) {
        this.incrementalBuilds = incrementalBuilds;
    }

    public int getDeployDelayInSeconds() {
        return deployDelayInSeconds;
    }

    public void setDeployDelayInSeconds(int deployDelayInSeconds) {
        this.deployDelayInSeconds = deployDelayInSeconds;
    }
    // -------------- Configurable interface implementation --------------------------

    @Nls
    public String getDisplayName() {
        return "AEM Plugin";
    }

    public Icon getIcon() {
        return IconLoader.getIcon("/images/asm.gif");
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        if (configDialog==null) configDialog = new AEMPluginConfigurationDialog();
        return configDialog.getRootPane();
    }

    public boolean isModified() {
        return configDialog!=null && configDialog.isModified(this);
    }

    public void apply() throws ConfigurationException {
        if (configDialog!=null) {
            configDialog.getData(this);
        }
    }

    public void reset() {
        if (configDialog!=null) {
            configDialog.setData(this);
        }
    }

    public void disposeUIResources() {
        configDialog = null;
    }

    // -------------------- state persistence

    public Element getState() {
        Element root = new Element("state");
        Element aemNode = new Element("aemConfiguration");
        aemNode.setAttribute("incrementalBuilds", String.valueOf(incrementalBuilds));
        aemNode.setAttribute(
            "deployDelayInSeconds",
            String.valueOf(incrementalBuilds ? deployDelayInSeconds : -1)
        );
        root.addContent(aemNode);
        return root;
    }

    public void loadState(final Element state) {
        Element aemNode = state.getChild("aemConfiguration");
        if(aemNode != null) {
            incrementalBuilds = aemNode.getAttributeValue("incrementalBuilds", "true").equalsIgnoreCase("true");
            try {
                String value = aemNode.getAttributeValue("deployDelayInSeconds", "-1");
                deployDelayInSeconds = Integer.parseInt(value);
            } catch(NumberFormatException e) {
                deployDelayInSeconds = -1;
            }
        }
    }
}


