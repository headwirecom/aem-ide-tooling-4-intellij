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
package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.CellAppearanceEx;
import com.intellij.ui.HtmlListCellRenderer;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

abstract class ServerNodeDescriptor
    extends NodeDescriptor
    implements CellAppearanceEx
{

    public ServerNodeDescriptor(Project project, NodeDescriptor parentDescriptor) {
        super(project, parentDescriptor);
    }

    public abstract boolean isAutoExpand();

    @Override
    public void customize(@NotNull SimpleColoredComponent component) {
        component.append(toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    @Override
    public void customize(@NotNull final HtmlListCellRenderer renderer) {
        renderer.append(toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    @NotNull
    public String getText() {
        return toString();
    }

    public abstract ServerConfiguration getServerConfiguration();

    public abstract ServerConfiguration.Module getModuleConfiguration();

}
