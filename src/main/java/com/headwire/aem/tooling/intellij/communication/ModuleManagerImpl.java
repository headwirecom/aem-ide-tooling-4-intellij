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
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schaefa on 3/18/16.
 */
public class ModuleManagerImpl
    implements ModuleManager
{
    ServerConfiguration serverConfiguration;
    Project project;

    public ModuleManagerImpl(@NotNull Project project) {
        this.project = project;
        serverConfiguration = ComponentProvider.getComponent(project, ServerConfiguration.class);
    }

    @NotNull
    @Override
    public List<ModuleContext> getModuleContexts() {
        List<ModuleContext> ret = new ArrayList<ModuleContext>();

        return ret;
    }

    @NotNull
    @Override
    public List<ModuleContext> getModuleContexts(@NotNull Project project) {
        List<ModuleContext> ret = new ArrayList<ModuleContext>();

        return ret;
    }

    @Nullable
    @Override
    public Module getProjectModuleByConfiguration(@NotNull ServerConfiguration.Module sourceModule) {
        Module ret = sourceModule.getModuleContext().getModule();
        if(ret == null) {
            Module[] modules = com.intellij.openapi.module.ModuleManager.getInstance(project).getModules();
            for(Module module: modules) {

            }
        }
        return ret;
    }

    @Nullable
    @Override
    public Module getProjectModuleByMaven(@NotNull MavenProject sourceModule) {
        return null;
    }

    @Nullable
    @Override
    public ServerConfiguration.Module getConfigurationModuleByMaven(@NotNull MavenProject sourceModule) {
        return null;
    }

    @Nullable
    @Override
    public ServerConfiguration.Module getConfigurationModuleByProject(@NotNull Module sourceModule) {
        return null;
    }

    @Nullable
    @Override
    public MavenProject getMavenModuleByProject(@NotNull Module sourceModule) {
        return null;
    }

    @Nullable
    @Override
    public MavenProject getMavenModuleByConfiguration(@NotNull ServerConfiguration.Module sourceModule) {
        return null;
    }
}
