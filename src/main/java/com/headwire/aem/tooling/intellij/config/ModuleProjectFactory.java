package com.headwire.aem.tooling.intellij.config;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schaefa on 7/2/15.
 */
public class ModuleProjectFactory {
    public static ModuleProject create(MavenProject mavenProject) {
        return new ModuleProjectImpl(mavenProject);
    }

    public static List<ModuleProject> getProjectModules(@NotNull Project project) {
        List<ModuleProject> ret = new ArrayList<ModuleProject>();
        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(project);
        List<MavenProject> mavenProjects = mavenProjectsManager.getNonIgnoredProjects();
        for(MavenProject mavenProject : mavenProjects) {
            ret.add(create(mavenProject));
        }
        return ret;
    }
}
