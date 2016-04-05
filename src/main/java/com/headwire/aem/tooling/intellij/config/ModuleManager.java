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

import org.jetbrains.annotations.NotNull;
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

    /**
     * Get a Server Configuration Module based on the Unified Module and Server Configuration
     *
     * @param unifiedModule Unified module that must be bound to the Server Configuration Module
     * @param serverConfiguration Server Configuration the Module belongs to
     * @return Server Configuration Module if found otherwise null
     */
    public ServerConfiguration.Module getSCM(@NotNull UnifiedModule unifiedModule, @NotNull ServerConfiguration serverConfiguration);

    /**
     * @param unifiedModule Unified Module
     * @return Maven Project if this is Maven based project
     */
    public MavenProject getMavenProject(@NotNull UnifiedModule unifiedModule);

    /**
     * Obtain the Underlying Unified Module inside the Server Configuration Module
     * @param scm Server Configuration Module to check
     * @return Unified Module if bound to the SCM or otherwise null
     */
    public UnifiedModule getUnifiedModule(@NotNull ServerConfiguration.Module scm);

    /**
     * Obtain all Unified Modules inside a given Server Configuration that are currently bound
     * @param serverConfiguration Server Configuration
     * @return List of all bound Unified Modules in the Server Configuration
     */
    public List<UnifiedModule> getUnifiedModules(@NotNull ServerConfiguration serverConfiguration);

    /**
     * Obtain all Unified Modules inside a given Server Configuration
     * @param serverConfiguration Server Configuration
     * @param force If true the it will try to bind the modules first
     * @return List of all Unified Modules in the Server Configuration
     */
    public List<UnifiedModule> getUnifiedModules(@NotNull ServerConfiguration serverConfiguration, boolean force);
}
