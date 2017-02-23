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

import com.headwire.aem.tooling.intellij.config.ConfigurationListener;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.ui.AemdcConfigurationDialog;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.headwire.aemdc.companion.Config;
import com.headwire.aemdc.gui.MainApp;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.components.panels.Wrapper;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by schaefa on 1/13/17.
 */
public class WebContentFXPanel
    extends Wrapper
{
    private Logger logger = LoggerFactory.getLogger(getClass());

    private JFXPanel panel;
    private WebView browser;
    private WebEngine webEngine;
    private String url = " https://www.headwire.com/t/intellij.html";

    public WebContentFXPanel() {

        Platform.setImplicitExit(false);
        panel = new JFXPanel();
        setContent(panel);
        Platform.runLater(() -> {
            initFX();
        });
    }

    private void initFX()
    {
        Scene scene = new Scene(new Group());
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);

        browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.load(url);

        scene.setRoot(browser);
        panel.setScene(scene);
    }
}
