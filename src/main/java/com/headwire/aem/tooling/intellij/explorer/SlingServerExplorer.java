package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.communication.ContentResourceChangeListener;
import com.headwire.aem.tooling.intellij.util.BundleStateHelper;
import com.headwire.aem.tooling.intellij.util.OSGiFactory;
import com.headwire.aem.tooling.intellij.eclipse.ServerUtil;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.ui.ServerConfigurationDialog;
import com.intellij.execution.ExecutionAdapter;
import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.KillableProcess;
import com.intellij.execution.RunManagerAdapter;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DataManager;
import com.intellij.ide.TreeExpander;
import com.intellij.ide.dnd.FileCopyPasteUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManagerListener;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IconUtil;
import com.intellij.util.containers.*;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.xml.DomEventListener;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.events.DomEvent;
import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.artifacts.EmbeddedArtifact;
import org.apache.sling.ide.artifacts.EmbeddedArtifactLocator;
import org.apache.sling.ide.osgi.OsgiClient;
import org.apache.sling.ide.osgi.OsgiClientException;
import org.apache.sling.ide.transport.Command;
import org.apache.sling.ide.transport.Repository;
import org.apache.sling.ide.transport.RepositoryInfo;
import org.apache.sling.ide.transport.ResourceProxy;
import org.apache.sling.ide.transport.Result;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.Version;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by schaefa on 3/19/15.
 */
public class SlingServerExplorer
    extends SimpleToolWindowPanel
    implements DataProvider, Disposable
{
    private static final Logger LOGGER = Logger.getInstance(SlingServerExplorer.class);
    public static final Topic<ExecutionListener> EXECUTION_TOPIC =
        Topic.create("AEM configuration executed", ExecutionListener.class, Topic.BroadcastDirection.TO_PARENT);
    public static final String TOOL_WINDOW_ID = "AEM";
//AS TODO: Moved to ServerConnectionManager -> remove
//    private static List<ServerConfiguration.ServerStatus> CONFIGURATION_IN_USE = Arrays.asList(
//        ServerConfiguration.ServerStatus.connecting,
//        ServerConfiguration.ServerStatus.connected,
//        ServerConfiguration.ServerStatus.disconnecting
//    );

    public static final String ROOT_FOLDER = "/jcr_root/";

    private Project myProject;
    private ServerExplorerTreeBuilder myBuilder;
    private Tree myTree;
    private ServerConnectionManager serverConnectionManager;
    private ServerTreeSelectionHandler selectionHandler;
    private KeyMapListener myKeyMapListener;
    private ServerConfigurationManager myConfig;
    private RunManagerEx myRunManager;
    private MessageBusConnection myConn = null;
    private MessageManager messageManager;

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

    public SlingServerExplorer(final Project project) {
        super(true, true);
        setTransferHandler(new MyTransferHandler());
        myProject = project;
        myConfig = ServerConfigurationManager.getInstance(project);
        final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
        myTree = new Tree(model);
        myTree.setRootVisible(true);
        myTree.setShowsRootHandles(true);
        myTree.setCellRenderer(new NodeRenderer());
        selectionHandler = new ServerTreeSelectionHandler(myTree);
        serverConnectionManager = new ServerConnectionManager(project, selectionHandler);
        myBuilder = new ServerExplorerTreeBuilder(project, myTree, model);
        TreeUtil.installActions(myTree);
        new TreeSpeedSearch(myTree);
        myTree.addMouseListener(new PopupHandler() {
            public void invokePopup(final Component comp, final int x, final int y) {
                popupInvoked(comp, x, y);
            }
        });
        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent e) {
                final int eventY = e.getY();
                final int row = myTree.getClosestRowForLocation(e.getX(), eventY);
                if(row >= 0) {
                    final Rectangle bounds = myTree.getRowBounds(row);
                    if(bounds != null && eventY > bounds.getY() && eventY < bounds.getY() + bounds.getHeight()) {
                        runSelection(DataManager.getInstance().getDataContext(myTree));
                        return true;
                    }
                }
                return false;
            }
        }.installOn(myTree);

        myTree.registerKeyboardAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                runSelection(DataManager.getInstance().getDataContext(myTree));
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), WHEN_FOCUSED);
        myTree.setLineStyleAngled();
