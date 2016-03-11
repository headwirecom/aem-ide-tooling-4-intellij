package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.util.CompositeAppearance;
import com.intellij.openapi.util.Comparing;
import com.intellij.util.ui.UIUtil;

import java.awt.Color;
import java.awt.Font;

final class SlingServerModuleNodeDescriptor
    extends AbstractSlingServerNodeDescriptor<Module>
{

    public SlingServerModuleNodeDescriptor(final Project project, final NodeDescriptor parentDescriptor, final ServerConfiguration.Module target) {
        super(project, parentDescriptor, target);
        myName = target.getName();
    }

    public boolean update() {
        final CompositeAppearance oldText = myHighlightedText;
        myHighlightedText = new CompositeAppearance();
        final Color color = UIUtil.getLabelForeground();
        TextAttributes nameAttributes = new TextAttributes(color, null, null, EffectType.BOXED, Font.PLAIN);

        myHighlightedText.getEnding().addText(myTarget.getSymbolicName(), nameAttributes);
        if(myTarget.getStatus() != null) {
            myHighlightedText.getEnding().addText(" (" + myTarget.getStatus().getName() + ')', ourPostfixAttributes);
        }
        return !Comparing.equal(myHighlightedText, oldText);
    }

    public String getTooltipText() {
        return getTarget().getName() + ":" + getTarget().getVersion();
    }

    @Override
    public ServerConfiguration getServerConfiguration() {
        return myTarget.getParent();
    }

    @Override
    public ServerConfiguration.Module getModuleConfiguration() {
        return myTarget;
    }
}
