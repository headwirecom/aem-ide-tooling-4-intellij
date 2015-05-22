package com.headwire.aem.tooling.intellij.eclipse.stub;

import java.io.File;

/**
 * Created by schaefa on 5/18/15.
 */
public class IJavaProject {

    IProject project;

    public IJavaProject(IProject project) {
        this.project = project;
    }

    public IPath getOutputLocation() {
        return new IPath(new File(project.getModule().getMavenProject().getBuildDirectory()));
    }
}
