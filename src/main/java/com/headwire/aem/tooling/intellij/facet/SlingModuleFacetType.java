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
