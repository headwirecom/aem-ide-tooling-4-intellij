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

package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/12/15.
 */
public class SlingServerTreeSelectionHandler
    extends AbstractProjectComponent
{

    private Tree tree;

    public SlingServerTreeSelectionHandler(@NotNull Project project) {
        super(project);
    }

    public void init(@NotNull Tree tree) {
        this.tree = tree;
    }

    @Nullable
    public ServerConfiguration getCurrentConfiguration() {
        final BaseNodeDescriptor descriptor = getCurrentConfigurationDescriptor();
        return descriptor == null ? null : descriptor.getServerConfiguration();
    }

    @Nullable
    public ServerConfiguration.Module getCurrentModuleConfiguration() {
        final BaseNodeDescriptor descriptor = getCurrentConfigurationDescriptor();
        return descriptor == null ? null : descriptor.getModuleConfiguration();
    }

    @Nullable
    public BaseNodeDescriptor getCurrentConfigurationDescriptor() {
        BaseNodeDescriptor ret = null;
        if(tree != null) {
            final TreePath path = tree.getSelectionPath();
            if(path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                while(node != null) {
                    final Object userObject = node.getUserObject();
                    if(userObject instanceof BaseNodeDescriptor) {
                        ret = (BaseNodeDescriptor) userObject;
                        break;
                    }
                    node = (DefaultMutableTreeNode) node.getParent();
                }
            }
        }
        return ret;
    }

    @Nullable
    public List<ServerConfiguration.Module> getModuleDescriptorListOfCurrentConfiguration() {
        return getCurrentConfigurationModuleDescriptorList(true);
    }

    @Nullable
    public List<ServerConfiguration.Module> getCurrentConfigurationModuleDescriptorList() {
        return getCurrentConfigurationModuleDescriptorList(false);
    }

    @Nullable
    private List<ServerConfiguration.Module> getCurrentConfigurationModuleDescriptorList(boolean all) {
        List<ServerConfiguration.Module> moduleList = new ArrayList<ServerConfiguration.Module>();
        ServerConfiguration serverConfiguration = getCurrentConfiguration();
        if(serverConfiguration != null) {
            if(all) {
                moduleList.addAll(serverConfiguration.getModuleList());
            } else {
                TreePath selectionPath = tree.getSelectionPath();
                if(selectionPath != null) {
                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                    final Object userObject = node.getUserObject();
                    if(userObject instanceof SlingServerModuleNodeDescriptor) {
                        moduleList.add(((SlingServerModuleNodeDescriptor) userObject).getTarget());
                    } else {
                        moduleList.addAll(serverConfiguration.getModuleList());
                    }
                }
            }
        }
        return moduleList;
    }

}
