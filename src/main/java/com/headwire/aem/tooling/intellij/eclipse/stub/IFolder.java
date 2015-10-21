package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Created by schaefa on 5/13/15.
 */
public class IFolder extends IResource {

    public IFolder() {}

    public IFolder(@NotNull Module module, @NotNull VirtualFile file) {
        super(module, file);
    }

    public IFolder(@NotNull Module module, @NotNull File file) {
        super(module, file);
    }

    public IResource findMember(String member) {
        // IntelliJ is expected forward slashes
        member = member.replace("\\", "/");
        VirtualFile memberFile = virtualFile.findChild(member);
        if(memberFile == null) {
            memberFile = virtualFile.findFileByRelativePath(member);
        }
        IResource ret = null;
        if(memberFile != null) {
            ret = memberFile.isDirectory() ? new IFolder(module, memberFile) : new IFile(module, memberFile);
        }
        return ret;
    }

    public IResource findMember(IPath member) {
        return findMember(member.toOSString());
    }

    public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
        if(virtualFile == null) {
            if(!file.exists()) {
                try {
                    file.createNewFile();
                } catch(IOException e) {
                    throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to create file: " + file, e));
                }
            }
        } else {
            if(!virtualFile.exists()) {
                File newFile = new File(virtualFile.getPath());
                try {
                    newFile.createNewFile();
                } catch(IOException e) {
                    throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to create file: " + newFile, e));
                }
            }
        }
    }
}
