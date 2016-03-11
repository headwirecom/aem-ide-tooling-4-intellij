package com.headwire.aem.tooling.intellij.config;

import com.headwire.aem.tooling.intellij.facet.SlingModuleExtensionProperties.ModuleType;
import com.headwire.aem.tooling.intellij.facet.SlingModuleFacet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.osmorc.facet.OsmorcFacet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schaefa on 7/2/15.
 */
public class ModuleProjectFactory {

    private static ModuleContext create(MavenProject mavenProject) {
        return new ModuleContextImpl(mavenProject);
    }

    private static ModuleContext create(Module module) {
        return new ModuleContextImpl(module);
    }

    public static List<ModuleContext> getProjectModules(@NotNull Project project, @NotNull ServerConfiguration serverConfiguration) {
        List<ModuleContext> ret = new ArrayList<ModuleContext>();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
        List<MavenProject> mavenProjects = mavenProjectsManager.getNonIgnoredProjects();
        for(Module module: modules) {
            SlingModuleFacet slingModuleFacet = SlingModuleFacet.getFacetByModule(module);
            // Find a corresponding Maven Project
            ModuleContext moduleContext = null;
            for(MavenProject mavenProject : mavenProjects) {
                if(!"pom".equalsIgnoreCase(mavenProject.getPackaging())) {
                    // This is only going to fly if the module and artifact id match but that does not have to be the case
//                    MavenId id = mavenProject.getMavenId();
//                    if(id.getArtifactId().equals(module.getName())) {
//                        moduleContext = create(mavenProject);
//                        break;
//                    }
                    // Use the Parent of the Module and Maven Pom File as they should be in the same folder
                    //AS TODO: We might want to check if one is the child folder of the other instead being in the same
                    VirtualFile moduleFolder = module.getModuleFile().getParent();
                    VirtualFile mavenFolder = mavenProject.getFile().getParent();
                    if(moduleFolder.equals(mavenFolder)) {
                        moduleContext = create(mavenProject);
                        break;
                    }
                }
            }
            if(moduleContext != null) {
                if(moduleContext.isOSGiBundle()) {
                    if(!serverConfiguration.isBuildWithMaven()) {
                        if(slingModuleFacet == null) {
                            //AS TODO: Show Alert that there is no Sling Module Facet configuration even though Maven is configured but disabled to build
                            continue;
                        } else {
                            if(slingModuleFacet.getConfiguration().getModuleType() != ModuleType.bundle) {
                                //AS TODO: Warn about the miss configuration but proceed
                            }
                            // Maven Module found but it is excluded from the build so see if there is an OSGi facet setup
                            OsmorcFacet osgiFacet = OsmorcFacet.getInstance(module);
                            if(osgiFacet != null) {
                                moduleContext = create(module);
                            } else {
                                //AS TODO: Show an alert that there is no OSGi Configuration and make the build fail
                            }
                        }
                    }
                } else if(moduleContext.isContent()) {
                    if(slingModuleFacet != null) {
                        if(slingModuleFacet.getConfiguration().getModuleType() != ModuleType.content) {
                            //AS TODO: Warn about the miss configuration but proceed
                            continue;
                        } else {
                            moduleContext = new ModuleContextImpl(module);
                        }
                    }
                }
            } else {
                if(slingModuleFacet != null) {
                    if(slingModuleFacet.getConfiguration().getModuleType() != ModuleType.excluded) {
                        moduleContext = create(module);
                    }
                }
            }
            if(moduleContext != null) {
                ret.add(moduleContext);
            }
        }
        return ret;
    }
}
