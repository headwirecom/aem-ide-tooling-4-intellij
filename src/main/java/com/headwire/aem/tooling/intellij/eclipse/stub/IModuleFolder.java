package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by schaefa on 5/18/15.
 */
public class IModuleFolder {
    private static final IModuleResource[] EMPTY_RESOURCE_ARRAY = new IModuleResource[0];

    private IContainer container;
    private String name;
    private IPath path;
    private IModuleResource[] members;

    /**
     * Creates a module folder.
     *
     * @param container the container, or <code>null</code> for unknown container
     * @param name a name
     * @param path the module relative path to the folder
     */
    public IModuleFolder(IContainer container, String name, IPath path) {
        if (name == null)
            throw new IllegalArgumentException();
        this.container = container;
        this.name = name;
        this.path = path;
    }

    /**
     * Sets the members (contents) of this folder.
     *
     * @param members the members
     */
    public void setMembers(IModuleResource[] members) {
        this.members = members;
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

    /* (non-Javadoc)
     * @see org.eclipse.wst.server.core.model.IModuleFolder#members()
     */
    public IModuleResource[] members() {
        if (members == null)
            return EMPTY_RESOURCE_ARRAY;
        return members;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof IModuleFolder))
            return false;

        IModuleFolder mf = (IModuleFolder) obj;
        if (!name.equals(mf.name))
            return false;
        if (!path.equals(mf.path))
            return false;
        return true;
    }

    public int hashCode() {
        return name.hashCode() * 37 + path.hashCode();
    }

    public Object getAdapter(Class cl) {
        if (IContainer.class.equals(cl) || IFolder.class.equals(cl))
            return container;
        return null;
    }

    public String toString() {
        return "ModuleFolder [" + name + ", " + path + "]";
    }
}
