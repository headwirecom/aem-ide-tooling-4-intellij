package com.headwire.aem.tooling.intellij.communication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import org.jetbrains.annotations.NotNull;

import static com.headwire.aem.tooling.intellij.communication.ServerConnectionManager.FileChangeType;

/**
 * Created by schaefa on 5/12/15.
 */
public class ContentResourceChangeListener {

    public ContentResourceChangeListener(@NotNull Project project, @NotNull final ServerConnectionManager serverConnectionManager) {
        // Create the Listener on File Changes
        VirtualFileManager.getInstance().addVirtualFileListener(
            new VirtualFileAdapter() {

                @Override
                public void contentsChanged(@NotNull VirtualFileEvent event) {
                    serverConnectionManager.handleFileChange(event.getFile(), FileChangeType.CHANGED);
                }

                @Override
                public void fileCreated(@NotNull VirtualFileEvent event) {
                    serverConnectionManager.handleFileChange(event.getFile(), FileChangeType.CREATED);
                }

                @Override
                public void fileDeleted(@NotNull VirtualFileEvent event) {
                    // When a file is deleted the remove does not work anymore because it is gone -> Delete the file
                    // on line before the actual deletion
                }

                @Override
                public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
                    // We delete before the Move because the original file still exists
                    VirtualFile file = event.getFile();
                    serverConnectionManager.handleFileChange(file, FileChangeType.DELETED);
                }

                @Override
                public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
                    // Delete the JCR Resource before the file is gone
                    serverConnectionManager.handleFileChange(event.getFile(), FileChangeType.DELETED);
                }

                @Override
                public void fileMoved(@NotNull VirtualFileMoveEvent event) {
                    // After the move we create the new file
                    VirtualFile file = event.getFile();
                    serverConnectionManager.handleFileChange(file, FileChangeType.CREATED);
                }

                @Override
                public void fileCopied(@NotNull VirtualFileCopyEvent event) {
                    serverConnectionManager.handleFileChange(event.getFile(), FileChangeType.CREATED);
                }
            },
            project
        );
    }
}
