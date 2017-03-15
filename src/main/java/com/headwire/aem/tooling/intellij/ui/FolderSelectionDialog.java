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

package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FolderSelectionDialog extends DialogWrapper {
    private JPanel contentPane;
    private TextFieldWithBrowseButton parentFolder;
    private Project project;
    private VirtualFile baseDir;

    public FolderSelectionDialog(@NotNull Project project) {
        super(project);
        setTitle(AEMBundle.message("dialog.aemdc.files.parent.title"));
        this.project = project;
        this.baseDir = project.getBaseDir();
        setModal(true);
        setUpDialog();
        init();
    }

    public void setUpDialog() {
        String basePath = baseDir.getPath();
        // Set plugin-based aemdc-files
        parentFolder.setTextFieldPreferredWidth(50);
        parentFolder.setText("..");
        parentFolder.addBrowseFolderListener(new BaseTextBrowseFolderListener(project, baseDir));
    }

    public String getFolder() {
        return parentFolder.getText();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private static class BaseTextBrowseFolderListener
        extends TextBrowseFolderListener
    {
        private VirtualFile baseDir;

        public BaseTextBrowseFolderListener(Project project, VirtualFile baseDir) {
            super(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project);
            this.baseDir = baseDir;
        };

        @Nullable
        @Override
        protected VirtualFile getInitialFile() {
            // Provide the initila file to make sure the clone folder is preset and does not
            // point to the IntelliJ installation folder
            String path = getComponentText();
            VirtualFile targetFolder = baseDir.findFileByRelativePath(path);
            if(targetFolder == null) {
                targetFolder = baseDir.getFileSystem().findFileByPath(path);
            }
            return targetFolder == null ? baseDir : targetFolder;
        }
        @NotNull
        @Override
        protected String chosenFileToResultingText(@NotNull VirtualFile chosenFile) {
            String answer = "";
            String newPath = chosenFile.getPath();
            String basePath = baseDir.getPath();
            if(newPath.startsWith(basePath)) {
                answer = "." + basePath.substring(newPath.length());
            } else {
                answer = newPath;
            }
            return answer;
        }
    }
}
