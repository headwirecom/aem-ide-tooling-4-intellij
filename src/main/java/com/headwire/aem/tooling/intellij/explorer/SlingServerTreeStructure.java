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
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.JBColor;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SlingServerTreeStructure extends AbstractTreeStructure {
    private static final Logger LOG = Logger.getInstance(SlingServerTreeStructure.class);
    private final Project myProject;
    private final Object myRoot = new Object();
    private boolean myFilteredTargets = false;

    public SlingServerTreeStructure(final Project project) {
        myProject = project;
    }

    @Override
    public boolean isToBuildChildrenInBackground(final Object element) {
        return true;
    }

    @Override
    @NotNull
    public NodeDescriptor createDescriptor(Object element, NodeDescriptor parentDescriptor) {
        if(element == myRoot) {
            return new RootNodeDescriptorSling(myProject, parentDescriptor);
        }

        if(element instanceof String) {
            return new TextInfoNodeDescriptorSling(myProject, parentDescriptor, (String) element);
        }

        if(element instanceof ServerConfiguration) {
            return new SlingServerNodeDescriptor(myProject, parentDescriptor, (ServerConfiguration) element);
        }

        if(element instanceof ServerConfiguration.Module) {
            return new SlingServerModuleNodeDescriptor(myProject, parentDescriptor, (ServerConfiguration.Module) element);
        }

        LOG.error("Unknown element for this tree structure " + element);
        return null;
    }

    @Override
    public Object[] getChildElements(Object element) {
        final ServerConfigurationManager configuration = myProject.getComponent(ServerConfigurationManager.class);
        if(element == myRoot) {
            if(!configuration.isInitialized()) {
                return new Object[]{AEMBundle.message("tree.builder.configurations.loading.name")};
            }
            final ServerConfiguration[] serverConfigurations = configuration.getServerConfigurations();
            return serverConfigurations.length == 0 ?
                new Object[]{AEMBundle.message("tree.builder.no.configurations.defined.name")} :
                serverConfigurations;
        }

        if (element instanceof ServerConfiguration) {
            return ((ServerConfiguration) element).getModuleList().toArray();
        }

        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Override
    @Nullable
    public Object getParentElement(Object element) {
        if(element instanceof ServerConfiguration) {
            return myRoot;
        }

        if(element instanceof ServerConfiguration.Module) {
            return ((ServerConfiguration.Module) element).getParent();
        }
        return null;
    }

    @Override
    public void commit() {
        PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    }

    @Override
    public boolean hasSomethingToCommit() {
        return PsiDocumentManager.getInstance(myProject).hasUncommitedDocuments();
    }

    @NotNull
    @Override
    public ActionCallback asyncCommit() {
        return asyncCommitDocuments(myProject);
    }

    @Override
    public Object getRootElement() {
        return myRoot;
    }

    private final class RootNodeDescriptorSling extends BaseNodeDescriptor {
        public RootNodeDescriptorSling(Project project, NodeDescriptor parentDescriptor) {
            super(project, parentDescriptor);
            myName = AEMBundle.message("tree.builder.root.node.name");
        }

        @Override
        public boolean isAutoExpand() {
            return true;
        }

        @Override
        public ServerConfiguration getServerConfiguration() {
            return null;
        }

        @Override
        public ServerConfiguration.Module getModuleConfiguration() {
            return null;
        }

        @Override
        public Object getElement() {
            return myRoot;
        }

        @Override
        public boolean update() {
//      myName = "";
            return false;
        }
    }

    private static final class TextInfoNodeDescriptorSling extends BaseNodeDescriptor {
        public TextInfoNodeDescriptorSling(Project project, NodeDescriptor parentDescriptor, String text) {
            super(project, parentDescriptor);
            myName = text;
            myColor = JBColor.blue;
        }

        @Override
        public Object getElement() {
            return myName;
        }

        @Override
        public boolean update() {
            return true;
        }

        @Override
        public boolean isAutoExpand() {
            return true;
        }

        @Override
        public ServerConfiguration getServerConfiguration() {
            return null;
        }

        @Override
        public ServerConfiguration.Module getModuleConfiguration() {
            return null;
        }
    }
}
