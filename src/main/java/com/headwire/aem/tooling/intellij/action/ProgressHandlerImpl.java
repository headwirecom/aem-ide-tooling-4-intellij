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

package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.progress.ProgressIndicator;

/**
 * Created by schaefa on 3/14/16.
 */
public class ProgressHandlerImpl
    implements ProgressHandler
{
    private int steps;
    private double currentStep;
    private String title;
    private ProgressHandlerImpl subTasks;
    private ProgressHandlerImpl parent;
    private ProgressIndicator progressIndicator;
    private boolean markedAsCanceled;
    private boolean notCancelable;
    private Boolean forceAsynchronous;

    public ProgressHandlerImpl(String title) {
        this(null, 0, title);
    }

    public ProgressHandlerImpl(ProgressIndicator progressIndicator, String title) {
        this(progressIndicator, 0, title);
    }

    public ProgressHandlerImpl(ProgressIndicator progressIndicator, int steps, String title) {
        this(progressIndicator, steps, title, new String[] {});
    }

    public ProgressHandlerImpl(ProgressIndicator progressIndicator, int steps, String title, String...params) {
        this.steps = steps;
        this.title = AEMBundle.messageOrKey(title, params);
        this.progressIndicator = progressIndicator;
    }

    @Override
    public ProgressHandler startSubTasks(int Steps, String title) {
        return startSubTasks(Steps, title, new String[] {});
    }

    @Override
    public ProgressHandler startSubTasks(int Steps, String title, String...params) {
        checkCancellation();
        if(subTasks != null) {
            // Finish any existing sub task
            subTasks.finish();
        }
        subTasks = new ProgressHandlerImpl(progressIndicator, steps, title, params);
        subTasks.parent = this;
        if(progressIndicator != null) {
            progressIndicator.pushState();
            progressIndicator.setText(title);
        }
        return subTasks;
    }

    @Override
    public void next(String task) {
        next(task, new String[]{});
    }

    @Override
    public void next(String task, String...params) {
        checkCancellation();
        // Check if the
        if(subTasks != null) {
            subTasks.finish();
        }
        if(progressIndicator != null) {
            progressIndicator.setText2(AEMBundle.messageOrKey(task, params));
            if(steps > 0) {
                // Set fraction as the ratio between the actual step and the total steps
                progressIndicator.setFraction(currentStep++ / steps);
            }
        }
    }

    public String getTitle() {
        return title;
    }

    @Override
    public void markAsCancelled() {
        // First go to the top of the list and then start marking them from there
        if(parent != null && !parent.isMarkedAsCancelled()) {
            parent.markAsCancelled();
        } else {
            markedAsCanceled = true;
            if(subTasks != null) {
                subTasks.markAsCancelled();
            }
        }
    }

    @Override
    public boolean isMarkedAsCancelled() {
        return markedAsCanceled;
    }

    @Override
    public void setNotCancelable(boolean notCancelable) {
        this.notCancelable = notCancelable;
        if(subTasks != null) {
            subTasks.setNotCancelable(notCancelable);
        }
    }

    @Override
    public boolean isNotCancelable() {
        return notCancelable;
    }

    @Override
    public ProgressHandler getParent() {
        return parent;
    }

    public ProgressHandlerImpl setForceAsynchronous(Boolean forceAsynchronous) {
        this.forceAsynchronous = forceAsynchronous;
        return this;
    }

    @Override
    public Boolean forceAsynchronous() {
        if(forceAsynchronous != null) {
            return forceAsynchronous;
        } else {
            if(parent != null) {
                return parent.forceAsynchronous();
            }
        }
        return null;
    }

    private void checkCancellation()
        throws CancellationException
    {
        if(markedAsCanceled && !notCancelable) {
            throw new CancellationException(getTrace());
        }
    }

    String getTrace() {
        String ret = "";
        if(parent == null) {
            return getTitle();
        } else {
            ret = parent.getTrace();
        }
        ret += " -> " + getTitle();
        return ret;
    }

    private void finish() {
        if(subTasks != null) {
            // First finish any sub tasks (recursively)
            subTasks.finish();
        }
        if(progressIndicator != null) {
            // Then pop the indicator state to go back to where this one was
            progressIndicator.popState();
            progressIndicator = null;
        }
    }

    public static class CancellationException
        extends RuntimeException
    {
        public CancellationException(String message) {
            super(message);
        }
    }
}
