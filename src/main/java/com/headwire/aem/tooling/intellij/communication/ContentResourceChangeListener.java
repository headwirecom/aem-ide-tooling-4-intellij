package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.config.general.AEMPluginConfiguration;
import com.intellij.analysis.AnalysisScopeBundle;
import com.intellij.compiler.impl.ModuleCompileScope;
import com.intellij.compiler.server.BuildManager;
import com.intellij.compiler.server.BuildManagerListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.importing.MavenImporter;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.project.MavenConsole;
import org.jetbrains.idea.maven.project.MavenEmbeddersManager;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.headwire.aem.tooling.intellij.communication.ServerConnectionManager.FileChangeType;

/**
 * Created by schaefa on 5/12/15.
 */
public class ContentResourceChangeListener {

    private AEMPluginConfiguration pluginConfiguration;
    private MessageBusConnection messageBusConnection;

    public ContentResourceChangeListener(@NotNull Project project, @NotNull final ServerConnectionManager serverConnectionManager, @NotNull MessageBusConnection messageBusConnection) {
        pluginConfiguration = ServiceManager.getService(AEMPluginConfiguration.class);
        // Create the Listener on File Changes
        VirtualFileManager.getInstance().addVirtualFileListener(
            new VirtualFileAdapter() {

                @Override
                public void contentsChanged(@NotNull VirtualFileEvent event) {
                    executeMake(event);
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        serverConnectionManager.handleFileChange(event.getFile(), FileChangeType.CHANGED);
                    }
                }

                @Override
                public void fileCreated(@NotNull VirtualFileEvent event) {
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        serverConnectionManager.handleFileChange(event.getFile(), FileChangeType.CREATED);
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
                        serverConnectionManager.handleFileChange(file, FileChangeType.DELETED);
                    }
                }

                @Override
                public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        // Delete the JCR Resource before the file is gone
                        serverConnectionManager.handleFileChange(event.getFile(), FileChangeType.DELETED);
                    }
                }

                @Override
                public void fileMoved(@NotNull VirtualFileMoveEvent event) {
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        // After the move we create the new file
                        VirtualFile file = event.getFile();
                        serverConnectionManager.handleFileChange(file, FileChangeType.CREATED);
                    }
                }

                @Override
                public void fileCopied(@NotNull VirtualFileCopyEvent event) {
                    if(serverConnectionManager.checkSelectedServerConfiguration(true, true)) {
                        serverConnectionManager.handleFileChange(event.getFile(), FileChangeType.CREATED);
                    }
                }
            },
            project
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

    private void executeMake(final VirtualFileEvent event) {
        if(pluginConfiguration.isIncrementalBuilds()) {
            // Check if the file is a Java Class and if os build it
            VirtualFile file = event.getFile();
            if("java".equalsIgnoreCase(file.getExtension())) {
                final Project project = ProjectUtil.guessProjectForFile(event.getFile());
                ProgressManager.getInstance().run(
                    new Task.Backgroundable(project, AnalysisScopeBundle.message("analyzing.project"), true) {
                        public void run(@NotNull ProgressIndicator indicator) {
                            executeMakeInUIThread(event);
                        }
                    }
                );
            }
        }
    }

    private void executeMakeInUIThread(VirtualFileEvent event) {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for(final Project project : projects) {
            if(project.isInitialized() && !project.isDisposed() &&
                project.isOpen() && !project.isDefault()) {
                final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
                final Module module = projectFileIndex.getModuleForFile(event.getFile());
                if(module != null) {
                    final CompilerManager compilerManager = CompilerManager.getInstance(project);
                    if(!compilerManager.isCompilationActive() &&
                        !compilerManager.isExcludedFromCompilation(event.getFile()) &&
                        !compilerManager.isUpToDate(new ModuleCompileScope(module, false))) {
                        // Changed file found in module. Make it.
                        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                            public void run() {
                                compilerManager.make(module, null);
                            }
                        });
                    }
                }
            }
//            }
        }
    }
}
