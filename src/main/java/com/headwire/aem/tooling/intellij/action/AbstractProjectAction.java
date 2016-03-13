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

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/13/15.
 */
public abstract class AbstractProjectAction
    extends AnAction
    implements DumbAware

{
    /** Map that contains the Toolbar Locks per Project **/
    private static Map<Project, Boolean> lockMap = new HashMap<Project, Boolean>();

    private String titleId;

    public AbstractProjectAction(@NotNull String textId) {
        super(
            AEMBundle.message(textId + ".text"),
            AEMBundle.message(textId + ".description"),
            null
        );
        this.titleId = textId + ".text";
    }

    public AbstractProjectAction() {
    }

    public void update(AnActionEvent event) {
        Project project = event.getProject();
        DataContext dataContext = event.getDataContext();
        event.getPresentation().setEnabled(
            project != null &&
            isEnabled(project, dataContext) &&
            !isLocked(project)
        );
    }

    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getProject();
        final DataContext dataContext = event.getDataContext();
        if(project != null && !isLocked(project)) {
            lock(project);
            if(isAsynchronous()) {
                ProgressManager.getInstance().run(new Task.Backgroundable(project, titleId) {
                    private String taskId = "plugin.no.task.id";

                    @Nullable
                    public NotificationInfo getNotificationInfo() {
                        return new NotificationInfo(AEMBundle.message("plugin.action.title"), getTitle(), AEMBundle.message(taskId));
                    }

                    public void run(@NotNull final ProgressIndicator indicator) {
                        try {
                            execute(project, dataContext, indicator);
                        } finally {
                            unlock(project);
                            getMessageManager(project).sendInfoNotification("aem.explorer.deploy.done");
                        }
                    }

                    void setTaskId(String taskId) {
                        if(taskId != null && AEMBundle.message(taskId) != null) {
                            this.taskId = taskId;
                        } else {
                            this.taskId = "plugin.no.task.id";
                        }
                    }
                });
            } else {
                try {
                    execute(project, dataContext, new NullProgressIndicator());
                } finally {
                    unlock(project);
                    getMessageManager(project).sendInfoNotification("aem.explorer.deploy.done");
                }
            }
        }
    }

    @NotNull
    protected MessageManager getMessageManager(@NotNull Project project) {
        return project.getComponent(MessageManager.class);
    }

    protected abstract void execute(@NotNull Project project, @NotNull DataContext dataContext, @NotNull final ProgressIndicator indicator);

    protected abstract boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext);

    /**
     * This method indicates if an Action is executing tasks in the background. If true then
     * the action must unlock the toolbar when the task is done.
     *
     * @return True if code is executed in the background and therefore the unlock must be postponed
     *         where it becomes the Action responsibility to unlock it when done.
     */
    protected boolean isAsynchronous() {
        return true;
    }

    private synchronized boolean isLocked(Project project) {
        Boolean ret = lockMap.get(project);
        return ret == null ? false : ret;
    }

    /**
     * Locks the Toolbar for the given Project
     *
     * @param project Project the toolbar will be locked on
     */
    protected synchronized void lock(@NotNull  Project project) {
        lockMap.put(project, true);
    }

    /**
     * Unlocks the Toolbar for the given Project
     *
     * @param project Project the toolbar will be unlocked from
     */
    protected synchronized void unlock(@NotNull Project project) {
        lockMap.put(project, false);
    }

    protected SlingServerTreeSelectionHandler getSelectionHandler(@Nullable Project project) {
        return project == null ? null : project.getComponent(SlingServerTreeSelectionHandler.class);
    }

    protected ServerConnectionManager getConnectionManager(@Nullable Project project) {
        return project == null ? null : project.getComponent(ServerConnectionManager.class);
    }

    protected ServerConfigurationManager getConfigurationManager(@Nullable Project project) {
        return project == null ? null : project.getComponent(ServerConfigurationManager.class);
    }
}
