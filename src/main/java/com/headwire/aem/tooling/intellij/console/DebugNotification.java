package com.headwire.aem.tooling.intellij.console;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;

/**
 * Created by schaefa on 7/1/15.
 */
public class DebugNotification
    extends Notification
{
    public DebugNotification(String title, String content) {
        super(ConsoleLogCategory.CONSOLE_LOG_CATEGORY, title, content, NotificationType.INFORMATION);
    }
}
