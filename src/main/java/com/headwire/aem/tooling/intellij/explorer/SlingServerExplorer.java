package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
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
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManagerListener;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.keymap.impl.ui.EditKeymapsDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
//import org.apache.sling.ide.impl.vlt.Activator;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.eclipse.core.runtime.MyBundleContext;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
//import java.awt.*;
import java.awt.*;
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
    private static List<ServerConfiguration.ServerStatus> CONFIGURATION_IN_USE = Arrays.asList(
        ServerConfiguration.ServerStatus.connecting,
        ServerConfiguration.ServerStatus.connected,
        ServerConfiguration.ServerStatus.disconnecting
    );

    public static final String ROOT_FOLDER = "/jcr_root/";

    private Project myProject;
    private ServerExplorerTreeBuilder myBuilder;
    private Tree myTree;
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
        new ContentResourceChangeListener(myProject, selectionHandler);

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

//        // Create FileVault Repository Access
//        LOGGER.debug("Before Create Repository Info");
//        RepositoryInfo repositoryInfo = new RepositoryInfo("admin", "admin", "http://localhost:4502/");
//        LOGGER.debug("After Create Repository Info: " + repositoryInfo);
//        RepositoryFactory factory = new VltRepositoryFactory();
//        LOGGER.debug("After Creating Repository Factory: " + factory);
//        try {
//            repository = factory.connectRepository(repositoryInfo);
//            LOGGER.debug("After Creating Repository: " + repository);
//        } catch (RepositoryException e) {
//            LOGGER.error("Failed to connect to VLT Repository", e);
//        }
//
//        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
//            @Override
//            public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
//                LOGGER.debug("VFS Property Changed Event: " + event.getFileName());
//            }
//            @Override
//            public void contentsChanged(@NotNull VirtualFileEvent event) {
//                String fileName = event.getFileName();
//                LOGGER.debug("VFS Content Changed Event: " + fileName);
//                String filePath = event.getFile().getPath();
//                int index = filePath.indexOf(ROOT_FOLDER);
//                if(index > 0) {
//                    String jcrPath = filePath.substring(index + ROOT_FOLDER.length() - 1);
//                    LOGGER.debug("Supported JCR Path: " + jcrPath);
//                    if(repository != null) {
//                        ResourceProxy resource = new ResourceProxy(jcrPath);
//                        resource.addProperty("jcr:primaryType", "nt:unstructured");
//                        FileInfo info = new FileInfo(
//                            filePath, jcrPath, fileName
//                        );
//                        LOGGER.debug("Before Create Command");
//                        Command<Void> cmd = repository.newAddOrUpdateNodeCommand(info, resource);
//                        LOGGER.debug("Before Execute Create Command: " + cmd);
//                        cmd.execute();
//                        LOGGER.debug("After Execute Create Command: " + cmd);
//                    }
//                }
//            }
//            @Override
//            public void fileCreated(@NotNull VirtualFileEvent event) {
//                LOGGER.debug("VFS File Created Event: " + event.getFileName());
//            }
//            @Override
//            public void fileDeleted(@NotNull VirtualFileEvent event) {
//                LOGGER.debug("VFS File Deleted Event: " + event.getFileName());
//            }
//            @Override
//            public void fileMoved(@NotNull VirtualFileMoveEvent event) {
//                LOGGER.debug("VFS File Moved Event: " + event.getFileName());
//            }
//            @Override
//            public void fileCopied(@NotNull VirtualFileCopyEvent event) {
//                LOGGER.debug("VFS File Copied Event: " + event.getFileName());
//            }
//        }, project);

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


//        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
//        ToolWindow toolWindow = toolWindowManager.getToolWindow(SlingServerReportView.TOOL_WINDOW_ID);
//        if(toolWindow == null) {
//            toolWindow = toolWindowManager.registerToolWindow(SlingServerReportView.TOOL_WINDOW_ID, true, ToolWindowAnchor.BOTTOM, myProject, true);
//AS TODO: Provide Tool Window Icon
//            toolWindow.setIcon(descriptor.getFramework().getToolWindowIcon());
//AS TODO: Not sure if that is needed as we use the initToolWindow() method below
//            descriptor.createToolWindowContent(myProject, toolWindow);
//            ServiceManager.getService(project, SlingServerReportView.class).initToolWindow(toolWindow, myProject);
//            ConsoleLog.getProjectComponent(myProject).initDefaultContent();
//            ConsoleLog.ProjectTracker projectTracker = new ConsoleLog.ProjectTracker(myProject);
//        }

