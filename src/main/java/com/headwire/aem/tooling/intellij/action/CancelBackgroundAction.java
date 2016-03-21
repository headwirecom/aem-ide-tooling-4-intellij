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

package com.headwire.aem.tooling.intellij.action;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

import static com.intellij.icons.AllIcons.Icon;

/**
 * This action enables the user to cancel a background action
 * by using the Progress Handler.
 *
 * Created by Andreas Schaefer (Headwire.com) on 6/13/15.
 */
public class CancelBackgroundAction
    extends AbstractProjectAction
{

    private boolean waitingForEnd = false;
    private Icon defaultIcon;

    public CancelBackgroundAction() {
        super("cancel.background.action");
    }

    @Override
    public void update(AnActionEvent event) {
        if(defaultIcon == null) {
            defaultIcon = event.getPresentation().getIcon();
        }
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        if(waitingForEnd && !isCancelable()) {
            // When the background action is cancelled then we set the Icon back to the default cancel icon
            event.getPresentation().setIcon(defaultIcon);
            waitingForEnd = false;
        }
        event.getPresentation().setEnabled(
            project != null &&
            isEnabled(project, dataContext) &&
            isLocked(project) &&
            isCancelable()
        );
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        if(!isCancelled()) {
            waitingForEnd = true;
            // The UI should reflect the button pressed and so we change to the Exit icon
            event.getPresentation().setIcon(Actions.Exit);
            cancel();
        }
    }

    @Override
    protected void execute(@NotNull Project project, @NotNull DataContext dataContext, final ProgressHandler progressHandler) {
    }

    @Override
    protected boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext) {
        return true;
    }
}
