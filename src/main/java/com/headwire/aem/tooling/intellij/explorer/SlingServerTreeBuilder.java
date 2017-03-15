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

import com.headwire.aem.tooling.intellij.action.StartRunConnectionAction;
import com.headwire.aem.tooling.intellij.config.ConfigurationListener;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.ide.util.treeView.*;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

final class SlingServerTreeBuilder extends AbstractTreeBuilder {

    private static final TreePath[] EMPTY_TREE_PATH = new TreePath[0];
    private final ConfigurationListener myConfigurationListener;
    private final Project myProject;
    private ServerConfigurationManager myConfig;
    private ExpandedStateUpdater myExpansionListener;
    private StartRunConnectionAction checkAction;

    public SlingServerTreeBuilder(Project project, JTree tree, DefaultTreeModel treeModel) {
        super(tree, treeModel, new SlingServerTreeStructure(project), IndexComparator.INSTANCE);
        myProject = project;
        myConfigurationListener = new ConfigurationListenerImpl();
        myConfig = ComponentProvider.getComponent(project, ServerConfigurationManager.class);
        myExpansionListener = new ExpandedStateUpdater();
        tree.addTreeExpansionListener(myExpansionListener);
        initRootNode();
        myConfig.addConfigurationListener(myConfigurationListener);
        getTree().getModel().addTreeModelListener(new ChangeListener());

        ActionManager actionManager = ActionManager.getInstance();
        checkAction = (StartRunConnectionAction) actionManager.getAction("AEM.Check.Action");
    }


    public void dispose() {
        final ExpandedStateUpdater expansionListener = myExpansionListener;
        final JTree tree = getTree();
        if(expansionListener != null && tree != null) {
            tree.removeTreeExpansionListener(expansionListener);
            myExpansionListener = null;
        }

        super.dispose();
    }

    protected boolean isAlwaysShowPlus(NodeDescriptor nodeDescriptor) {
        return false;
    }

    protected boolean isAutoExpandNode(NodeDescriptor nodeDescriptor) {
        // This is what expands the tree automatically when it opens
        return ((BaseNodeDescriptor) nodeDescriptor).isAutoExpand();
    }

    public void setTargetsFiltered(boolean value) {
        queueUpdate();
    }

    @NotNull
    protected ProgressIndicator createProgressIndicator() {
        return ProgressIndicatorUtils.forceWriteActionPriority(new ProgressIndicatorBase(true), this);
    }

    private final class ConfigurationListenerImpl
        implements ConfigurationListener {

        private boolean first = true;

        public void configurationLoaded() {
            queueUpdate();
        }
    }

    public void expandAll() {
        final List<Object> pathsToExpand = new ArrayList<Object>();
        final List<Object> selectionPaths = new ArrayList<Object>();
        TreeBuilderUtil.storePaths(this, getRootNode(), pathsToExpand, selectionPaths, true);
        int row = 0;
        while(row < getTree().getRowCount()) {
            getTree().expandRow(row);
            row++;
        }
        getTree().setSelectionPaths(EMPTY_TREE_PATH);
        TreeBuilderUtil.restorePaths(this, pathsToExpand, selectionPaths, true);
    }

    void collapseAll() {
        final List<Object> pathsToExpand = new ArrayList<Object>();
        final List<Object> selectionPaths = new ArrayList<Object>();
        TreeBuilderUtil.storePaths(this, getRootNode(), pathsToExpand, selectionPaths, true);
        collapseAll(getTree(), 1);
    }

    public static void collapseAll(@NotNull final JTree tree, final int keepSelectionLevel) {
        final TreePath leadSelectionPath = tree.getLeadSelectionPath();
        // Collapse all
        int row = tree.getRowCount() - 1;
        while(row >= 0) {
            tree.collapseRow(row);
            row--;
        }
    }

    private class ExpandedStateUpdater implements TreeExpansionListener {
        public void treeExpanded(TreeExpansionEvent event) {
            setExpandedState(event, true);
        }

        public void treeCollapsed(TreeExpansionEvent event) {
            setExpandedState(event, false);
        }

        private void setExpandedState(TreeExpansionEvent event, boolean shouldExpand) {
//AS TODO: Make sure this code is really not needed (3/1/2017)
//            final TreePath path = event.getPath();
//            final AbstractTreeUi ui = getUi();
//            final Object lastPathComponent = path.getLastPathComponent();
//            if(lastPathComponent != null) {
//                final Object element = ui.getElementFor(lastPathComponent);
//                if(element instanceof BaseNodeDescriptor) {
////          ((BaseNodeDescriptor)element).setShouldExpand(shouldExpand);
//                }
//            }
        }
    }

    private class ChangeListener implements TreeModelListener {

        @Override
        public void treeNodesChanged(TreeModelEvent treeModelEvent) {
        }

        @Override
        public void treeNodesInserted(TreeModelEvent treeModelEvent) {
            myConfigurationListener.configurationLoaded();
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent treeModelEvent) {
        }

        @Override
        public void treeStructureChanged(TreeModelEvent treeModelEvent) {
        }
    }
}
