package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.communication.ContentResourceChangeListener;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.intellij.execution.ExecutionAdapter;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.BorderLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

/**
 * Created by schaefa on 3/19/15.
 */
public class SlingServerExplorer
    extends SimpleToolWindowPanel
    implements DataProvider, Disposable
{
    private Project myProject;
    private Tree myTree;
    private ServerConnectionManager serverConnectionManager;
    private ServerConfigurationManager myConfig;
    private MessageBusConnection myConn = null;
    private MessageManager messageManager;

    public SlingServerExplorer(final Project project) {
        super(true, true);
//AS TODO; This is for drag and drop but we don't support that -> remove later
//        setTransferHandler(new MyTransferHandler());
        myProject = project;
        myConfig = ServiceManager.getService(project, ServerConfigurationManager.class);

        ServerTreeManager serverTreeManager = ServiceManager.getService(project, ServerTreeManager.class);
        if(serverTreeManager == null) {
            messageManager.showAlert("Failure", "Failure to find Server Tree Manager");
        }
        myTree = serverTreeManager.getTree();
        setToolbar(createToolbarPanel(serverTreeManager));
        setContent(ScrollPaneFactory.createScrollPane(myTree));
        ToolTipManager.sharedInstance().registerComponent(myTree);
        final MessageBus bus = myProject.getMessageBus();
        myConn = bus.connect();
        serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        ServerTreeSelectionHandler selectionHandler = ServiceManager.getService(project, ServerTreeSelectionHandler.class);
        new ContentResourceChangeListener(myProject, serverConnectionManager, myConn);

        RunManagerEx myRunManager = RunManagerEx.getInstanceEx(myProject);
        messageManager = ServiceManager.getService(myProject, MessageManager.class);

        // Hook up to the Bus and Register an Execution Listener in order to know when Debug Connection is established
        // and when it is taken down even when not started or stopped through the Plugin
        myConn.subscribe(
            ExecutionManager.EXECUTION_TOPIC,
            new ExecutionAdapter() {
                @Override
                public void processNotStarted(String executorId, @NotNull ExecutionEnvironment env) {
                    // This is called when the Debug Session failed to start and / or connect
                    ServerConfiguration configuration = myConfig.findServerConfigurationByName(env.getRunProfile().getName());
                    if(configuration != null) {
                        configuration.setServerStatus(ServerConfiguration.ServerStatus.failed);
                        myConfig.updateServerConfiguration(configuration);
                        //AS TODO: Update Bundle Status
                        // Mark any Bundles inside the Tree as unknown
                    }
                }

                @Override
                public void processStarted(String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
                    // This is called when the Debug Session successfully started and connected
                    ServerConfiguration configuration = myConfig.findServerConfigurationByName(env.getRunProfile().getName());
                    if(configuration != null) {
                        configuration.setServerStatus(ServerConfiguration.ServerStatus.connected);
                        myConfig.updateServerConfiguration(configuration);
                        //AS TODO: Update Bundle Status
                        // Now obtain the Status of the Bundles inside AEM and add as entries to the Tree underneath the Configuration Entry
                    }
                }

                @Override
                public void processTerminated(@NotNull RunProfile runProfile, @NotNull ProcessHandler handler) {
                    // Called when a successful connected session is stopped
                    ServerConfiguration configuration = myConfig.findServerConfigurationByName(runProfile.getName());
                    if(configuration != null) {
                        configuration.setServerStatus(ServerConfiguration.ServerStatus.disconnected);
                        myConfig.updateServerConfiguration(configuration);
                        //AS TODO: Update Bundle Status
                        // Mark any Bundles inside the Tree as disconnected
                    }
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

    public void dispose() {
        if(myConn != null) {
            Disposer.dispose(myConn);
        }
        myConn = null;
        myProject = null;
    }

    private JPanel createToolbarPanel(ServerTreeManager treeManager) {
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(actionManager.getAction("AEM.Toolbar"));
        treeManager.adjustToolbar(group);

        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.ANT_EXPLORER_TOOLBAR, group, true);

        final JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
        return buttonsPanel;
    }

    @Nullable
    public Object getData(@NonNls String dataId) {
//AS TODO: We don't need this for now. It is called on startup and when a connection is selected. Not sure if that is put into the dataContext etc
//        if(CommonDataKeys.NAVIGATABLE.is(dataId)) {
////            final AntBuildFile buildFile = getCurrentBuildFile();
////            if (buildFile == null) {
////                return null;
////            }
////            final VirtualFile file = buildFile.getVirtualFile();
////            if (file == null) {
////                return null;
////            }
//            final TreePath treePath = myTree.getLeadSelectionPath();
//            if(treePath == null) {
//                return null;
//            }
//            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
//            if(node == null) {
//                return null;
//            }
////            if (node.getUserObject() instanceof AntTargetNodeDescriptor) {
////                final AntTargetNodeDescriptor targetNodeDescriptor = (AntTargetNodeDescriptor)node.getUserObject();
////                final AntBuildTargetBase buildTarget = targetNodeDescriptor.getTarget();
////                final OpenFileDescriptor descriptor = buildTarget.getOpenFileDescriptor();
////                if (descriptor != null) {
////                    final VirtualFile descriptorFile = descriptor.getFile();
////                    if (descriptorFile.isValid()) {
////                        return descriptor;
////                    }
////                }
////            }
////            if (file.isValid()) {
////                return new OpenFileDescriptor(myProject, file);
////            }
//        } else if(PlatformDataKeys.HELP_ID.is(dataId)) {
////            return HelpID.ANT;
//            return null;
//        } else if(PlatformDataKeys.TREE_EXPANDER.is(dataId)) {
//            String test = "";
////            return myProject != null ? myTreeExpander : null;
//        } else if(CommonDataKeys.VIRTUAL_FILE_ARRAY.is(dataId)) {
////            final java.util.List<VirtualFile> virtualFiles = collectAntFiles(new Function<AntBuildFile, VirtualFile>() {
////                @Override
////                public VirtualFile fun(AntBuildFile buildFile) {
////                    final VirtualFile virtualFile = buildFile.getVirtualFile();
////                    if (virtualFile != null && virtualFile.isValid()) {
////                        return virtualFile;
////                    }
////                    return null;
////                }
////            });
////            return virtualFiles == null ? null : virtualFiles.toArray(new VirtualFile[virtualFiles.size()]);
//            return null;
//        } else if(LangDataKeys.PSI_ELEMENT_ARRAY.is(dataId)) {
////            final java.util.List<PsiElement> elements = collectAntFiles(new Function<AntBuildFile, PsiElement>() {
////                @Override
////                public PsiElement fun(AntBuildFile buildFile) {
////                    return buildFile.getAntFile();
////                }
////            });
////            return elements == null ? null : elements.toArray(new PsiElement[elements.size()]);
//            return null;
//        }
        return super.getData(dataId);
    }
//
//    //AS TODO: Do we really need this (transfer of what)
//    private final class MyTransferHandler extends TransferHandler {
//
//        @Override
//        public boolean importData(final TransferSupport support) {
//            if(canImport(support)) {
////                addBuildFile(getAntFiles(support));
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public boolean canImport(final TransferSupport support) {
//            return FileCopyPasteUtil.isFileListFlavorAvailable(support.getDataFlavors());
//        }
//
//        private VirtualFile[] getAntFiles(final TransferSupport support) {
//            java.util.List<VirtualFile> virtualFileList = new ArrayList<VirtualFile>();
//            final java.util.List<File> fileList = FileCopyPasteUtil.getFileList(support.getTransferable());
//            if(fileList != null) {
//                for(File file : fileList) {
//                    ContainerUtil.addIfNotNull(virtualFileList, VfsUtil.findFileByIoFile(file, true));
//                }
//            }
//
//            return VfsUtil.toVirtualFileArray(virtualFileList);
//        }
//    }
}
