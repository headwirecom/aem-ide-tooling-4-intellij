package com.headwire.aem.tooling.intellij.sync.content;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.sync.content.WorkspaceDirectory;
import org.apache.sling.ide.sync.content.WorkspaceFile;
import org.apache.sling.ide.sync.content.WorkspaceProject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class IntelliJWorkspaceFile
    extends IntelliJWorkspaceResource
    implements WorkspaceFile
{
    public IntelliJWorkspaceFile(WorkspaceProject project, VirtualFile resource, Set<String> ignoredFileNames) {
        super(project, resource, ignoredFileNames);
    }

    @Override
    public InputStream getContents() throws IOException {
        return getResource().getInputStream();
    }

    @Override
    public WorkspaceDirectory getParent() {
        WorkspaceDirectory answer = null;
        VirtualFile parent = getResource().getParent();
        if(parent != null) {
            answer = new IntelliJWorkspaceDirectory(getProject(), parent, getIgnoredFileNames());
        }
        return answer;
    }
}
