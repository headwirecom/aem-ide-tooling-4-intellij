package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by schaefa on 5/16/15.
 */
public class ProjectHelper {
    public static boolean isBundleProject(IProject project) {
        throw new UnsupportedOperationException("Not Implemented Yet");
    }

    public static boolean isContentProject(IProject project) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static IJavaProject asJavaProject(IProject project) {
        return new IJavaProject(project);
    }
}
