package com.headwire.aem.tooling.intellij.eclipse.stub;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Created by schaefa on 5/14/15.
 */
public class IModule {

    private Module module;

    public IModule(Module module) {
        this.module = module;
    }

    public IProject getProject() {
        return new IProject(module);
    }
}
