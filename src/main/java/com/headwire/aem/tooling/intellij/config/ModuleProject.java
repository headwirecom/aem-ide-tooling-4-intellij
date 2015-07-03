package com.headwire.aem.tooling.intellij.config;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 * Created by schaefa on 7/2/15.
 */
public interface ModuleProject {
    public String getGroupId();
    public String getArtifactId();
    public String getVersion();
    public String getName();
    public String getBuildFileName();
    public boolean isOSGiBundle();
    public boolean isContent();
    public String getBuildDirectoryName();
    public VirtualFile getModuleDirectory();
    public List<String> getContentDirectoryPaths();
}
