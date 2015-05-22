package com.headwire.aem.tooling.intellij.eclipse.stub;

import java.util.List;

/**
 * Created by schaefa on 5/18/15.
 */
public class SlingPublisherBase {

    public IModuleResource[] getResources(IModule[] module) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
