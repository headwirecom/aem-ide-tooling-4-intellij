/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.explorer.SlingModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.hash.HashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenPropertiesPanel;
import org.jetbrains.idea.maven.model.MavenArchetype;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenEnvironmentForm;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenUtil;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sergey Evdokimov
 */
public class ArchetypePropertiesStep extends ModuleWizardStep {

    public static final String NAME_PLACEHOLDER = "$name$";
    public static final String NAME_FOR_FOLDER_PLACEHOLDER = "$nameForFolder$";
    public static final String ARTIFACT_ID_PLACEHOLDER = "$artifactId$";
    private final Project myProjectOrNull;
    private final SlingModuleBuilder myBuilder;

    private JPanel myMainPanel;
    private JPanel myEnvironmentPanel;
    private JPanel myPropertiesPanel;
    private JTextField artifactName;
    private JCheckBox doFillIn;

    private MavenEnvironmentForm myEnvironmentForm;
    private MavenPropertiesPanel myMavenPropertiesPanel;

    private Map<String, String> myAvailableProperties = new HashMap<String, String>();
    private Map<String, String> requiredProperties = new java.util.HashMap<String, String>();

    public ArchetypePropertiesStep(@Nullable Project project, SlingModuleBuilder builder) {
        myProjectOrNull = project;
        myBuilder = builder;

        initComponents();
    }

    private void initComponents() {
        myEnvironmentForm = new MavenEnvironmentForm();

        Project project = myProjectOrNull == null ? ProjectManager.getInstance().getDefaultProject() : myProjectOrNull;
        myEnvironmentForm.getData(MavenProjectsManager.getInstance(project).getGeneralSettings().clone());

        myEnvironmentPanel.add(myEnvironmentForm.createComponent(), BorderLayout.CENTER);

        //AS TODO: If we keep on using the archetype properties we might add a description to the Required Properties
        //AS TODO: but then we need to copy this class over and add the description to the dialog.
        myMavenPropertiesPanel = new MavenPropertiesPanel(myAvailableProperties);
        myPropertiesPanel.add(myMavenPropertiesPanel);

        doFillIn.setSelected(true);
        artifactName.addKeyListener(
            new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent keyEvent) {
                    super.keyReleased(keyEvent);
                    if(doFillIn.isSelected()) {
                        updateProperties();
                    }
                }
            }
        );
        artifactName.addFocusListener(
            new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent focusEvent) {
                    super.focusLost(focusEvent);
                    if(doFillIn.isSelected()) {
                        updateProperties();
                    }
                }
            }
        );
    }

    @Override
    public void updateStep() {
        SlingModuleBuilder.ArchetypeTemplate archetypeTemplate = myBuilder.getArchetypeTemplate();
        MavenArchetype archetype = archetypeTemplate.getMavenArchetype();

        Map<String, String> props = new LinkedHashMap<String, String>();

        MavenId projectId = myBuilder.getProjectId();

        props.put("groupId", projectId.getGroupId());
        props.put("artifactId", projectId.getArtifactId());
        props.put("version", projectId.getVersion());

        props.put("archetypeGroupId", archetype.groupId);
        props.put("archetypeArtifactId", archetype.artifactId);
        props.put("archetypeVersion", archetype.version);
        if (archetype.repository != null) props.put("archetypeRepository", archetype.repository);
        myMavenPropertiesPanel.setDataFromMap(props);

        //Add any props from the Builder
        requiredProperties = archetypeTemplate.getRequiredProperties();
        updateProperties();
    }

    private void updateProperties() {
        MavenId projectId = myBuilder.getProjectId();
        Map<String, String> props = myMavenPropertiesPanel.getDataAsMap();
        for(Map.Entry<String, String> entry: requiredProperties.entrySet()) {
            String propValue = entry.getValue();
            if(StringUtil.isNotEmpty(propValue)) {
                if(propValue.contains(NAME_PLACEHOLDER)) {
                    propValue = propValue.replace(NAME_PLACEHOLDER, artifactName.getText());
                } else if(propValue.contains(NAME_FOR_FOLDER_PLACEHOLDER)) {
                    propValue = propValue.replace(NAME_FOR_FOLDER_PLACEHOLDER, artifactName.getText().toLowerCase().replaceAll("[^a-zA-Z]", ""));
                } else if(propValue.contains(ARTIFACT_ID_PLACEHOLDER)) {
                    propValue = propValue.replace(ARTIFACT_ID_PLACEHOLDER, projectId.getArtifactId());
                }
            }
            props.put(entry.getKey(), propValue);
        }

        myMavenPropertiesPanel.setDataFromMap(props);
    }

    @Override
    public JComponent getComponent() {
        return myMainPanel;
    }

    @Override
    public boolean isStepVisible() {
        return myBuilder.getArchetypeTemplate() != null;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        File mavenHome = MavenUtil.resolveMavenHomeDirectory(myEnvironmentForm.getMavenHome());
        if (mavenHome == null) {
            throw new ConfigurationException("Maven home directory is not specified");
        }

        if (!MavenUtil.isValidMavenHome(mavenHome)) {
            throw new ConfigurationException("Maven home directory is invalid: " + mavenHome);
        }

        return true;
    }

    @Override
    public void updateDataModel() {
        myBuilder.setEnvironmentForm(myEnvironmentForm);
        myBuilder.setPropertiesToCreateByArtifact(myMavenPropertiesPanel.getDataAsMap());
    }

    @Override
    public String getHelpId() {
        return "New_Projects_from_Scratch_Maven_Settings_Page";
    }
}
