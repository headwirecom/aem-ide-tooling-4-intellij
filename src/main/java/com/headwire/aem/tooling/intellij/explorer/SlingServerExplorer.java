package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.communication.ContentResourceChangeListener;
import com.headwire.aem.tooling.intellij.ui.BuildSelectionDialog;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.ui.ServerConfigurationDialog;
import com.intellij.execution.ExecutionAdapter;
import com.intellij.execution.ExecutionListener;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManagerAdapter;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
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
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManagerListener;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
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
import org.apache.sling.ide.osgi.OsgiClient;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

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
//    private ServerExplorerTreeBuilder myBuilder;
    private Tree myTree;
    private ServerConnectionManager serverConnectionManager;
    private ServerTreeSelectionHandler selectionHandler;
    private ServerConfigurationManager myConfig;
    private RunManagerEx myRunManager;
    private MessageBusConnection myConn = null;
    private MessageManager messageManager;

    public SlingServerExplorer(final Project project) {
        super(true, true);
        setTransferHandler(new MyTransferHandler());
        myProject = project;
//        myConfig = ServerConfigurationManager.getInstance(project);
        myConfig = ServiceManager.getService(project, ServerConfigurationManager.class);

        ServerTreeManager serverTreeManager = ServiceManager.getService(project, ServerTreeManager.class);
        if(serverTreeManager == null) {
            messageManager.showAlert("Failure", "Failure to find Server Tree Manager");
        }
        myTree = serverTreeManager.getTree();
//        final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
//        myTree = new Tree(model);
//        myTree.setRootVisible(true);
//        myTree.setShowsRootHandles(true);
//        myTree.setCellRenderer(new NodeRenderer());
//        selectionHandler = new ServerTreeSelectionHandler(myTree);
//        serverConnectionManager = new ServerConnectionManager(project, selectionHandler);
//        myBuilder = new ServerExplorerTreeBuilder(project, myTree, model, this);
//        TreeUtil.installActions(myTree);
//        new TreeSpeedSearch(myTree);
//        myTree.addMouseListener(new PopupHandler() {
//            public void invokePopup(final Component comp, final int x, final int y) {
//                popupInvoked(comp, x, y);
//            }
//        });
//        new DoubleClickListener() {
//            @Override
//            protected boolean onDoubleClick(MouseEvent e) {
//                final int eventY = e.getY();
//                final int row = myTree.getClosestRowForLocation(e.getX(), eventY);
//                if(row >= 0) {
//                    final Rectangle bounds = myTree.getRowBounds(row);
//                    if(bounds != null && eventY > bounds.getY() && eventY < bounds.getY() + bounds.getHeight()) {
//                        runSelection(DataManager.getInstance().getDataContext(myTree));
//                        return true;
//                    }
//                }
//                return false;
//            }
//        }.installOn(myTree);
//
//        myTree.registerKeyboardAction(new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                runSelection(DataManager.getInstance().getDataContext(myTree));
//            }
//        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), WHEN_FOCUSED);
//        myTree.setLineStyleAngled();
////        myAntBuildFilePropertiesAction = new AntBuildFilePropertiesAction(this);
        setToolbar(createToolbarPanel());
        setContent(ScrollPaneFactory.createScrollPane(myTree));
        ToolTipManager.sharedInstance().registerComponent(myTree);
        final MessageBus bus = myProject.getMessageBus();
        myConn = bus.connect();
        serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        selectionHandler = ServiceManager.getService(project, ServerTreeSelectionHandler.class);
        new ContentResourceChangeListener(myProject, serverConnectionManager, myConn);

        myRunManager = RunManagerEx.getInstanceEx(myProject);
        messageManager = ServiceManager.getService(myProject, MessageManager.class);

        // Hook up to the Bus and Register an Execution Listener in order to know when Debug Connection is established
        // and when it is taken down even when not started or stopped through the Plugin
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
        myTree.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent containerEvent) {
                messageManager.sendDebugNotification("Container Event: " + containerEvent);
            }

            @Override
            public void componentRemoved(ContainerEvent containerEvent) {
                messageManager.sendDebugNotification("Container Event: " + containerEvent);
            }
        });
    }

//    public ServerConnectionManager getServerConnectionManager() {
//        return serverConnectionManager;
//    }
//
//    public ServerTreeSelectionHandler getSelectionHandler() {
//        return selectionHandler;
//    }

    public void dispose() {
        if(myConn != null) {
            Disposer.dispose(myConn);
        }
        myConn = null;
        myProject = null;
//        myConfig = null;
    }

    private JPanel createToolbarPanel() {
//        final DefaultActionGroup group = new DefaultActionGroup();
//        group.add(new AddAction());
//        group.add(new RemoveAction());
//        group.add(new EditAction());
//        group.add(new CheckAction());
//        group.add(new DebugAction());
//        group.add(new StopAction());
//        group.add(new DeployAction());
//        group.add(new ForceDeployAction());
//        group.add(new BuildConfigureAction());
//        AnAction action = CommonActionsManager.getInstance().createExpandAllAction(myTreeExpander, this);
//        action.getTemplatePresentation().setDescription(AEMBundle.message("eam.explorer.expand.all.nodes.action.description"));
//        group.add(action);
//        action = CommonActionsManager.getInstance().createCollapseAllAction(myTreeExpander, this);
//        action.getTemplatePresentation().setDescription(AEMBundle.message("aem.explorer.collapse.all.nodes.action.description"));
//        group.add(action);

        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(actionManager.getAction("AEM.Toolbar"));

        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.ANT_EXPLORER_TOOLBAR, group, true);

        final JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
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

