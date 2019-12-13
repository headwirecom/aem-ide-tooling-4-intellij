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

package com.headwire.aem.tooling.intellij.console;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.util.AbstractProjectComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.notification.NotificationsAdapter;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.ShutDownTracker;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.headwire.aem.tooling.intellij.console.ConsoleLog.DEFAULT_CATEGORY;
import static com.headwire.aem.tooling.intellij.console.ConsoleLog.LOG_REQUESTOR;
import static com.headwire.aem.tooling.intellij.util.ExecutionUtil.runReadAction;

/**
 * Created by Andreas Schaefer (Headwire.com) on 7/3/15.
 */
public class ConsoleLogProjectTracker
    extends AbstractProjectComponent
    implements Disposable
{
    private final Map<String, ConsoleLogConsole> myCategoryMap = ContainerUtil.newConcurrentMap();
    private final List<Notification> myInitial = ContainerUtil.createLockFreeCopyOnWriteList();
    private final ConsoleLogModel myProjectModel;
    private final List<String> acceptGroupIds = Arrays.asList(ConsoleLogCategory.CONSOLE_LOG_CATEGORY, "HotSwap");

    public ConsoleLogProjectTracker(@NotNull final Project project) {
        super(project);

        myProjectModel = new ConsoleLogModel(project, project);

        ConsoleLog consoleLog = ConsoleLog.getApplicationComponent();
        if(consoleLog != null) {
            ConsoleLogModel model = consoleLog.getModel();
            if(model != null) {
                for(Notification notification : model.takeNotifications()) {
                    printNotification(notification);
                }
            }
        }

        project.getMessageBus().connect(project).subscribe(Notifications.TOPIC, new NotificationsAdapter() {
            @Override
            public void notify(@NotNull Notification notification) {
                printNotification(notification);
            }
        });
    }

    public void initDefaultContent() {
        createNewContent(DEFAULT_CATEGORY);

        for(Notification notification : myInitial) {
            doPrintNotification(notification, ObjectUtils.assertNotNull(getConsole(notification)));
        }
        myInitial.clear();
    }

    public ConsoleLogModel getMyProjectModel() {
        return myProjectModel;
    }

    protected void printNotification(Notification notification) {
        // Only show Plugin Log statements in our AEM Console
        if(acceptGroupIds.contains(notification.getGroupId())) {
            SlingServerTreeSelectionHandler selectionHandler = ServiceManager.getService(myProject, SlingServerTreeSelectionHandler.class);
            if(selectionHandler != null) {
                ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                ServerConfiguration.LogFilter logFilter = serverConfiguration != null ? serverConfiguration.getLogFilter() : ServerConfiguration.LogFilter.info;
                switch(logFilter) {
                    case debug:
                        break;
                    case info:
                        if(notification instanceof DebugNotification) {
                            return;
                        }
                        break;
                    case warning:
                        if(notification.getType() == NotificationType.INFORMATION) {
                            return;
                        }
                        break;
                    case error:
                    default:
                        if(notification.getType() != NotificationType.ERROR) {
                            return;
                        }
                }
            }
            myProjectModel.addNotification(notification);

            ConsoleLogConsole console = getConsole(notification);
            if(console == null) {
                myInitial.add(notification);
            } else {
                doPrintNotification(notification, console);
            }
        }
    }

    private void doPrintNotification(@NotNull final Notification notification, @NotNull final ConsoleLogConsole console) {
        StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new DumbAwareRunnable() {
            @Override
            public void run() {
                if(!ShutDownTracker.isShutdownHookRunning() && !myProject.isDisposed()) {
                    runReadAction(
                        new Runnable() {
                            @Override
                            public void run() {
                                console.doPrintNotification(notification);
                            }
                        }
                    );
                }
            }
        });
    }

    @Nullable
    protected ConsoleLogConsole getConsole(Notification notification) {
        if(myCategoryMap.get(DEFAULT_CATEGORY) == null) {
            return null; // still not initialized
        }

        String name = ConsoleLog.getContentName(notification);
        ConsoleLogConsole console = myCategoryMap.get(name);
        return console != null ? console : createNewContent(name);
    }

    @NotNull
    private ConsoleLogConsole createNewContent(String name) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        ConsoleLogConsole newConsole = new ConsoleLogConsole(myProjectModel);
        ConsoleLogToolWindowFactory.createContent(myProject, ConsoleLog.getLogWindow(myProject), newConsole, name);
        myCategoryMap.put(name, newConsole);

        return newConsole;
    }

    @Override
    public void dispose() {
        ConsoleLog consoleLog = ConsoleLog.getApplicationComponent();
        if(consoleLog != null) {
            ConsoleLogModel model = consoleLog.getModel();
            if(model != null) {
                model.setStatusMessage(null, 0);
            }
        }
        StatusBar.Info.set("", null, LOG_REQUESTOR);
    }
}
