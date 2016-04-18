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

import com.intellij.openapi.application.ApplicationManager;
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
                stopSignal.await();
            } else {
                runner.run();
            }
        } catch(Exception e) {
            runner.handleException(e);
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
