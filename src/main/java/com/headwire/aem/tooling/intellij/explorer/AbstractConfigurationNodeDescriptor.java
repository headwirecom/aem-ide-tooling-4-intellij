/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.explorer;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.CellAppearanceEx;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.ui.HtmlListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Font;

/**
 * Created by schaefa on 6/13/15.
 */
public abstract class AbstractConfigurationNodeDescriptor<T>
    extends ServerNodeDescriptor
{
    protected static final TextAttributes ourPostfixAttributes = new TextAttributes(new JBColor(new Color(128, 0, 0), JBColor.RED), null, null, EffectType.BOXED, Font.PLAIN);

    protected final T myTarget;
    protected CompositeAppearance myHighlightedText;

    public AbstractConfigurationNodeDescriptor(final Project project, final NodeDescriptor parentDescriptor, final T target) {
        super(project, parentDescriptor);
        myTarget = target;
        myHighlightedText = new CompositeAppearance();
    }

    public Object getElement() {
        return myTarget;
    }

    public T getTarget() {
        return myTarget;
    }

    public abstract boolean update();

    private boolean addShortcutText(String actionId) {
        return addShortcutText(actionId, myHighlightedText);
    }

    public static boolean addShortcutText(String actionId, CompositeAppearance appearance) {
        Keymap activeKeymap = KeymapManager.getInstance().getActiveKeymap();
        Shortcut[] shortcuts = activeKeymap.getShortcuts(actionId);
        if (shortcuts != null && shortcuts.length > 0) {
            appearance.getEnding().addText(" (" + KeymapUtil.getShortcutText(shortcuts[0]) + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            return true;
        } else return false;
    }

    public CellAppearanceEx getHighlightedText() {
        return myHighlightedText;
    }

    public boolean isAutoExpand() {
        return false;
    }

    public void customize(@NotNull SimpleColoredComponent component) {
        getHighlightedText().customize(component);
        component.setIcon(getIcon());
        String toolTipText = getTooltipText();
        component.setToolTipText(toolTipText);
    }

    @Override
    public void customize(@NotNull final HtmlListCellRenderer renderer) {
        getHighlightedText().customize(renderer);
        renderer.setIcon(getIcon());
        String toolTipText = getTooltipText();
        renderer.setToolTipText(toolTipText);
    }

    public abstract String getTooltipText();
}
