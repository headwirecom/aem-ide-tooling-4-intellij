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

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.headwire.aemdc.companion.Config;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import git4idea.commands.GitCommand;
import git4idea.commands.GitSimpleHandler;
import git4idea.config.GitExecutableValidator;
import git4idea.config.GitVcsApplicationSettings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.headwire.aemdc.companion.Constants.CONFIGPROP_SOURCE_FOLDER;
import static com.headwire.aemdc.companion.Constants.CONFIGPROP_TARGET_JAVA_FOLDER;
import static com.headwire.aemdc.companion.Constants.CONFIGPROP_TARGET_JAVA_PACKAGE;
import static com.headwire.aemdc.companion.Constants.CONFIGPROP_TARGET_JAVA_PACKAGE_FOLDER;
import static com.headwire.aemdc.companion.Constants.CONFIGPROP_TARGET_OSGI_SUBFOLDER;
import static com.headwire.aemdc.companion.Constants.CONFIGPROP_TARGET_PROJECT_APPS_FOLDER;
import static com.headwire.aemdc.companion.Constants.CONFIGPROP_TARGET_PROJECT_CONF_FOLDER;
import static com.headwire.aemdc.companion.Constants.CONFIGPROP_TARGET_PROJECT_DESIGN_FOLDER;
import static com.headwire.aemdc.companion.Constants.CONFIGPROP_TARGET_UI_FOLDER;
import static com.headwire.aemdc.runner.ConfigPropsRunner.CONFIG_PROPS_FILENAME;

public class AemdcConfigurationDialog extends DialogWrapper {

    public static final String AEMDC_FILES_DEFAULT = "aemdc-files";
    public static final String PULL_BUTTON = "Pull";
    public static final String CLONE_BUTTON = "Clone";
    public static final String RUN_MODES_DEFAULT = "/configuration";

    public static final int MAX_RELATIVE_LEVEL = 2;

    private JPanel contentPane;
    private JTextField javaPackage;
    private JTextField runModes;
    private TextFieldWithBrowseButton aemdcFiles;
    private TextFieldWithBrowseButton uiFolder;
    private TextFieldWithBrowseButton javaFolder;
    private JTextArea feedback;
    private JButton resetButton;
    private JButton validateButton;
    private JTextField appsFolderName;
    private JCheckBox confSameAsApps;
    private JTextField confFolderName;
    private JTextField designFolderName;
    private JCheckBox designSameAsApps;
    private JTextField javaPath;
    private JCheckBox derivedFromJavaPackage;
    private JButton cloneOrPullButton;

    private Project project;
    private VirtualFile baseDir;

    private MessageManager messageManager;

