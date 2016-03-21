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

package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.config.ModuleContext;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.List;

/**
 * This plugin has 3 types of modules:
 * 1) IntelliJ Project Module
 * 2) Maven Module
 * 3) Server Configuration Module
 *
 * Each of them describe a module in one way or the other but
 * it can be frustrating to find one based on another and
 * the logic is distributed all over the plugin.
 *
 * This class unifies the code / logic and makes it possible
 * to find one type of Module by another one.
 *
 * Created by schaefa on 3/18/16.
 */
public interface ModuleManager {

    //AS TODO: Do we need to support also Module Contexts?

    /**
     * Obtains all the Module Contexts for a current Project
     *
     * @return List of Module Contexts which might be empty
     */
    public @NotNull List<ModuleContext> getModuleContexts();

    /**
     * Obtains all the Module Contexts for a given Project
     *
     * @param project Project for which we look for the Module Contexts
     * @return List of Module Contexts which might be empty
     */
    public @NotNull List<ModuleContext> getModuleContexts(@NotNull Project project);

    /**
     * Obtains the IntelliJ Project Module by a given Configuration Module
     *
     * @param sourceModule Configuration Module
     * @return Project Module if it could be extracted otherwise null
     */
    public @Nullable Module getProjectModuleByConfiguration(@NotNull ServerConfiguration.Module sourceModule);

    /**
     * Obtains the Maven (Project) Module by a given Configuration Module
     *
     * @param sourceModule Maven (Project) Module
     * @return Project Module if it could be extracted otherwise null
     */
    public @Nullable Module getProjectModuleByMaven(@NotNull MavenProject sourceModule);

    /**
     * Obtains the Server Configuration Module by a given Configuration Module
     *
     * @param sourceModule Maven (Project) Module
     * @return Server Configuration Module if it could be extracted otherwise null
     */
    public @Nullable ServerConfiguration.Module getConfigurationModuleByMaven(@NotNull MavenProject sourceModule);

    /**
     * Obtains the Server Configuration Module by a given Configuration Module
     *
     * @param sourceModule Intellij Project Module
     * @return Server Configuration Module if it could be extracted otherwise null
     */
    public @Nullable ServerConfiguration.Module getConfigurationModuleByProject(@NotNull Module sourceModule);

    /**
     * Obtains the Maven (Project) Module by a given Configuration Module
     *
     * @param sourceModule Intellij Project Module
     * @return Maven (Project) Module if it could be extracted otherwise null
     */
    public @Nullable MavenProject getMavenModuleByProject(@NotNull Module sourceModule);

    /**
     * Obtains the Maven (Project) Module by a given Configuration Module
     *
     * @param sourceModule Server Configuration Module
     * @return Maven (Project) Module if it could be extracted otherwise null
     */
    public @Nullable MavenProject getMavenModuleByConfiguration(@NotNull ServerConfiguration.Module sourceModule);

}
