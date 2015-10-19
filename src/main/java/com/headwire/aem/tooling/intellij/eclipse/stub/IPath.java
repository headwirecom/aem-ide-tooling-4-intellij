package com.headwire.aem.tooling.intellij.eclipse.stub;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by schaefa on 5/13/15.
 */
public class IPath {

    private IPath base;
    private String relativePath;
    private File file;
    private List<String> segments;

    private boolean windows;

    public IPath(@NotNull File file) {
        this.file = file;
    }

    public IPath(@NotNull String filePath) {
        windows = filePath.contains("\\");
        if((windows && (filePath.contains(":\\") || filePath.startsWith("\\")))
            || filePath.startsWith("/")
        ) {
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
        File ret = null;
        if(file != null) {
            ret = file;
        } else if(base != null) {
            ret = new File(base.toFile(), relativePath);
        } else {
            ret = new File(relativePath);
        }
        return ret;
    }

    public IPath makeRelativeTo(IPath otherPath) {
        IPath ret = null;
        File parent = otherPath.toFile();
        if(parent != null && parent.exists() && parent.isDirectory()) {
            IPath absolute = this.makeAbsolute();
            if(absolute.file.getAbsolutePath().startsWith(parent.getAbsolutePath())) {
                // Cut of the beginning
                String relativePath = absolute.file.getAbsolutePath().substring(parent.getAbsolutePath().length());
                if(relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                ret = new IPath(otherPath, relativePath);
            }
        } else if(relativePath != null && relativePath.startsWith(otherPath.relativePath)) {
            String temp = relativePath.substring(otherPath.relativePath.length());
            if(temp.startsWith("/")) {
                temp = temp.substring(1);
            }
            ret = new IPath(otherPath, temp);
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
        return getSegments().size();
    }

    public String segment(int i) {
        return getSegments().get(i);
    }

    private List<String> getSegments() {
        if(segments == null) {
            //AS TODO: Handle Windows Paths
            // If Windows then remove Drive letter
            String path = relativePath;
            if(path != null && windows && relativePath.contains(":\\")) {
                path = path.substring(3);
                if(!path.startsWith("\\")) {
                    path = "\\" + path;
                }
                path = path.replaceAll("\\\\", "/");
            }
            String[] tokens = (path == null ? file.getPath() : path).split("/");
            List<String> ret = new ArrayList<String>(Arrays.asList(tokens));
            Iterator<String> i = ret.iterator();
            while(i.hasNext()) {
                String value = i.next();
                if(value == null || value.length() == 0) {
                    i.remove();
                }
            }
            segments = ret;
        }
        return segments;
    }

    public IPath removeLastSegments(int i) {
        IPath ret;
        if(file != null) {
            ret = new IPath(file.getParentFile());
        } else {
            // If Windows then remove Drive letter
            String path = relativePath;
            if(path != null && windows && relativePath.contains(":\\")) {
                path = path.substring(3);
                if(!path.startsWith("\\")) {
                    path = "\\" + path;
                }
                path = path.replaceAll("\\\\", "/");
            }
            String cleanPath = path.endsWith("/") ?
                (path.length() > 1 ? path.substring(0, path.length() - 1) : "") :
                path;
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
