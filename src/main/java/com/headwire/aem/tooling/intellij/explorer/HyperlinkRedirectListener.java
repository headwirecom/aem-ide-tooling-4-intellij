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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
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

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by schaefa on 3/10/17.
 */
public class HyperlinkRedirectListener implements ChangeListener<State>, EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(HyperlinkRedirectListener.class);

    private static final String CLICK_EVENT = "click";
    private static final String ANCHOR_TAG = "a";

    private WebView webView;

    public HyperlinkRedirectListener() {
    }

    public HyperlinkRedirectListener(WebView webView) {
        this.webView = webView;
    }

    public void setWebView(WebView webView) {
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

        boolean done = false;
        try {
            if(Desktop.isDesktopSupported()) {
                openLinkInSystemBrowser(href);
                done = true;
            } else {
                LOGGER.warn("OS does not support desktop operations like browsing. Cannot open link '{}'.", href);
            }
        } catch(URISyntaxException | IOException e) {
            LOGGER.warn("OS does not support desktop operations like browsing. Failed open link '{}'.", href, e);
        }
        if(done) {
            event.preventDefault();
        }
    }

    private void openLinkInSystemBrowser(String url) throws URISyntaxException, IOException {
        LOGGER.trace("Opening link '{}' in default system browser.", url);

        URI uri = new URI(url);
        Desktop.getDesktop().browse(uri);
    }
}
