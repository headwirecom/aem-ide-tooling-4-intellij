package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenResource;

import java.util.ArrayList;
import java.util.List;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Created by schaefa on 5/13/15.
 */
public class IProject {
    private Module module;

    public IProject(@NotNull Module module) {
        this.module = module;
    }

    public IFolder getFolder(IPath path) {
        VirtualFile file = module.getProject().getFile().getFileSystem().findFileByPath(path.toOSString());
        return new IFolder(module, file);
    }

    /* IntelliJ Specific Methods */

    public List<String> getSourceFolderList() {
        List<String> ret = new ArrayList<String>();
        for(MavenResource mavenResource: module.getProject().getResources()) {
            ret.add(mavenResource.getDirectory());
        }
        return ret;
    }

    public IResource findMember(IPath path) {
        String filePath = path.toOSString();
        VirtualFile file;
        if(filePath.startsWith("/")) {
            file = module.getProject().getFile().getFileSystem().findFileByPath(filePath);
        } else {
            file = module.getProject().getFile().findFileByRelativePath(filePath);
        }
        return file.isDirectory() ? new IFolder(module, file) : new IFile(module, file);
    }
}
