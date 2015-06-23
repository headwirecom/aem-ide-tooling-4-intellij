package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by schaefa on 5/13/15.
 */
public class IFile extends IResource {

    public IFile() {}

    public IFile(@NotNull ServerConfiguration.Module module, @NotNull VirtualFile file) {
        super(module, file);
    }

    public IFile(@NotNull ServerConfiguration.Module module, @NotNull File file) {
        super(module, file);
    }

    public InputStream getContents() throws IOException {
        if(virtualFile != null) {
            return virtualFile.getInputStream();
        } else {
            return null;
        }
    }

    public Long getLocalTimeStamp() {
        if(virtualFile == null) {
            return file.lastModified();
        } else {
            return virtualFile.getTimeStamp();
        }
    }

    public void create(InputStream content, boolean force, IProgressMonitor monitor) throws CoreException {
        File newFile = file;
        if(virtualFile != null && !virtualFile.exists()) {
            newFile = new File(virtualFile.getPath());
        }
        if(!newFile.exists()) {
            String parentPath = newFile.getParent();
            VirtualFile parent = module.getProject().getProjectFile().getFileSystem().findFileByPath(parentPath);
            if(parent != null) {
                VirtualFile child = null;
                try {
                    child = parent.createChildData(this, newFile.getName());
                    this.virtualFile = child;
                    setContents(content, 0, null);
                } catch(IOException e) {
                    throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to create file: " + virtualFile, e));
                }
            } else {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Parent: " + parent + " could not be found"));
            }
        }
    }

    public void setContents(InputStream content, int updateFlags, IProgressMonitor monitor) throws CoreException {
        if(virtualFile != null && virtualFile.exists()) {
            byte[] bytes;
            try {
                bytes = IOUtils.toByteArray(content);
                virtualFile.setBinaryContent(bytes);
            } catch(IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to write bytes to: " + virtualFile, e));
            }
        }
    }
}
