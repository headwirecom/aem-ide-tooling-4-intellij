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

package com.headwire.aem.tooling.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author nik
 */
public class SlingModuleFacetType extends FacetType<SlingModuleFacet, SlingModuleFacetConfiguration> {
    public static final String STRING_ID = "sling-module-facet";
    public static final String SLING_MODULE_FACET = "Sling Module Facet";

    public SlingModuleFacetType() {
        super(SlingModuleFacet.ID, STRING_ID, SLING_MODULE_FACET);
    }

    public SlingModuleFacetConfiguration createDefaultConfiguration() {
        return new SlingModuleFacetConfiguration();
    }

    public SlingModuleFacet createFacet(@NotNull Module module, String name, @NotNull SlingModuleFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new SlingModuleFacet(this, module, name, configuration);
    }

    public boolean isSuitableModuleType(ModuleType moduleType) {
        return moduleType instanceof JavaModuleType;
    }

    @NotNull
    @Override
    public String getDefaultFacetName() {
        return SLING_MODULE_FACET;
    }

    @Override
    public Icon getIcon() {
        return HeadwireIcons.HWLogo;
    }
}
