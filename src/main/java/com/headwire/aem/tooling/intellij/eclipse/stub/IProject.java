package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Created by schaefa on 5/13/15.
 */
@Deprecated
public class IProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Project project;
    private Module module;

    public IProject(@NotNull Module module) {
        this.project = module.getProject();
        this.module = module;
    }

    public IFolder getFolder(IPath path) {
        com.headwire.aem.tooling.intellij.communication.MessageManager messageManager = project.getComponent(
            com.headwire.aem.tooling.intellij.communication.MessageManager.class
        );
        messageManager.sendDebugNotification("Given Path: '" + path + "'");

        String filePath = path.toOSString();

        messageManager.sendDebugNotification("Retrieved OS String: '" + filePath + "'");

        VirtualFileSystem vfs = module.getProject().getBaseDir().getFileSystem();
        VirtualFile file = vfs.findFileByPath(path.toOSString());
        return new IFolder(module, file);
    }

    /* IntelliJ Specific Methods */

    public List<String> getSourceFolderList() {
        List<String> ret = new ArrayList<String>();
        for(String path: module.getModuleContext().getContentDirectoryPaths()) {
            ret.add(path);
        }
        return ret;
    }

    public IResource findMember(IPath path) {
        String filePath = path.toOSString();
        VirtualFile file;
        //AS TODO: What is the proper handling here (for Windows that is)?
        if(filePath.startsWith("/") || filePath.contains(":\\")) {
            file = module.getProject().getBaseDir().getFileSystem().findFileByPath(filePath);
        } else {
            file = module.getProject().getBaseDir().findFileByRelativePath(filePath);
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