////        //AS TODO: This is to stub the Activator to make it work without Eclipse and OSGi
//        org.apache.sling.ide.impl.vlt.Activator activator = new org.apache.sling.ide.impl.vlt.Activator();
//
//        // Check if that works with just a null bundle context
//        BundleContext bundleContext = new MyBundleContext();
//        try {
//            activator.start(bundleContext);
//        } catch(Exception e) {
//            //AS TODO: Create a proper message
//            messageManager.sendUnexpectedException(e);
//        }
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

    private void markConfigurationAsSynchronized(String configurationName) {
        ServerConfiguration configuration = myConfig.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setServerStatus(ServerConfiguration.ServerStatus.upToDate);
            myConfig.updateServerConfiguration(configuration);
            //AS TODO: Update Bundle Status
            // Mark any Bundles inside the Tree as disconnected
        }
    }

    private void markConfigurationAsOutDated(String configurationName) {
        ServerConfiguration configuration = myConfig.findServerConfigurationByName(configurationName);
        if(configuration != null) {
            configuration.setServerStatus(ServerConfiguration.ServerStatus.outdated);
            myConfig.updateServerConfiguration(configuration);
            //AS TODO: Update Bundle Status
            // Mark any Bundles inside the Tree as disconnected
        }
    }

//    private void addServerConfiguration() {
//
////        final FileChooserDescriptor descriptor = createXmlDescriptor();
//////        descriptor.setTitle(AntBundle.message("select.ant.build.file.dialog.title"));
//////        descriptor.setDescription(AntBundle.message("select.ant.build.file.dialog.description"));
////        final VirtualFile[] files = FileChooser.chooseFiles(descriptor, myProject, null);
////        addBuildFile(files);
//    }

//    private void addBuildFile(final VirtualFile[] files) {
//        if (files.length == 0) {
//            return;
//        }
//        ApplicationManager.getApplication().invokeLater(new Runnable() {
//            public void run() {
////                final AntConfiguration antConfiguration = myConfig;
////                if (antConfiguration == null) {
////                    return;
////                }
////                final java.util.List<VirtualFile> ignoredFiles = new ArrayList<VirtualFile>();
////                for (VirtualFile file : files) {
////                    try {
////                        antConfiguration.addBuildFile(file);
////                    }
////                    catch (AntNoFileException e) {
////                        ignoredFiles.add(e.getFile());
////                    }
////                }
////                if (ignoredFiles.size() != 0) {
////                    String messageText;
////                    final StringBuilder message = StringBuilderSpinAllocator.alloc();
////                    try {
////                        String separator = "";
////                        for (final VirtualFile virtualFile : ignoredFiles) {
////                            message.append(separator);
////                            message.append(virtualFile.getPresentableUrl());
////                            separator = "\n";
////                        }
////                        messageText = message.toString();
////                    }
////                    finally {
////                        StringBuilderSpinAllocator.dispose(message);
////                    }
////                    Messages.showWarningDialog(myProject, messageText, AntBundle.message("cannot.add.ant.files.dialog.title"));
////                }
//            }
//        });
//    }

//    public void removeBuildFile() {
////        final AntBuildFile buildFile = getCurrentBuildFile();
////        if (buildFile == null) {
////            return;
////        }
////        final String fileName = buildFile.getPresentableUrl();
////        final int result = Messages.showYesNoDialog(myProject, AntBundle.message("remove.the.reference.to.file.confirmation.text", fileName),
////                AntBundle.message("confirm.remove.dialog.title"), Messages.getQuestionIcon());
////        if (result != Messages.YES) {
////            return;
////        }
////        myConfig.removeBuildFile(buildFile);
//    }

