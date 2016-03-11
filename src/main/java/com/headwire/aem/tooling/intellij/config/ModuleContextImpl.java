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

package com.headwire.aem.tooling.intellij.config;

import com.headwire.aem.tooling.intellij.facet.SlingModuleExtensionProperties.ModuleType;
import com.headwire.aem.tooling.intellij.facet.SlingModuleFacet;
import com.headwire.aem.tooling.intellij.facet.SlingModuleFacetConfiguration;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenResource;
import org.jetbrains.idea.maven.project.MavenProject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas Schaefer (Headwire.com) on 7/2/15.
 */
public class ModuleContextImpl
    implements ModuleContext
{
    public static final String NO_OSGI_BUNDLE = "No OSGi Bundle";

    private MavenProject mavenProject;

    private Module module;
    private SlingModuleFacetConfiguration slingConfiguration;
    private Object osgiConfiguration;


    public ModuleContextImpl(@NotNull MavenProject mavenProject) {
        init(mavenProject);
    }

    public ModuleContextImpl(@NotNull Module module) {
        init(module);
    }

    public void init(@NotNull Object payload) {
        if(payload instanceof  MavenProject) {
            this.mavenProject = (MavenProject) payload;
            this.module = null;
            this.slingConfiguration = null;
            this.osgiConfiguration = null;
        } else if(payload instanceof Module) {
            this.module = (Module) payload;
            this.mavenProject = null;
            SlingModuleFacet slingModuleFacet = SlingModuleFacet.getFacetByModule(module);
            if(slingModuleFacet == null) {
                throw new IllegalArgumentException("Module: '" + module.getName() + "' is not a Sling Module");
            }
            slingConfiguration = slingModuleFacet.getConfiguration();
            // Ignore OSGi Configurations if not marked as such
            if(slingConfiguration.getModuleType() == ModuleType.bundle) {
                FacetManager facetManager = module.getComponent(FacetManager.class);
                if(facetManager != null) {
                    Facet[] facets = facetManager.getAllFacets();
                    Facet osmorcFacet = null;
                    for(Facet facet: facets) {
                        if(facet.getClass().getName().endsWith("OsmorcFacet")) {
                            osmorcFacet = facet;
                            break;
                        }
                    }
                    if(osmorcFacet == null) {
                        throw new IllegalArgumentException("Module: '" + module.getName() + "' is not an OSGi Module");
                    } else {
                        osgiConfiguration = osmorcFacet.getConfiguration();
                    }
                }
            }
        }
    }

    public boolean isMavenBased() {
        return mavenProject != null;
    }

    @Override
    public String getSymbolicName() {
        if(mavenProject != null) {
            return mavenProject.getMavenId().getGroupId() + "." + mavenProject.getMavenId().getArtifactId();
        } else {
            return osgiConfiguration != null ?
                getValue(osgiConfiguration, "getBundleSymbolicName") :
                // In order to find the module we need to provide a symbolic name
                module.getName();
            //            NO_OSGI_BUNDLE;
        }
    }

    private String getValue(Object instance, String methodName) {
        String ret = "";
        try {
            Method method = instance.getClass().getMethod(methodName);
            ret =  "" + method.invoke(instance);
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        } catch(InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public String getVersion() {
        if(mavenProject != null) {
            return mavenProject.getMavenId().getVersion();
        } else {
            return osgiConfiguration != null ?
                getValue(osgiConfiguration, "getBundleVersion") :
                NO_OSGI_BUNDLE;
        }
    }

    @Override
    public String getName() {
        if(mavenProject != null) {
            return mavenProject.getName();
        } else {
            return module.getName();
        }
    }

    @Override
    public String getBuildFileName() {
        if(mavenProject != null) {
            //AS TODO: Make sure this is true
            return mavenProject.getFinalName() + ".jar";
        } else {
            return osgiConfiguration != null ?
                getValue(osgiConfiguration, "getJarFileName") :
                NO_OSGI_BUNDLE;
        }
    }

    @Override
    public boolean isOSGiBundle() {
        if(mavenProject != null) {
            return mavenProject.getPackaging().equalsIgnoreCase("bundle");
        } else {
            return osgiConfiguration != null;
        }
    }

    @Override
    public boolean isContent() {
        if(mavenProject != null) {
            return mavenProject.getPackaging().equalsIgnoreCase("content-package");
        } else {
            return osgiConfiguration == null;
        }
    }

    @Override
    public String getBuildDirectoryPath() {
        if(mavenProject != null) {
            return mavenProject.getBuildDirectory();
        } else {
            CompilerProjectExtension compilerProjectExtension = CompilerProjectExtension.getInstance(module.getProject());
            if(compilerProjectExtension == null) {
                throw new IllegalArgumentException("Compiler Project Extension not found for Module: '" + module.getName() + "'");
            }
            VirtualFile outputPath = compilerProjectExtension.getCompilerOutput();
            return outputPath.getPath();
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
}
