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

package com.headwire.aem.tooling.intellij.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;

/**
 * Created by schaefa on 4/8/16.
 */
public class ExecutionUtil {

    public static void runAndWait(final @NotNull WaitableRunner runner) {
        try {
            if(runner.isAsynchronous()) {
                final CountDownLatch stopSignal = runner.getLatch();
                Logger.getInstance("#com.headwire.aem.tooling.intellij.util.ExecutionUtil").debug("Is Application Dispatcher Thread: " + ApplicationManager.getApplication().isDispatchThread());
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            runner.run();
                        } finally {
                            // Make sure the latch is released
                            stopSignal.countDown();
                        }
                    }
                });
                //AS TODO: re-opening another project will start this method as part of the when project is initialized. The runnable task
                //AS TODO: is not started and the next step waits forever.
                stopSignal.await();
            } else {
                runner.run();
            }
        } catch(Exception e) {
            runner.handleException(e);
        }
    }

    public static void invokeAndWait(final @NotNull InvokableRunner runner) {
        ModalityState modalityState = runner.getModalityState();
        if(modalityState != null) {
            ApplicationManager.getApplication().invokeAndWait(runner, modalityState);
        } else {
            ApplicationManager.getApplication().invokeAndWait(runner);
        }
    }

    public static void invokeLater(final @NotNull InvokableRunner runner) {
        ModalityState modalityState = runner.getModalityState();
        if(modalityState != null) {
            ApplicationManager.getApplication().invokeLater(runner, modalityState);
        } else {
            ApplicationManager.getApplication().invokeLater(runner);
        }
    }

    public static void runReadAction(final @NotNull Runnable runner) {
        ApplicationManager.getApplication().runReadAction(runner);
    }


    public static void queueTaskLater(final Task task) {
        final Application app = ApplicationManager.getApplication();
        if (app.isDispatchThread()) {
            task.queue();
        } else {
            app.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        task.queue();
                        String test3 = "done";
                    }
                },
                ModalityState.any()
            );
            String test4 = "done";
        }
    }

    public static abstract class InvokableRunner
        implements Runnable
    {
        private ModalityState modalityState;

        public InvokableRunner() {}

        public InvokableRunner(ModalityState modalityState) {
            this.modalityState = modalityState;
        }

        public ModalityState getModalityState() {
            return modalityState;
        }
    }

    public static abstract class WaitableRunner<T>
        implements Runnable
    {
        private CountDownLatch latch = new CountDownLatch(1);
        private T response;

        public WaitableRunner() {
        }

        public WaitableRunner(T defaultValue) {
            response = defaultValue;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        public T getResponse() {
            return response;
        }

        void setResponse(T response) {
            this.response = response;
        }

        public void handleException(Exception e) {};

        public boolean isAsynchronous() { return true; }
    }
}
