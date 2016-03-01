package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.intellij.debugger.engine.RemoteDebugProcessHandler;
import com.intellij.execution.ExecutionAdapter;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.hash.HashMap;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This class listens to Background Process Execution Events
 * and lets another thread wait for its completion.
 *
 * Created by schaefa on 2/18/16.
 */
public class RunExecutionMonitor {

    public static final String DEBUG_EXECUTION = "Debug";
    public static final String RUN_EXECUTION = "Run";

    private static Map<Project, RunExecutionMonitor> instances = new HashMap<Project, RunExecutionMonitor>();

    public static RunExecutionMonitor getInstance(@NotNull Project project) {
        RunExecutionMonitor ret = instances.get(project);
        if(ret == null) {
            project.getComponent(MessageManager.class).sendDebugNotification(
                "Create new REM for Project: " + project
            );
            ret = new RunExecutionMonitor(project);
            instances.put(project, ret);
        }
        return ret;
    }

    public static void disposeInstance(@NotNull Project project) {
        RunExecutionMonitor instance = instances.remove(project);
        if(instance != null) {
            instance.dispose();
        }
    }

    public enum WaitState {done, timedOut, interrupted};

    Project project;
    private MessageBusConnection connection;
    private ServerConfigurationManager serverConfigurationManager;
//    private Semaphore waitSemaphore;
//    private volatile CountDownLatch startSignal = null;
    private volatile CountDownLatch stopSignal = null;

    public RunExecutionMonitor(@NotNull Project project) {
        this.project = project;
        init();
    }

    private void init() {
//        startSignal = new CountDownLatch(1);
        stopSignal = new CountDownLatch(0);
        serverConfigurationManager = project.getComponent(ServerConfigurationManager.class);
        final MessageBus bus = project.getMessageBus();
        connection = bus.connect();
        // Hook up to the Bus and Register an Execution Listener in order to know when Debug Connection is established
        // and when it is taken down even when not started or stopped through the Plugin
        connection.subscribe(
            ExecutionManager.EXECUTION_TOPIC,
            new ExecutionAdapter() {
                @Override
                public void processStartScheduled(String executorId, ExecutionEnvironment env) {
                    if(RUN_EXECUTION.equals(executorId)) {
                        boolean isDispatcher = ApplicationManager.getApplication().isDispatchThread();
                        project.getComponent(MessageManager.class).sendDebugNotification(
                            "Schedule Maven Run on Project: " + project
                        );
//                        startSignal.countDown();
                    }
                }

                @Override
                public void processNotStarted(String executorId, @NotNull ExecutionEnvironment env) {
                    // Handle the failure to start Debug Connection
                    if(DEBUG_EXECUTION.equals(executorId)) {
                        // This is called when the Debug Session failed to start and / or connect
                        ServerConfiguration configuration = serverConfigurationManager.findServerConfigurationByName(env.getRunProfile().getName());
                        if(configuration != null) {
                            configuration.setServerStatus(ServerConfiguration.ServerStatus.failed);
                            serverConfigurationManager.updateServerConfiguration(configuration);
                            //AS TODO: Update Bundle Status
                            // Mark any Bundles inside the Tree as unknown
                        }
                    } else if(RUN_EXECUTION.equals(executorId)) {
                        project.getComponent(MessageManager.class).sendDebugNotification(
                            "Maven Run Failed to Start on Project: " + project
                        );
//                        startSignal = new CountDownLatch(1);
                        if(stopSignal != null) {
                            stopSignal.countDown();
                        }
                    }
                }

                @Override
                public void processStarted(String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
                    // Handle the successful start Debug Connection
                    if(DEBUG_EXECUTION.equals(executorId)) {
                        // This is called when the Debug Session successfully started and connected
                        ServerConfiguration configuration = serverConfigurationManager.findServerConfigurationByName(env.getRunProfile().getName());
                        if(configuration != null) {
                            configuration.setServerStatus(ServerConfiguration.ServerStatus.connected);
                            serverConfigurationManager.updateServerConfiguration(configuration);
                            //AS TODO: Update Bundle Status
                            // Now obtain the Status of the Bundles inside AEM and add as entries to the Tree underneath the Configuration Entry
                        }
                    } else {
                        String id = executorId;
                    }
                }

                @Override
                public void processTerminated(@NotNull RunProfile runProfile, @NotNull ProcessHandler handler) {
                    // Handle the Termination of the Debug Connection
                    if(handler instanceof RemoteDebugProcessHandler) {
                        // Called when a successful connected session is stopped
                        ServerConfiguration configuration = serverConfigurationManager.findServerConfigurationByName(runProfile.getName());
                        if(configuration != null) {
                            configuration.setServerStatus(ServerConfiguration.ServerStatus.disconnected);
                            serverConfigurationManager.updateServerConfiguration(configuration);
                            //AS TODO: Update Bundle Status
                            // Mark any Bundles inside the Tree as disconnected
                        }
                    } else if(runProfile instanceof MavenRunConfiguration) {
                        project.getComponent(MessageManager.class).sendDebugNotification(
                            "Finish Maven Run Ended Project: " + project
                        );
//                        startSignal = new CountDownLatch(1);
                        if(stopSignal != null) {
                            stopSignal.countDown();
                        }
                    } else {
                        String test = handler.toString();
                    }
                }
            }
        );
    }

    private void dispose() {
        if(connection != null) {
            connection.dispose();
            connection = null;
            project = null;
        }
    }

    public void startMavenBuild() {
        stopSignal = new CountDownLatch(1);
    }

    public WaitState waitFor(int timeoutInSeconds) {
//        project.getComponent(MessageManager.class).sendDebugNotification(
//            "Wait for " + (start ? "Start" : "End") + " of Maven Run on Project: " + project
//        );
        try {
//            if(start) {
//                return startSignal.await(10, TimeUnit.SECONDS) ? WaitState.done : WaitState.timedOut;
//            } else {
                return stopSignal.await(timeoutInSeconds, TimeUnit.SECONDS) ? WaitState.done : WaitState.timedOut;
//            }
        } catch(InterruptedException e) {
            return WaitState.interrupted;
        }
    }
}