//    public void setBuildFileProperties() {
////        final AntBuildFileBase buildFile = getCurrentBuildFile();
////        if (buildFile != null && BuildFilePropertiesPanel.editBuildFile(buildFile, myProject)) {
////            myConfig.updateBuildFile(buildFile);
////            myBuilder.queueUpdate();
////            myTree.repaint();
////        }
//    }

    private void runSelection(final DataContext dataContext) {
        if(!canRunSelection()) {
            return;
        }
//        final AntBuildFileBase buildFile = getCurrentBuildFile();
//        if (buildFile != null) {
//            final TreePath[] paths = myTree.getSelectionPaths();
//            final String[] targets = getTargetNamesFromPaths(paths);
//            ExecutionHandler.runBuild(buildFile, targets, null, dataContext, Collections.<BuildFileProperty>emptyList(), AntBuildListener.NULL);
//        }
    }

    private boolean canRunSelection() {
        if(myTree == null) {
            return false;
        }
        final TreePath[] paths = myTree.getSelectionPaths();
        if(paths == null) {
            return false;
        }
//        final AntBuildFile buildFile = getCurrentBuildFile();
//        if (buildFile == null || !buildFile.exists()) {
//            return false;
//        }
//        for (final TreePath path : paths) {
//            final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
//            final Object userObject = node.getUserObject();
//            final AntBuildFileNodeDescriptor buildFileNodeDescriptor;
//            if (userObject instanceof AntTargetNodeDescriptor) {
//                buildFileNodeDescriptor = (AntBuildFileNodeDescriptor)((DefaultMutableTreeNode)node.getParent()).getUserObject();
//            }
//            else if (userObject instanceof AntBuildFileNodeDescriptor){
//                buildFileNodeDescriptor = (AntBuildFileNodeDescriptor)userObject;
//            }
//            else {
//                buildFileNodeDescriptor = null;
//            }
//            if (buildFileNodeDescriptor == null || buildFileNodeDescriptor.getBuildFile() != buildFile) {
//                return false;
//            }
//        }
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

//    private static AntBuildTarget[] getTargetObjectsFromPaths(TreePath[] paths) {
//        final java.util.List<AntBuildTargetBase> targets = new ArrayList<AntBuildTargetBase>();
//        for (final TreePath path : paths) {
//            final Object userObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
//            if (!(userObject instanceof AntTargetNodeDescriptor)) {
//                continue;
//            }
//            final AntBuildTargetBase target = ((AntTargetNodeDescriptor)userObject).getTarget();
//            targets.add(target);
//
//        }
//        return targets.toArray(new AntBuildTargetBase[targets.size()]);
//    }

    public boolean isConfigurationSelected() {
        boolean ret = false;
        if(myProject != null) {
            ret = selectionHandler.getCurrentConfiguration() != null;
        }
        return ret;
    }

//    public boolean isConnectionEstablished() {
//        boolean ret = false;
//        if(myProject != null) {
////AS TODO: Add the check if there is a Connection Established
////            ret = getCurrentConfiguration() != null;
//        }
//        return ret;
//    }

//    public boolean isBuildFileSelected() {
////        if( myProject == null) return false;
////        final AntBuildFileBase file = getCurrentBuildFile();
////        return file != null && file.exists();
//        return false;
//    }

//    @Nullable
//    private AntBuildFileBase getCurrentBuildFile() {
//        final AntBuildFileNodeDescriptor descriptor = getCurrentBuildFileNodeDescriptor();
//        return (AntBuildFileBase)((descriptor == null) ? null : descriptor.getBuildFile());
//    }

//    @Nullable
//    private AntBuildFileNodeDescriptor getCurrentBuildFileNodeDescriptor() {
//        if (myTree == null) {
//            return null;
//        }
//        final TreePath path = myTree.getSelectionPath();
//        if (path == null) {
//            return null;
//        }
//        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
//        while (node != null) {
//            final Object userObject = node.getUserObject();
//            if (userObject instanceof AntBuildFileNodeDescriptor) {
//                return (AntBuildFileNodeDescriptor)userObject;
//            }
//            node = (DefaultMutableTreeNode)node.getParent();
//        }
//        return null;
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

//    private <T> java.util.List<T> collectAntFiles(final Function<AntBuildFile, T> function) {
//        final TreePath[] paths = myTree.getSelectionPaths();
//        if (paths == null) {
//            return null;
//        }
//        Set<AntBuildFile> antFiles = new LinkedHashSet<AntBuildFile>();
//        for (final TreePath path : paths) {
//            for (DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
//                 node != null;
//                 node = (DefaultMutableTreeNode)node.getParent()) {
//                final Object userObject = node.getUserObject();
//                if (!(userObject instanceof AntBuildFileNodeDescriptor)) {
//                    continue;
//                }
//                final AntBuildFile buildFile = ((AntBuildFileNodeDescriptor)userObject).getBuildFile();
//                if (buildFile != null) {
//                    antFiles.add(buildFile);
//                }
//                break;
//            }
//        }
//        final java.util.List<T> result = new ArrayList<T>();
//        ContainerUtil.addAllNotNull(result, ContainerUtil.map(antFiles, new Function<AntBuildFile, T>() {
//            @Override
//            public T fun(AntBuildFile buildFile) {
//                return function.fun(buildFile);
//            }
//        }));
//        return result.isEmpty() ? null : result;
//    }

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
            event.getPresentation().setEnabled(isConfigurationSelected() && !isConfigurationInUse(selectionHandler.getCurrentConfiguration()));
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
            event.getPresentation().setEnabled(isConfigurationSelected() && !isConfigurationInUse(selectionHandler.getCurrentConfiguration()));
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

    private boolean isConfigurationInUse(ServerConfiguration serverConfiguration) {
        return serverConfiguration != null && CONFIGURATION_IN_USE.contains(serverConfiguration.getServerStatus());
    }

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
            OsgiClient osgiClient = obtainSGiClient(serverConfiguration);
            if(osgiClient != null) {
                BundleStatus status = checkAndUpdateSupportBundle(serverConfiguration, osgiClient, false);
                if(status != BundleStatus.failed) {
                    checkModules(serverConfiguration, osgiClient);
                }
            }
        }

        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(isConfigurationSelected());
        }
    }

    private void checkModules(ServerConfiguration serverConfiguration, OsgiClient osgiClient) {
        // We only support Maven Modules as of now
        //AS TODO: are we support Facets as well -> check later
        MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(myProject);
        messageManager.sendDebugNotification("Maven Projects Manager: '" + mavenProjectsManager);
        boolean allSynchronized = true;
        List<MavenProject> mavenProjects = mavenProjectsManager.getNonIgnoredProjects();
        for(MavenProject mavenProject: mavenProjects) {
            MavenId mavenId = mavenProject.getMavenId();
            String moduleName = mavenProject.getName();
            String artifactId = mavenId.getArtifactId();
            String version = mavenId.getVersion();
            // Check if this Module is listed in the Module Sub Tree of the Configuration. If not add it.
            messageManager.sendDebugNotification("Maven Module: '" + moduleName + "', artifact id: '" + artifactId + "', version: '" + version + "'");
            // Ignore the Unnamed Projects
            if(moduleName == null) { continue; }
            // Change any dashes to dots
            version = version.replaceAll("-", ".");
            Version localVersion = new Version(version);
            ServerConfiguration.Module module = serverConfiguration.obtainModuleBySymbolicName(ServerConfiguration.Module.getSymbolicName(mavenProject));
            if(module == null) {
                module = serverConfiguration.addModule(mavenProject);
            } else {
                // If the module already exists then it could be from the Storage so we need to re-bind with the maven project
                module.rebind(mavenProject);
            }
            try {
                if(module.isOSGiBundle()) {
                    Version remoteVersion = osgiClient.getBundleVersion(module.getSymbolicName());
                    messageManager.sendDebugNotification("Check OSGi Module: '" + moduleName + "', artifact id: '" + artifactId + "', version: '" + remoteVersion + "' vs. '" + localVersion + "'");
                    boolean moduleUpToDate = remoteVersion != null && remoteVersion.compareTo(localVersion) >= 0;
                    Object state = BundleStateHelper.getBundleState(module);
                    messageManager.sendDebugNotification("Bundle State of Module: '" + module.getName() + "', state: '" + state + "'");
                    if(moduleUpToDate) {
                        // Mark as synchronized
                        module.setStatus(ServerConfiguration.BundleStatus.upToDate);
                    } else {
                        // Mark as out of date
                        module.setStatus(ServerConfiguration.BundleStatus.outdated);
                        allSynchronized = false;
                    }
                } else if(module.isSlingPackage()) {
                    //AS TODO: Handle Sling Package
                }
            } catch(OsgiClientException e1) {
                // Mark connection as failed
                module.setStatus(ServerConfiguration.BundleStatus.failed);
                allSynchronized = false;
            }
        }
        if(allSynchronized) {
            markConfigurationAsSynchronized(serverConfiguration.getName());
        } else {
            markConfigurationAsOutDated(serverConfiguration.getName());
        }
    }

    public enum BundleStatus { upToDate, outDated, failed };
    private BundleStatus checkAndUpdateSupportBundle(ServerConfiguration serverConfiguration, OsgiClient osgiClient, boolean onlyCheck) {
        BundleStatus ret = BundleStatus.failed;
        try {
            EmbeddedArtifactLocator artifactLocator = OSGiFactory.getArtifactLocator();
            Version remoteVersion = osgiClient.getBundleVersion(EmbeddedArtifactLocator.SUPPORT_BUNDLE_SYMBOLIC_NAME);

            messageManager.sendInfoNotification("aem.explorer.version.installed.support.bundle", remoteVersion);

            final EmbeddedArtifact supportBundle = artifactLocator.loadToolingSupportBundle();
            final Version embeddedVersion = new Version(supportBundle.getVersion());

            if(remoteVersion == null || remoteVersion.compareTo(embeddedVersion) < 0) {
                ret = BundleStatus.outDated;
                if(!onlyCheck) {
                    InputStream contents = null;
                    try {
                        messageManager.sendInfoNotification("aem.explorer.begin.installing.support.bundle", embeddedVersion);
                        contents = supportBundle.openInputStream();
                        osgiClient.installBundle(contents, supportBundle.getName());
                        ret = BundleStatus.upToDate;
                    } finally {
                        IOUtils.closeQuietly(contents);
                    }
                    remoteVersion = embeddedVersion;
                }
            } else {
                ret = BundleStatus.upToDate;
            }
            messageManager.sendInfoNotification("aem.explorer.finished.connection.to.remote");
        } catch(IOException e) {
            messageManager.sendErrorNotification("aem.explorer.cannot.read.installation.support.bundle", serverConfiguration.getName(), e);
        } catch(OsgiClientException e) {
            messageManager.sendErrorNotification("aem.explorer.osgi.client.problem", serverConfiguration.getName(), e);
        }
        return ret;
    }

    private OsgiClient obtainSGiClient(ServerConfiguration serverConfiguration) {
        OsgiClient ret = null;

        try {
            boolean success = false;
            Result<ResourceProxy> result = null;
            messageManager.sendInfoNotification("aem.explorer.begin.connecting.sling.repository");
            Repository repository = ServerUtil.connectRepository(
                new IServer(serverConfiguration), new NullProgressMonitor()
            );
            Command<ResourceProxy> command = repository.newListChildrenNodeCommand("/");
            result = command.execute();
            success = result.isSuccess();

            messageManager.sendInfoNotification("aem.explorer.connected.sling.repository", success);
            if(success) {
                RepositoryInfo repositoryInfo = ServerUtil.getRepositoryInfo(
                    new IServer(serverConfiguration), new NullProgressMonitor()
                );
                //AS TODO: Activator is an Eclipse OSGi component and so we need to find a way to extract this and code it so that it works for Eclipse and IntelliJ
                ret = Activator.getDefault().getOsgiClientFactory().createOsgiClient(repositoryInfo);
//                ret = OSGiFactory.getOSGiClientFactory().createOsgiClient(repositoryInfo);
            }
        } catch(URISyntaxException e) {
            messageManager.sendErrorNotification("aem.explorer.server.uri.bad", serverConfiguration.getName(), e);
//        } catch(ServerException e) {
//            messageManager.sendErrorNotification("aem.explorer.cannot.connect.repository", serverConfiguration.getName(), e);
        } catch(CoreException e) {
            messageManager.sendErrorNotification("aem.explorer.cannot.connect.repository", serverConfiguration.getName(), e);
        }

        return ret;
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
            ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
            // Create Remote Connection to Server using the IntelliJ Run / Debug Connection
            //AS TODO: It is working but the configuration is listed and made persistent. That is not too bad because
            //AS TODO: after changes a reconnect will update the configuration.
            RemoteConfigurationType remoteConfigurationType = new RemoteConfigurationType();
            RunConfiguration runConfiguration = remoteConfigurationType.getFactory().createTemplateConfiguration(myProject);
            RemoteConfiguration remoteConfiguration = (RemoteConfiguration) runConfiguration;
            // Server means if you are listening. If not you are attaching.
            remoteConfiguration.SERVER_MODE = false;
            remoteConfiguration.USE_SOCKET_TRANSPORT = true;
            remoteConfiguration.HOST = serverConfiguration.getHost();
            remoteConfiguration.PORT = serverConfiguration.getConnectionDebugPort() + "";
            // Set a Name of the Configuration so that it is properly listed.
            remoteConfiguration.setName(serverConfiguration.getName());
            RunnerAndConfigurationSettings configuration = new RunnerAndConfigurationSettingsImpl(
                (RunManagerImpl) myRunManager,
                runConfiguration,
                false
            );
            myRunManager.setTemporaryConfiguration(configuration);
//            myRunManager.setSelectedConfiguration(configuration);
            //AS TODO: Make sure that this is the proper way to obtain the DEBUG Executor
            Executor executor = ExecutorRegistry.getInstance().getExecutorById(ToolWindowId.DEBUG);
            ExecutionUtil.runConfiguration(configuration, executor);
            // Update the Modules with the Remote Sling Server
            OsgiClient osgiClient = obtainSGiClient(serverConfiguration);
            if(osgiClient != null) {
                BundleStatus status = checkAndUpdateSupportBundle(serverConfiguration, osgiClient, false);
                if(status != BundleStatus.failed) {
                    checkModules(serverConfiguration, osgiClient);
                }
            }
        }

        public void update(AnActionEvent event) {
            //AS TODO: Disabled this when a session is started and (re)enable it when it is stopped
            event.getPresentation().setEnabled(isConfigurationSelected() && !isConfigurationInUse(selectionHandler.getCurrentConfiguration()));
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
            ProcessHandler activeProcessHandler = getHandler(dataContext);

            if(canBeStopped(activeProcessHandler)) {
                stopProcess(activeProcessHandler);
            }
        }

        public void update(AnActionEvent event) {
            event.getPresentation().setEnabled(isConfigurationSelected() && isConfigurationInUse(selectionHandler.getCurrentConfiguration()));
        }

        private void stopProcess(@NotNull ProcessHandler processHandler) {
            if(processHandler instanceof KillableProcess && processHandler.isProcessTerminating()) {
                ((KillableProcess) processHandler).killProcess();
                return;
            }

            if(processHandler.detachIsDefault()) {
                processHandler.detachProcess();
            } else {
                processHandler.destroyProcess();
            }
        }

        @Nullable
        private ProcessHandler getHandler(@NotNull DataContext dataContext) {
            final RunContentDescriptor contentDescriptor = LangDataKeys.RUN_CONTENT_DESCRIPTOR.getData(dataContext);
            if(contentDescriptor != null) {
                // toolwindow case
                return contentDescriptor.getProcessHandler();
            } else {
                // main menu toolbar
                final Project project = CommonDataKeys.PROJECT.getData(dataContext);
                final RunContentDescriptor selectedContent =
                    project == null ? null : ExecutionManager.getInstance(project).getContentManager().getSelectedContent();
                return selectedContent == null ? null : selectedContent.getProcessHandler();
            }
        }

        private boolean canBeStopped(@Nullable ProcessHandler processHandler) {
            return processHandler != null && !processHandler.isProcessTerminated()
                && (!processHandler.isProcessTerminating()
                || processHandler instanceof KillableProcess && ((KillableProcess) processHandler).canKillProcess());
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
            event.getPresentation().setEnabled(true);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            // First Check if the Install Support Bundle is installed
            ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
            OsgiClient osgiClient = obtainSGiClient(serverConfiguration);
            BundleStatus bundleStatus = checkAndUpdateSupportBundle(serverConfiguration, osgiClient, true);
            if(bundleStatus == BundleStatus.upToDate) {
                List<ServerConfiguration.Module> moduleList = selectionHandler.getCurrentConfigurationModuleDescriptorList();
                // Deploy all selected Modules
                deployModules(osgiClient, moduleList);
            }
        }

        private void deployModules(OsgiClient osgiClient, List<ServerConfiguration.Module> moduleList) {
            for(ServerConfiguration.Module module: moduleList) {
                InputStream contents = null;
                // Check if this is a OSGi Bundle
                if(module.getProject().getPackaging().equalsIgnoreCase("bundle")) {
                    try {
//                    sendInfoNotification("aem.explorer.begin.installing.support.bundle", embeddedVersion);
                        File buildDirectory = new File(module.getProject().getBuildDirectory());
                        if(buildDirectory.exists() && buildDirectory.isDirectory()) {
                            File buildFile = new File(buildDirectory, module.getProject().getFinalName() + ".jar");
                            messageManager.sendDebugNotification("Build File Name: " + buildFile.toURL());
                            if(buildFile.exists()) {
                                EmbeddedArtifact bundle = new EmbeddedArtifact(module.getSymbolicName(), module.getVersion(), buildFile.toURL());
                                contents = bundle.openInputStream();
                                osgiClient.installBundle(contents, bundle.getName());
                                module.setStatus(ServerConfiguration.BundleStatus.upToDate);
                                //                            ret = BundleStatus.upToDate;
                            }
                        }
                    } catch(MalformedURLException e) {
                        module.setStatus(ServerConfiguration.BundleStatus.failed);
                        messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.bad.url", e);
                    } catch(OsgiClientException e) {
                        module.setStatus(ServerConfiguration.BundleStatus.failed);
                        messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.client", e);
                    } catch(IOException e) {
                        module.setStatus(ServerConfiguration.BundleStatus.failed);
                        messageManager.sendErrorNotification("aem.explorer.deploy.module.failed.io", e);
                    } finally {
                        IOUtils.closeQuietly(contents);
                    }
                } else {
                    module.setStatus(ServerConfiguration.BundleStatus.unsupported);
                    messageManager.sendDebugNotification("Module: '" + module.getName() + "' is not an OSGi Bundle");
                }
            }
        }
    }


    private final class ShowAllTargetsAction extends ToggleAction {
        public ShowAllTargetsAction() {
//            super(AntBundle.message("filter.ant.targets.action.name"), AntBundle.message("filter.ant.targets.action.description"),
//                    AllIcons.General.Filter);
            super("Show All Target Action", "Description", null);
        }

        public boolean isSelected(AnActionEvent event) {
//            final Project project = myProject;
//            return project != null? AntConfigurationBase.getInstance(project).isFilterTargets() : false;
            return false;
        }

        public void setSelected(AnActionEvent event, boolean flag) {
            setTargetsFiltered(flag);
        }
    }

    private void setTargetsFiltered(boolean value) {
        myBuilder.setTargetsFiltered(value);
//        AntConfigurationBase.getInstance(myProject).setFilterTargets(value);
    }

