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

import com.intellij.ui.components.panels.Wrapper;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    private String url = "https://www.headwire.com/t/intellij-1.0.3.3.html";
    private String altUrl = "https://www.headwire.com/t/intellij-1.0.3.1.html";

    public WebContentFXPanel() {

        Platform.setImplicitExit(false);
        panel = new JFXPanel();
        setContent(panel);

        String actualUrl = url;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            if (response.getStatusLine().getStatusCode() != 200) {
                actualUrl = altUrl;
            }
        } catch(IOException e) {
            // Ignore
        }
        final String targetUrl = actualUrl;
        Platform.runLater(() -> {
            initFX(targetUrl);
        });
    }

    private void initFX(String pageUrl)
    {
        Scene scene = new Scene(new Group());
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);

        browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.load(pageUrl);

        scene.setRoot(browser);
        panel.setScene(scene);

        webEngine.getLoadWorker().stateProperty().addListener(
            new HyperlinkRedirectListener(browser)
        );
    }
}
