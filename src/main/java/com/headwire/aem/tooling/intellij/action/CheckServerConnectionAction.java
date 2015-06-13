package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.apache.sling.ide.osgi.OsgiClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by schaefa on 6/12/15.
 */
public class CheckServerConnectionAction
    extends AnAction
    implements DumbAware
{

    public CheckServerConnectionAction() {
//        super(
//            AEMBundle.message("check.configuration.action.name"),
//            AEMBundle.message("check.configuration.action.description"),
//            AllIcons.CodeStyle.Gear
//        );
    }

    public void actionPerformed(AnActionEvent e) {
        doCheck(e.getProject());
    }

    public void update(AnActionEvent event) {
        Project project = event.getProject();
        if(project != null) {
            ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
            event.getPresentation().setEnabled(serverConnectionManager.isConfigurationSelected());
        }
    }

    public void doCheck(final Project project) {
        final ServerTreeSelectionHandler selectionHandler = ServiceManager.getService(project, ServerTreeSelectionHandler.class);
        final ServerConnectionManager serverConnectionManager = ServiceManager.getService(project, ServerConnectionManager.class);
        final String title = AEMBundle.message("check.configuration.action.name");
        final String description = AEMBundle.message("check.configuration.action.description");

        ProgressManager.getInstance().run(
            new Task.Modal(project, title, false) {
                @Nullable
                public NotificationInfo getNotificationInfo() {
                    return new NotificationInfo("Sling", "Sling Deployment Checks", "");
                }

                public void run(@NotNull final ProgressIndicator indicator) {
                    indicator.setIndeterminate(false);
                    indicator.pushState();
                    try {
                        indicator.setText(description);
                        indicator.setFraction(0.0);
                        ApplicationManager.getApplication().runReadAction(new Runnable() {
                            public void run() {
                                if(!serverConnectionManager.checkSelectedServerConfiguration(true, false)) {
                                    return;
                                }
                                ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                                //AS TODO: this is not showing if the check is short but if it takes longer it will update
                                indicator.setFraction(0.1);
                                serverConnectionManager.updateServerStatus(
                                    serverConfiguration.getName(),ServerConfiguration.ServerStatus.checking
                                );
                                indicator.setFraction(0.2);
                                try

                                {
                                    Thread.sleep(1000);
                                }

                                catch(
                                    InterruptedException e1
                                    )

                                {
                                    e1.printStackTrace();
                                }

                                indicator.setFraction(0.3);
                                OsgiClient osgiClient = serverConnectionManager.obtainSGiClient();
                                if(osgiClient!=null)

                                {
                                    indicator.setFraction(0.4);
                                    ServerConnectionManager.BundleStatus status = serverConnectionManager.checkAndUpdateSupportBundle(false);
                                    if(status != ServerConnectionManager.BundleStatus.failed) {
                                        // If a Module is selected then check only this one
                                        indicator.setFraction(0.6);
                                        ServerConfiguration.Module module = selectionHandler.getCurrentModuleConfiguration();
                                        indicator.setFraction(0.7);
                                        if(module != null) {
                                            // Handle Module only
                                            serverConnectionManager.checkModule(osgiClient, module);
                                        } else {
                                            // Handle entire Project
                                            serverConnectionManager.checkModules(osgiClient);
                                        }
                                        indicator.setFraction(1.0);
                                        serverConnectionManager.updateServerStatus(
                                            serverConfiguration.getName(), ServerConfiguration.ServerStatus.checked
                                        );
                                    }
                                }
                            }
                        });
                    }
                    finally {
                        indicator.popState();
                    }
                }
            }
        );
    }
}
