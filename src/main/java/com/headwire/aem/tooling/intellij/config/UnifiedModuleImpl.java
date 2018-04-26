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

package com.headwire.aem.tooling.intellij.config;

import com.headwire.aem.tooling.intellij.facet.SlingModuleExtensionProperties.ModuleType;
import com.headwire.aem.tooling.intellij.facet.SlingModuleFacet;
import com.headwire.aem.tooling.intellij.facet.SlingModuleFacetConfiguration;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.model.MavenResource;
import org.jetbrains.idea.maven.project.MavenProject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Module inside an IntelliJ project. It
 * is either based on a Maven Project (module) or an IntelliJ Module
 * with a Sling Facet.
 *
 * Created by Andreas Schaefer (Headwire.com) on 7/2/15.
 */
public class UnifiedModuleImpl
    implements UnifiedModule
{
    public static final String NO_OSGI_BUNDLE = "No OSGi Bundle";
    public static final String BUNDLE_SYMBOLIC_NAME_FIELD = "Bundle-SymbolicName";
    public static final String FELIX_GROUP_ID = "org.apache.felix";
    public static final String BUNDLE_PLUGIN_ARTIFACT_ID = "maven-bundle-plugin";
    public static final String INSTRUCTIONS_SECTION = "instructions";
    public static final String MANIFEST_MF_FILE_NAME = "MANIFEST.MF";

    MavenProject mavenProject;

    private Module module;
    private SlingModuleFacetConfiguration slingConfiguration;

    /** @deprecated This should be managed by the Module Manager **/
    private List<ServerConfiguration.Module> serverConfigurationModulList = new ArrayList<ServerConfiguration.Module>();

    public UnifiedModuleImpl(@NotNull MavenProject mavenProject, @NotNull Module module) {
        init(mavenProject, module);
    }

    public UnifiedModuleImpl(@NotNull Module module) {
        init(null, module);
    }

    public void init(MavenProject mavenProject, @NotNull Module module) {
        this.mavenProject = mavenProject;
        this.module = module;
        this.slingConfiguration = null;
        SlingModuleFacet slingModuleFacet = SlingModuleFacet.getFacetByModule(module);
        if(slingModuleFacet != null) {
            slingConfiguration = slingModuleFacet.getConfiguration();
        }
    }

    @Override
    public boolean containsServerConfigurationModule(@NotNull ServerConfiguration.Module module) {
        return serverConfigurationModulList.contains(module);
    }

    @Override
    public boolean addServerConfigurationModule(@NotNull ServerConfiguration.Module module) {
        boolean ret = containsServerConfigurationModule(module);
        if(!ret) {
            serverConfigurationModulList.add(module);
        }
        return !ret;
    }

    @Override
    public boolean removeServerConfigurationModule(@NotNull ServerConfiguration.Module module) {
        boolean ret = containsServerConfigurationModule(module);
        if(ret) {
            serverConfigurationModulList.remove(module);
        }
        return ret;
    }

    @Override
    public List<ServerConfiguration.Module> getSererConfigurationModuleList() {
        return serverConfigurationModulList;
    }

    @Override
    public ServerConfiguration.Module getServerConfigurationModule(@NotNull ServerConfiguration serverConfiguration) {
        ServerConfiguration.Module ret = null;
        for(ServerConfiguration.Module module: serverConfigurationModulList) {
            if(serverConfiguration == module.getParent()) {
                ret = module;
                break;
            }
        }
        return ret;
    }

    public boolean isMavenBased() {
        return mavenProject != null;
    }

    @Override
    public String getSymbolicName() {
        // This is a verify central function as the Symbolic Name is the key to find the
        // bundle on the target server.
        //
        // - If this is a Maven Plugin then we check the maven-bundle-plugin and
        //   check if hte Symbolic Name is set there and use this one
        // - Otherwise Check if there is a Symbolic Name specified in the Sling
        //   Facet
        // - Otherwise check if a Manifest.mf can be found and look for the 'Bundle-SymbolicName'
        // - Otherwise take the groupId.artifactId

        String answer = null;
        if(mavenProject != null) {
            // Check if there is an Maven Bundle Plugin with an Symbolic Name override
            List<MavenPlugin> mavenPlugins = mavenProject.getPlugins();
            for(MavenPlugin mavenPlugin : mavenPlugins) {
                if(FELIX_GROUP_ID.equals(mavenPlugin.getGroupId()) && BUNDLE_PLUGIN_ARTIFACT_ID.equals(mavenPlugin.getArtifactId())) {
                    Element configuration = mavenPlugin.getConfigurationElement();
                    if(configuration != null) {
                        Element instructions = configuration.getChild(INSTRUCTIONS_SECTION);
                        if(instructions != null) {
                            Element bundleSymbolicName = instructions.getChild(BUNDLE_SYMBOLIC_NAME_FIELD);
                            if(bundleSymbolicName != null) {
                                answer = bundleSymbolicName.getValue();
                                answer = answer != null && answer.trim().isEmpty() ? null : answer.trim();
                            }
                        }
                    }
                }
            }
        }
        if(answer == null && slingConfiguration != null) {
            answer = slingConfiguration.getOsgiSymbolicName();
        }
        if(answer == null) {
            VirtualFile baseDir = module.getProject().getBaseDir();
            VirtualFile buildDir = baseDir.getFileSystem().findFileByPath(getBuildDirectoryPath());
            if(buildDir != null) {
                // Find a Metainf.mf file
                VirtualFile manifest = Util.findFileOrFolder(buildDir, MANIFEST_MF_FILE_NAME, false);
                if(manifest != null) {
                    try {
                        String content = new String(manifest.contentsToByteArray());
                        int index = content.indexOf(BUNDLE_SYMBOLIC_NAME_FIELD);
                        if(index >= 0) {
                            int index2 = content.indexOf("\n", index);
                            if(index2 >= 0) {
                                answer = content.substring(index + BUNDLE_SYMBOLIC_NAME_FIELD.length() + 2, index2);
                                answer = answer.trim().isEmpty() ? null : answer.trim();
                            }
                        }
                    } catch(IOException e) {
                        //AS TODO: Report the issues
                        e.printStackTrace();
                    }
                }
            }
        }
        if(answer == null && mavenProject != null) {
            answer = mavenProject.getMavenId().getGroupId() + "." + mavenProject.getMavenId().getArtifactId();
        }
        return answer == null ? NO_OSGI_BUNDLE : answer;
    }

    @Override
    public String getVersion() {
        String ret = "";
        if(slingConfiguration != null) {
            ret = slingConfiguration.getOsgiVersion();
        }
        if(ret.length() == 0 && mavenProject != null) {
            ret = mavenProject.getMavenId().getVersion();
        }
        return ret;
    }

    @Override
    public String getName() {
        return module.getName();
    }

    @Override
    public String getBuildFileName() {
        if(mavenProject != null) {
            //AS TODO: Make sure this is true
            return mavenProject.getFinalName() + ".jar";
        } else {
            return slingConfiguration.getOsgiJarFileName();
        }
    }

    @Override
    public boolean isOSGiBundle() {
        if(mavenProject != null) {
            return mavenProject.getPackaging().equalsIgnoreCase("bundle");
        } else {
            return slingConfiguration.getModuleType() == ModuleType.bundle;
        }
    }

    @Override
    public boolean isContent() {
        if(mavenProject != null) {
            return mavenProject.getPackaging().equalsIgnoreCase("content-package");
        } else {
            return slingConfiguration.getModuleType() == ModuleType.content;
        }
    }

    @Override
    public String getBuildDirectoryPath() {
        if(mavenProject != null) {
            return mavenProject.getBuildDirectory();
        } else {
            // The build file is normally placed inside the parent folder of the module output directory
            VirtualFile moduleOutputDirectory = CompilerPaths.getModuleOutputDirectory(module, false);
            return moduleOutputDirectory.getParent().getPath();
        }
    }

    @Override
    public String getModuleDirectory() {
        if(mavenProject != null) {
            return mavenProject.getDirectoryFile().getPath();
        } else {
            String ret = "";
            VirtualFile moduleFile = module.getModuleFile();
            if(moduleFile != null) {
                ret = moduleFile.getParent().getPath();
            }
            return ret;
        }
    }

    @Override
    public List<String> getContentDirectoryPaths() {
        if(mavenProject != null) {
            List<MavenResource> resourceList = mavenProject.getResources();
            List<String> ret = new ArrayList<String>(resourceList.size());
            for(MavenResource resource : resourceList) {
                ret.add(resource.getDirectory());
            }
            return ret;
        } else {
            List<String> ret = new ArrayList<String>();
            if(slingConfiguration != null && slingConfiguration.getModuleType() == ModuleType.content) {
                ret.add(slingConfiguration.getSourceRootPath());
            }
            return ret;
        }
    }

    @Override
    public List<String> getSourceDirectoryPaths() {
        List<String> ret = new ArrayList<String>();
        if(mavenProject != null) {
            ret = mavenProject.getSources();
        } else {
            if(slingConfiguration != null && slingConfiguration.getModuleType() == ModuleType.bundle) {
                ret.add(slingConfiguration.getSourceRootPath());
            }
        }
        return ret;
    }

    @Override
    public String getMetaInfPath() {
        if(mavenProject != null) {
            return null;
        } else {
            if(slingConfiguration != null) {
                return slingConfiguration.getMetaInfPath();
            } else {
                return null;
            }
        }
    }

    @Override
    public Module getModule() {
        return module;
    }

    public String toString() {
        return "UnifiedModuleImpl { " +
            (
                module == null ? "No Module" :
                   "Name: '" + getName() + "'"
            ) +
            ", " +
            (
                mavenProject == null ? "No Maven Project" :
                    "Maven Project Name: '" + mavenProject.getName() + "'"
            ) +
            " }";
    }
}
