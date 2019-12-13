package com.headwire.aem.tooling.intellij.util;

import com.intellij.openapi.project.Project;

public class AbstractProjectComponent {
    protected final Project myProject;

    protected AbstractProjectComponent(Project project) {
        this.myProject = project;
    }
}
