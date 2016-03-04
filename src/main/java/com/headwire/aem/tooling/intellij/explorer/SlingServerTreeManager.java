package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.execution.RunManagerAdapter;
import com.intellij.execution.RunManagerEx;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManagerListener;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.xml.DomEventListener;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.events.DomEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

/**
 * Created by schaefa on 6/12/15.
 */
public class SlingServerTreeManager
    extends AbstractProjectComponent
    implements Disposable
{

    private Tree tree;
    private SlingServerTreeBuilder myBuilder;
    private ServerConfigurationManager myConfig;
    private KeyMapListener myKeyMapListener;

    private final TreeExpander myTreeExpander = new TreeExpander() {
        public void expandAll() {
            myBuilder.expandAll();
        }

        public boolean canExpand() {
            final ServerConfigurationManager config = myConfig;
            return config != null && config.serverConfigurationSize() > 0;
        }

        public void collapseAll() {
            myBuilder.collapseAll();
        }

        public boolean canCollapse() {
            return canExpand();
        }
    };


    public SlingServerTreeManager(@NotNull Project project) {
        super(project);
        final MessageManager messageManager = project.getComponent(MessageManager.class);
        final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
        tree = new Tree(model);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new NodeRenderer());
        SlingServerTreeSelectionHandler selectionHandler = project.getComponent(SlingServerTreeSelectionHandler.class);
        selectionHandler.init(tree);
        ServerConnectionManager serverConnectionManager = project.getComponent(ServerConnectionManager.class);
        serverConnectionManager.init(selectionHandler);
        myConfig = project.getComponent(ServerConfigurationManager.class);
        myBuilder = new SlingServerTreeBuilder(project, tree, model);
        TreeUtil.installActions(tree);
        new TreeSpeedSearch(tree);
        tree.addMouseListener(new PopupHandler() {
            public void invokePopup(final Component comp, final int x, final int y) {
                popupInvoked(comp, x, y);
            }
        });
        tree.setLineStyleAngled();
        ToolTipManager.sharedInstance().registerComponent(tree);

        tree.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent containerEvent) {
                messageManager.sendDebugNotification("Container Event: " + containerEvent);
            }

            @Override
            public void componentRemoved(ContainerEvent containerEvent) {
                messageManager.sendDebugNotification("Container Event: " + containerEvent);
            }
        });
        DomManager.getDomManager(project).addDomEventListener(new DomEventListener() {
            public void eventOccured(DomEvent event) {
                myBuilder.queueUpdate();
            }
        }, this);
        RunManagerEx myRunManager = RunManagerEx.getInstanceEx(project);
        myRunManager.addRunManagerListener(
            new RunManagerAdapter() {
                public void beforeRunTasksChanged() {
                    myBuilder.queueUpdate();
                }
            }
        );
        myKeyMapListener = new KeyMapListener();
    }

    public Tree getTree() {
        return tree;
    }

    private void popupInvoked(final Component comp, final int x, final int y) {
        Object userObject = null;
        final TreePath path = tree.getSelectionPath();
        if(path != null) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if(node != null) {
                userObject = node.getUserObject();
            }
        }
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup group = new DefaultActionGroup();
        if(
            userObject instanceof SlingServerNodeDescriptor ||
                userObject instanceof SlingServerModuleNodeDescriptor
            ) {
            group.add(actionManager.getAction("AEM.Connection.Popup"));
        } else {
            group.add(actionManager.getAction("AEM.Root.Popup"));
        }
        final ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.ANT_EXPLORER_POPUP, group);
        popupMenu.getComponent().show(comp, x, y);
    }

    @Override
    public void dispose() {
        final SlingServerTreeBuilder builder = myBuilder;
        if(builder != null) {
            Disposer.dispose(builder);
            myBuilder = null;
        }

        final Tree aTree = tree;
        if(aTree != null) {
            ToolTipManager.sharedInstance().unregisterComponent(aTree);
            for(KeyStroke keyStroke : aTree.getRegisteredKeyStrokes()) {
                aTree.unregisterKeyboardAction(keyStroke);
            }
            tree = null;
        }

        final KeyMapListener listener = myKeyMapListener;
        if(listener != null) {
            myKeyMapListener = null;
            listener.stopListen();
        }
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Sling Server Tree Manager";
    }

    public void adjustToolbar(DefaultActionGroup group) {
        AnAction action = CommonActionsManager.getInstance().createExpandAllAction(myTreeExpander, tree);
        action.getTemplatePresentation().setDescription(AEMBundle.message("eam.explorer.expand.all.nodes.action.description"));
        group.add(action);
        action = CommonActionsManager.getInstance().createCollapseAllAction(myTreeExpander, tree);
        action.getTemplatePresentation().setDescription(AEMBundle.message("aem.explorer.collapse.all.nodes.action.description"));
        group.add(action);
    }

    private class KeyMapListener implements KeymapManagerListener, Keymap.Listener {
        private Keymap myCurrentKeyMap = null;

        public KeyMapListener() {
            final KeymapManagerEx keyMapManager = KeymapManagerEx.getInstanceEx();
            final Keymap activeKeymap = keyMapManager.getActiveKeymap();
            listenTo(activeKeymap);
            keyMapManager.addKeymapManagerListener(this);
        }

        public void activeKeymapChanged(Keymap keyMap) {
            listenTo(keyMap);
            updateTree();
        }

        private void listenTo(Keymap keyMap) {
            if(myCurrentKeyMap != null) {
                myCurrentKeyMap.removeShortcutChangeListener(this);
            }
            myCurrentKeyMap = keyMap;
            if(myCurrentKeyMap != null) {
                myCurrentKeyMap.addShortcutChangeListener(this);
            }
        }

        private void updateTree() {
            myBuilder.updateFromRoot();
        }

        public void onShortcutChanged(String actionId) {
            updateTree();
        }

        public void stopListen() {
            listenTo(null);
            KeymapManagerEx.getInstanceEx().removeKeymapManagerListener(this);
        }
    }

    private static final class NodeRenderer extends ColoredTreeCellRenderer {
        public void customizeCellRenderer(JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
            final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if(userObject instanceof BaseNodeDescriptor) {
                final BaseNodeDescriptor descriptor = (BaseNodeDescriptor) userObject;
                descriptor.customize(this);
            } else {
                append(tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
    }
}
