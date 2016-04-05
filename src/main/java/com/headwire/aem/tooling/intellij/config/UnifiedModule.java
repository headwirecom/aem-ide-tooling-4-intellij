/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.config;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.List;

/**
 * Created by Andreas Schaefer (Headwire.com) on 7/2/15.
 */
public interface UnifiedModule {

    /** Resets the given Module to support a change in its configuration at runtime **/
    public void init(MavenProject mavenProject, @NotNull Module module);

    /**
     * Checks if the given Server Configuration Module is already registered
     * @param module Module to check against
     * @return True if already registered
     *
     * @deprecated This should be managed by the Module Manager
     */
    public boolean containsServerConfigurationModule(@NotNull ServerConfiguration.Module module);

    /**
     * Add a Server Configuation Module to the list
     *
     * @param module Module to be added
     * @return True if the module was not already in the list and therefore added
     * @deprecated This should be managed by the Module Manager
     */
    public boolean addServerConfigurationModule(@NotNull ServerConfiguration.Module module);

    /**
     * Remove a given Server Configuration Module if registered
     * @param module Module to be removed
     * @return True if the module was registered and therefore removed
     * @deprecated This should be managed by the Module Manager
     */
    public boolean removeServerConfigurationModule(@NotNull ServerConfiguration.Module module);

    /** @return List of currently registered Server Configuration Modules
     * @deprecated This should be managed by the Module Manager
     *  **/
    public List<ServerConfiguration.Module> getSererConfigurationModuleList();

    /**
     * Obtain the Server Configuration Module based on the given Server Configuration
     *
     * @param serverConfiguration Server Configuration to find its Module
     * @return Server Configuration Module that belongs to this Module Context if found otherwise null
     *
     * @deprecated This should be managed by the Module Manager
     */
    public ServerConfiguration.Module getServerConfigurationModule(@NotNull ServerConfiguration serverConfiguration);

    /** @return True if the Module is configured by Maven only **/
    public boolean isMavenBased();

    /** @return Symbolic Name which is the OSGi Symbolic Name or Maven Group & Artifact Id **/
    public String getSymbolicName();

    /** @return Version of the Module (Maven or OSGi Bundle version) **/
    public String getVersion();

    /** @return Name of the Module **/
    public String getName();

    /** @return Name of the final Build File **/
    public String getBuildFileName();

    /** @return True if the Module generates and OSGi Bundle **/
    public boolean isOSGiBundle();

    /** @return True if the Module generates a Sling Content Package **/
    public boolean isContent();

    /** @return Name of the Build Directory Path including extension **/
    public String getBuildDirectoryPath();

    /** @return Root Directory Path of the Module **/
    public String getModuleDirectory();

    /** List of all Content Directory Path if this is a Sling Content. This return as list but might be empty **/
    public List<String> getContentDirectoryPaths();

    /** @return the Metainf Path if set otherwise null **/
    public String getMetaInfPath();

    /** @return IntelliJ Module if set otherwise null **/
    public Module getModule();
}
