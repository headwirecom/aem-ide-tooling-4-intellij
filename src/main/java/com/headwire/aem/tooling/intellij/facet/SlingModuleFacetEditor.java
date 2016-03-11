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

import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.eclipse.ProjectUtil;
import com.headwire.aem.tooling.intellij.facet.SlingModuleExtensionProperties.ModuleType;
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

    private SlingModuleFacetConfiguration slingModuleFacetConfiguration;
    private FacetEditorContext editorContext;
    private FacetValidatorsManager validatorsManager;

    public SlingModuleFacetEditor(SlingModuleFacetConfiguration slingModuleFacetConfiguration, final FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        this.slingModuleFacetConfiguration = slingModuleFacetConfiguration;
        this.editorContext = editorContext;
        this.validatorsManager = validatorsManager;
        reset();
        sourceRootPath.addBrowseFolderListener(
            "Source Content Root Folder",
            "For Content Modules you need to specify the source content root folder",
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
                        VirtualFile moduleFile = module.getModuleFile();
                        String filePath = sourceRootPath.getText();
                        if(filePath == null || filePath.length() == 0) {
                            return new ValidationResult(
                                "For a Content Module a Content Root Path must be set",
                                null //AS TODO: Shall we create a Quick Fix here?
                            );
                        } else {
                            VirtualFile folder = moduleFile.getFileSystem().findFileByPath(filePath);
                            if(folder == null) {
                                return new ValidationResult(
                                    "Content Root Folder: '" + filePath + "' could not be found",
                                    null //AS TODO: Shall we create a Quick Fix here?
                                );
                            } else if(!folder.isDirectory()) {
                                return new ValidationResult(
                                    "Content Root Folder: '" + filePath + "' is not a folder",
                                    null //AS TODO: Shall we create a Quick Fix here?
                                );
                            }
                        }
                    }
                    return ValidationResult.OK;
                }
            },
            sourceRootPath
        );
        metaInfPath.addBrowseFolderListener(
            "Filter Root Folder",
            "For Content Modules you need to specify the META-INF folder (containing the vault/filter.xml file)",
            editorContext.getProject(),
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
        validatorsManager.registerValidator(
            new FacetEditorValidator() {
                @NotNull
                @Override
                public ValidationResult check() {
                    String result = "";
                    ModuleType moduleType = getModuleType();
                    String sourceRoot = "";
                    if(moduleType == ModuleType.content) {
                        Module module = editorContext.getModule();
                        VirtualFile moduleFile = module.getModuleFile();
                        String filePath = metaInfPath.getText();
                        if(filePath == null || filePath.length() == 0) {
                            return new ValidationResult(
                                "For a Content Module a Meta Inf Path must be set",
                                null //AS TODO: Shall we create a Quick Fix here?
                            );
                        } else {
                            VirtualFile folder = moduleFile.getFileSystem().findFileByPath(filePath);
                            if(folder == null) {
                                return new ValidationResult(
                                    "META-INF Folder: '" + filePath + "' could not be found",
                                    null //AS TODO: Shall we create a Quick Fix here?
                                );
                            } else if(!folder.isDirectory()) {
                                return new ValidationResult(
                                    "META-INF Folder: '" + filePath + "' is not a folder",
                                    null //AS TODO: Shall we create a Quick Fix here?
                                );
                            } else {
                                // Check that the Filter file is there
                                VirtualFile filterFile = ProjectUtil.findFileOrFolder(folder, VAULT_FILTER_FILE_NAME, false);
                                if(filterFile == null) {
                                    return new ValidationResult(
                                        "META-INF Folder: '" + filePath + "' does not contain filter file (" + VAULT_FILTER_FILE_NAME + ")",
                                        null //AS TODO: Shall we create a Quick Fix here?
                                    );
                                }
                            }
                        }
                    }
                    return ValidationResult.OK;
                }
            }, metaInfPath
        );
        ChangeListener groupChangeListener = new GroupChangeListener();
        contentCheckBox.addChangeListener(groupChangeListener);
        bundleCheckBox.addChangeListener(groupChangeListener);
        excludedCheckBox.addChangeListener(groupChangeListener);
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
        LOG.debug(
            "Module Type Changed: " + moduleTypeChanged +
            "source path changed: " + sourcePathChanged +
            "metainf path changed: " + metainfPathChanged
        );
        return moduleTypeChanged ||
            (moduleType == ModuleType.content && (sourcePathChanged || metainfPathChanged));
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
            if(filePath == null) {
                throw new ConfigurationException("Source Root Path must be set for Content Module");
            } else {
                VirtualFile folder = moduleFile.getFileSystem().findFileByPath(filePath);
                if(folder == null) {
                    throw new ConfigurationException("Source Root Path: " + filePath + " does not point to a folder");
                } else {
                    sourceRoot = folder.getPath();
                }
            }
        }
        slingModuleFacetConfiguration.setSourceRootPath(sourceRoot);
        String filterRoot = "";
        if(moduleType == ModuleType.content) {
            Module module = editorContext.getModule();
            VirtualFile moduleFile = module.getModuleFile();
            String filePath = metaInfPath.getText();
            if(filePath == null) {
                throw new ConfigurationException("Filter Root Path must be set for Content Module");
            } else {
                VirtualFile folder = moduleFile.getFileSystem().findFileByPath(filePath);
                if(folder == null) {
                    throw new ConfigurationException("Metainf Path: " + filePath + " does not point to a folder");
                } else {
                    filterRoot = folder.getPath();
                }
            }
        }
        slingModuleFacetConfiguration.setMetaInfPath(filterRoot);
        // Inform the Server Configuration Manager about the change and that he should refresh the modules
        ServerConfigurationManager serverConfigurationManager =
            editorContext.getProject().getComponent(ServerConfigurationManager.class);
        boolean succeeded = false;
        if(serverConfigurationManager != null) {
            succeeded = serverConfigurationManager.updateCurrentServerConfiguration();
        }
        if(!succeeded) {
            throw new ConfigurationException("Failed to update server configuration");
        }
    }

    @Override
    public void reset() {
        sourceRootPath.setText(slingModuleFacetConfiguration.getSourceRootPath());
        metaInfPath.setText(slingModuleFacetConfiguration.getMetaInfPath());
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
        return "Sling Content Facet";
    }

    private void checkCheckBoxes() {
        sourceRootPath.setEnabled(contentCheckBox.isSelected());
        metaInfPath.setEnabled(contentCheckBox.isSelected());
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
