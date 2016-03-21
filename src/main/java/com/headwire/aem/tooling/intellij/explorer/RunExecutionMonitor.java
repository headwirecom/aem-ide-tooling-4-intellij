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

package com.headwire.aem.tooling.intellij.explorer;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.ServerConfigurationManager;
import com.headwire.aem.tooling.intellij.util.ComponentProvider;
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
 * Created by Andreas Schaefer (Headwire.com) on 2/18/16.
 */
public class RunExecutionMonitor {

    public static final String DEBUG_EXECUTION = "Debug";
    public static final String RUN_EXECUTION = "Run";

    private static Map<Project, RunExecutionMonitor> instances = new HashMap<Project, RunExecutionMonitor>();

    public static RunExecutionMonitor getInstance(@NotNull Project project) {
        RunExecutionMonitor ret;
        // Only one thread should be able to read / write on the instances map
        synchronized(instances) {
            ret = instances.get(project);
            if(ret == null) {
                ComponentProvider.getComponent(project, MessageManager.class).sendDebugNotification("Create new REM for Project: " + project);
                ret = new RunExecutionMonitor(project);
                instances.put(project, ret);
            }
        }
        return ret;
    }

    public static void disposeInstance(@NotNull Project project) {
        synchronized(instances) {
            RunExecutionMonitor instance = instances.remove(project);
            if(instance != null) {
                instance.dispose();
            }
        }
    }

    public enum WaitState {done, timedOut, interrupted};

    Project project;
    private MessageBusConnection connection;
    private ServerConfigurationManager serverConfigurationManager;
    private volatile CountDownLatch stopSignal = new CountDownLatch(1);
    private volatile boolean intercepted = false;

    public RunExecutionMonitor(@NotNull Project project) {
        this.project = project;
        init();
    }

    private void init() {
        serverConfigurationManager = ComponentProvider.getComponent(project, ServerConfigurationManager.class);
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
                        ComponentProvider.getComponent(project, MessageManager.class).sendDebugNotification(
                            "Schedule Maven Run on Project: " + project
                        );
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
                        ComponentProvider.getComponent(project, MessageManager.class).sendDebugNotification(
                            "Maven Run Failed to Start on Project: " + project
                        );
                        stopSignal.countDown();
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
                        ComponentProvider.getComponent(project, MessageManager.class).sendDebugNotification(
                            "Finish Maven Run Ended Project: " + project
                        );
                        stopSignal.countDown();
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
        intercepted = false;
    }

    public void cancelMavenBuild() {
        if(stopSignal.getCount() > 0) {
            intercepted = true;
            stopSignal.countDown();
        }
    }

    public WaitState waitFor() {
        try {
            stopSignal.await();
            // If intercepted then return the flag accordingly
            return intercepted ?
                WaitState.interrupted :
                WaitState.done;
        } catch(InterruptedException e) {
            return WaitState.interrupted;
        }
    }

    @Deprecated
    public WaitState waitFor(int timeoutInSeconds) {
        //AS TODO: Instead of a Timeout we should wait forever as in order for a successful deployment the Maven
        //AX TODO: Build must end.
        //AS TODO: We could instead have a Cancel Action button that let us terninate the Thread
        try {
            if(timeoutInSeconds > 0) {
                return stopSignal.await(timeoutInSeconds, TimeUnit.SECONDS) ? WaitState.done : WaitState.timedOut;
            } else {
                stopSignal.await();
                return WaitState.done;
            }
        } catch(InterruptedException e) {
            return WaitState.interrupted;
        }
    }
}
