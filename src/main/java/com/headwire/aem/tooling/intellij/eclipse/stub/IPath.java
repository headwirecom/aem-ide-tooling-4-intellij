package com.headwire.aem.tooling.intellij.eclipse.stub;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by schaefa on 5/13/15.
 */
public class IPath {

    private IPath base;
    private String relativePath;
    private File file;

    public IPath(@NotNull File file) {
        this.file = file;
    }

    public IPath(@NotNull String filePath) {
        if(filePath.startsWith("/")) {
            this.file = new File(filePath);
        } else {
            this.relativePath = filePath;
        }
    }

    public IPath(@NotNull IPath base, @NotNull String relativePath) {
//        this.file = new File(relativePath);
        this.relativePath = relativePath;
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
        return file != null ? file.getPath() : relativePath;
    }

    public String toOSString() {
        return file != null ? file.getPath() : relativePath;
    }

    public IPath makeAbsolute() {
        IPath ret = this;
        if(base != null) {
            ret = new IPath(new File(base.toFile(), relativePath));
        } else if(file == null) {
            ret = new IPath(new File(relativePath));
        }
        return ret;
    }

    public int segmentCount() {
        return 0;
    }

    public IPath removeLastSegments(int i) {
        IPath ret;
        if(file != null) {
            ret = new IPath(file.getParentFile());
        } else {
            String cleanPath = relativePath.endsWith("/") ?
                (relativePath.length() > 1 ? relativePath.substring(0, relativePath.length() - 1) : "") :
                relativePath;
            int index = cleanPath.lastIndexOf("/");
            String newPath = index >= 0 ?
                (index == 0 ? "" : cleanPath.substring(0, index)) :
                "";
            ret = new IPath(base, newPath);
        }
        return ret;
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
