package com.headwire.aem.tooling.intellij.config;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 * Created by schaefa on 7/2/15.
 */
public interface ModuleProject {
    /** @return Group Name of the Artifact **/
    public String getGroupId();
    /** @return Name of the Artifact **/
    public String getArtifactId();
    /** @return Version of the Artifact **/
    public String getVersion();
    /** @return Name of the Module **/
    public String getName();
    /** @return Name of the File without Extension of the final Build File **/
    public String getBuildFileName();
    /** @return True if the Module generates and OSGi Bundle **/
    public boolean isOSGiBundle();
    /** @return True if the Module generates a Sling Content Package **/
    public boolean isContent();
    /** @return Name of the Build Directory Path **/
    public String getBuildDirectoryPath();
    /** @return Root Directory Path of the Module **/
    public String getModuleDirectory();
    /** List of all Content Directory Path if this is a Sling Content. This return as list but might be empty **/
    public List<String> getContentDirectoryPaths();
}
