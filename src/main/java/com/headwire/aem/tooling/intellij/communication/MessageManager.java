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

package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.console.ConsoleLogCategory;
import com.headwire.aem.tooling.intellij.console.ConsoleLogToolWindowFactory;
import com.headwire.aem.tooling.intellij.console.DebugNotification;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.log.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/14/15.
 */
public class MessageManager
    extends AbstractProjectComponent
{
    private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.toolWindowGroup(ConsoleLogCategory.CONSOLE_LOG_CATEGORY, ConsoleLogToolWindowFactory.TOOL_WINDOW_ID);

    private Logger logger = Activator.getDefault().getPluginLogger();

    public MessageManager(@NotNull Project project) {
        super(project);
    }

    public void sendDebugNotification(String message) {
        logger.trace(message);
        java.util.logging.Logger.getLogger(getClass().getName()).log(Level.INFO, message);
        new DebugNotification("Debug Message", message).notify(myProject);
    }

    private void sendNotification(String title, String message, NotificationType type) {
        NOTIFICATION_GROUP.createNotification(
            title,
            message,
            type,
            null
        ).notify(myProject);
    }

    public void sendNotification(String bundleMessageId, NotificationType type, Object ... parameters) {
        String message = getMessage(bundleMessageId, parameters);
        sendNotification(
            getTitle(bundleMessageId),
            message,
            type
        );
        handlePossibleExceptions(parameters);
    }

    public void sendUnexpectedException(Throwable t) {
        PluginManager.processException(t);
    }

    private void handlePossibleExceptions(Object ... parameters) {
        for(Object parameter: parameters) {
            if(parameter instanceof Throwable) {
                PluginManager.processException((Throwable) parameter);
            }
        }
    }

    private String getTitle(String bundleMessageId) {
        return AEMBundle.message(bundleMessageId + ".title");
    }

    private String getMessage(String bundleMessageId, Object ... parameters) {
        return AEMBundle.message(bundleMessageId + ".description", parameters);
    }

    public void sendInfoNotification(String bundleMessageId, Object ... parameters) {
        sendNotification(bundleMessageId, NotificationType.INFORMATION, parameters);
    }

    public void sendErrorNotification(String bundleMessageId, Object ... parameters) {
        sendNotification(bundleMessageId, NotificationType.ERROR, parameters);
    }

    public int showAlertWithOptions(@Nullable NotificationType type, @NotNull final String messageId, Object ... arguments) {
        String title = getTitle(messageId);
        String message = getMessage(messageId, arguments);
        // Make sure the Message is also placed inside the Log Console
        sendNotification(title, message, type);
        List<String> selections = new ArrayList<String>();
        for(int i = 0; i < 3; i++) {
            String selectionText = AEMBundle.message(messageId + "." + i);
            if(StringUtils.isNotBlank(selectionText)) {
                selections.add(selectionText);
            }
        }
        int ret = 0;
        if(selections.isEmpty()) {
            ret = Messages.showOkCancelDialog(
                message,
                title,
                getIcon(type)
            );
        } else {
            String[] options = selections.toArray(new String[selections.size()]);
            ret = Messages.showDialog(
                myProject,
                message,
                title,
                options,
                0,
                getIcon(type)
            );
        }
        return ret;
    }

    private Icon getIcon(NotificationType type) {
        switch(type) {
            case INFORMATION:
                return Messages.getInformationIcon();
            case WARNING:
                return Messages.getWarningIcon();
            case ERROR:
            default:
                return Messages.getErrorIcon();
        }
    }

    public void showAlert(@NotNull final String messageId) {
        showAlert(getTitle(messageId), getMessage(messageId));
    }

    public void showAlertWithArguments(@NotNull final String messageId, Object...arguments) {
        showAlert(getTitle(messageId), getMessage(messageId, arguments));
    }

    public void showAlertWithArguments(@NotNull NotificationType type, @NotNull final String messageId, Object...arguments) {
        showAlert(type, getTitle(messageId), getMessage(messageId, arguments));
    }

    public void showAlert(@NotNull final String title, @NotNull final String message) {
        // AS TODO: Originally the call was placed onto another Thread but it seems not to be necessary -> check and adjust
        showAlert(NotificationType.ERROR, title, message);
    }

    public void showAlert(@NotNull final NotificationType type, @NotNull final String title, @NotNull final String message) {
        // Make sure the Message is also placed inside the Log Console
        sendNotification(title, message, type);
        ApplicationManager.getApplication().invokeLater(
            new Runnable() {
                public void run() {
                    Messages.showDialog(
                        myProject,
                        message,
                        title,
                        new String[]{Messages.OK_BUTTON},
                        0,
                        getIcon(type)
                    );
                }
            }
        );
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Message Manager";
    }
}
