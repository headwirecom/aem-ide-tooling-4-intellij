package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by schaefa on 5/13/15.
 */
public class IFile extends IResource {

    public IFile(@NotNull ServerConfiguration.Module module, @NotNull VirtualFile file) {
        super(module, file);
    }

    public InputStream getContents() throws IOException {
        return file.getInputStream();
    }
}
