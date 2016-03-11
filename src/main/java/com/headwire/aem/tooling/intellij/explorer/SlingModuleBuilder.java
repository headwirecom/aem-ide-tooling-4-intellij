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

package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.ui.ArchetypePropertiesStep;
import com.headwire.aem.tooling.intellij.ui.SlingArchetypesStep;
import com.headwire.aem.tooling.intellij.util.PropertiesHandler;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.indices.MavenIndicesManager;
import org.jetbrains.idea.maven.model.MavenArchetype;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenEnvironmentForm;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.jetbrains.idea.maven.wizards.MavenModuleBuilder;
import org.jetbrains.idea.maven.wizards.MavenModuleBuilderHelper;
import org.jetbrains.idea.maven.wizards.MavenModuleWizardStep;

import javax.swing.Icon;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This Module Builder is making sure the Sling / AEM Archetypes are provides and gives the user
 * the appropriate choices. It will then hand it over to Maven to finish the Setup.
 *
 * Created by schaefa on 7/20/15.
 */
public class SlingModuleBuilder
//AS NOTE: We extend the Maven Module Builder not because we need some of the code but rather because
//AS NOTE: we then can use 'MavenModuleWizardStep' class instead of copying over here
    extends MavenModuleBuilder
    implements SourcePathsBuilder
{
    public static final String ARCHETYPES_CONFIGURATION_PROPERTIES = "archetypes.configuration.properties";
    public static final String GROUP_ID = "groupId";
    public static final String ARTIFACT_ID = "artifactId";
    public static final String VERSION = "version";
    public static final String REPOSITORY = "repository";
    public static final String DESCRIPTION = "description";
    public static final String REQUIRED_PROPERTY = "required-property";
    public static final String ARCHETYPE = "archetype";

    private MavenProject myAggregatorProject;
    private MavenProject myParentProject;

    private boolean myInheritGroupId;
    private boolean myInheritVersion;

    private MavenId myProjectId;
    private List<ArchetypeTemplate> archetypeTemplateList = new ArrayList<ArchetypeTemplate>();
    private ArchetypeTemplate archetypeTemplate;

    private MavenEnvironmentForm myEnvironmentForm;

    private Map<String, String> myPropertiesToCreateByArtifact;

    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        final Project project = rootModel.getProject();

        final VirtualFile root = createAndGetContentEntry();
        rootModel.addContentEntry(root);

        // todo this should be moved to generic ModuleBuilder
        if (myJdk != null){
            rootModel.setSdk(myJdk);
        } else {
            rootModel.inheritSdk();
        }

        MavenUtil.runWhenInitialized(
            project, new DumbAwareRunnable() {
            public void run() {
                if (myEnvironmentForm != null) {
                    myEnvironmentForm.setData(MavenProjectsManager.getInstance(project).getGeneralSettings());
                }

                new MavenModuleBuilderHelper(myProjectId, myAggregatorProject, myParentProject, myInheritGroupId,
                    myInheritVersion, archetypeTemplate.getMavenArchetype(), myPropertiesToCreateByArtifact, "Create new Sling Maven module").configure(project, root, false);
                }
            }
        );
    }

    @Override
    public String getBuilderId() {
        return getClass().getName();
    }

    @Override
    public String getPresentableName() {
        return "Sling";
    }

    @Override
    public String getParentGroup() {
        return JavaModuleType.BUILD_TOOLS_GROUP;
    }

    @Override
    public int getWeight() {
        return JavaModuleBuilder.BUILD_SYSTEM_WEIGHT;
    }

    @Override
    public String getDescription() {
        return "Sling Maven modules are used for developing <b>JVM-based</b> applications with dependencies managed by <b>Maven</b>. " +
            "You can create either a blank Maven module or a module based on a <b>Maven archetype</b>.";
    }

    @Override
    public Icon getBigIcon() {
        return AllIcons.Modules.Types.JavaModule;
    }

    @Override
    public Icon getNodeIcon() {
        return IconLoader.getIcon("/images/sling.gif");
    }

    public ModuleType getModuleType() {
        return StdModuleTypes.JAVA;
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdk) {
        return sdk == JavaSdk.getInstance();
    }

    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
        // It is not possible with IntelliJ to use the same Form with another class AFAIK
        // The original Select Properties Step is replaced by our own as we need to handle additional Properties from
        // archetype (required properties) and provide a Name field for convenience.
        return new ModuleWizardStep[]{
            new MavenModuleWizardStep(this, wizardContext, !wizardContext.isNewWizard()),
            new ArchetypePropertiesStep(wizardContext.getProject(), this)
        };
    }

    private VirtualFile createAndGetContentEntry() {
        String path = FileUtil.toSystemIndependentName(getContentEntryPath());
        new File(path).mkdirs();
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
    }

    public List<Pair<String, String>> getSourcePaths() {
        return Collections.emptyList();
    }

    public void setSourcePaths(List<Pair<String, String>> sourcePaths) {
    }

    public void addSourcePath(Pair<String, String> sourcePathInfo) {
    }

    public void setAggregatorProject(MavenProject project) {
        myAggregatorProject = project;
    }

    public MavenProject getAggregatorProject() {
        return myAggregatorProject;
    }

    public void setParentProject(MavenProject project) {
        myParentProject = project;
    }

    public MavenProject getParentProject() {
        return myParentProject;
    }

    public void setInheritedOptions(boolean groupId, boolean version) {
        myInheritGroupId = groupId;
        myInheritVersion = version;
    }

    public boolean isInheritGroupId() {
        return myInheritGroupId;
    }

    public boolean isInheritVersion() {
        return myInheritVersion;
    }

    public void setProjectId(MavenId id) {
        myProjectId = id;
    }

    public MavenId getProjectId() {
        return myProjectId;
    }

    public void setArchetypeTemplate(ArchetypeTemplate archetypeTemplate) {
        this.archetypeTemplate = archetypeTemplate;
    }

    public ArchetypeTemplate getArchetypeTemplate() {
        return archetypeTemplate;
    }

    public void selectArchetype(MavenArchetype mavenArchetype) {
        if(mavenArchetype == null) {
            archetypeTemplate = null;
        } else {
            for (ArchetypeTemplate template : archetypeTemplateList) {
                if (template.getMavenArchetype().equals(mavenArchetype)) {
                    archetypeTemplate = template;
                    // Found and Done
                    break;
                }
            }
        }
    }

    public MavenEnvironmentForm getEnvironmentForm() {
        return myEnvironmentForm;
    }

    public void setEnvironmentForm(MavenEnvironmentForm environmentForm) {
        myEnvironmentForm = environmentForm;
    }

    public Map<String, String> getPropertiesToCreateByArtifact() {
        return myPropertiesToCreateByArtifact;
    }

    public void setPropertiesToCreateByArtifact(Map<String, String> propertiesToCreateByArtifact) {
        myPropertiesToCreateByArtifact = propertiesToCreateByArtifact;
    }

    @Override
    public String getGroupName() {
        return "Sling";
    }

    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        // Prepare the Sling / AEM Archetypes for IntelliJ
        if(archetypeTemplateList.isEmpty()) {
            archetypeTemplateList = obtainArchetypes();
        }
        List<ArchetypeTemplate> list = context.getProject() == null ? archetypeTemplateList : new ArrayList<ArchetypeTemplate>();
        // Instead of displaying a List of All Maven Archetypes we just show the ones applicable.
        SlingArchetypesStep step = new SlingArchetypesStep(this, list);
        Disposer.register(parentDisposable, step);
        return step;
    }

    //AS TODO: Change this to a dynamic discovery
    private List<ArchetypeTemplate> obtainArchetypes() {
        List<ArchetypeTemplate> ret = new ArrayList<ArchetypeTemplate>();
        MavenIndicesManager mavenIndicesManager = MavenIndicesManager.getInstance();
        Set<MavenArchetype> loadedArchetypes = mavenIndicesManager.getArchetypes();
        try {
            InputStream inputStream = this.getClass().getResourceAsStream(ARCHETYPES_CONFIGURATION_PROPERTIES);
            Properties archetypeProperties = new Properties();
            archetypeProperties.load(inputStream);
            // Filter out any entries that does not start with the archetype token
            archetypeProperties = PropertiesHandler.filterProperties(archetypeProperties, ARCHETYPE);
            // Now Collect the Properties by Indexes
            Map<Integer, Properties> collectedArchetypeProperties = PropertiesHandler.collectProperties(archetypeProperties);
            for(Integer index: collectedArchetypeProperties.keySet()) {
                // Make sure there is an entry for each index
                while(index >= ret.size()) {
                    ret.add(new ArchetypeTemplate());
                }
                ArchetypeTemplate archetype = ret.get(index);
                Properties props = collectedArchetypeProperties.get(index);
                for(String name: props.stringPropertyNames()) {
                    if(GROUP_ID.equals(name)) {
                        archetype.setGroupId(props.getProperty(name));
                    } else if(ARTIFACT_ID.equals(name)) {
                        archetype.setArtifactId(props.getProperty(name));
                    } else if(VERSION.equals(name)) {
                        archetype.setVersion(props.getProperty(name));
                    } else if(REPOSITORY.equals(name)) {
                        archetype.setRepository(props.getProperty(name));
                    } else if(DESCRIPTION.equals(name)) {
                        archetype.setDescription(props.getProperty(name));
                    } else {
                        // Filter for Required Properties
                        Properties requiredProperties = PropertiesHandler.filterProperties(props, REQUIRED_PROPERTY);
                        for(String requiredPropertyName: requiredProperties.stringPropertyNames()) {
                            archetype.addRequiredProperty(requiredPropertyName, requiredProperties.getProperty(requiredPropertyName));
                        }
                    }
                }
            }
            // At the end remove any invalid entries
            Iterator<ArchetypeTemplate> i = ret.iterator();
            while(i.hasNext()) {
                if(!i.next().isValid()) {
                    //AS TODO: Add a Debug Statement to indicate that an entry was removed
                    i.remove();
                }
            }
            // Finally Add any archetypes to Maven in order to make the available
            i = ret.iterator();
            while(i.hasNext()) {
                MavenArchetype mavenArchetype = i.next().getMavenArchetype();
                if (!loadedArchetypes.contains(mavenArchetype)) {
                    mavenIndicesManager.addArchetype(mavenArchetype);
                }
            }
        } catch (FileNotFoundException e) {
            //AS TODO: Report that problem
        } catch (IOException e) {
            //AS TODO: Report that problem
        }
        return ret;
    }

    public static class ArchetypeTemplate {
        private String groupId;
        private String artifactId;
        private String version;
        private String description;
        private String repository;
        private Map<String,String> requiredProperties = new HashMap<String, String>();

        public MavenArchetype getMavenArchetype() {
            return new MavenArchetype(
                groupId,
                artifactId,
                version,
                repository,
                description
            );
        }

        public boolean isValid() {
            return StringUtil.isNotEmpty(groupId) && StringUtil.isNotEmpty(artifactId) && StringUtil.isNotEmpty(version);
        }

        public String getGroupId() {
            return groupId;
        }

        public ArchetypeTemplate setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public ArchetypeTemplate setArtifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public ArchetypeTemplate setVersion(String version) {
            this.version = version;
            return this;
        }

        public String getRepository() {
            return repository;
        }

        public ArchetypeTemplate setRepository(String repository) {
            this.repository = repository;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public ArchetypeTemplate setDescription(String description) {
            this.description = description;
            return this;
        }

        public Map<String, String> getRequiredProperties() {
            return requiredProperties;
        }

        public void addRequiredProperty(String name, String defaultValue) {
            requiredProperties.put(name, defaultValue);
        }
    }
}
