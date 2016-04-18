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

package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.action.ProgressHandlerImpl;
import com.headwire.aem.tooling.intellij.action.StartDebugConnectionAction;
import com.headwire.aem.tooling.intellij.action.StartRunConnectionAction;
import com.headwire.aem.tooling.intellij.config.general.AEMPluginConfiguration;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeManager;
import com.headwire.aem.tooling.intellij.explorer.SlingServerNodeDescriptor;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.codeInsight.CodeSmellInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vcs.CodeSmellDetector;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import static com.headwire.aem.tooling.intellij.communication.ServerConnectionManager.FileChangeType;
import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/12/15.
 */
public class ContentResourceChangeListener
    extends AbstractProjectComponent
{

    private AEMPluginConfiguration pluginConfiguration;
    private final ServerConnectionManager serverConnectionManager;
    private final Project project;

    private final LinkedList<FileChange> queue = new LinkedList<FileChange>();


    public ContentResourceChangeListener(@NotNull Project project) {
        super(project);
        final ServerConnectionManager serverConnectionManager = ComponentProvider.getComponent(project, ServerConnectionManager.class);
        pluginConfiguration = ComponentProvider.getComponent(project, AEMPluginConfiguration.class);
        this.serverConnectionManager = serverConnectionManager;
        this.project = project;

        // File Change Events are not handled right away but queued up and handled in
        // batches to avoid a constant load on the IDEA. The timeout is configurable so
        // that the user can decide the delay
        Thread thread = new Thread(new Runner());
        thread.setDaemon(true);
        thread.start();

        // Create the Listener on File Changes
        VirtualFileManager.getInstance().addVirtualFileListener(
            new VirtualFileAdapter() {
                @Override
                public void contentsChanged(@NotNull VirtualFileEvent event) {
                    if(event.isFromSave()) {
                        executeMake(event);
                        if (serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                            handleChange(event.getFile(), FileChangeType.CHANGED);
                        }
                    }
                }

                @Override
                public void fileCreated(@NotNull VirtualFileEvent event) {
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        handleChange(event.getFile(), FileChangeType.CREATED);
                    }
                }

                @Override
                public void fileDeleted(@NotNull VirtualFileEvent event) {
                    // When a file is deleted the remove does not work anymore because it is gone -> Delete the file
                    // on line before the actual deletion
                }

                @Override
                public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        // We delete before the Move because the original file still exists
                        VirtualFile file = event.getFile();
                        handleChange(file, FileChangeType.DELETED);
                    }
                }

                @Override
                public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        // Delete the JCR Resource before the file is gone
                        handleChange(event.getFile(), FileChangeType.DELETED);
                    }
                }

                @Override
                public void fileMoved(@NotNull VirtualFileMoveEvent event) {
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        // After the move we create the new file
                        VirtualFile file = event.getFile();
                        handleChange(event.getFile(), FileChangeType.CREATED);
                    }
                }

                @Override
                public void fileCopied(@NotNull VirtualFileCopyEvent event) {
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        handleChange(event.getFile(), FileChangeType.CREATED);
                    }
                }
            },
            project
        );

        // Register a Startup Manager to check the project if it is default after the project is initialized
        StartupManager startupManager = StartupManager.getInstance(project);
        startupManager.runWhenProjectIsInitialized(
            new Runnable() {
                @Override
                public void run() {
                    SlingServerTreeManager slingServerTreeManager = ComponentProvider.getComponent(myProject, SlingServerTreeManager.class);
                    if(slingServerTreeManager != null) {
                        // At the end of the Tool Window is created we run the Check if a project is marked as Default
                        Object modelRoot = slingServerTreeManager.getTree().getModel().getRoot();
                        if (modelRoot instanceof DefaultMutableTreeNode) {
                            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) modelRoot;
                            Enumeration e = rootNode.children();
                            while (e.hasMoreElements()) {
                                TreeNode child = (TreeNode) e.nextElement();
                                if (child instanceof DefaultMutableTreeNode) {
                                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child;
                                    Object target = childNode.getUserObject();
                                    if (target instanceof SlingServerNodeDescriptor) {
                                        ActionManager actionManager = ActionManager.getInstance();
                                        SlingServerNodeDescriptor descriptor = (SlingServerNodeDescriptor) target;
                                        switch (descriptor.getTarget().getDefaultMode()) {
                                            case run:
                                                slingServerTreeManager.getTree().setSelectionPath(new TreePath(childNode.getPath()));
                                                StartRunConnectionAction runAction = (StartRunConnectionAction) actionManager.getAction("AEM.Check.Action");
                                                if (runAction != null) {
                                                    runAction.doRun(myProject, SimpleDataContext.EMPTY_CONTEXT, new ProgressHandlerImpl("Connection Change Listener Check"));
                                                }
                                                break;
                                            case debug:
                                                slingServerTreeManager.getTree().setSelectionPath(new TreePath(childNode.getPath()));
                                                StartDebugConnectionAction debugAction = (StartDebugConnectionAction) actionManager.getAction("AEM.Start.Debug.Action");
                                                if (debugAction != null) {
                                                    debugAction.doDebug(myProject, serverConnectionManager);
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        );
        //AS TODO: That would work if we would build the project without maven but that is not the case
        //AS TODO: Also maven is not sending events on the Build Manager Listeners
        //AS TODO: If that does not work we can remove the messageBusConnection from the constructor
//        messageBusConnection.subscribe(
//            BuildManagerListener.TOPIC,
//            new BuildManagerListener() {
//
//                @Override
//                public void buildStarted(Project project, UUID sessionId, boolean isAutomake) {
//                    // Test this here
//                    String test = "start";
//                }
//
//                @Override
//                public void buildFinished(Project project, UUID sessionId, boolean isAutomake) {
//                    // Test this here
//                    String test = "end";
//                }
//            }
//        );
    }

    private void handleChange(VirtualFile file, FileChangeType fileChangeType) {
        String path = file.getPath();
        if(path.indexOf("/" + JCR_ROOT_FOLDER_NAME + "/") > 0) {
            synchronized(queue) {
                queue.add(new FileChange(file, fileChangeType));
                queue.notifyAll();
            }
        }
    }

    private void executeMake(final VirtualFileEvent event) {
        if(pluginConfiguration == null || pluginConfiguration.isIncrementalBuilds()) {
            // Check if the file is a Java Class and if os build it
            VirtualFile file = event.getFile();
            if("java".equalsIgnoreCase(file.getExtension())) {
                //AS TODO: In order to use the Code Snell Detector this needs to be invoked in a Read Only Thread but part of the Dispatcher Thread
                ApplicationManager.getApplication().invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            executeMakeInUIThread(event);
                        }
                    }
                );
            }
        }
    }

    private void executeMakeInUIThread(final VirtualFileEvent event) {
        if(project.isInitialized() && !project.isDisposed() && project.isOpen()) {
            final CompilerManager compilerManager = CompilerManager.getInstance(project);
            if(!compilerManager.isCompilationActive() &&
                !compilerManager.isExcludedFromCompilation(event.getFile()) // &&
            ) {
                // Check first if there are no errors in the code
                CodeSmellDetector codeSmellDetector = CodeSmellDetector.getInstance(project);
                boolean isOk = true;
                if(codeSmellDetector != null) {
                    List<CodeSmellInfo> codeSmellInfoList = codeSmellDetector.findCodeSmells(Arrays.asList(event.getFile()));
                    for(CodeSmellInfo codeSmellInfo: codeSmellInfoList) {
                        if(codeSmellInfo.getSeverity() == HighlightSeverity.ERROR) {
                            isOk = false;
                            break;
                        }
                    }
                }
                if(isOk) {
                    // Changed file found in module. Make it.
                    final ToolWindow tw = ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.MESSAGES_WINDOW);
                    final boolean isShown = tw != null && tw.isVisible();
                    compilerManager.compile(
                        new VirtualFile[]{event.getFile()},
                        new CompileStatusNotification() {
                            @Override
                            public void finished(boolean b, int i, int i1, CompileContext compileContext) {
                                if (tw != null && tw.isVisible()) {
                                    // Close / Hide the Build Message Window after we did the build if it wasn't shown
                                    if(!isShown) {
                                        tw.hide(null);
                                    }
                                }
                            }
                        }
                    );
                } else {
                    MessageManager messageManager = ComponentProvider.getComponent(project, MessageManager.class);
                    if(messageManager != null) {
                        messageManager.sendErrorNotification(
                            "server.update.file.change.with.error",
                            event.getFile()
                        );
                    }
                }
            }
        }
    }

    private  class Runner
        implements Runnable
    {
        @Override
        public void run() {
            while(true) {
                LinkedList<FileChange> work = null;
                synchronized(queue) {
                    work = new LinkedList<FileChange>(queue);
                    queue.clear();
                }
                    if(work.isEmpty()) {
                        synchronized(queue) {
                            try {
                                // This should block until an entry is written to the queue
                                // where a notifyAll() will wake up this thread for another loop
                                queue.wait();
                            } catch(InterruptedException e) {
                                // Ignore it
                            }
                        }
                    } else {
                        // Do the updates
                        serverConnectionManager.handleFileChanges(work);
                        // Wait for the timeout
                        try {
                            int delay = pluginConfiguration == null ?
                                30 :
                                pluginConfiguration.getDeployDelayInSeconds();
                            if(delay > 0) {
                                Thread.sleep(delay * 1000);
                            }
                        } catch(InterruptedException e) {
                            // Ignore it
                        }
                    }
            }
        }
    }
}
