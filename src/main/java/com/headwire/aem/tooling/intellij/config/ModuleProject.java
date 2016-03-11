/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.config;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 * Created by Andreas Schaefer (Headwire.com) on 7/2/15.
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
