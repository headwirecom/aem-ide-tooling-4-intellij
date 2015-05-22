package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by schaefa on 5/15/15.
 */
public class IModuleResourceDelta {
    public static final int ADDED = 1;
    public static final int CHANGED = 2;
    public static final int NO_CHANGE = 3;
    public static final int REMOVED = 4;

    protected IModuleResource resource;
    protected int kind;

    protected IModuleResourceDelta[] children;

    public IModuleResourceDelta(IModuleResource resource, int kind) {
        this.resource = resource;
        this.kind = kind;
    }

    public void setChildren(IModuleResourceDelta[] children) {
        this.children = children;
    }

    /* (non-Javadoc)
     * @see org.eclipse.wst.server.core.model.IModuleResource#getModuleRelativePath()
     */
    public IPath getModuleRelativePath() {
        return resource.getModuleRelativePath();
    }

    /* (non-Javadoc)
     * @see org.eclipse.wst.server.core.model.IModuleResourceDelta#getModuleResource()
     */
    public IModuleResource getModuleResource() {
        return resource;
    }

    /* (non-Javadoc)
     * @see org.eclipse.wst.server.core.model.IModuleResourceDelta#getKind()
     */
    public int getKind() {
        return kind;
    }

    /* (non-Javadoc)
     * @see org.eclipse.wst.server.core.model.IModuleResourceDelta#getAffectedChildren()
     */
    public IModuleResourceDelta[] getAffectedChildren() {
        return children;
    }

    public String toString() {
        return "ModuleResourceDelta [" + resource + ", " + kind + "]";
    }

    public void trace(String indent) {
        System.out.println(indent + toString());
        if (children != null) {
            int size = children.length;
            for (int i = 0; i < size; i++) {
                ((IModuleResourceDelta)children[i]).trace(indent + "  ");
            }
        }
    }
}
