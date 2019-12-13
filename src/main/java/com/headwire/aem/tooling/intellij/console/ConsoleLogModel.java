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
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.Topic;
import com.intellij.util.ui.UIUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/6/15.
 */
public class ConsoleLogModel
    implements Disposable
{
    public static final Topic<Runnable> LOG_MODEL_CHANGED = Topic.create("LOG_MODEL_CHANGED", Runnable.class, Topic.BroadcastDirection.NONE);

    private final List<Notification> myNotifications = new ArrayList<Notification>();
    private final Map<Notification, Long> myStamps = Collections.synchronizedMap(new WeakHashMap<Notification, Long>());
    private final Map<Notification, String> myStatuses = Collections.synchronizedMap(new WeakHashMap<Notification, String>());
    private Trinity<Notification, String, Long> myStatusMessage;
    private final Project myProject;
    final Map<Notification, Runnable> removeHandlers = new THashMap<Notification, Runnable>();

    ConsoleLogModel(@Nullable Project project, @NotNull Disposable parentDisposable) {
        myProject = project;
        Disposer.register(parentDisposable, this);
    }

    void addNotification(Notification notification) {
        long stamp = System.currentTimeMillis();
        if(myProject != null) {
            SlingServerTreeSelectionHandler selectionHandler = ServiceManager.getService(myProject, SlingServerTreeSelectionHandler.class);
            if(selectionHandler != null) {
                ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                ServerConfiguration.LogFilter logFilter = serverConfiguration != null ? serverConfiguration.getLogFilter() : ServerConfiguration.LogFilter.info;
                switch(logFilter) {
                    case debug:
                        add(notification);
                        break;
                    case info:
                        if(!(notification instanceof DebugNotification)) {
                            add(notification);
                        }
                        break;
                    case warning:
                        if(notification.getType() != NotificationType.INFORMATION) {
                            add(notification);
                        }
                        break;
                    case error:
                    default:
                        if(notification.getType() == NotificationType.ERROR) {
                            add(notification);
                        }
                        break;
                }
            }
            myStamps.put(notification, stamp);
            myStatuses.put(notification, ConsoleLog.formatForLog(notification, "").status);
            setStatusMessage(notification, stamp);
            fireModelChanged();
        }
    }

    private void add(Notification notification) {
        synchronized (myNotifications) {
            myNotifications.add(notification);
        }
    }

    private static void fireModelChanged() {
        ApplicationManager.getApplication().getMessageBus().syncPublisher(LOG_MODEL_CHANGED).run();
    }

    List<Notification> takeNotifications() {
        final ArrayList<Notification> result;
        synchronized (myNotifications) {
            result = getNotifications();
            myNotifications.clear();
        }
        fireModelChanged();
        return result;
    }

    void setStatusMessage(@Nullable Notification statusMessage, long stamp) {
        synchronized (myNotifications) {
            if (myStatusMessage != null && myStatusMessage.first == statusMessage) return;
            if (myStatusMessage == null && statusMessage == null) return;

            myStatusMessage = statusMessage == null ? null : Trinity.create(statusMessage, myStatuses.get(statusMessage), stamp);
        }
        StatusBar.Info.set("", myProject, ConsoleLog.LOG_REQUESTOR);
    }

    @Nullable
    Trinity<Notification, String, Long> getStatusMessage() {
        synchronized (myNotifications) {
            return myStatusMessage;
        }
    }

    void logShown() {
        for (Notification notification : getNotifications()) {
            if (!notification.isImportant()) {
                removeNotification(notification);
            }
        }
        setStatusToImportant();
    }

    public ArrayList<Notification> getNotifications() {
        synchronized (myNotifications) {
            return new ArrayList<Notification>(myNotifications);
        }
    }

    @Nullable
    public Long getNotificationTime(Notification notification) {
        return myStamps.get(notification);
    }

    public void removeNotification(Notification notification) {
        synchronized (myNotifications) {
            myNotifications.remove(notification);
        }

        Runnable handler = removeHandlers.remove(notification);
        if (handler != null) {
            UIUtil.invokeLaterIfNeeded(handler);
        }

        Trinity<Notification, String, Long> oldStatus = getStatusMessage();
        if (oldStatus != null && notification == oldStatus.first) {
            setStatusToImportant();
        }
        fireModelChanged();
    }

    private void setStatusToImportant() {
        ArrayList<Notification> notifications = getNotifications();
        Collections.reverse(notifications);
        Notification message = ContainerUtil.find(notifications, new Condition<Notification>() {
            @Override
            public boolean value(Notification notification) {
                return notification.isImportant();
            }
        });
        if (message == null) {
            setStatusMessage(null, 0);
        }
        else {
            Long notificationTime = getNotificationTime(message);
            assert notificationTime != null;
            setStatusMessage(message, notificationTime);
        }
    }

    public Project getProject() {
        //noinspection ConstantConditions
        return myProject;
    }

    @Override
    public void dispose() {
    }
}
