package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
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
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.HtmlListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Font;

final class SlingServerModuleNodeDescriptor
    extends ServerNodeDescriptor
{

    private static final TextAttributes ourPostfixAttributes = new TextAttributes(new JBColor(new Color(128, 0, 0), JBColor.RED), null, null, EffectType.BOXED, Font.PLAIN);

    private final ServerConfiguration.Module myTarget;
    private CompositeAppearance myHighlightedText;

    public SlingServerModuleNodeDescriptor(final Project project, final NodeDescriptor parentDescriptor, final ServerConfiguration.Module target) {
        super(project, parentDescriptor);
        myTarget = target;
        myHighlightedText = new CompositeAppearance();
    }

    public Object getElement() {
        return myTarget;
    }

    public ServerConfiguration.Module getTarget() {
        return myTarget;
    }

    public boolean update() {
        final CompositeAppearance oldText = myHighlightedText;
//    final boolean isMeta = myTarget instanceof MetaTarget;
//
//    setIcon(isMeta ? AntIcons.MetaTarget : AntIcons.Target);

        myHighlightedText = new CompositeAppearance();

//    final AntBuildFile buildFile = isMeta ? ((MetaTarget)myTarget).getBuildFile() : myTarget.getModel().getBuildFile();
        final Color color = UIUtil.getLabelForeground();
//    TextAttributes nameAttributes = new TextAttributes(color, null, null, EffectType.BOXED, myTarget.isDefault() ? Font.BOLD : Font.PLAIN);
        TextAttributes nameAttributes = new TextAttributes(color, null, null, EffectType.BOXED, Font.PLAIN);

        myHighlightedText.getEnding().addText(myTarget.getName() + ", version: " + myTarget.getVersion(), nameAttributes);

//    AntConfigurationBase antConfiguration = AntConfigurationBase.getInstance(myProject);
//    final ArrayList<String> addedNames = new ArrayList<String>(4);
//    for (final ExecutionEvent event : antConfiguration.getEventsForTarget(myTarget)) {
//      final String presentableName;
//      if ((event instanceof ExecuteCompositeTargetEvent)) {
//        presentableName = ((ExecuteCompositeTargetEvent)event).getMetaTargetName();
//        if (presentableName.equals(myTarget.getName())) {
//          continue;
//        }
//      }
//      else {
//        presentableName = event.getPresentableName();
//      }
//      if (!addedNames.contains(presentableName)) {
//        addedNames.add(presentableName);
        if(myTarget.getStatus() != null) {
            myHighlightedText.getEnding().addText(" (" + myTarget.getStatus().getName() + ')', ourPostfixAttributes);
        }
//      }
//    }
//    final RunManagerEx runManager = RunManagerEx.getInstanceEx(myProject);
//    final VirtualFile vFile = buildFile.getVirtualFile();
//    if (vFile != null) {
//      for (AntBeforeRunTask task : runManager.getBeforeRunTasks(AntBeforeRunTaskProvider.ID)) {
//        if (task.isRunningTarget(myTarget)) {
//          myHighlightedText.getEnding().addText(" (Before Run/Debug)", ourPostfixAttributes);
//          break;
//        }
//      }
//    }
//    myName = myHighlightedText.getText();
//
//    final AntBuildTargetBase target = getTarget();
//    if (!addShortcutText(target.getActionId())) {
//      if (target.isDefault()) {
//        addShortcutText(((AntBuildModelBase)target.getModel()).getDefaultTargetActionId());
//      }
//    }

        return !Comparing.equal(myHighlightedText, oldText);
    }

    private boolean addShortcutText(String actionId) {
        return addShortcutText(actionId, myHighlightedText);
    }

    public static boolean addShortcutText(String actionId, CompositeAppearance appearance) {
        Keymap activeKeymap = KeymapManager.getInstance().getActiveKeymap();
        Shortcut[] shortcuts = activeKeymap.getShortcuts(actionId);
        if(shortcuts != null && shortcuts.length > 0) {
            appearance.getEnding().addText(" (" + KeymapUtil.getShortcutText(shortcuts[0]) + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            return true;
        } else {
            return false;
        }
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
        String toolTipText = getTarget().getName() + " : " + getTarget().getArtifactId() + " : " + getTarget().getVersion();
        component.setToolTipText(toolTipText);
    }

    @Override
    public void customize(@NotNull final HtmlListCellRenderer renderer) {
        getHighlightedText().customize(renderer);
        renderer.setIcon(getIcon());
        String toolTipText = getTarget().getName() + " : " + getTarget().getArtifactId() + " : " + getTarget().getVersion();
        renderer.setToolTipText(toolTipText);
    }
}
