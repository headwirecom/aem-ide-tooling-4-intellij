package com.headwire.aem.tooling.intellij.sync.content;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.sync.content.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IntelliJWorkspaceDirectory
    extends IntelliJWorkspaceResource
    implements WorkspaceDirectory
{

    public IntelliJWorkspaceDirectory(WorkspaceProject project, VirtualFile resource, Set<String> ignoredFileNames) {
        super(project, resource, ignoredFileNames);
    }

    @Override
    public WorkspaceFile getFile(WorkspacePath relativePath) {
        return new IntelliJWorkspaceFile(getProject(), getResource().findFileByRelativePath(relativePath.asPortableString()), getIgnoredFileNames());
    }

    @Override
    public WorkspaceDirectory getDirectory(WorkspacePath relativePath) {
        return new IntelliJWorkspaceDirectory(getProject(), getResource().findFileByRelativePath(relativePath.asPortableString()), getIgnoredFileNames());
    }

    @Override
    public List<WorkspaceResource> getChildren() {
        List<WorkspaceResource> answer = new ArrayList<>();
        VirtualFile[] children = getResource().getChildren();
        for(VirtualFile child: children) {
            if(child.isDirectory()) {
                answer.add(new IntelliJWorkspaceDirectory(getProject(), child, getIgnoredFileNames()));
            } else {
                answer.add(new IntelliJWorkspaceFile(getProject(), child, getIgnoredFileNames()));
            }
        }
        return answer;
    }
}
