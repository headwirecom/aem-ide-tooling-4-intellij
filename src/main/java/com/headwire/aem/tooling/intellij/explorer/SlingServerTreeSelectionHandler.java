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
 * Created by schaefa on 5/12/15.
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