//    private void popupInvoked(final Component comp, final int x, final int y) {
//        Object userObject = null;
//        final TreePath path = myTree.getSelectionPath();
//        if(path != null) {
//            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
//            if(node != null) {
//                userObject = node.getUserObject();
//            }
//        }
//        final DefaultActionGroup group = new DefaultActionGroup();
//        if(
//            userObject instanceof SlingServerNodeDescriptor ||
//            userObject instanceof SlingServerModuleNodeDescriptor
//        ) {
//            group.add(new RemoveAction());
//            group.add(new EditAction());
//            group.add(new CheckAction());
//            group.add(new DebugAction());
//            group.add(new StopAction());
//            group.add(new DeployAction());
//            group.add(new ForceDeployAction());
//            group.add(new BuildConfigureAction());
//        } else {
//            group.add(new AddAction());
//        }
//        final ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.ANT_EXPLORER_POPUP, group);
//        popupMenu.getComponent().show(comp, x, y);
//    }

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
            String test = "";
//            return myProject != null ? myTreeExpander : null;
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
            event.getPresentation().setEnabled(serverConnectionManager.isConnectionIsStoppable());
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
            event.getPresentation().setEnabled(serverConnectionManager.isConfigurationSelected());
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            final DataContext dataContext = e.getDataContext();
            doDeploy(dataContext, false);
        }
    }

    private final class ForceDeployAction extends AnAction {
        public ForceDeployAction() {
            super(
                AEMBundle.message("force.deploy.configuration.action.name"),
                AEMBundle.message("force.deploy.configuration.action.description"),
                AllIcons.Actions.ForceRefresh
            );
        }

        @Override
        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(serverConnectionManager.isConfigurationSelected());
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            final DataContext dataContext = e.getDataContext();
            doDeploy(dataContext, true);
        }
    }

    private void doDeploy(final DataContext dataContext, final boolean forceDeploy) {
        final String title = AEMBundle.message("deploy.configuration.action.name");

        ProgressManager.getInstance().run(
            new Task.Modal(myProject, title, false) {
                @Nullable
                public NotificationInfo getNotificationInfo() {
                    return new NotificationInfo("Sling", "Sling Deployment Checks", "");
                }

                public void run(@NotNull final ProgressIndicator indicator) {
                    //AS TODO: Check if there is a new version of IntelliJ CE that would allow to use
                    //AS TODO: the ProgressAdapter.
                    //AS TODO: Or create another Interface / Wrapper to make it IDE independent
                    indicator.setIndeterminate(false);
                    indicator.pushState();
                    ApplicationManager.getApplication().runReadAction(
                        new Runnable() {
                            public void run() {
                                try {
                                    final String description = AEMBundle.message("deploy.configuration.action.description");

                                    indicator.setText(description);
                                    indicator.setFraction(0.0);

                                    // There is no Run Connection to be made to the AEM Server like with DEBUG (no HotSwap etc).
                                    // So we just need to setup a connection to the AEM Server to handle OSGi Bundles and Sling Packages
                                    ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                                    indicator.setFraction(0.1);
                                    //AS TODO: this is not showing if the check is short but if it takes longer it will update
                                    serverConnectionManager.updateStatus(serverConfiguration, ServerConfiguration.SynchronizationStatus.updating);
                                    indicator.setFraction(0.2);
                                    try {
                                        Thread.sleep(1000);
                                    } catch(InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                    indicator.setFraction(0.3);
                                    // First Check if the Install Support Bundle is installed
                                    ServerConnectionManager.BundleStatus bundleStatus = serverConnectionManager.checkAndUpdateSupportBundle(true);
                                    indicator.setFraction(0.5);
                                    ServerConfiguration.Module module = selectionHandler.getCurrentModuleConfiguration();
                                    if(module != null) {
                                        // Deploy only the selected Module
                                        serverConnectionManager.deployModule(module, forceDeploy);
                                    } else {
                                        // Deploy all Modules of the Project
                                        serverConnectionManager.deployModules(dataContext, forceDeploy);
                                    }
                                    indicator.setFraction(1.0);
                                } finally {
                                    indicator.popState();
                                }
                            }
                        }
                    );
                }
            }
        );
    }

    private final class BuildConfigureAction extends AnAction {
        public BuildConfigureAction() {
            super(
                AEMBundle.message("build.configuration.action.name"),
                AEMBundle.message("build.configuration.action.description"),
                AllIcons.Actions.Module
            );
        }

        public void actionPerformed(AnActionEvent e) {
            ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
            BuildSelectionDialog dialog = new BuildSelectionDialog(myProject, serverConfiguration);
            if(dialog.showAndGet()) {
                // Modules might have changed and so update the tree
                myConfig.updateServerConfiguration(serverConfiguration);
            }
        }

        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(serverConnectionManager.isConfigurationSelected());
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
