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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;

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

        webEngine.getLoadWorker().stateProperty().addListener(
            new HyperlinkRedirectListener(browser)
        );
//        webEngine.locationProperty().addListener(
//            new ChangeListener<String>() {
//                @Override
//                public void changed(ObservableValue<? extends String> observable, final String oldValue, String newValue)
//                {
//                    try {
//                        webEngine.
//                        URI uri = new URI(newValue);
//                        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
//                        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
//                            try {
//                                desktop.browse(uri);
//                            } catch (Exception e) {
//                                logger.warn("Failed to open link: '" + uri + "' in external browser", e);
//                            }
//                        }
//                    } catch (URISyntaxException e) {
//                        logger.warn("Link: '" + newValue + "' is not valid", e);
//                    }
//                }
//            }
//        );
    }

    public static class HyperlinkRedirectListener implements ChangeListener<Worker.State>, EventListener {
        private static final Logger LOGGER = LoggerFactory.getLogger(HyperlinkRedirectListener.class);

        private static final String CLICK_EVENT = "click";
        private static final String ANCHOR_TAG = "a";

        private final WebView webView;

        public HyperlinkRedirectListener(WebView webView) {
            this.webView = webView;
        }

        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
            if(State.SUCCEEDED.equals(newValue)) {
                Document document = webView.getEngine().getDocument();
                NodeList anchors = document.getElementsByTagName(ANCHOR_TAG);
                for(int i = 0; i < anchors.getLength(); i++) {
                    Node node = anchors.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    eventTarget.addEventListener(CLICK_EVENT, this, false);
                }
            }
        }

        @Override
        public void handleEvent(Event event) {
            HTMLAnchorElement anchorElement = (HTMLAnchorElement) event.getCurrentTarget();
            String href = anchorElement.getHref();

            if(Desktop.isDesktopSupported()) {
                openLinkInSystemBrowser(href);
            } else {
                LOGGER.warn("OS does not support desktop operations like browsing. Cannot open link '{}'.", href);
            }

            event.preventDefault();
        }

        private void openLinkInSystemBrowser(String url) {
            LOGGER.debug("Opening link '{}' in default system browser.", url);

            try {
                URI uri = new URI(url);
                Desktop.getDesktop().browse(uri);
            } catch (Throwable e) {
                LOGGER.error("Error on opening link '{}' in system browser.", url);
            }
        }
    }
}
