package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Created by schaefa on 5/13/15.
 */
public class IFolder extends IResource {

    public IFolder() {}

    public IFolder(@NotNull Module module, @NotNull VirtualFile file) {
        super(module, file);
    }

    public IResource findMember(String member) {
        VirtualFile memberFile = file.findChild(member);
        if(memberFile == null) {
            memberFile = file.findFileByRelativePath(member);
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
}