    public AemdcConfigurationDialog(@NotNull Project project, @NotNull ServerConfiguration serverConfiguration) {
        super(project);
        this.project = project;
        this.baseDir = project.getBaseDir();
        messageManager = ComponentProvider.getComponent(project, MessageManager.class);

        setTitle(AEMBundle.message("dialog.aemdc.configuration.title"));
        setModal(true);
        setUpDialog(serverConfiguration);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public void setUpDialog(ServerConfiguration serverConfiguration) {
        String basePath = baseDir.getPath();
        // Set plugin-based aemdc-files
        aemdcFiles.setTextFieldPreferredWidth(50);
        aemdcFiles.setText(AEMDC_FILES_DEFAULT);
        aemdcFiles.addBrowseFolderListener(new BaseTextBrowseFolderListener(project, baseDir));
        aemdcFiles.getChildComponent().addFocusListener(new FolderTextFieldFocusListener(project, aemdcFiles, false));
        aemdcFiles.getChildComponent().addFocusListener(new AemdcFilesChangeListener());

        // Find first content root and set it as UI Folder
        String contentRoot = null;
        String javaRoot = null;
        for(Module module: serverConfiguration.getModuleList()) {
            if(module.isSlingPackage()) {
                if(contentRoot == null) {
                    List<String> roots = module.getUnifiedModule().getContentDirectoryPaths();
                    if(!roots.isEmpty()) {
                        contentRoot = roots.get(0);
                        if(contentRoot.startsWith(basePath)) {
                            contentRoot = contentRoot.substring(basePath.length() + 1);
                        }
                    }
                }
            } else if(module.isOSGiBundle()) {
                if(javaRoot == null) {
                    List<String> roots = module.getUnifiedModule().getSourceDirectoryPaths();
                    if(!roots.isEmpty()) {
                        javaRoot = roots.get(0);
                        if(javaRoot.startsWith(basePath)) {
                            javaRoot = javaRoot.substring(basePath.length() + 1);
                        }
                    }
                }
            }
        }
        uiFolder.setText(contentRoot);
        uiFolder.addBrowseFolderListener(new BasePathTextBrowseFolderListener(project, baseDir));
        uiFolder.getChildComponent().addFocusListener(new FolderTextFieldFocusListener(project, uiFolder, true));
        VirtualFile contentRootFile = baseDir.findFileByRelativePath(contentRoot);
        if(contentRootFile != null) {
            VirtualFile appsFile = contentRootFile.findFileByRelativePath("apps");
            if(appsFile != null) {
                VirtualFile[] children = appsFile.getChildren();
                if(children.length > 0) {
                    appsFolderName.setText(children[0].getName());
                }
            }
            boolean done = false;
            VirtualFile designFile = contentRootFile.findFileByRelativePath("design");
            if(designFile != null) {
                VirtualFile[] children = designFile.getChildren();
                if(children.length > 0) {
                    designFolderName.setText(children[0].getName());
                    designSameAsApps.setSelected(false);
                    done = true;
                }
            }
            if(!done) {
                designFolderName.setText(appsFolderName.getText());
                designSameAsApps.setSelected(true);
            }
            done = false;
            VirtualFile confFile = contentRootFile.findFileByRelativePath("conf");
            if(confFile != null) {
                VirtualFile[] children = confFile.getChildren();
                if(children.length > 0) {
                    confFolderName.setText(children[0].getName());
                    confSameAsApps.setSelected(false);
                    done = true;
                }
            }
            if(!done) {
                confFolderName.setText(appsFolderName.getText());
                confSameAsApps.setSelected(true);
            }
        }

        javaFolder.setText(javaRoot);
        javaFolder.addBrowseFolderListener(new BasePathTextBrowseFolderListener(project, baseDir));
        javaFolder.getChildComponent().addFocusListener(new FolderTextFieldFocusListener(project, javaFolder, true));

        // Find first bundle and set first source as Java folder
        // Set default package name
        javaPackage.setText("com.headwire.aemdc.samples");
        if(derivedFromJavaPackage.isSelected()) {
            javaPath.setText((javaPackage.getText().replaceAll("\\.", "/")));
        }
        // Set default OSGi Configuration
        runModes.setText(RUN_MODES_DEFAULT);
        File configurationFile = new File(basePath, CONFIG_PROPS_FILENAME);
        Config config = new Config(new File(basePath), CONFIG_PROPS_FILENAME);
        // Fill fields if config file is found
        if(configurationFile.exists()) {
            String value = config.getProperties().getProperty(CONFIGPROP_SOURCE_FOLDER);
            if(value != null && !value.isEmpty()) {
                aemdcFiles.setText(value);
            }
            value = config.getProperties().getProperty(CONFIGPROP_TARGET_UI_FOLDER);
            if(value != null && !value.isEmpty()) {
                uiFolder.setText(value);
            }
            String apps = config.getProperties().getProperty(CONFIGPROP_TARGET_PROJECT_APPS_FOLDER);
            if(apps != null && !apps.isEmpty()) {
                appsFolderName.setText(apps);
            }
            String conf = config.getProperties().getProperty(CONFIGPROP_TARGET_PROJECT_CONF_FOLDER);
            if(conf != null && !conf.isEmpty()) {
                confFolderName.setText(conf);
                confSameAsApps.setSelected(apps != null && apps.equals(conf));
                if(confSameAsApps.isSelected()) {
                    confFolderName.setEnabled(false);
                }
            }
            String design = config.getProperties().getProperty(CONFIGPROP_TARGET_PROJECT_DESIGN_FOLDER);
            if(design != null && !design.isEmpty()) {
                designFolderName.setText(design);
                designSameAsApps.setSelected(apps != null && apps.equals(design));
                if(designSameAsApps.isSelected()) {
                    designFolderName.setEnabled(false);
                }
            }
            String sourceFolder = config.getProperties().getProperty(CONFIGPROP_TARGET_JAVA_FOLDER);
            if(sourceFolder != null && !sourceFolder.isEmpty()) {
                javaFolder.setText(sourceFolder);
            }
            String aPackage = config.getProperties().getProperty(CONFIGPROP_TARGET_JAVA_PACKAGE);
            if(aPackage != null && !aPackage.isEmpty()) {
                javaPackage.setText(aPackage);
            }
            String packageAsFolder = config.getProperties().getProperty(CONFIGPROP_TARGET_JAVA_PACKAGE_FOLDER);
            if(packageAsFolder != null && packageAsFolder.startsWith(sourceFolder)) {
                packageAsFolder = packageAsFolder.substring(sourceFolder.length() + 1);
            }
            if(packageAsFolder != null && !packageAsFolder.isEmpty()) {
                javaPath.setText(packageAsFolder);
                derivedFromJavaPackage.setSelected(aPackage != null && packageAsFolder.equals(aPackage.replaceAll("\\.", "/")));
                if(derivedFromJavaPackage.isSelected()) {
                    javaPath.setEnabled(false);
                }
            }
            value = config.getProperties().getProperty(CONFIGPROP_TARGET_OSGI_SUBFOLDER);
            if(value != null && !value.isEmpty()) {
                runModes.setText(value);
            }
        }
        confSameAsApps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confFolderName.setEnabled(!confSameAsApps.isSelected());
                if(confSameAsApps.isSelected()) {
                    confFolderName.setText(appsFolderName.getText());
                }
            }
        });
        designSameAsApps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                designFolderName.setEnabled(!designSameAsApps.isSelected());
                if(designSameAsApps.isSelected()) {
                    designFolderName.setText(appsFolderName.getText());
                }
            }
        });
        appsFolderName.getDocument().addDocumentListener(
            new DependentDocumentListener(confSameAsApps, new DoIt() {
                @Override
                public void doIt() {
                    confFolderName.setText(appsFolderName.getText());
                }
            })
        );
        appsFolderName.getDocument().addDocumentListener(
            new DependentDocumentListener(designSameAsApps, new DoIt() {
                @Override
                public void doIt() {
                    designFolderName.setText(appsFolderName.getText());
                }
            })
        );
        derivedFromJavaPackage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                javaPath.setEnabled(!derivedFromJavaPackage.isSelected());
                if(derivedFromJavaPackage.isSelected()) {
                    javaPath.setText(javaPackage.getText().replaceAll("\\.", "/"));
                }
            }
        });
        javaPackage.getDocument().addDocumentListener(
            new DependentDocumentListener(derivedFromJavaPackage, new DoIt() {
                @Override
                public void doIt() {
                    javaPath.setText(javaPackage.getText().replaceAll("\\.", "/"));
                }
            })
        );

        List < String > validationReports = config.validateConfiguration();
        feedback.setText("");
        for(String report: validationReports) {
            feedback.append(report);
        }

        validateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // - Creates a backup of the original file (if there)
                handleConfigurationFile(new File(basePath, CONFIG_PROPS_FILENAME), false);
            }
        });

        adjustGitHandleButton();
        cloneOrPullButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cloneAemdcFilesAndSet();
            }
        });
    }

    private void adjustGitHandleButton() {
        String aemdcFilesFolderPath = aemdcFiles.getText();
        File aemdcFilesFolder = getFile(baseDir.getPath(), aemdcFilesFolderPath);
        boolean aemdcFilesExists = aemdcFilesFolder.exists();
        boolean aemdcFilesPullabled = false;
        if(aemdcFilesExists) {
            if(aemdcFilesFolder.isDirectory()) {
                File aemdcFilesGitFolder = new File(aemdcFilesFolder, ".git");
                aemdcFilesPullabled = aemdcFilesGitFolder.exists() && aemdcFilesGitFolder.isDirectory();
            }
        }
        // Check if the aemdc-files folder exists
        if(aemdcFilesPullabled) {
            cloneOrPullButton.setText(PULL_BUTTON);
        } else {
            cloneOrPullButton.setText(CLONE_BUTTON);
        }
    }

    private void cloneAemdcFilesAndSet() {
        // First check if the Git Executable is set
        final GitExecutableValidator validator = GitVcs.getInstance(project).getExecutableValidator();
        if(validator != null) {
            String pathToGit = GitVcsApplicationSettings.getInstance().getPathToGit();
            if(!validator.isExecutableValid(pathToGit)) {
                // Tell the user to set Git in the preferences
                feedback.append(AEMBundle.message("aemdc.panel.git.path.invalid.description", pathToGit));
            } else {
                if(PULL_BUTTON.equals(cloneOrPullButton.getText())) {
                    String aemdcFilesFolderPath = aemdcFiles.getText();
                    File aemdcFilesFolder = getFile(baseDir.getPath(), aemdcFilesFolderPath);
                    GitSimpleHandler handler = new GitSimpleHandler(project, aemdcFilesFolder, GitCommand.PULL);
                     try {
                        handler.run();
                         feedback.append(AEMBundle.message("aemdc.panel.aemdc.files.git.repo.pulled.successfully", aemdcFilesFolder.getAbsolutePath()));
                    } catch(VcsException e) {
                        feedback.append(AEMBundle.message("aemdc.panel.aemdc.files.could.not.be.pulled.description", aemdcFilesFolder.getAbsolutePath(), e.getMessage()));
                    }
                } else {
                    // Ask user to enter the parent folder of the where the aemdc-files are extracted to
                    FolderSelectionDialog dialog = new FolderSelectionDialog(project);
                    if(dialog.showAndGet()) {
                        String folderPath = dialog.getFolder();
                        File folder = getFile(baseDir.getPath(), folderPath);
                        if(!folder.exists()) {
                            feedback.append(AEMBundle.message("aemdc.panel.parent.folder.for.aemdc.files.does.not.exist.description", folder.getAbsolutePath()));
                        } else if(!folder.isDirectory()) {
                            feedback.append(AEMBundle.message("aemdc.panel.parent.folder.for.aemdc.files.is.not.a.folder.description", folder.getAbsolutePath()));
                        } else {
                            GitSimpleHandler handler = new GitSimpleHandler(project, folder, GitCommand.CLONE);
                            handler.addParameters("https://github.com/headwirecom/aemdc-files.git");
                            try {
                                handler.run();
                                // After successful cloning update the text field
                                aemdcFiles.setText(adjustPath(project, folderPath + "/aemdc-files", true));
                                cloneOrPullButton.setText(PULL_BUTTON);
                                feedback.append(AEMBundle.message("aemdc.panel.aemdc.files.git.repo.cloned.successfully", folder.getAbsolutePath()));
                            } catch(VcsException e) {
                                feedback.append(AEMBundle.message("aemdc.panel.aemdc.files.could.not.be.cloned.description", folder.getAbsolutePath(), e.getMessage()));
                            } catch(BadFolderException e) {
                                feedback.append(AEMBundle.message("aemdc.panel.aemdc.files.could.not.be.cloned.description", folder.getAbsolutePath(), "Folder does nto exist"));
                            }
                        }
                    }
                }
            }
        }
    }

    protected void doOKAction() {
        String basePath = baseDir.getPath();
        boolean isOk = handleConfigurationFile(new File(basePath, CONFIG_PROPS_FILENAME), true);
        if(isOk) {
            super.doOKAction();
        } else {
            feedback.append("Failed to write configuration file -> fix it or hit cancel to close w/o saving");
        }
    }

    private boolean handleConfigurationFile(File targetFile, boolean write) {
        // - Writes the changes out to the file
        boolean answer = false;
        try {
            ClassLoader cl = getClass().getClassLoader();
            //AS TODO: a folder with name aemd.files translatese to 'aemdc/files when looking it up with the Class Loader
            String filePath = "types/config/aemdc/files/" + CONFIG_PROPS_FILENAME;
            InputStream is = cl.getResourceAsStream(filePath);
            if(is != null) {
                File tempTargetFile = new File(targetFile.getParent(), targetFile.getName() + ".temp");
                OutputStream os = FileUtils.openOutputStream(tempTargetFile);

                String content = IOUtils.toString(is);
                content = content.replace("../aemdc-files", aemdcFiles.getText());
                content = content.replace("{{ PH_TARGET_UI_PROJECT_FOLDER }}/src/main/content/jcr_root", uiFolder.getText());

                content = content.replace("{{ PH_TARGET_PROJECT_APPS_FOLDER }}", appsFolderName.getText());
                content = content.replace("{{ PH_TARGET_PROJECT_CONF_FOLDER }}", confFolderName.getText());
                content = content.replace("{{ PH_TARGET_PROJECT_DESIGN_FOLDER }}", designFolderName.getText());

                content = content.replace("{{ PH_TARGET_CORE_PROJECT_FOLDER }}/src/main/java", javaFolder.getText());
                content = content.replace("{{ PH_TARGET_JAVA_PACKAGE }}", javaPackage.getText());

                content = content.replace("{{TARGET_JAVA_FOLDER}}/{{ PH_TARGET_JAVA_PACKAGE_FOLDER }}", "{{TARGET_JAVA_FOLDER}}/" + javaPath.getText());

                content = content.replace("{{ PH_TARGET_OSGI_SUBFOLDER }}", runModes.getText());

                IOUtils.write(content, os);
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(is);
                // - validates the configuration
                List<String> validationReports = Config.validateThisConfiguration(tempTargetFile.getParentFile(), tempTargetFile.getName());
                // - Writes out feedback
                if(!validationReports.isEmpty()) {
                    feedback.append("Validation Report of Current Settings");
                    for(String report : validationReports) {
                        feedback.append(report);
                    }
                } else {
                    feedback.append("Current Settings are valid");
                }
                if(write) {
                    if(validationReports.isEmpty()) {
                        if(targetFile.exists()) {
                            targetFile.renameTo(new File(targetFile.getParent(), targetFile.getName() + ".back"));
                        }
                        if(!tempTargetFile.renameTo(targetFile)) {
                            feedback.append("Rename to the Target File failed");
                        } else {
                            feedback.append("Target File: "+ targetFile.getName() + " was created successfully");
                            answer = true;
                        }
                    } else {
                        feedback.append("Errors -> Config File was not created");
                    }
                } else {
                    answer = validationReports.isEmpty();
                }
            } else {
                feedback.append("Resource with Path: '" + filePath + "' not found");
            }
        } catch(IOException e) {
            feedback.append("Failed to read aemdc config file template");
        }
        return answer;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        feedback = new Feedback();
    }

    private static interface DoIt {
        public void doIt();
    }

    private static class DependentDocumentListener
        implements DocumentListener
    {
        private DoIt doIt;
        private JCheckBox switchBox;

        public DependentDocumentListener(JCheckBox switchBox, DoIt doIt) {
            this.switchBox = switchBox;
            this.doIt = doIt;
        }

        @Override
        public void insertUpdate(DocumentEvent e) { handleChange(); }

        @Override
        public void removeUpdate(DocumentEvent e) { handleChange(); }

        @Override
        public void changedUpdate(DocumentEvent e) { handleChange(); }

        private void handleChange() {
            if(switchBox.isSelected()) {
                doIt.doIt();
            }
        }
    }

    private class BasePathTextBrowseFolderListener
        extends BaseTextBrowseFolderListener
    {
        public BasePathTextBrowseFolderListener(Project project, VirtualFile projectFolder) {
            super(project, projectFolder);
        };

        @NotNull
        @Override
        protected String chosenFileToResultingText(@NotNull VirtualFile chosenFile) {
            String answer = "";
            String newPath = chosenFile.getPath();
            String basePath = projectFolder.getPath();
            if(newPath.length() > basePath.length() + 1) {
              answer = newPath.substring(basePath.length() + 1);
            }
            return answer;
        }
    }

    private class BaseTextBrowseFolderListener
        extends TextBrowseFolderListener
    {
        protected VirtualFile projectFolder;

        public  BaseTextBrowseFolderListener(Project project, VirtualFile projectFolder) {
            super(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project);
            myFileChooserDescriptor.setRoots(
                baseDir,
                baseDir.getFileSystem().findFileByPath("/")
            );
            this.projectFolder = projectFolder;
        };

        @Nullable
        @Override
        protected VirtualFile getInitialFile() {
            String path = getComponentText();
            VirtualFile targetFolder = projectFolder.findFileByRelativePath(path);
            if(targetFolder == null) {
                targetFolder = projectFolder.getFileSystem().findFileByPath(path);
            }
            return targetFolder == null ? projectFolder : targetFolder;
        }

        @NotNull
        @Override
        protected String chosenFileToResultingText(@NotNull VirtualFile chosenFile) {
            try {
                return adjustPath(getProject(), chosenFile.getPath(), true);
            } catch(BadFolderException e) {
                if(e.isFolderDoesNotExist()) {
                    feedback.append(AEMBundle.message("aemdc.panel.aemdc.files.path.does.not.exist", e.getPath()));
                } else {
                    feedback.append(AEMBundle.message("aemdc.panel.aemdc.files.parent.path.does.not.exist", e.getPath()));
                }
            }
            return chosenFile.getPath();
        }

        @Override
        protected void onFileChosen(@NotNull VirtualFile chosenFile) {
            super.onFileChosen(chosenFile);
            adjustGitHandleButton();
        }
    }

    private String adjustPath(Project project, String givenPath, boolean targetMustExist)
        throws BadFolderException
    {
        String answer = givenPath;
        String basePath = project.getBasePath();
        File base = new File(basePath);
        File target = getFile(base, answer);
        if(targetMustExist && !target.exists()) {
            throw new BadFolderException().setFolderDoesNotExist(true).setPath(answer);
        } else {
            File parent = target.getParentFile();
            if(parent != null && !parent.exists()) {
                throw new BadFolderException().setParentFolderDoesNotExist(true).setPath(answer);
            }
        }
        if(target.isAbsolute()) {
            answer = adjustRelativePath(answer, 0, base, MAX_RELATIVE_LEVEL);
        }

        return answer.replaceAll("\\\\", "/");
    }

    private String adjustRelativePath(String folderPath, int level, File parentFolder, int maxLevel ) {
        if(parentFolder == null || level > maxLevel) {
            return folderPath;
        } else {
            String basePath = parentFolder.getPath();
            basePath = basePath.replaceAll("\\\\", "/");
            if(folderPath.startsWith(basePath)) {
                if(level == 0) {
                    String answer = folderPath.substring(basePath.length());
                    if(!answer.isEmpty()) {
                        if(answer.charAt(0) == '/') {
                            answer = answer.substring(1);
                        }
                    }
                    return answer;
                } else {
                    String suffix = "";
                    for(int i = 0; i < level; i++) {
                        suffix += (i > 0 ? "/" : "") + "..";
                    }
                    return suffix + folderPath.substring(basePath.length());
                }
            } else {
                return adjustRelativePath(folderPath, level + 1, parentFolder.getParentFile(), maxLevel);
            }
        }
    }

    private File getFile(String basePath, String filePath) {
        return getFile(new File(basePath), filePath);
    }

    private File getFile(File baseFolder, String filePath) {
        File answer = new File(filePath);
        if(!answer.exists() && !answer.isAbsolute()) {
            answer = new File(baseFolder, filePath);
        }
        return answer;
    }

    public static class BadFolderException
        extends Exception
    {
        private boolean folderDoesNotExist;
        private boolean parentFolderDoesNotExist;
        private String path;

        public boolean isFolderDoesNotExist() {
            return folderDoesNotExist;
        }

        public boolean isParentFolderDoesNotExist() {
            return parentFolderDoesNotExist;
        }

        public String getPath() {
            return path;
        }

        public BadFolderException setFolderDoesNotExist(boolean folderDoesNotExist) {
            this.folderDoesNotExist = folderDoesNotExist;
            return this;
        }

        public BadFolderException setParentFolderDoesNotExist(boolean parentFolderDoesNotExist) {
            this.parentFolderDoesNotExist = parentFolderDoesNotExist;
            return this;
        }

        public BadFolderException setPath(String path) {
            this.path = path;
            return this;
        }
    }

    private class AemdcFilesChangeListener
        implements FocusListener
    {
        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent event) {
            adjustGitHandleButton();
        }
    }

    private class FolderTextFieldFocusListener
        implements FocusListener
    {
        private Project project;
        private TextFieldWithBrowseButton textFieldWithBrowseButton;
        private boolean leafFolderMustExist;

        public FolderTextFieldFocusListener(Project project, TextFieldWithBrowseButton textFieldWithBrowseButton, boolean leafFolderMustExist) {
            this.project = project;
            this.textFieldWithBrowseButton = textFieldWithBrowseButton;
            this.leafFolderMustExist = leafFolderMustExist;
        }

        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent event) {
            try {
                textFieldWithBrowseButton.setText(
                    adjustPath(project, textFieldWithBrowseButton.getText(), leafFolderMustExist)
                );
            } catch(BadFolderException e) {
                if(e.isFolderDoesNotExist()) {
                    feedback.append(AEMBundle.message("aemdc.panel.aemdc.files.path.does.not.exist", e.getPath()));
                } else {
                    feedback.append(AEMBundle.message("aemdc.panel.aemdc.files.parent.path.does.not.exist", e.getPath()));
                }
            }
        }
    }

    private class Feedback
        extends JTextArea
    {
        private DateFormat dateFormatter = new SimpleDateFormat("hh:mm:ss");

        public void append(String message) {
            super.append(dateFormatter.format(new Date()) + ": " + message + "\n\n");
        }
    }
}
