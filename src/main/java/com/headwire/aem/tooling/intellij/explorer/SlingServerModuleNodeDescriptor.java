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

final class SlingServerModuleNodeDescriptor
    extends AbstractConfigurationNodeDescriptor<ServerConfiguration.Module>
{

    public SlingServerModuleNodeDescriptor(final Project project, final NodeDescriptor parentDescriptor, final ServerConfiguration.Module target) {
        super(project, parentDescriptor, target);
        myName = target.getName();
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

        myHighlightedText.getEnding().addText(myTarget.getArtifactId() + ", version: " + myTarget.getVersion(), nameAttributes);

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
