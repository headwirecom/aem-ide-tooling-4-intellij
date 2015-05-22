package com.headwire.aem.tooling.intellij.eclipse.stub;

import java.io.File;

/**
 * Created by schaefa on 5/18/15.
 */
public class IModuleFile extends IModuleResource {
    private IFile file;
    private File file2;
    private String name;
    private IPath path;
    private long stamp = -1;

    /**
     * Creates a workspace module file with the current modification stamp.
     *
     * @param file a file in the workspace
     * @param name a name
     * @param path the path to the file
     */
    public IModuleFile(IFile file, String name, IPath path) {
        if (name == null)
            throw new IllegalArgumentException();
        this.file = file;
        this.name = name;
        this.path = path;
        if (file != null)
            stamp = file.getModificationStamp() + file.getLocalTimeStamp();
    }

    /**
     * Creates an external module file with the current modification stamp.
     *
     * @param file
     * @param name
     * @param path
     */
    public IModuleFile(File file, String name, IPath path) {
        if (name == null)
            throw new IllegalArgumentException();
        this.file2 = file;
        this.name = name;
        this.path = path;
        if (file2 != null)
            stamp = file2.lastModified();
    }

    /**
     * Creates a module file with a specific modification stamp and no
     * file reference.
     *
     * @param name
     * @param path
     * @param stamp
     */
    public IModuleFile(String name, IPath path, long stamp) {
        if (name == null)
            throw new IllegalArgumentException();
        this.name = name;
        this.path = path;
        this.stamp = stamp;
    }

    /**
     * Creates a workspace module file with a specific modification stamp.
     *
     * @param file
     * @param name
     * @param path
     * @param stamp
     * @deprecated use one of the top two constructors instead
     */
    public IModuleFile(IFile file, String name, IPath path, long stamp) {
        this.file = file;
        this.name = name;
        this.path = path;
        this.stamp = stamp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.wst.server.core.model.IModuleFile#getModificationStamp()
     */
    public long getModificationStamp() {
        return stamp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.wst.server.core.model.IModuleResource#getModuleRelativePath()
     */
    public IPath getModuleRelativePath() {
        return path;
    }

    /* (non-Javadoc)
     * @see org.eclipse.wst.server.core.model.IModuleResource#getName()
     */
    public String getName() {
        return name;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof IModuleFile))
            return false;

        IModuleFile mf = (IModuleFile) obj;
        if (!name.equals(mf.getName()))
            return false;
        if (!path.equals(mf.getModuleRelativePath()))
            return false;
        return true;
    }

    public int hashCode() {
        return name.hashCode() * 37 + path.hashCode();
    }

    public Object getAdapter(Class cl) {
        if (IFile.class.equals(cl))
            return file;
        if (File.class.equals(cl))
            return file2;
        return null;
    }

    public String toString() {
        return "ModuleFile [" + name + ", " + path + ", " + stamp + "]";
    }
}