//        myAntBuildFilePropertiesAction = new AntBuildFilePropertiesAction(this);
        setToolbar(createToolbarPanel());
        setContent(ScrollPaneFactory.createScrollPane(myTree));
        ToolTipManager.sharedInstance().registerComponent(myTree);
        myKeyMapListener = new KeyMapListener();
        new ContentResourceChangeListener(myProject, serverConnectionManager);

        DomManager.getDomManager(project).addDomEventListener(new DomEventListener() {
            public void eventOccured(DomEvent event) {
                myBuilder.queueUpdate();
            }
        }, this);
        myRunManager = RunManagerEx.getInstanceEx(myProject);
        myRunManager.addRunManagerListener(
            new RunManagerAdapter() {
                public void beforeRunTasksChanged() {
                    myBuilder.queueUpdate();
                }
            }
        );

        messageManager = MessageManager.getInstance(myProject);

        final MessageBus bus = myProject.getMessageBus();
        // Hook up to the Bus and Register an Execution Listener in order to know when Debug Connection is established
        // and when it is taken down even when not started or stopped through the Plugin
        myConn = bus.connect();
        myConn.subscribe(
            ExecutionManager.EXECUTION_TOPIC,
            new ExecutionAdapter() {
                @Override
                public void processNotStarted(String executorId, @NotNull ExecutionEnvironment env) {
                    // This is called when the Debug Session failed to start and / or connect
                    markConfigurationAsFailed(env.getRunProfile().getName());
                }

                @Override
                public void processStarted(String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
                    // This is called when the Debug Session successfully started and connected
                    markConfigurationAsStarted(env.getRunProfile().getName());
                }

                @Override
                public void processTerminated(@NotNull RunProfile runProfile, @NotNull ProcessHandler handler) {
                    // Called when a successful connected session is stopped
                    markConfigurationAsTerminated(runProfile.getName());
                }
            }
        );
    }

    public void dispose() {
        final KeyMapListener listener = myKeyMapListener;
        if(listener != null) {
            myKeyMapListener = null;
            listener.stopListen();
        }

        final ServerExplorerTreeBuilder builder = myBuilder;
        if(builder != null) {
            Disposer.dispose(builder);
            myBuilder = null;
        }

        final Tree tree = myTree;
        if(tree != null) {
            ToolTipManager.sharedInstance().unregisterComponent(tree);
            for(KeyStroke keyStroke : tree.getRegisteredKeyStrokes()) {
                tree.unregisterKeyboardAction(keyStroke);
            }
            myTree = null;
        }

        if(myConn != null) {
            Disposer.dispose(myConn);
        }
        myConn = null;
        myProject = null;
//        myConfig = null;
    }

    private JPanel createToolbarPanel() {
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AddAction());
        group.add(new RemoveAction());
        group.add(new EditAction());
        group.add(new CheckAction());
        group.add(new DebugAction());
        group.add(new StopAction());
        group.add(new DeployAction());
        AnAction action = CommonActionsManager.getInstance().createExpandAllAction(myTreeExpander, this);
        action.getTemplatePresentation().setDescription(AEMBundle.message("eam.explorer.expand.all.nodes.action.description"));
        group.add(action);
        action = CommonActionsManager.getInstance().createCollapseAllAction(myTreeExpander, this);
        action.getTemplatePresentation().setDescription(AEMBundle.message("aem.explorer.collapse.all.nodes.action.description"));
        group.add(action);

        final ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.ANT_EXPLORER_TOOLBAR, group, true);
        final JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.add(actionToolBar.getComponent(), BorderLayout.CENTER);
        return buttonsPanel;
    }

    private void markConfigurationAsFailed(String configurationName) {
        ServerConfiguration configuration = myConfig.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setServerStatus(ServerConfiguration.ServerStatus.failed);
            myConfig.updateServerConfiguration(configuration);
            //AS TODO: Update Bundle Status
            // Mark any Bundles inside the Tree as unknown
        }
    }

    private void markConfigurationAsStarted(String configurationName) {
        ServerConfiguration configuration = myConfig.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setServerStatus(ServerConfiguration.ServerStatus.connected);
            myConfig.updateServerConfiguration(configuration);
            //AS TODO: Update Bundle Status
            // Now obtain the Status of the Bundles inside AEM and add as entries to the Tree underneath the Configuration Entry
        }
    }

    private void markConfigurationAsTerminated(String configurationName) {
        ServerConfiguration configuration = myConfig.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setServerStatus(ServerConfiguration.ServerStatus.disconnected);
            myConfig.updateServerConfiguration(configuration);
            //AS TODO: Update Bundle Status
            // Mark any Bundles inside the Tree as disconnected
        }
    }

    private void runSelection(final DataContext dataContext) {
        if(!canRunSelection()) {
            return;
        }
    }

    private boolean canRunSelection() {
        if(myTree == null) {
            return false;
        }
        final TreePath[] paths = myTree.getSelectionPaths();
        if(paths == null) {
            return false;
        }
        return true;
    }

    private static String[] getTargetNamesFromPaths(TreePath[] paths) {
        final java.util.List<String> targets = new ArrayList<String>();
        for(final TreePath path : paths) {
            final Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
//            if (!(userObject instanceof AntTargetNodeDescriptor)) {
//                continue;
//            }
//            final AntBuildTarget target = ((AntTargetNodeDescriptor)userObject).getTarget();
//            if (target instanceof MetaTarget) {
//                ContainerUtil.addAll(targets, ((MetaTarget) target).getTargetNames());
//            }
//            else {
//                targets.add(target.getName());
//            }
        }
        return ArrayUtil.toStringArray(targets);
    }

