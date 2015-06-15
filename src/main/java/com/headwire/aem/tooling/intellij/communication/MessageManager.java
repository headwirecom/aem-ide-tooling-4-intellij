package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.console.ConsoleLogCategory;
import com.headwire.aem.tooling.intellij.console.ConsoleLogToolWindowFactory;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.log.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Created by schaefa on 5/14/15.
 */
public class MessageManager
    implements ProjectComponent
{
    private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.toolWindowGroup(ConsoleLogCategory.CONSOLE_LOG_CATEGORY, ConsoleLogToolWindowFactory.TOOL_WINDOW_ID);

    private Logger logger = Activator.getDefault().getPluginLogger();

    private Project project;

    public MessageManager(@NotNull Project project) {
        this.project = project;
    }

    public void sendDebugNotification(String message) {
//AS TODO: Find a way to switch it off when released -> Constant
//        sendNotification("Debug Message", message, NotificationType.INFORMATION);
        logger.trace(message);
    }

//    public void sendNotification(String message, NotificationType type) {
//        sendNotification("", message, type);
//    }

    private void sendNotification(String title, String message, NotificationType type) {
        NOTIFICATION_GROUP.createNotification(
            title,
            message,
            type,
            null
        ).notify(project);
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

    public void showAlert(@NotNull final String messageId) {
        showAlert(getTitle(messageId), getMessage(messageId));
    }

    public void showAlertWithArguments(@NotNull final String messageId, Object...arguments) {
        showAlert(getTitle(messageId), getMessage(messageId, arguments));
    }

    public void showAlert(@NotNull final String title, @NotNull final String message) {
        ApplicationManager.getApplication().invokeLater(
            new Runnable() {
                public void run() {
                    Messages.showWarningDialog(project, message, title);
                }
            }
        );
    }

    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Message Manager";
    }
}
