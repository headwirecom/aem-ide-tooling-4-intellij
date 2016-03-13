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

package com.headwire.aem.tooling.intellij.facet;

import com.headwire.aem.tooling.intellij.facet.SlingModuleExtensionProperties.ModuleType;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

//AS TODO: This file might not be needed as we can place that into the ServerConfiguration.Module instance.

/**
 * @author Andreas Schaefer
 */
public class SlingModuleFacetConfiguration implements FacetConfiguration, PersistentStateComponent<SlingModuleExtensionProperties> {
    private SlingModuleExtensionProperties properties = new SlingModuleExtensionProperties();

    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[]{new SlingModuleFacetEditor(this, editorContext, validatorsManager)};
    }

    public void readExternal(Element element) throws InvalidDataException {
    }

    public void writeExternal(Element element) throws WriteExternalException {
    }

    public SlingModuleExtensionProperties getState() {
        return properties;
    }

    public void loadState(SlingModuleExtensionProperties state) {
        properties = state;
    }

    public String getSourceRootPath() {
        return properties.sourceRootPath;
    }

    public void setSourceRootPath(String sourceRootPath) {
        properties.sourceRootPath = sourceRootPath;
    }

    public String getMetaInfPath() {
        return properties.metaInfPath;
    }

    public void setMetaInfPath(String metainfPath) {
        properties.metaInfPath = metainfPath;
    }

    public ModuleType getModuleType() {
        return properties.moduleType;
    }

    public void setModuleType(ModuleType moduleType) {
        properties.moduleType = moduleType;
    }
}
