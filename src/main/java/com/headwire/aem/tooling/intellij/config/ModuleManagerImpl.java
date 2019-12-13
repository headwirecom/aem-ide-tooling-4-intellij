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
import com.headwire.aem.tooling.intellij.util.AbstractProjectComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schaefa on 3/18/16.
 */
public class ModuleManagerImpl
    extends AbstractProjectComponent
    implements ModuleManager, Disposable
{
    private static final Logger LOGGER = Logger.getInstance(ModuleManagerImpl.class);

    private ServerConfigurationManager serverConfigurationManager;

    protected ModuleManagerImpl(Project project) {
        super(project);
    }

    @Override
    public void dispose() {
        serverConfigurationManager = null;
    }

    @Override
    public ServerConfiguration.Module getSCM(@NotNull UnifiedModule unifiedModule, @NotNull ServerConfiguration serverConfiguration) {
        return new InstanceImpl(myProject, serverConfiguration).findModule(unifiedModule);
    }

    public MavenProject getMavenProject(@NotNull UnifiedModule unifiedModule) {
        if(unifiedModule.isMavenBased()) {
            return ((UnifiedModuleImpl) unifiedModule).mavenProject;
        }
        return null;
    }

    @Override
    public UnifiedModule getUnifiedModule(@NotNull ServerConfiguration.Module scm) {
        return scm.getUnifiedModule();
    }

    @Override
    public List<UnifiedModule> getUnifiedModules(@NotNull ServerConfiguration serverConfiguration) {
        return getUnifiedModules(serverConfiguration, false);
    }

    @Override
    public List<UnifiedModule> getUnifiedModules(@NotNull ServerConfiguration serverConfiguration, boolean force) {
        return new InstanceImpl(myProject, serverConfiguration).getUnifiedModules(force);
    }

    public static class InstanceImpl {
        ServerConfiguration serverConfiguration;
        Project project;

        public InstanceImpl(@NotNull Project project, @NotNull ServerConfiguration serverConfiguration) {
            this.project = project;
            this.serverConfiguration = serverConfiguration;
        }

        @NotNull
        public ServerConfiguration getServerConfiguration() {
            return serverConfiguration;
        }

        @NotNull
        public Project getProject() {
            return project;
        }

        public ServerConfiguration.Module findModule(@NotNull UnifiedModule unifiedModule) {
            return serverConfiguration.obtainModuleByName(unifiedModule.getName());
        }

        @NotNull
        public List<UnifiedModule> getUnifiedModules(boolean force) {
            List<UnifiedModule> ret = new ArrayList<UnifiedModule>();
            if(!force && serverConfiguration.isBound()) {
                LOGGER.debug("Get Unified Modules, not forced and is bound");
                for(ServerConfiguration.Module module: serverConfiguration.getModuleList()) {
                    LOGGER.debug("Get Unified Modules, add unified module: ", module.getUnifiedModule().getName());
                    ret.add(module.getUnifiedModule());
                }
            } else {
                Module[] modules = com.intellij.openapi.module.ModuleManager.getInstance(project).getModules();
                MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
                List<MavenProject> mavenProjects = mavenProjectsManager.getNonIgnoredProjects();
                for(Module module : modules) {
                    LOGGER.debug("Get Unified Modules, handle module: ", module.getName());
                    if(!force) {
                        // First look for a Server Configuration Module with that Module in the Module Context and see if it is bound
                        // (If found then this is most likely)
                        ServerConfiguration.Module scm = serverConfiguration.obtainModuleByName(module.getName());
                        LOGGER.debug("Get Unified Modules, add unforced module (scm, name, is bound): ",
                            scm, scm != null ? scm.getName() : "null",
                            scm != null ? scm.isBound() : "null"
                        );
                        if(scm != null && scm.isBound()) {
                            ret.add(scm.getUnifiedModule());
                            // We found our Module so go to the next
                            continue;
                        }
                    }
                    SlingModuleFacet slingModuleFacet = SlingModuleFacet.getFacetByModule(module);
                    // Find a corresponding Maven Project
                    UnifiedModule unifiedModule = null;
                    for(MavenProject mavenProject : mavenProjects) {
                        LOGGER.debug("Get Unified Modules, handled Maven project: ",
                            mavenProject != null ?
                                mavenProject.getName() : "null"
                        );
                        if(!"pom".equalsIgnoreCase(mavenProject.getPackaging())) {
                            // Use the Parent of the Module and Maven Pom File as they should be in the same folder
                            //AS TODO: We might want to check if one is the child folder of the other instead being in the same
                            VirtualFile moduleFile = module.getModuleFile();
                            if(moduleFile == null) {
                                // Temporary issue where the IML file is not found by the Virtual File System
                                // Fix: get the path, remove the IML file and find the Virtual File with that folder
                                String filePath = module.getModuleFilePath();
                                LOGGER.debug("Get Unified Modules, Maven project file path: ", filePath);
                                if(filePath != null) {
                                    int index = filePath.lastIndexOf("/");
                                    if(index > 0 && index < filePath.length() - 2) {
                                        filePath = filePath.substring(0, index);
                                        moduleFile = project.getBaseDir().getFileSystem().findFileByPath(filePath);
                                        LOGGER.debug("Get Unified Modules, Maven project module file: ", moduleFile);
                                    }
                                }
                            }
                            if(moduleFile != null) {
                                VirtualFile moduleFolder = moduleFile.getParent();
                                VirtualFile mavenFolder = mavenProject.getFile().getParent();
                                if(moduleFolder.equals(mavenFolder)) {
                                    unifiedModule = new UnifiedModuleImpl(mavenProject, module);
                                    LOGGER.debug("Get Unified Modules, Maven project module unified module: ", unifiedModule.getName());
                                    break;
                                }
                            }
                        }
                    }
                    if(unifiedModule != null) {
                        if(unifiedModule.isOSGiBundle()) {
                            if(slingModuleFacet != null) {
                                LOGGER.debug("Get Unified Modules, check OSGi");
                                SlingModuleFacetConfiguration slingModuleFacetConfiguration = slingModuleFacet.getConfiguration();
                                LOGGER.debug("Get Unified Modules, sling module facet (fact, facet conf): ", slingModuleFacet, slingModuleFacetConfiguration);
                                if(slingModuleFacetConfiguration.getModuleType() != ModuleType.bundle) {
                                    //AS TODO: Show an alert that Maven Module and Sling Facet Module type od not match
                                    LOGGER.debug("Get Unified Modules, not a maven bundle -> ignore");
                                    continue;
                                } else {
                                    if(slingModuleFacetConfiguration.getOsgiSymbolicName().length() == 0) {
                                        //AS TODO: Show an alert that no Symbolic Name is provided
                                        LOGGER.debug("Get Unified Modules, no Symbolic Name -> ignore");
                                        continue;
                                    }
                                    if(slingModuleFacetConfiguration.isIgnoreMaven()) {
                                        if(slingModuleFacetConfiguration.getOsgiVersion().length() == 0) {
                                            //AS TODO: Show an alert that no Version is provided
                                            LOGGER.debug("Get Unified Modules, no OSGi Version -> ignore");
                                            continue;
                                        }
                                        if(slingModuleFacetConfiguration.getOsgiJarFileName().length() == 0) {
                                            //AS TODO: Show an alert that no Jar File Name is provided
                                            LOGGER.debug("Get Unified Modules, no OSGi Jar File Name -> ignore");
                                            continue;
                                        }
                                    }
                                }
                            }
                        } else if(unifiedModule.isContent()) {
                            if(slingModuleFacet != null) {
                                LOGGER.debug("Get Unified Modules, check Content");
                                if(slingModuleFacet.getConfiguration().getModuleType() != ModuleType.content) {
                                    //AS TODO: Warn about the miss configuration but proceed
                                    LOGGER.debug("Get Unified Modules, not a Maven content -> ignore");
                                    continue;
                                } else {
                                    unifiedModule = new UnifiedModuleImpl(module);
                                }
                            }
                        }
                    } else {
                        if(slingModuleFacet != null) {
                            LOGGER.debug("Get Unified Modules, check others");
                            if(slingModuleFacet.getConfiguration().getModuleType() != ModuleType.excluded) {
                                unifiedModule = new UnifiedModuleImpl(module);
                            }
                        }
                    }
                    if(unifiedModule != null) {
                        LOGGER.debug("Get Unified Modules, finally add unified module to response: ", unifiedModule.getName());
                        ret.add(unifiedModule);
                        // Bind the Server Configuration Module with the Module Context
                        ServerConfiguration.Module serverConfigurationModule = serverConfiguration.obtainModuleByName(unifiedModule.getName());
                        if(serverConfigurationModule == null) {
                            LOGGER.debug("Get Unified Modules, add module to server configuration: ", unifiedModule.getName());
                            serverConfiguration.addModule(project, unifiedModule);
                        } else {
                            LOGGER.debug("Get Unified Modules, rebind module with server configuration: ", unifiedModule.getName());
                            serverConfigurationModule.rebind(project, unifiedModule);
                        }
                    }
                }
            }
            return ret;
        }
    }
}
