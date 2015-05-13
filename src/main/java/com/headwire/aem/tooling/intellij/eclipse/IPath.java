package com.headwire.aem.tooling.intellij.eclipse;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by schaefa on 5/13/15.
 */
public class IPath {

    private File file;

    public IPath(@NotNull File file) {
        this.file = file;
    }

    public IPath(@NotNull String filePath) {
        this.file = new File(filePath);
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
                ret = new IPath(relativePath);
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
        return null;
    }

    public int segmentCount() {
        return 0;
    }

    public IPath removeLastSegments(int i) {
        return null;
    }

    public IPath append(String osPath) {
        return null;
    }

    public boolean isEmpty() {
        return false;
    }
}