//    private final class ExecuteOnEventAction extends ToggleAction {
//        private final AntBuildTargetBase myTarget;
//        private final ExecutionEvent myExecutionEvent;
//
//        public ExecuteOnEventAction(final AntBuildTargetBase target, final ExecutionEvent executionEvent) {
//            super(executionEvent.getPresentableName());
//            myTarget = target;
//            myExecutionEvent = executionEvent;
//        }
//
//        public boolean isSelected(AnActionEvent e) {
//            return myTarget.equals(AntConfigurationBase.getInstance(myProject).getTargetForEvent(myExecutionEvent));
//        }
//
//        public void setSelected(AnActionEvent event, boolean state) {
//            final AntConfigurationBase antConfiguration = AntConfigurationBase.getInstance(myProject);
//            if (state) {
//                final AntBuildFileBase buildFile =
//                        (AntBuildFileBase)((myTarget instanceof MetaTarget) ? ((MetaTarget)myTarget).getBuildFile() : myTarget.getModel().getBuildFile());
//                antConfiguration.setTargetForEvent(buildFile, myTarget.getName(), myExecutionEvent);
//            }
//            else {
//                antConfiguration.clearTargetForEvent(myExecutionEvent);
//            }
//            myBuilder.queueUpdate();
//        }
//
//        public void update(AnActionEvent e) {
//            super.update(e);
//            final AntBuildFile buildFile = myTarget.getModel().getBuildFile();
//            e.getPresentation().setEnabled(buildFile != null && buildFile.exists());
//        }
//    }

