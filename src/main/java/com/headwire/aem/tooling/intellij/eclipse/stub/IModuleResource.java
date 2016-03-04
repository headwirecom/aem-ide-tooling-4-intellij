package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by schaefa on 5/15/15.
 */
@Deprecated
public abstract class IModuleResource {
    public IPath getModuleRelativePath() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public abstract Object getAdapter(Class clazz);
}
