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

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.util.ui.UIUtil;

import java.awt.Color;
import java.awt.Font;

public final class SlingServerNodeDescriptor
    extends AbstractSlingServerNodeDescriptor<ServerConfiguration> {

    public SlingServerNodeDescriptor(final Project project, final NodeDescriptor parentDescriptor, final ServerConfiguration target) {
        super(project, parentDescriptor, target);
        myName = target.getName();
    }

    public boolean update() {
        final CompositeAppearance oldText = myHighlightedText;
        myHighlightedText = new CompositeAppearance();
        final Color color = UIUtil.getLabelForeground();
        TextAttributes nameAttributes = new TextAttributes(color, null, null, EffectType.BOXED, Font.PLAIN);
        myHighlightedText.getEnding().addText(myTarget.getName() + " at " + myTarget.getHost(), nameAttributes);
        if(myTarget.getServerStatus() != null) {
            myHighlightedText.getEnding().addText(" (" + myTarget.getServerStatus().getName() + ')', ourPostfixAttributes);
        }
        return !Comparing.equal(myHighlightedText, oldText);
    }

    public String getTooltipText() {
        return getTarget().getDescription();
    }

    @Override
    public ServerConfiguration getServerConfiguration() {
        return myTarget;
    }

    @Override
    public ServerConfiguration.Module getModuleConfiguration() {
        return null;
    }
}
