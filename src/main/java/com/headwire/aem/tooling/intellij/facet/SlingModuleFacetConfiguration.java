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
