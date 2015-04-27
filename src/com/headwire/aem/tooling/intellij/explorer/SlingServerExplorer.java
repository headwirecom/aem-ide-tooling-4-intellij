package com.headwire.aem.tooling.intellij.explorer;

import com.intellij.debugger.DebuggerBundle;
import com.intellij.debugger.DebuggerManager;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.ui.ServerConfigurationDialog;
import com.headwire.aem.tooling.intellij.util.ServerUtil;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.impl.DebuggerManagerAdapter;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.impl.HotSwapFile;
import com.intellij.debugger.impl.HotSwapManager;
import com.intellij.debugger.ui.HotSwapVetoableListener;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunManagerAdapter;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.icons.AllIcons;
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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilationStatusListener;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerTopics;
import com.intellij.openapi.compiler.ex.CompilerPathsEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManagerListener;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.keymap.impl.ui.EditKeymapsDialog;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
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
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.xml.DomEventListener;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.events.DomEvent;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.spi.Connection;
import gnu.trove.THashSet;
//import org.apache.sling.ide.impl.vlt.AddOrUpdateNodeCommand;
//import org.apache.sling.ide.impl.vlt.VltRepositoryFactory;
//import org.apache.sling.ide.transport.Command;
//import org.apache.sling.ide.transport.FileInfo;
//import org.apache.sling.ide.transport.Repository;
//import org.apache.sling.ide.transport.RepositoryException;
//import org.apache.sling.ide.transport.RepositoryFactory;
//import org.apache.sling.ide.transport.RepositoryInfo;
//import org.apache.sling.ide.transport.ResourceProxy;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.util.JpsPathUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by schaefa on 3/19/15.
 */
