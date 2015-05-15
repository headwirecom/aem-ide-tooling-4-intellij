package com.headwire.aem.tooling.intellij.eclipse.stub;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by schaefa on 5/13/15.
 */
public class IPath {

    private IPath base;
    private File file;

    public IPath(@NotNull File file) {
        this.file = file;
    }

    public IPath(@NotNull String filePath) {
        this.file = new File(filePath);
    }

    public IPath(@NotNull IPath base, @NotNull String relativePath) {
        this.file = new File(relativePath);
        this.base = base;
    }

    public File toFile() {
        return this.file;
    }

    public IPath makeRelativeTo(IPath fullPath) {
        IPath ret = null;
        File parent = fullPath.toFile();
        if(parent.exists() && parent.isDirectory()) {
            if(file.getAbsolutePath().startsWith(parent.getAbsolutePath())) {
                // Cut of the beginning
                String relativePath = file.getAbsolutePath().substring(parent.getAbsolutePath().length());
                if(relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                ret = new IPath(fullPath, relativePath);
            }
        }
        return ret;
    }

    public String toPortableString() {
        return file.getPath();
    }

    public String toOSString() {
        return file.getPath();
    }

    public IPath makeAbsolute() {
        IPath ret = this;
        if(base != null) {
            ret = new IPath(new File(base.toFile(), file.getPath()));
        }
        return ret;
    }

    public int segmentCount() {
        return 0;
    }

    public IPath removeLastSegments(int i) {
        return null;
    }

    public IPath append(String osPath) {
        IPath ret = makeAbsolute();
        ret = new IPath(new File(ret.toFile(), osPath));
        return ret;
    }

    public boolean isEmpty() {
        return makeAbsolute().toFile().listFiles().length == 0;
    }
}
