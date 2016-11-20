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

package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.action.ProgressHandlerImpl.CancellationException;
import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.headwire.aem.tooling.intellij.util.ExecutionUtil.WaitableRunner;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SystemNotifications;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.headwire.aem.tooling.intellij.util.ExecutionUtil.runAndWait;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/13/15.
 */
public abstract class AbstractProjectAction
    extends AnAction
    implements DumbAware

{
    /** Map that contains the Toolbar Locks per Project **/
    private static Map<Project, Boolean> lockMap = new HashMap<Project, Boolean>();
    private static ProgressHandlerImpl progressHandler;

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
                        return new NotificationInfo(AEMBundle.message("plugin.action.title"), AEMBundle.message(myTitle), AEMBundle.message(taskId));
                    }

                    public void run(@NotNull final ProgressIndicator indicator) {
                        try {
                            getMessageManager(project).sendInfoNotification("action.start", AEMBundle.message(myTitle));
                            progressHandler = new ProgressHandlerImpl(indicator, getTitle());
                            execute(project, dataContext, progressHandler);
                            getMessageManager(project).sendInfoNotification("action.end", AEMBundle.message(myTitle));
                        } catch(CancellationException e) {
                            // The user cancelled the task and so we catch the exception and report is to the user
                            getMessageManager(project).sendInfoNotification("action.cancelled", e.getMessage());
                        } finally {
                            progressHandler = null;
                            unlock(project);
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
                    execute(project, dataContext, new ProgressHandlerImpl(AEMBundle.message(titleId)));
                } finally {
                    unlock(project);
                }
            }
        }
    }

    @NotNull
    protected MessageManager getMessageManager(@NotNull Project project) {
        return ComponentProvider.getComponent(project, MessageManager.class);
    }

    protected boolean doVerify(@NotNull final Project project, @Nullable final DataContext dataContext, @NotNull final ProgressHandler progressHandler) {
        // First Run the Verifier and if the Server Configuration has changed also the Purge Cache
        ActionManager actionManager = ActionManager.getInstance();
        final VerifyConfigurationAction verifyConfigurationAction = (VerifyConfigurationAction) actionManager.getAction("AEM.Verify.Configuration.Action");
        WaitableRunner<AtomicBoolean> runner = null;
        if(verifyConfigurationAction != null) {
            runner = new WaitableRunner<AtomicBoolean>(new AtomicBoolean(true)) {
                @Override
                public void run() {
                    progressHandler.next("progress.start.verification");
                    getResponse().set(
                        verifyConfigurationAction.doVerify(project, SimpleDataContext.getSimpleContext(VerifyConfigurationAction.VERIFY_CONTENT_WITH_WARNINGS, false, dataContext), progressHandler)
                    );
                }
                @Override
                public void handleException(Exception e) {
                    // Catch and report unexpected exception as debug message to keep it going
                    getMessageManager(project).sendErrorNotification("server.configuration.verification.failed.unexpected", e);
                }
                @Override
                public boolean isAsynchronous() {
                    return AbstractProjectAction.this.isAsynchronous();
                }
            };
            runAndWait(runner);
        }
        return runner == null || runner.getResponse().get();
    }

    /** Method that provides does do the action. If the action is handled in the background the Progress Indicator is set otherwise null */
    protected abstract void execute(@NotNull Project project, @NotNull DataContext dataContext, final ProgressHandler progressHandler);

    protected abstract boolean isEnabled(@NotNull Project project, @NotNull DataContext dataContext);

    /** @return True if the Action must be executed in the background. If so the Progress Indicator is provided otherwise it is null */
    protected boolean isAsynchronous() {
        return true;
    }

    protected synchronized boolean isLocked(Project project) {
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

    protected boolean isCancelable() {
        return progressHandler != null;
    }

    protected boolean isCancelled() {
        return progressHandler != null && progressHandler.isMarkedAsCancelled();
    }

    protected void cancel() {
        if(progressHandler != null) {
            progressHandler.markAsCancelled();
        }
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
        return project == null ? null : ComponentProvider.getComponent(project, SlingServerTreeSelectionHandler.class);
    }

    protected ServerConnectionManager getConnectionManager(@Nullable Project project) {
        return project == null ? null : ComponentProvider.getComponent(project, ServerConnectionManager.class);
    }

    protected ServerConfigurationManager getConfigurationManager(@Nullable Project project) {
        return project == null ? null : ComponentProvider.getComponent(project, ServerConfigurationManager.class);
    }
}