//AS TODO: Moved to ServerConnectionManager -> remove
//    public boolean isConfigurationSelected() {
//        boolean ret = false;
//        if(myProject != null) {
//            ret = selectionHandler.getCurrentConfiguration() != null;
//        }
//        return ret;
//    }

    private void popupInvoked(final Component comp, final int x, final int y) {
        Object userObject = null;
        final TreePath path = myTree.getSelectionPath();
        if(path != null) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if(node != null) {
                userObject = node.getUserObject();
            }
        }
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new CheckAction());
        group.add(new DebugAction());
        group.add(new DeployAction());
        group.add(ActionManager.getInstance().getAction(IdeActions.ACTION_EDIT_SOURCE));
//        if (userObject instanceof AntBuildFileNodeDescriptor) {
//            group.add(new RemoveBuildFileAction(this));
//        }
//        if (userObject instanceof AntTargetNodeDescriptor) {
//            final AntBuildTargetBase target = ((AntTargetNodeDescriptor)userObject).getTarget();
//            final DefaultActionGroup executeOnGroup =
//                    new DefaultActionGroup(AntBundle.message("ant.explorer.execute.on.action.group.name"), true);
//            executeOnGroup.add(new ExecuteOnEventAction(target, ExecuteBeforeCompilationEvent.getInstance()));
//            executeOnGroup.add(new ExecuteOnEventAction(target, ExecuteAfterCompilationEvent.getInstance()));
//            executeOnGroup.addSeparator();
//            executeOnGroup.add(new ExecuteBeforeRunAction(target));
//            group.add(executeOnGroup);
//            group.add(new AssignShortcutAction(target.getActionId()));
//        }
//        group.add(myAntBuildFilePropertiesAction);
        final ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.ANT_EXPLORER_POPUP, group);
        popupMenu.getComponent().show(comp, x, y);
    }

    @Nullable
    public Object getData(@NonNls String dataId) {
        if(CommonDataKeys.NAVIGATABLE.is(dataId)) {
//            final AntBuildFile buildFile = getCurrentBuildFile();
//            if (buildFile == null) {
//                return null;
//            }
//            final VirtualFile file = buildFile.getVirtualFile();
//            if (file == null) {
//                return null;
//            }
            final TreePath treePath = myTree.getLeadSelectionPath();
            if(treePath == null) {
                return null;
            }
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
            if(node == null) {
                return null;
            }
//            if (node.getUserObject() instanceof AntTargetNodeDescriptor) {
//                final AntTargetNodeDescriptor targetNodeDescriptor = (AntTargetNodeDescriptor)node.getUserObject();
//                final AntBuildTargetBase buildTarget = targetNodeDescriptor.getTarget();
//                final OpenFileDescriptor descriptor = buildTarget.getOpenFileDescriptor();
//                if (descriptor != null) {
//                    final VirtualFile descriptorFile = descriptor.getFile();
//                    if (descriptorFile.isValid()) {
//                        return descriptor;
//                    }
//                }
//            }
//            if (file.isValid()) {
//                return new OpenFileDescriptor(myProject, file);
//            }
        } else if(PlatformDataKeys.HELP_ID.is(dataId)) {
//            return HelpID.ANT;
            return null;
        } else if(PlatformDataKeys.TREE_EXPANDER.is(dataId)) {
            return myProject != null ? myTreeExpander : null;
        } else if(CommonDataKeys.VIRTUAL_FILE_ARRAY.is(dataId)) {
//            final java.util.List<VirtualFile> virtualFiles = collectAntFiles(new Function<AntBuildFile, VirtualFile>() {
//                @Override
//                public VirtualFile fun(AntBuildFile buildFile) {
//                    final VirtualFile virtualFile = buildFile.getVirtualFile();
//                    if (virtualFile != null && virtualFile.isValid()) {
//                        return virtualFile;
//                    }
//                    return null;
//                }
//            });
//            return virtualFiles == null ? null : virtualFiles.toArray(new VirtualFile[virtualFiles.size()]);
            return null;
        } else if(LangDataKeys.PSI_ELEMENT_ARRAY.is(dataId)) {
//            final java.util.List<PsiElement> elements = collectAntFiles(new Function<AntBuildFile, PsiElement>() {
//                @Override
//                public PsiElement fun(AntBuildFile buildFile) {
//                    return buildFile.getAntFile();
//                }
//            });
//            return elements == null ? null : elements.toArray(new PsiElement[elements.size()]);
            return null;
        }
        return super.getData(dataId);
    }

    public static FileChooserDescriptor createXmlDescriptor() {
        return new FileChooserDescriptor(true, false, false, false, false, true) {
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                boolean b = super.isFileVisible(file, showHiddenFiles);
                if(!file.isDirectory()) {
                    b &= StdFileTypes.XML.equals(file.getFileType());
                }
                return b;
            }
        };
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
            LOGGER.debug("Node Renderer: user object: " + userObject);
            if(userObject instanceof ServerNodeDescriptor) {
                final ServerNodeDescriptor descriptor = (ServerNodeDescriptor) userObject;
                descriptor.customize(this);
            } else {
                append(tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
    }

    private final class AddAction extends AnAction {
        public AddAction() {
            super(
                AEMBundle.message("add.configuration.action.name"),
                AEMBundle.message("add.configuration.action.description"),
                IconUtil.getAddIcon()
            );
        }

        public void actionPerformed(AnActionEvent e) {
            editServerConfiguration(e.getProject(), null);
        }
    }

    private final class RemoveAction extends AnAction {
        public RemoveAction() {
            super(
                AEMBundle.message("remove.configuration.action.name"),
                AEMBundle.message("remove.configuration.action.description"),
                IconUtil.getRemoveIcon()
            );
        }

        public void actionPerformed(AnActionEvent e) {
            ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
            myConfig.removeServerConfiguration(serverConfiguration);
//            myConfig.getServerConfigurationList().remove(serverConfiguration);
//            myTree.repaint();
        }

        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(serverConnectionManager.isConnectionNotInUse());
        }
    }

    private final class EditAction extends AnAction {
        public EditAction() {
            super(
                AEMBundle.message("edit.configuration.action.name"),
                AEMBundle.message("edit.configuration.action.description"),
                AllIcons.Actions.EditSource
            );
        }

        public void actionPerformed(AnActionEvent e) {
            ServerConfiguration source = selectionHandler.getCurrentConfiguration();
            editServerConfiguration(e.getProject(), source);
        }

        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(serverConnectionManager.isConnectionNotInUse());
        }
    }

    /**
     * Adds or Edits a Server Configuration and makes sure the configuration is valid
     *
     * @param project The Current Project
     * @param source  The Server Configuration that needs to be edited. Null if a new one should be created.
     */
    private void editServerConfiguration(@NotNull Project project, @Nullable ServerConfiguration source) {
        boolean isOk;
        do {
            isOk = true;
            ServerConfigurationDialog dialog = new ServerConfigurationDialog(project, source);
            if(dialog.showAndGet()) {
                // Check if there is not a name collision due to changed name
                ServerConfiguration target = dialog.getConfiguration();
                if(source != null && !source.getName().equals(target.getName())) {
                    // Name has changed => check for name collisions
                    ServerConfiguration other = myConfig.findServerConfigurationByName(target.getName());
                    if(other != null) {
                        // Collision found -> alert and retry
                        isOk = false;
                        messageManager.sendErrorNotification("aem.explorer.cannot.change.configuration", target.getName());
                    }
                } else {
                    // Verity Content
                    String message = target.verify();
                    if(message != null) {
                        isOk = false;
                        messageManager.sendErrorNotification("aem.explorer.server.configuration.invalid", AEMBundle.message(message));
                    }
                }
                if(isOk) {
                    if(source != null) {
                        myConfig.updateServerConfiguration(source, target);
                    } else {
                        myConfig.addServerConfiguration(target);
                    }
                }
            }
        } while(!isOk);
    }

