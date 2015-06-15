package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.communication.ServerConnectionManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.explorer.ServerTreeSelectionHandler;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by schaefa on 6/13/15.
 */
public class DeployToServerAction
    extends AbstractProjectAction
{
    protected boolean isForced() {
        return false;
    }

    @Override
    protected void execute(@NotNull Project project, DataContext dataContext) {
        if(dataContext != null && project != null) {
            doDeploy(dataContext, project, isForced());
        }
    }

    @Override
    protected boolean isEnabled(@Nullable Project project) {
        ServerConnectionManager connectionManager = getConnectionManager(project);
        return connectionManager != null && connectionManager.isConfigurationSelected();
    }

    private void doDeploy(final DataContext dataContext, final Project project, final boolean forceDeploy) {
        final ServerConnectionManager connectionManager = getConnectionManager(project);
        final ServerTreeSelectionHandler selectionHandler = getSelectionHandler(project);
        final String title = AEMBundle.message("deploy.configuration.action.name");

        ProgressManager.getInstance().run(
            new Task.Modal(project, title, false) {
                @Nullable
                public NotificationInfo getNotificationInfo() {
                    return new NotificationInfo("Sling", "Sling Deployment Checks", "");
                }

                public void run(@NotNull final ProgressIndicator indicator) {
                    //AS TODO: Check if there is a new version of IntelliJ CE that would allow to use
                    //AS TODO: the ProgressAdapter.
                    //AS TODO: Or create another Interface / Wrapper to make it IDE independent
                    indicator.setIndeterminate(false);
                    indicator.pushState();
                    ApplicationManager.getApplication().runReadAction(
                        new Runnable() {
                            public void run() {
                                try {
                                    final String description = AEMBundle.message("deploy.configuration.action.description");

                                    indicator.setText(description);
                                    indicator.setFraction(0.0);

                                    // There is no Run Connection to be made to the AEM Server like with DEBUG (no HotSwap etc).
                                    // So we just need to setup a connection to the AEM Server to handle OSGi Bundles and Sling Packages
                                    ServerConfiguration serverConfiguration = selectionHandler.getCurrentConfiguration();
                                    indicator.setFraction(0.1);
                                    //AS TODO: this is not showing if the check is short but if it takes longer it will update
                                    connectionManager.updateStatus(serverConfiguration, ServerConfiguration.SynchronizationStatus.updating);
                                    indicator.setFraction(0.2);
                                    try {
                                        Thread.sleep(1000);
                                    } catch(InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                    indicator.setFraction(0.3);
                                    // First Check if the Install Support Bundle is installed
                                    ServerConnectionManager.BundleStatus bundleStatus = connectionManager.checkAndUpdateSupportBundle(true);
                                    indicator.setFraction(0.5);
                                    ServerConfiguration.Module module = selectionHandler.getCurrentModuleConfiguration();
                                    if(module != null) {
                                        // Deploy only the selected Module
                                        connectionManager.deployModule(module, forceDeploy);
                                    } else {
                                        // Deploy all Modules of the Project
                                        connectionManager.deployModules(dataContext, forceDeploy);
                                    }
                                    indicator.setFraction(1.0);
                                } finally {
                                    indicator.popState();
                                }
                            }
                        }
                    );
                }
            }
        );
    }
}
