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

package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.ui.AemdcConfigurationDialog;
import com.headwire.aemdc.companion.Config;
import com.headwire.aemdc.companion.RunnableCompanion;
import com.headwire.aemdc.gui.MainApp;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.panels.Wrapper;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by schaefa on 1/13/17.
 */
public class AemdcPanel
    extends Wrapper
    implements ProjectComponent
{
    public static final String AEMDC_CONFIG_PROPERTIES = "aemdc-config.properties";
    public static final String LAZYBONES_FOLDER = ".lazybones";
    private Logger logger = LoggerFactory.getLogger(getClass());

    private JFXPanel panel;
    private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
    private MainApp aemdc;
    private Project project;
    private JBTabbedPane container;
    private Scene aemdcScene;
    private HyperlinkRedirectListener hyperlinkRedirectListener;

    public AemdcPanel(Project project) {
        this.project = project;
        panel = new JFXPanel();
        setContent(panel);
    }

    private void initFX(Project project)
    {
        String currentBasePath = project.getBasePath();
        if(!Config.setProjectRootPath(currentBasePath)) {
            logger.debug("Base Path: '{}' was rejected", currentBasePath);
        }
        if(aemdc == null) {
            logger.debug("Setup AEMDC Main App");
            aemdc = new MainApp();
        }
        if(aemdcScene != null && hyperlinkRedirectListener != null) {
            // Remove existing Hyper Link Redirect Listener
            WebView browser = aemdc.getBrowser();
            browser.getEngine().getLoadWorker().stateProperty().removeListener(hyperlinkRedirectListener);
        }
        aemdcScene = aemdc.getMainScene(new File(currentBasePath));

        // Add new Hyper Link Redirect Listener
        WebView browser = aemdc.getBrowser();
        hyperlinkRedirectListener = new HyperlinkRedirectListener(browser);
        browser.getEngine().getLoadWorker().stateProperty().addListener(hyperlinkRedirectListener);

        panel.setScene(aemdcScene);
    }

    public void setContainer(JBTabbedPane container) {
        this.container = container;
    }

    public boolean isShown() {
        int tabIndex = container.indexOfComponent(this);
        return tabIndex >= 0;
    }

    public void reset() {
        if(isShown()) {
            setContent(null);
            panel = new JFXPanel();
            setContent(panel);
            if(aemdcScene == null) {
                Platform.runLater(() -> {
                    initFX(project);
                });
            } else {
                panel.setScene(aemdcScene);
            }
            setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 1, false));
        }
    }

    public void display(boolean doShow) {
        if(doShow) {
            // Check if the Config to see if we need to bring up the dialog
            if(!Config.validateThisConfiguration(new File(project.getBasePath()), AEMDC_CONFIG_PROPERTIES).isEmpty()) {
                if(!showDialog()) {
                    // Show dialog why panel is not shown
                    ServiceManager.getService(project, MessageManager.class).showAlertWithArguments(
                        NotificationType.ERROR,
                        "dialog.aemdc.invalid.configuration"
                    );
                    return;
                } else {
                    aemdcScene = null;
                }
            }
            // Re-adding the Panel will create a dual-pane view. To avoid this we have to re-create the JFX Panel on each showing
            int nextTab = container.getTabCount();
            container.insertTab("AEM DC", null, this, "AEM DC UI", nextTab);
            reset();
            container.setSelectedIndex(nextTab);
        } else {
            container.remove(this);
        }
    }

    public boolean showDialog() {
        // First check if the aemdc configuration file exists. If not but there is a lazybones configuration
        // try auto configuration first
        VirtualFile aemdcConfigPropertiesFile = project.getBaseDir().findChild(AEMDC_CONFIG_PROPERTIES);
        if(aemdcConfigPropertiesFile == null) {
            if(project.getBaseDir().findChild(LAZYBONES_FOLDER) != null) {
                // Ask the user if he wants to auto configure from the lazybones configuration
                int response = ServiceManager.getService(project, MessageManager.class).showAlertWithOptions(
                    NotificationType.INFORMATION,
                    "dialog.aemdc.do.auto.configuration"
                );
                if(response == 1) {
                    try {
                        RunnableCompanion.main(new String[]{"-temp=" + project.getBasePath(), "config"});
                    } catch(IOException e) {
                        ServiceManager.getService(project, MessageManager.class).showAlertWithArguments(NotificationType.ERROR, "dialog.aemdc.failed.auto.configuration");
                    }
                }
            }
        }
        SlingServerTreeSelectionHandler slingServerTreeSelectionHandler = ServiceManager.getService(project, SlingServerTreeSelectionHandler.class);
        ServerConfiguration serverConfiguration = slingServerTreeSelectionHandler.getCurrentConfiguration();
        AemdcConfigurationDialog dialog = new AemdcConfigurationDialog(project, serverConfiguration);
        return dialog.showAndGet();
    }

    @Nullable
    public VirtualFile virtualFileBy(String fileName) {
        return fileManager.refreshAndFindFileByUrl(
            "file://" + fileName
        );
    }

    private File ensureExists(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() && !folder.mkdirs()) {
            logger.error("Folder does not exit and could not be created: '{}'", folder.getPath());
        }
        return folder;
    }

    private void extractArchive(InputStream archiveContent, File destinationFolder) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(archiveContent);
        ZipEntry entry = null;
        while((entry = zipInputStream.getNextEntry()) != null) {
            // The Name is the path of the file relative to the root of the ZIP file
            // Therefore there is no need to do it recursively
            File target = new File(destinationFolder, entry.getName());
            if(entry.isDirectory()) {
                if(!target.exists()) {
                    target.mkdir();
                }
            } else {
                if(target.exists()) {
                    target.delete();
                }
                FileOutputStream fos = null;
                try {
                    byte[] buffer = new byte[1024];
                    fos = new FileOutputStream(target);
                    int len = 0;
                    while((len = zipInputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                } finally {
                    if(fos != null) {
                        fos.close();
                    }
                }
            }
        }
    }

    public void initComponent() {
        String pluginPath = PathManager.getPluginsPath();
        logger.info("File Location: " + pluginPath);
        String defaultFolderPath = pluginPath + "/" + "aemdc-files";
        File defaultFolderFile = ensureExists(defaultFolderPath);
        try {
            ClassLoader cl = getClass().getClassLoader();
            InputStream aemdFiles = cl.getResourceAsStream("aemdc-files.zip");
            extractArchive(aemdFiles, defaultFolderFile);
        } catch(IOException e) {
            logger.error("Failed to extract AEMDC Files", e);
        } catch(Throwable e) {
            logger.error("Failed to extract AEMDC Files (Unkown Cause)", e);
        }
        VirtualFile defaultFolder = virtualFileBy(defaultFolderPath);

        Platform.setImplicitExit(false);
        ServerConfigurationManager serverConfigurationManager = ServiceManager.getService(project, ServerConfigurationManager.class);
        Platform.runLater(() -> {
            initFX(project);
        });
    }
}
