/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.action.StartRunServerConnectionAction;
import com.headwire.aem.tooling.intellij.config.ConfigurationListener;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.intellij.ide.util.treeView.*;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.components.ServiceManager;
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

final class ServerExplorerTreeBuilder extends AbstractTreeBuilder {

    private static final TreePath[] EMPTY_TREE_PATH = new TreePath[0];
    private final ConfigurationListener myConfigurationListener;
    private final Project myProject;
    private ServerConfigurationManager myConfig;
    private ExpandedStateUpdater myExpansionListener;
    private StartRunServerConnectionAction checkAction;

    public ServerExplorerTreeBuilder(Project project, JTree tree, DefaultTreeModel treeModel) {
        super(tree, treeModel, new ServerExplorerTreeStructure(project), IndexComparator.INSTANCE);
        myProject = project;
        myConfigurationListener = new ConfigurationListenerImpl();
        myConfig = ServiceManager.getService(project, ServerConfigurationManager.class);
        myExpansionListener = new ExpandedStateUpdater();
        tree.addTreeExpansionListener(myExpansionListener);
        initRootNode();
        myConfig.addConfigurationListener(myConfigurationListener);
        getTree().getModel().addTreeModelListener(new ChangeListener());

        ActionManager actionManager = ActionManager.getInstance();
        checkAction = (StartRunServerConnectionAction) actionManager.getAction("AEM.Check.Action");
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
        return ((ServerNodeDescriptor) nodeDescriptor).isAutoExpand();
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
//AS TODO: TreeUtil.collapseAll() does expand all entries at the end. Copied over and took out what did not work
//    TreeUtil.collapseAll(getTree(), 1);
        collapseAll(getTree(), 1);
//    getTree().setSelectionPaths(EMPTY_TREE_PATH);
//    pathsToExpand.clear();
//    TreeBuilderUtil.restorePaths(this, pathsToExpand, selectionPaths, true);
    }

    public static void collapseAll(@NotNull final JTree tree, final int keepSelectionLevel) {
        final TreePath leadSelectionPath = tree.getLeadSelectionPath();
        // Collapse all
        int row = tree.getRowCount() - 1;
        while(row >= 0) {
            tree.collapseRow(row);
            row--;
        }
//        tree.expandRow(0);
//        final DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
//        tree.expandPath(new TreePath(root));
//        if (leadSelectionPath != null) {
//            final Object[] path = leadSelectionPath.getPath();
//            final Object[] pathToSelect = new Object[path.length > keepSelectionLevel && keepSelectionLevel >= 0 ? keepSelectionLevel : path.length];
//            System.arraycopy(path, 0, pathToSelect, 0, pathToSelect.length);
//            if (pathToSelect.length == 0) return;
//            selectPath(tree, new TreePath(pathToSelect));
//        }
    }

    private class ExpandedStateUpdater implements TreeExpansionListener {
        public void treeExpanded(TreeExpansionEvent event) {
            setExpandedState(event, true);
        }

        public void treeCollapsed(TreeExpansionEvent event) {
            setExpandedState(event, false);
        }

        private void setExpandedState(TreeExpansionEvent event, boolean shouldExpand) {
            final TreePath path = event.getPath();
            final AbstractTreeUi ui = getUi();
            final Object lastPathComponent = path.getLastPathComponent();
            if(lastPathComponent != null) {
                final Object element = ui.getElementFor(lastPathComponent);
                if(element instanceof ServerNodeDescriptor) {
//          ((ServerNodeDescriptor)element).setShouldExpand(shouldExpand);
                }
            }
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
//            myConfigurationListener.configurationLoaded();
        }
    }
}