//    private boolean isConfigurationInUse(ServerConfiguration serverConfiguration) {
//        return serverConfiguration != null && CONFIGURATION_IN_USE.contains(serverConfiguration.getServerStatus());
//    }

    private final class CheckAction extends AnAction {
        public CheckAction() {
            super(
                AEMBundle.message("check.configuration.action.name"),
                AEMBundle.message("check.configuration.action.description"),
                AllIcons.Actions.Execute
            );
        }

        public void actionPerformed(AnActionEvent e) {
            // There is no Run Connection to be made to the AEM Server like with DEBUG (no HotSwap etc).
            // So we just need to setup a connection to the AEM Server to handle OSGi Bundles and Sling Packages
            ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
            OsgiClient osgiClient = serverConnectionManager.obtainSGiClient();
            if(osgiClient != null) {
                ServerConnectionManager.BundleStatus status = serverConnectionManager.checkAndUpdateSupportBundle(false);
                if(status != ServerConnectionManager.BundleStatus.failed) {
                    serverConnectionManager.checkModules(osgiClient);
                }
            }
        }

        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(serverConnectionManager.isConfigurationSelected());
        }
    }

    private final class DebugAction extends AnAction {
        public DebugAction() {
            super(
                AEMBundle.message("debug.configuration.action.name"),
                AEMBundle.message("debug.configuration.action.description"),
                AllIcons.General.Debug
            );
        }

        public void actionPerformed(AnActionEvent e) {
            serverConnectionManager.connectInDebugMode(myRunManager);
        }

        public void update(AnActionEvent event) {
            //AS TODO: Disabled this when a session is started and (re)enable it when it is stopped
            event.getPresentation().setEnabled(serverConnectionManager.isConnectionNotInUse());
        }
    }

    private final class StopAction extends AnAction {
        public StopAction() {
            super(
                AEMBundle.message("stop.configuration.action.name"),
                AEMBundle.message("stop.configuration.action.description"),
                AllIcons.Process.Stop
            );
        }

        public void actionPerformed(AnActionEvent e) {
            // This code was taken from the 'com.intellij.execution.actions.StopAction' which is the Stop Action of
            // Debug Stop Button but it was simplified because we have a connection or not and no UI.
            final DataContext dataContext = e.getDataContext();
            serverConnectionManager.stopDebugConnection(dataContext);
        }

        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(serverConnectionManager.isConnectionInUse());
        }
    }

    private final class DeployAction extends AnAction {
        public DeployAction() {
            super(
                AEMBundle.message("deploy.configuration.action.name"),
                AEMBundle.message("deploy.configuration.action.description"),
                AllIcons.Actions.Export
            );
        }

        @Override
        public void update(AnActionEvent event) {
//            event.getPresentation().setEnabled(isConfigurationSelected() && isConfigurationInUse(getCurrentConfiguration()));
            event.getPresentation().setEnabled(serverConnectionManager.isConnectionInUse());
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            // First Check if the Install Support Bundle is installed
            ServerConnectionManager.BundleStatus bundleStatus = serverConnectionManager.checkAndUpdateSupportBundle(true);
//            if(bundleStatus == ServerConnectionManager.BundleStatus.upToDate) {
                // Deploy all selected Modules
                serverConnectionManager.deployModules();
//            }
        }
    }

//    private void setTargetsFiltered(boolean value) {
//        myBuilder.setTargetsFiltered(value);
////        AntConfigurationBase.getInstance(myProject).setFilterTargets(value);
//    }

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

    //AS TODO: Do we really need this (transfer of what)
    private final class MyTransferHandler extends TransferHandler {

        @Override
        public boolean importData(final TransferSupport support) {
            if(canImport(support)) {
//                addBuildFile(getAntFiles(support));
                return true;
            }
            return false;
        }

        @Override
        public boolean canImport(final TransferSupport support) {
            return FileCopyPasteUtil.isFileListFlavorAvailable(support.getDataFlavors());
        }

        private VirtualFile[] getAntFiles(final TransferSupport support) {
            java.util.List<VirtualFile> virtualFileList = new ArrayList<VirtualFile>();
            final java.util.List<File> fileList = FileCopyPasteUtil.getFileList(support.getTransferable());
            if(fileList != null) {
                for(File file : fileList) {
                    ContainerUtil.addIfNotNull(virtualFileList, VfsUtil.findFileByIoFile(file, true));
                }
            }

            return VfsUtil.toVirtualFileArray(virtualFileList);
        }
    }
}
