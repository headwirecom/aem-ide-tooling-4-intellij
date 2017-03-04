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

import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.facet.SlingModuleExtensionProperties.ModuleType;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetEditorValidator;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static com.headwire.aem.tooling.intellij.util.Constants.VAULT_FILTER_FILE_NAME;
import static com.headwire.aem.tooling.intellij.facet.FacetUtil.createValidatorResult;
import static com.headwire.aem.tooling.intellij.facet.FacetUtil.createConfigurationException;

/**
 * Created by Andreas Schaefer (Headwire.com) on 3/4/16.
 */
public class SlingModuleFacetEditor
    extends FacetEditorTab
{
    private static final Logger LOG = Logger.getInstance("#com.headwire.aem.tooling.intellij.facet.SlingModuleFacetEditor");

    private JPanel panel1;
    private TextFieldWithBrowseButton sourceRootPath;
    private JCheckBox contentCheckBox;
    private JCheckBox bundleCheckBox;
    private JCheckBox excludedCheckBox;
    private TextFieldWithBrowseButton metaInfPath;
    private JTextField bundleSymbolicName;
    private JTextField bundleVersion;
    private JCheckBox ignoreMaven;
    private JTextField jarFileName;
    private JScrollPane facetHelpScrollPane;
    private JTextArea facetHelp;
    private JCheckBox generatedFilter;

    private SlingModuleFacetConfiguration slingModuleFacetConfiguration;
    private FacetEditorContext editorContext;
    private FacetValidatorsManager validatorsManager;

    public SlingModuleFacetEditor(SlingModuleFacetConfiguration slingModuleFacetConfiguration, final FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        this.slingModuleFacetConfiguration = slingModuleFacetConfiguration;
        this.editorContext = editorContext;
        this.validatorsManager = validatorsManager;
        this.facetHelp.setText(AEMBundle.message("facet.help.text"));
        reset();
        sourceRootPath.addBrowseFolderListener(
            AEMBundle.message("facet.content.filter.title"),
            AEMBundle.message("facet.content.filter.description"),
            editorContext.getProject(),
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
        validatorsManager.registerValidator(
            new FacetEditorValidator() {
                @NotNull
                @Override
                public ValidationResult check() {
                    ModuleType moduleType = getModuleType();
                    if(moduleType == ModuleType.content) {
                        Module module = editorContext.getModule();
                        String filePath = sourceRootPath.getText();
                        switch(FacetUtil.checkFile(module, filePath, true)) {
                            case fileEmpty:
                                return createValidatorResult("facet.content.root.not.set");
                            case fileNotFound:
                                return createValidatorResult("facet.content.folder.not.found", filePath);
                            case notDirectory:
                                return createValidatorResult("facet.content.is.not.folder", filePath);
                        }
                    }
                    return ValidationResult.OK;
                }
            },
            sourceRootPath
        );
        metaInfPath.addBrowseFolderListener(
            AEMBundle.message("facet.meta-inf.filter.title"),
            AEMBundle.message("facet.meta-inf.filter.description"),
            editorContext.getProject(),
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
        validatorsManager.registerValidator(
            new FacetEditorValidator() {
                @NotNull
                @Override
                public ValidationResult check() {
                    ModuleType moduleType = getModuleType();
                    if(moduleType == ModuleType.content) {
                        boolean generatedFilterFlag = generatedFilter.isSelected();
                        Module module = editorContext.getModule();
                        String filePath = metaInfPath.getText();
                        switch(FacetUtil.checkFile(module, filePath, true)) {
                            case fileEmpty:
                                return createValidatorResult("facet.meta-inf.root.not.set");
                            case fileNotFound:
                                return createValidatorResult("facet.meta-inf.folder.not.found", filePath);
                            case notDirectory:
                                return createValidatorResult("facet.meta-inf.is.not.folder", filePath);
                            case ok:
                                if(!generatedFilterFlag) {
                                    // Because the filter.xml is generated the filter.xml might not be there yet
                                    VirtualFile folder = module.getModuleFile().getFileSystem().findFileByPath(filePath);
                                    // Check that the Filter file is there
                                    VirtualFile filterFile = Util.findFileOrFolder(folder, VAULT_FILTER_FILE_NAME, false);
                                    if(filterFile == null) {
                                        return createValidatorResult("facet.meta-inf.folder.filter.not.found", filePath);
                                    }
                                }
                        }
                    }
                    return ValidationResult.OK;
                }
            }, metaInfPath
        );
        validatorsManager.registerValidator(
            new FacetEditorValidator() {
                @NotNull
                @Override
                public ValidationResult check() {
                    ModuleType moduleType = getModuleType();
                    if(moduleType == ModuleType.bundle) {
                        String symbolicName = bundleSymbolicName.getText();
                        if(symbolicName.length() == 0) {
                            return createValidatorResult("facet.osgi.symbolic.name.missing");
                        }
                    }
                    return ValidationResult.OK;
                }
            },
            bundleSymbolicName
        );
        validatorsManager.registerValidator(
            new FacetEditorValidator() {
                @NotNull
                @Override
                public ValidationResult check() {
                    ModuleType moduleType = getModuleType();
                    if(moduleType == ModuleType.bundle) {
                        boolean ignore = ignoreMaven.isSelected();
                        String version = bundleVersion.getText();
                        if(ignore) {
                            if(version.length() == 0) {
                                return createValidatorResult("facet.osgi.version.missing");
                            }
                        } else {
                            if(version.length() > 0) {
                                return createValidatorResult("facet.osgi.version.not.allowed.with.maven");
                            }
                        }
                    }
                    return ValidationResult.OK;
                }
            },
            bundleVersion
        );
        validatorsManager.registerValidator(
            new FacetEditorValidator() {
                @NotNull
                @Override
                public ValidationResult check() {
                    ModuleType moduleType = getModuleType();
                    if(moduleType == ModuleType.bundle) {
                        boolean ignore = ignoreMaven.isSelected();
                        String jar = jarFileName.getText();
                        if(ignore) {
                            if(jar.length() == 0) {
                                return createValidatorResult("facet.osgi.jar.missing");
                            }
                        } else {
                            if(jar.length() > 0) {
                                return createValidatorResult("facet.osgi.jar.not.allowed.with.maven");
                            }
                        }
                    }
                    return ValidationResult.OK;
                }
            },
            jarFileName
        );
        ChangeListener groupChangeListener = new GroupChangeListener();
        contentCheckBox.addChangeListener(groupChangeListener);
        bundleCheckBox.addChangeListener(groupChangeListener);
        excludedCheckBox.addChangeListener(groupChangeListener);
        generatedFilter.addChangeListener(groupChangeListener);
        ignoreMaven.addChangeListener(groupChangeListener);
        // Make sure the source root path is enabled correctly at the beginnning
        checkCheckBoxes();
    }

    @NotNull
    @Override
    public JComponent createComponent() {
        return panel1;
    }

    @Override
    public boolean isModified() {
        ModuleType moduleType = getModuleType();
        boolean moduleTypeChanged = moduleType != slingModuleFacetConfiguration.getModuleType();
        boolean sourcePathChanged = !sourceRootPath.getText().equals(slingModuleFacetConfiguration.getSourceRootPath());
        boolean metainfPathChanged = !metaInfPath.getText().equals(slingModuleFacetConfiguration.getMetaInfPath());
        LOG.debug(AEMBundle.message("debug.facet.module.type.changed", moduleTypeChanged));
        LOG.debug(AEMBundle.message("debug.facet.source.path.changed", sourcePathChanged));
        LOG.debug(AEMBundle.message("debug.facet.meta-inf.path.changed", metainfPathChanged));
        boolean generatedFilterChanged = generatedFilter.isSelected() != slingModuleFacetConfiguration.isGeneratedFilter();
        boolean ignoreMavenChanged = ignoreMaven.isSelected() != slingModuleFacetConfiguration.isIgnoreMaven();
        boolean symbolicNameChanged = bundleSymbolicName.getText().equals(slingModuleFacetConfiguration.getOsgiSymbolicName());
        boolean versionChanged = bundleVersion.getText().equals(slingModuleFacetConfiguration.getOsgiVersion());
        boolean jarFileNameChanged = jarFileName.getText().equals(slingModuleFacetConfiguration.getOsgiJarFileName());
        return moduleTypeChanged ||
            (moduleType == ModuleType.content && (sourcePathChanged || metainfPathChanged || generatedFilterChanged)) ||
            (moduleType == ModuleType.bundle && (ignoreMavenChanged || generatedFilterChanged || symbolicNameChanged || versionChanged || jarFileNameChanged))
            ;
    }

    @Override
    public void apply() throws ConfigurationException {
        ModuleType moduleType = getModuleType();
        slingModuleFacetConfiguration.setModuleType(moduleType);
        String sourceRoot = "";
        if(moduleType == ModuleType.content) {
            Module module = editorContext.getModule();
            VirtualFile moduleFile = module.getModuleFile();
            String filePath = sourceRootPath.getText();
            switch(FacetUtil.checkFile(module, filePath, true)) {
                case fileEmpty:
                    throw createConfigurationException("facet.content.root.not.set");
                case fileNotFound:
                    throw createConfigurationException("facet.content.folder.not.found", filePath);
                case notDirectory:
                    throw createConfigurationException("facet.content.is.not.folder", filePath);
                default:
                    VirtualFile folder = moduleFile.getFileSystem().findFileByPath(filePath);
                    sourceRoot = folder.getPath();
            }
        }
        slingModuleFacetConfiguration.setSourceRootPath(sourceRoot);
        String filterRoot = "";
        boolean generatedFilterFlag = generatedFilter.isSelected();
        if(moduleType == ModuleType.content) {
            Module module = editorContext.getModule();
            String filePath = metaInfPath.getText();
            switch(FacetUtil.checkFile(module, filePath, true)) {
                case fileEmpty:
                    throw createConfigurationException("facet.meta-inf.root.not.set");
                case fileNotFound:
                    throw createConfigurationException("facet.meta-inf.folder.not.found", filePath);
                case notDirectory:
                    throw createConfigurationException("facet.meta-inf.is.not.folder", filePath);
                case ok:
                    VirtualFile folder = module.getModuleFile().getFileSystem().findFileByPath(filePath);
                    // Check that the Filter file is there
                    VirtualFile filterFile = Util.findFileOrFolder(folder, VAULT_FILTER_FILE_NAME, false);
                    if(filterFile == null) {
                        throw createConfigurationException("facet.meta-inf.folder.filter.not.found", filePath);
                    } else {
                        filterRoot = folder.getPath();
                    }
            }
        }
        slingModuleFacetConfiguration.setMetaInfPath(filterRoot);
        boolean ignore = true;
        String symbolicName = "";
        String version = "";
        String jar = "";
        if(moduleType == ModuleType.bundle) {
            ignore = ignoreMaven.isSelected();
            symbolicName = bundleSymbolicName.getText();
            version = bundleVersion.getText();
            jar = jarFileName.getText();
            if(ignore) {
                if(version.length() == 0) {
                    throw createConfigurationException("facet.osgi.version.missing");
                } else if(jar.length() == 0) {
                    throw createConfigurationException("facet.osgi.jar.missing");
                }
            }
            if(symbolicName.length() == 0) {
                throw createConfigurationException("facet.osgi.symbolic.name.missing");
            }
        }
        slingModuleFacetConfiguration.setGeneratedFilter(generatedFilterFlag);
        slingModuleFacetConfiguration.setIgnoreMaven(ignore);
        slingModuleFacetConfiguration.setOsgiSymbolicName(symbolicName);
        slingModuleFacetConfiguration.setOsgiVersion(version);
        slingModuleFacetConfiguration.setOsgiJarFileName(jar);
        // Inform the Server Configuration Manager about the change and that he should refresh the modules
        ServerConfigurationManager serverConfigurationManager =
            editorContext.getProject().getComponent(ServerConfigurationManager.class);
        boolean succeeded = false;
        if(serverConfigurationManager != null) {
            succeeded = serverConfigurationManager.updateCurrentServerConfiguration();
        }
        if(!succeeded) {
            throw new ConfigurationException(AEMBundle.message("facet.update.server.configuration.failed"));
        }
    }

    @Override
    public void reset() {
        sourceRootPath.setText(slingModuleFacetConfiguration.getSourceRootPath());
        metaInfPath.setText(slingModuleFacetConfiguration.getMetaInfPath());
        generatedFilter.setSelected(slingModuleFacetConfiguration.isGeneratedFilter());
        ignoreMaven.setSelected(slingModuleFacetConfiguration.isIgnoreMaven());
        bundleSymbolicName.setText(slingModuleFacetConfiguration.getOsgiSymbolicName());
        bundleVersion.setText(slingModuleFacetConfiguration.getOsgiVersion());
        jarFileName.setText(slingModuleFacetConfiguration.getOsgiJarFileName());
        switch(slingModuleFacetConfiguration.getModuleType()) {
            case content:
                contentCheckBox.setSelected(true);
                break;
            case bundle:
                bundleCheckBox.setSelected(true);
                break;
            default:
                excludedCheckBox.setSelected(true);
        }
    }

    @Override
    public void disposeUIResources() {
    }

    @Nls
    @Override
    public String getDisplayName() {
        return AEMBundle.message("facet.name");
    }

    private void checkCheckBoxes() {
        sourceRootPath.setEnabled(contentCheckBox.isSelected());
        metaInfPath.setEnabled(contentCheckBox.isSelected());
        generatedFilter.setEnabled(contentCheckBox.isSelected());
        bundleVersion.setEnabled(ignoreMaven.isSelected());
        jarFileName.setEnabled(ignoreMaven.isSelected());
    }

    private ModuleType getModuleType() {
        return contentCheckBox.isSelected() ? ModuleType.content :
                bundleCheckBox.isSelected() ? ModuleType.bundle : ModuleType.excluded;
    }

    private class GroupChangeListener
        implements ChangeListener
    {

        @Override
        public void stateChanged(ChangeEvent e) {
            checkCheckBoxes();
        }
    }
}
