package com.headwire.aem.tooling.intellij.sync.content;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.sync.content.WorkspacePath;
import org.apache.sling.ide.sync.content.WorkspaceProject;
import org.apache.sling.ide.sync.content.WorkspaceResource;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public abstract class IntelliJWorkspaceResource
    implements WorkspaceResource
{

    private WorkspaceProject project;
    private VirtualFile resource;
    private final Set<String> ignoredFileNames;

    public IntelliJWorkspaceResource(Set<String> ignoredFileNames) {
        this.ignoredFileNames = ignoredFileNames;
    }

    public IntelliJWorkspaceResource(WorkspaceProject project, VirtualFile resource, Set<String> ignoredFileNames) {
        setProject(project);
        setResource(resource);
        this.ignoredFileNames = ignoredFileNames;
    }

    public Set<String> getIgnoredFileNames() {
        return ignoredFileNames;
    }

    public IntelliJWorkspaceResource setProject(WorkspaceProject project) {
        this.project = project;
        return this;
    }

    public IntelliJWorkspaceResource setResource(VirtualFile resource) {
        this.resource = resource;
        return this;
    }

    public VirtualFile getResource() {
        return resource;
    }

    @Override
    public boolean exists() {
        return resource.exists();
    }

    @Override
    public boolean isIgnored() {
        return ignoredFileNames.contains(getName());
    }

    @Override
    public WorkspacePath getLocalPath() {
        //AS TODO: For LocalFileSystem Virtual File the paths should be platform independent -> verify that on Windows
        return new WorkspacePath(resource.getPath());
    }

    @Override
    public Path getOSPath() {
        //AS TODO: If the resource.getPath() is independent then we probably need to convert them back -> check both assumptions
        return Paths.get(resource.getPath().replaceAll("/", File.separator));
    }

    @Override
    public WorkspaceProject getProject() {
        return project;
    }

    @Override
    public long getLastModified() {
        return resource.getModificationStamp();
    }

    @Override
    //AS TODO: implement this
    public Object getTransientProperty(String propertyName) {
        return null;
    }

    @Override
    //AS TODO: implement this
    public WorkspacePath getPathRelativeToSyncDir() {
        return null;
    }
}