public class SlingServerExplorer
        extends SimpleToolWindowPanel implements DataProvider, Disposable
{
    private static final Logger LOGGER = Logger.getInstance(SlingServerExplorer.class);

    public static final String ROOT_FOLDER = "/jcr_root/";

    private Project myProject;
    private ServerExplorerTreeBuilder myBuilder;
    private Tree myTree;
    private KeymapListener myKeymapListener;
//    private final AntBuildFilePropertiesAction myAntBuildFilePropertiesAction;
    private ServerConfigurationManager myConfig;

//    private Repository repository;

    private final TreeExpander myTreeExpander = new TreeExpander() {
        public void expandAll() {
            myBuilder.expandAll();
        }

        public boolean canExpand() {
            final ServerConfigurationManager config = myConfig;
            return config != null && config.getServerConfigurationList().size() != 0;
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
//        myTree.setRootVisible(false);
        myTree.setRootVisible(true);
        myTree.setShowsRootHandles(true);
        myTree.setCellRenderer(new NodeRenderer());
        myBuilder = new ServerExplorerTreeBuilder(project, myTree, model);
//        myBuilder.setTargetsFiltered(AntConfigurationBase.getInstance(project).isFilterTargets());
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
                if (row >= 0) {
                    final Rectangle bounds = myTree.getRowBounds(row);
                    if (bounds != null && eventY > bounds.getY() && eventY < bounds.getY() + bounds.getHeight()) {
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
        myKeymapListener = new KeymapListener();

        DomManager.getDomManager(project).addDomEventListener(new DomEventListener() {
            public void eventOccured(DomEvent event) {
                myBuilder.queueUpdate();
            }
        }, this);
        RunManagerEx.getInstanceEx(myProject).addRunManagerListener(new RunManagerAdapter() {
            public void beforeRunTasksChanged() {
                myBuilder.queueUpdate();
            }
        });

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

        // Hook into the Debugger Manager
        DebuggerManager debugManager = myProject.getComponent(DebuggerManager.class);
//        final MessageBus bus = myProject.getComponent(MessageBus.class);
        final MessageBus bus = myProject.getMessageBus();
        ((DebuggerManagerEx) debugManager).addDebuggerManagerListener(
            new DebuggerManagerAdapter() {
                private MessageBusConnection myConn = null;
                private int mySessionCount = 0;

                @Override
                public void sessionAttached(DebuggerSession session) {
                    if(mySessionCount++ == 0) {
                        myConn = bus.connect();
//                        myConn.subscribe(CompilerTopics.COMPILATION_STATUS, new MyCompilationStatusListener());
                    }
                }

                @Override
                public void sessionDetached(DebuggerSession session) {
                    mySessionCount = Math.max(0, mySessionCount - 1);
                    if(mySessionCount == 0) {
                        final MessageBusConnection conn = myConn;
                        if(conn != null) {
                            Disposer.dispose(conn);
                            myConn = null;
                        }
                    }
                }
            }
        );
    }

    public void dispose() {
        final KeymapListener listener = myKeymapListener;
        if (listener != null) {
            myKeymapListener = null;
            listener.stopListen();
        }

        final ServerExplorerTreeBuilder builder = myBuilder;
        if (builder != null) {
            Disposer.dispose(builder);
            myBuilder = null;
        }

        final Tree tree = myTree;
        if (tree != null) {
            ToolTipManager.sharedInstance().unregisterComponent(tree);
            for (KeyStroke keyStroke : tree.getRegisteredKeyStrokes()) {
                tree.unregisterKeyboardAction(keyStroke);
            }
            myTree = null;
        }

        myProject = null;
//        myConfig = null;
    }

    private JPanel createToolbarPanel() {
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AddAction());
        group.add(new RemoveAction());
        group.add(new RunAction());
//        group.add(new ShowAllTargetsAction());
//        AnAction action = CommonActionsManager.getInstance().createExpandAllAction(myTreeExpander, this);
//        action.getTemplatePresentation().setDescription(AntBundle.message("ant.explorer.expand.all.nodes.action.description"));
//        group.add(action);
//        action = CommonActionsManager.getInstance().createCollapseAllAction(myTreeExpander, this);
//        action.getTemplatePresentation().setDescription(AntBundle.message("ant.explorer.collapse.all.nodes.action.description"));
//        group.add(action);
//        group.add(myAntBuildFilePropertiesAction);
//        group.add(new ContextHelpAction(HelpID.ANT));

        final ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar(ActionPlaces.ANT_EXPLORER_TOOLBAR, group, true);
        final JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.add(actionToolBar.getComponent(), BorderLayout.CENTER);
        return buttonsPanel;
    }

    private void addServerConfiguration() {

//        final FileChooserDescriptor descriptor = createXmlDescriptor();
////        descriptor.setTitle(AntBundle.message("select.ant.build.file.dialog.title"));
////        descriptor.setDescription(AntBundle.message("select.ant.build.file.dialog.description"));
//        final VirtualFile[] files = FileChooser.chooseFiles(descriptor, myProject, null);
//        addBuildFile(files);
    }

    private void addBuildFile(final VirtualFile[] files) {
        if (files.length == 0) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
//                final AntConfiguration antConfiguration = myConfig;
//                if (antConfiguration == null) {
//                    return;
//                }
//                final java.util.List<VirtualFile> ignoredFiles = new ArrayList<VirtualFile>();
//                for (VirtualFile file : files) {
//                    try {
//                        antConfiguration.addBuildFile(file);
//                    }
//                    catch (AntNoFileException e) {
//                        ignoredFiles.add(e.getFile());
//                    }
//                }
//                if (ignoredFiles.size() != 0) {
//                    String messageText;
//                    final StringBuilder message = StringBuilderSpinAllocator.alloc();
//                    try {
//                        String separator = "";
//                        for (final VirtualFile virtualFile : ignoredFiles) {
//                            message.append(separator);
//                            message.append(virtualFile.getPresentableUrl());
//                            separator = "\n";
//                        }
//                        messageText = message.toString();
//                    }
//                    finally {
//                        StringBuilderSpinAllocator.dispose(message);
//                    }
//                    Messages.showWarningDialog(myProject, messageText, AntBundle.message("cannot.add.ant.files.dialog.title"));
//                }
            }
        });
    }

    public void removeBuildFile() {
//        final AntBuildFile buildFile = getCurrentBuildFile();
//        if (buildFile == null) {
//            return;
//        }
//        final String fileName = buildFile.getPresentableUrl();
//        final int result = Messages.showYesNoDialog(myProject, AntBundle.message("remove.the.reference.to.file.confirmation.text", fileName),
//                AntBundle.message("confirm.remove.dialog.title"), Messages.getQuestionIcon());
//        if (result != Messages.YES) {
//            return;
//        }
//        myConfig.removeBuildFile(buildFile);
    }

    public void setBuildFileProperties() {
//        final AntBuildFileBase buildFile = getCurrentBuildFile();
//        if (buildFile != null && BuildFilePropertiesPanel.editBuildFile(buildFile, myProject)) {
//            myConfig.updateBuildFile(buildFile);
//            myBuilder.queueUpdate();
//            myTree.repaint();
//        }
    }

    private void runSelection(final DataContext dataContext) {
        if (!canRunSelection()) {
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
        if (myTree == null) {
            return false;
        }
        final TreePath[] paths = myTree.getSelectionPaths();
        if (paths == null) {
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
        for (final TreePath path : paths) {
            final Object userObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
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

    public boolean isBuildFileSelected() {
//        if( myProject == null) return false;
//        final AntBuildFileBase file = getCurrentBuildFile();
//        return file != null && file.exists();
        return false;
    }

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
        if (path != null) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node != null) {
                userObject = node.getUserObject();
            }
        }
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RunAction());
//        group.add(new CreateMetaTargetAction());
        group.add(new MakeAntRunConfigurationAction());
//        group.add(new RemoveMetaTargetsOrBuildFileAction());
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
        if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
//            final AntBuildFile buildFile = getCurrentBuildFile();
//            if (buildFile == null) {
//                return null;
//            }
//            final VirtualFile file = buildFile.getVirtualFile();
//            if (file == null) {
//                return null;
//            }
            final TreePath treePath = myTree.getLeadSelectionPath();
            if (treePath == null) {
                return null;
            }
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
            if (node == null) {
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
        }
        else if (PlatformDataKeys.HELP_ID.is(dataId)) {
//            return HelpID.ANT;
            return null;
        }
        else if (PlatformDataKeys.TREE_EXPANDER.is(dataId)) {
            return myProject != null? myTreeExpander : null;
        }
        else if (CommonDataKeys.VIRTUAL_FILE_ARRAY.is(dataId)) {
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
        }
        else if (LangDataKeys.PSI_ELEMENT_ARRAY.is(dataId)) {
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
        return new FileChooserDescriptor(true, false, false, false, false, true){
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                boolean b = super.isFileVisible(file, showHiddenFiles);
                if (!file.isDirectory()) {
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
            final Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            LOGGER.debug("Node Renderer: user object: " + userObject);
            if (userObject instanceof ServerNodeDescriptor) {
                final ServerNodeDescriptor descriptor = (ServerNodeDescriptor)userObject;
                descriptor.customize(this);
            }
            else {
                append(tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
    }

    private final class AddAction extends AnAction {
        public AddAction() {
//            super(AntBundle.message("add.ant.file.action.name"), AntBundle.message("add.ant.file.action.description"), IconUtil.getAddIcon());
            super("Add Action", "Add a New Server Configuration", IconUtil.getAddIcon());
        }

        public void actionPerformed(AnActionEvent e) {
            ServerConfigurationDialog dialog = new ServerConfigurationDialog(e.getProject());

            if (!dialog.showAndGet()) {
//                historyService.setCanceledCommand(dialog.getGoals());
                return;
            }
            ServerConfiguration serverConfiguration = dialog.getConfiguration();
            myConfig.getServerConfigurationList().add(serverConfiguration);
            myTree.repaint();
//            addServerConfiguration();
        }
    }

    private final class RemoveAction extends AnAction {
        public RemoveAction() {
//            super(AntBundle.message("remove.ant.file.action.name"), AntBundle.message("remove.ant.file.action.description"),
//                    IconUtil.getRemoveIcon());
            super("Remove Action", "Description", IconUtil.getRemoveIcon());
        }

        public void actionPerformed(AnActionEvent e) {
            removeBuildFile();
        }

        public void update(AnActionEvent event) {
//            event.getPresentation().setEnabled(getCurrentBuildFile() != null);
        }
    }

    private final class RunAction extends AnAction {
        public RunAction() {
//            super(AntBundle.message("run.ant.file.or.target.action.name"), AntBundle.message("run.ant.file.or.target.action.description"),
//                    AllIcons.Actions.Execute);
            super("Run Action", "Hot Swap Class", AllIcons.Actions.Execute);
        }

        public void actionPerformed(AnActionEvent e) {
//            runSelection(e.getDataContext());
            // Create a Connection
            // Create a Virtual Machine
            // Load the Compiled Class
            // Obtain the Reference Type
            // Redefine the Class on the Remote Server
        }

        public void update(AnActionEvent event) {
            final Presentation presentation = event.getPresentation();
//            final String place = event.getPlace();
//            if (ActionPlaces.ANT_EXPLORER_TOOLBAR.equals(place)) {
////                presentation.setText(AntBundle.message("run.ant.file.or.target.action.name"));
//            }
//            else {
//                final TreePath[] paths = myTree.getSelectionPaths();
//                if (paths != null && paths.length == 1) {
//                    Object temp = ((DefaultMutableTreeNode)paths[0].getLastPathComponent()).getUserObject();
//                    LOGGER.debug("Selected User Object: '{}'", temp);
//                    if(temp instanceof SlingServerNodeDescriptor) {
//                        SlingServerNodeDescriptor node = (SlingServerNodeDescriptor) temp;
//                        ServerConfiguration serverConfiguration = node.getTarget();
//                        ServerUtil.connectRepository(serverConfiguration);
//                    } else {
//                        LOGGER.debug("Selected object is not a Server Configuration but: '{}'", temp);
//                    }
////                    presentation.setText(AntBundle.message("run.ant.build.action.name"));
//                }
////                else {
////                    if (paths == null || paths.length == 1) {
////                        presentation.setText(AntBundle.message("run.ant.target.action.name"));
////                    }
////                    else {
////                        presentation.setText(AntBundle.message("run.ant.targets.action.name"));
////                    }
////                }
//            }
//
//            presentation.setEnabled(canRunSelection());
            presentation.setEnabled(true);
        }
    }
    private final class MakeAntRunConfigurationAction extends AnAction {
        public MakeAntRunConfigurationAction() {
//            super(AntBundle.message("make.ant.runconfiguration.name"), null, AntIcons.Build);
            super("Make / Run Action", "Description", null);
        }

        @Override
        public void update(AnActionEvent e) {
            super.update(e);

            final Presentation presentation = e.getPresentation();
            presentation.setEnabled(myTree.getSelectionCount() == 1 && canRunSelection());
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
//            final AntBuildFile buildFile = getCurrentBuildFile();
//            if (buildFile == null || !buildFile.exists()) {
//                return;
//            }
//
//            TreePath selectionPath = myTree.getSelectionPath();
//            if (selectionPath == null) return;
//            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
//            final Object userObject = node.getUserObject();
//            AntBuildTarget target = null;
//            if (userObject instanceof AntTargetNodeDescriptor) {
//                AntTargetNodeDescriptor targetNodeDescriptor = (AntTargetNodeDescriptor)userObject;
//                target = targetNodeDescriptor.getTarget();
//            }
//            else if (userObject instanceof AntBuildFileNodeDescriptor){
//                AntBuildModel model = ((AntBuildFileNodeDescriptor)userObject).getBuildFile().getModel();
//                target = model.findTarget(model.getDefaultTargetName());
//            }
//            String name = target != null ? target.getDisplayName() : null;
//            if (target == null || name == null) {
//                return;
//            }
//
//            RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(e.getProject());
//            RunnerAndConfigurationSettings settings =
//                    runManager.createRunConfiguration(name, AntRunConfigurationType.getInstance().getFactory());
//            AntRunConfiguration configuration  = (AntRunConfiguration)settings.getConfiguration();
//            configuration.acceptSettings(target);
//            if (RunDialog.editConfiguration(e.getProject(), settings, ExecutionBundle
//                    .message("create.run.configuration.for.item.dialog.title", configuration.getName()))) {
//                runManager.addConfiguration(settings,
//                        runManager.isConfigurationShared(settings),
//                        runManager.getBeforeRunTasks(settings.getConfiguration()), false);
//                runManager.setSelectedConfiguration(settings);
//            }
        }
    }


    private final class ShowAllTargetsAction extends ToggleAction {
        public ShowAllTargetsAction() {
//            super(AntBundle.message("filter.ant.targets.action.name"), AntBundle.message("filter.ant.targets.action.description"),
//                    AllIcons.General.Filter);
            super("Show ALl Taget Action", "Description", null);
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
            if (paths == null) {
                return;
            }
            try {
                // try to remove build file
                if (paths.length == 1) {
                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[0].getLastPathComponent();
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
            }
            finally {
                myBuilder.queueUpdate();
                myTree.repaint();
            }
        }

        public void update(AnActionEvent e) {
            final Presentation presentation = e.getPresentation();
            final TreePath[] paths = myTree.getSelectionPaths();
            if (paths == null) {
                presentation.setEnabled(false);
                return;
            }

            if (paths.length == 1) {
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
            }
            else {
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

    private class KeymapListener implements KeymapManagerListener, Keymap.Listener {
        private Keymap myCurrentKeymap = null;

        public KeymapListener() {
            final KeymapManagerEx keymapManager = KeymapManagerEx.getInstanceEx();
            final Keymap activeKeymap = keymapManager.getActiveKeymap();
            listenTo(activeKeymap);
            keymapManager.addKeymapManagerListener(this);
        }

        public void activeKeymapChanged(Keymap keymap) {
            listenTo(keymap);
            updateTree();
        }

        private void listenTo(Keymap keymap) {
            if (myCurrentKeymap != null) {
                myCurrentKeymap.removeShortcutChangeListener(this);
            }
            myCurrentKeymap = keymap;
            if (myCurrentKeymap != null) {
                myCurrentKeymap.addShortcutChangeListener(this);
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

    private final class MyTransferHandler extends TransferHandler {

        @Override
        public boolean importData(final TransferSupport support) {
            if (canImport(support)) {
                addBuildFile(getAntFiles(support));
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
            if (fileList != null) {
                for (File file : fileList ) {
                    ContainerUtil.addIfNotNull(virtualFileList, VfsUtil.findFileByIoFile(file, true));
                }
            }

            return VfsUtil.toVirtualFileArray(virtualFileList);
        }
    }

//    private Connector findConnector(String connectorName) throws ExecutionException {
//        VirtualMachineManager virtualMachineManager;
//        try {
//            virtualMachineManager = Bootstrap.virtualMachineManager();
//        }
//        catch (Error e) {
//            final String error = e.getClass().getName() + " : " + e.getLocalizedMessage();
//            throw new ExecutionException(DebuggerBundle.message("debugger.jdi.bootstrap.error", error));
//        }
//        List connectors;
//        if (SOCKET_ATTACHING_CONNECTOR_NAME.equals(connectorName) || SHMEM_ATTACHING_CONNECTOR_NAME.equals(connectorName)) {
//            connectors = virtualMachineManager.attachingConnectors();
//        }
//        else if (SOCKET_LISTENING_CONNECTOR_NAME.equals(connectorName) || SHMEM_LISTENING_CONNECTOR_NAME.equals(connectorName)) {
//            connectors = virtualMachineManager.listeningConnectors();
//        }
//        else {
//            return null;
//        }
//        for (Object connector1 : connectors) {
//            Connector connector = (Connector)connector1;
//            if (connectorName.equals(connector.name())) {
//                return connector;
//            }
//        }
//        return null;
//    }

    private static final String CLASS_EXTENSION = ".class";

    private final Map<DebuggerSession, Long> myTimeStamps = new com.intellij.util.containers.HashMap<DebuggerSession, Long>();

    private long getTimeStamp(DebuggerSession session) {
        Long tStamp = myTimeStamps.get(session);
        return tStamp != null ? tStamp.longValue() : 0;
    }

    public Map<Object, Map<String, byte[]>> findModifiedClasses2(Map<String, List<String>> generatedPaths) {
        final Map<Object, Map<String, byte[]>> result = new java.util.HashMap<Object, Map<String, byte[]>>();
        for (Map.Entry<String, List<String>> entry : generatedPaths.entrySet()) {
            final File root = new File(entry.getKey());
            for (String relativePath : entry.getValue()) {
                if (SystemInfo.isFileSystemCaseSensitive? StringUtil.endsWith(relativePath, CLASS_EXTENSION) : StringUtil.endsWithIgnoreCase(relativePath, CLASS_EXTENSION)) {
                    final String qualifiedName = relativePath.substring(0, relativePath.length() - CLASS_EXTENSION.length()).replace('/', '.');
                    final File file = new File(root, relativePath);
                    final byte[] content = getResourceContentsAsByteArray(file);
                    final long fileStamp = file.lastModified();
                    Map<String, byte[]> container = new java.util.HashMap<String, byte[]>();
                    container.put(qualifiedName, content);
                    result.put(new Object(), container);
                }
            }
        }
        return result;
    }

    public Map<DebuggerSession, Map<String, byte[]>> findModifiedClasses(List<DebuggerSession> sessions, Map<String, List<String>> generatedPaths) {
        final Map<DebuggerSession, Map<String, byte[]>> result = new java.util.HashMap<DebuggerSession, Map<String, byte[]>>();
        List<Pair<DebuggerSession, Long>> sessionWithStamps = new ArrayList<Pair<DebuggerSession, Long>>();
        for (DebuggerSession session : sessions) {
            sessionWithStamps.add(new Pair<DebuggerSession, Long>(session, getTimeStamp(session)));
        }
        for (Map.Entry<String, List<String>> entry : generatedPaths.entrySet()) {
            final File root = new File(entry.getKey());
            for (String relativePath : entry.getValue()) {
                if (SystemInfo.isFileSystemCaseSensitive? StringUtil.endsWith(relativePath, CLASS_EXTENSION) : StringUtil.endsWithIgnoreCase(relativePath, CLASS_EXTENSION)) {
                    final String qualifiedName = relativePath.substring(0, relativePath.length() - CLASS_EXTENSION.length()).replace('/', '.');
                    final File file = new File(root, relativePath);
                    final byte[] content = getResourceContentsAsByteArray(file);
                    final long fileStamp = file.lastModified();
                    for (Pair<DebuggerSession, Long> pair : sessionWithStamps) {
                        final DebuggerSession session = pair.first;
                        if (fileStamp > pair.second) {
                            Map<String, byte[]> container = result.get(session);
                            if (container == null) {
                                container = new java.util.HashMap<String, byte[]>();
                                result.put(session, container);
                            }
                            container.put(qualifiedName, content);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static byte[] getResourceContentsAsByteArray(File file) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            return getInputStreamAsByteArray(stream, -1);
        } catch(FileNotFoundException e) {
            throw new RuntimeException("Failed to open file", e);
        } catch(IOException e) {
            throw new RuntimeException("Failed to read file", e);
        } finally {
            try {
                if(stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static final int DEFAULT_READING_SIZE = 8192;

    /**
     * Returns the given input stream's contents as a byte array.
     * If a length is specified (i.e. if length != -1), only length bytes
     * are returned. Otherwise all bytes in the stream are returned.
     * Note this doesn't close the stream.
     * @throws IOException if a problem occured reading the stream.
     */
    public static byte[] getInputStreamAsByteArray(InputStream stream, int length)
        throws IOException {
        byte[] contents;
        if (length == -1) {
            contents = new byte[0];
            int contentsLength = 0;
            int amountRead = -1;
            do {
                int amountRequested = Math.max(stream.available(), DEFAULT_READING_SIZE);  // read at least 8K

                // resize contents if needed
                if (contentsLength + amountRequested > contents.length) {
                    System.arraycopy(
                        contents,
                        0,
                        contents = new byte[contentsLength + amountRequested],
                        0,
                        contentsLength);
                }

                // read as many bytes as possible
                amountRead = stream.read(contents, contentsLength, amountRequested);

                if (amountRead > 0) {
                    // remember length of contents
                    contentsLength += amountRead;
                }
            } while (amountRead != -1);

            // resize contents if necessary
            if (contentsLength < contents.length) {
                System.arraycopy(
                    contents,
                    0,
                    contents = new byte[contentsLength],
                    0,
                    contentsLength);
            }
        } else {
            contents = new byte[length];
            int len = 0;
            int readSize = 0;
            while ((readSize != -1) && (len != length)) {
                // See PR 1FMS89U
                // We record first the read size. In this case len is the actual read size.
                len += readSize;
                readSize = stream.read(contents, len, length - len);
            }
        }

        return contents;
    }
}
