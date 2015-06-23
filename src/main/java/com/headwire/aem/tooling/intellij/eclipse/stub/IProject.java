package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Created by schaefa on 5/13/15.
 */
public class IProject {

    private Project project;
    private Module module;

    public IProject(@NotNull Module module) {
        this.project = module.getProject();
        this.module = module;
    }

    public IFolder getFolder(IPath path) {
        VirtualFile file = module.getMavenProject().getFile().getFileSystem().findFileByPath(path.toOSString());
        return new IFolder(module, file);
    }

    /* IntelliJ Specific Methods */

    public List<String> getSourceFolderList() {
        List<String> ret = new ArrayList<String>();
        for(MavenResource mavenResource: module.getMavenProject().getResources()) {
            ret.add(mavenResource.getDirectory());
        }
        return ret;
    }

    public IResource findMember(IPath path) {
        String filePath = path.toOSString();
        VirtualFile file;
        if(filePath.startsWith("/")) {
            file = module.getMavenProject().getFile().getFileSystem().findFileByPath(filePath);
        } else {
            file = module.getMavenProject().getFile().findFileByRelativePath(filePath);
        }
        return file.isDirectory() ? new IFolder(module, file) : new IFile(module, file);
    }

    public Workspace getWorkspace() {
        return new Workspace();
    }

    public class Workspace {

        public Root getRoot() {
            return new Root();
        }
    }

    public class Root {

        public IResource findMember(IPath childPath) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    public Module getModule() {
        return module;
    }

    public IFile getFile(String path) {
        IFile ret = null;
        if(path.startsWith("/")) {
            VirtualFile virtualFile = project.getProjectFile().getFileSystem().findFileByPath(path);
            if(virtualFile != null) {
                ret = new IFile(module, virtualFile);
            } else {
                ret = new IFile(module, new File(path));
            }
        } else {
            VirtualFile virtualFile = project.getProjectFile().findFileByRelativePath(path);
            if(virtualFile != null) {
                ret = new IFile(module, virtualFile);
            } else {
                ret = new IFile(module, new File(project.getProjectFile().getPath(), path));
            }
        }
        return ret;
    }
}