//    private final class ExecuteBeforeRunAction extends AnAction {
//        private final AntBuildTarget myTarget;
//
//        public ExecuteBeforeRunAction(final AntBuildTarget target) {
//            super(AntBundle.message("executes.before.run.debug.acton.name"));
//            myTarget = target;
//        }
//
//        public void actionPerformed(AnActionEvent e) {
//            final AntExecuteBeforeRunDialog dialog = new AntExecuteBeforeRunDialog(myProject, myTarget);
//            dialog.show();
//        }
//
//        public void update(AnActionEvent e) {
//            e.getPresentation().setEnabled(myTarget.getModel().getBuildFile().exists());
//        }
//    }

    private final class CreateMetaTargetAction extends AnAction {

        public CreateMetaTargetAction() {
//            super(AntBundle.message("ant.create.meta.target.action.name"), AntBundle.message("ant.create.meta.target.action.description"), null
            super("Create Meta Action", "Description", null);
/*IconLoader.getIcon("/actions/execute.png")*/
        }

        public void actionPerformed(AnActionEvent e) {
//            final AntBuildFile buildFile = getCurrentBuildFile();
//            final String[] targets = getTargetNamesFromPaths(myTree.getSelectionPaths());
//            final ExecuteCompositeTargetEvent event = new ExecuteCompositeTargetEvent(targets);
//            final SaveMetaTargetDialog dialog = new SaveMetaTargetDialog(myTree, event, AntConfigurationBase.getInstance(myProject), buildFile);
//            dialog.setTitle(e.getPresentation().getText());
//            if (dialog.showAndGet()) {
//                myBuilder.queueUpdate();
//                myTree.repaint();
//            }
        }

        public void update(AnActionEvent e) {
            final TreePath[] paths = myTree.getSelectionPaths();
            e.getPresentation().setEnabled(paths != null && paths.length > 1 && canRunSelection());
        }
    }

    private final class RemoveMetaTargetsOrBuildFileAction extends AnAction {

        public RemoveMetaTargetsOrBuildFileAction() {
//            super(AntBundle.message("remove.meta.targets.action.name"), AntBundle.message("remove.meta.targets.action.description"), null);
            super("Remove Meta Action", "Description", null);
            registerCustomShortcutSet(CommonShortcuts.getDelete(), myTree);
            Disposer.register(SlingServerExplorer.this, new Disposable() {
                public void dispose() {
                    RemoveMetaTargetsOrBuildFileAction.this.unregisterCustomShortcutSet(myTree);
                }
            });
            myTree.registerKeyboardAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    doAction();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }

        public void actionPerformed(AnActionEvent e) {
            doAction();
        }

        private void doAction() {
            final TreePath[] paths = myTree.getSelectionPaths();
            if(paths == null) {
                return;
            }
            try {
                // try to remove build file
                if(paths.length == 1) {
                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
//                    if (node.getUserObject() instanceof AntBuildFileNodeDescriptor) {
//                        final AntBuildFileNodeDescriptor descriptor = (AntBuildFileNodeDescriptor)node.getUserObject();
//                        if (descriptor.getBuildFile().equals(getCurrentBuildFile())) {
//                            removeBuildFile();
//                            return;
//                        }
//                    }
                }
                // try to remove meta targets
//                final AntBuildTarget[] targets = getTargetObjectsFromPaths(paths);
//                final AntConfigurationBase antConfiguration = AntConfigurationBase.getInstance(myProject);
//                for (final AntBuildTarget buildTarget : targets) {
//                    if (buildTarget instanceof MetaTarget) {
//                        for (final ExecutionEvent event : antConfiguration.getEventsForTarget(buildTarget)) {
//                            if (event instanceof ExecuteCompositeTargetEvent) {
//                                antConfiguration.clearTargetForEvent(event);
//                            }
//                        }
//                    }
//                }
            } finally {
                myBuilder.queueUpdate();
                myTree.repaint();
            }
        }

        public void update(AnActionEvent e) {
            final Presentation presentation = e.getPresentation();
            final TreePath[] paths = myTree.getSelectionPaths();
            if(paths == null) {
                presentation.setEnabled(false);
                return;
            }

            if(paths.length == 1) {
//                String text = AntBundle.message("remove.meta.target.action.name");
//                boolean enabled = false;
//                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[0].getLastPathComponent();
//                if (node.getUserObject() instanceof AntBuildFileNodeDescriptor) {
//                    final AntBuildFileNodeDescriptor descriptor = (AntBuildFileNodeDescriptor)node.getUserObject();
//                    if (descriptor.getBuildFile().equals(getCurrentBuildFile())) {
//                        text = AntBundle.message("remove.selected.build.file.action.name");
//                        enabled = true;
//                    }
//                }
//                else {
//                    if (node.getUserObject() instanceof AntTargetNodeDescriptor) {
//                        final AntTargetNodeDescriptor descr = (AntTargetNodeDescriptor)node.getUserObject();
//                        final AntBuildTargetBase target = descr.getTarget();
//                        if (target instanceof MetaTarget) {
//                            enabled = true;
//                        }
//                    }
//                }
//                presentation.setText(text);
//                presentation.setEnabled(enabled);
            } else {
//                presentation.setText(AntBundle.message("remove.selected.meta.targets.action.name"));
//                final AntBuildTarget[] targets = getTargetObjectsFromPaths(paths);
//                boolean enabled = targets.length > 0;
//                for (final AntBuildTarget buildTarget : targets) {
//                    if (!(buildTarget instanceof MetaTarget)) {
//                        enabled = false;
//                        break;
//                    }
//                }
//                presentation.setEnabled(enabled);
            }
        }
    }

    private final class AssignShortcutAction extends AnAction {
        private final String myActionId;

        public AssignShortcutAction(String actionId) {
//            super(AntBundle.message("ant.explorer.assign.shortcut.action.name"));
            super("Assign Shortcut Action", "Description", null);
            myActionId = actionId;
        }

        public void actionPerformed(AnActionEvent e) {
            new EditKeymapsDialog(myProject, myActionId).show();
        }

        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(myActionId != null && ActionManager.getInstance().getAction(myActionId) != null);
        }
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
