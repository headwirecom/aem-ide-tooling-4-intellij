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

/**
 * IntelliJ's Progress Indicator is fine but way to complicated and
 * does not work out with Eclipse. This class simplifies the handling
 * and works even when there is no Progress Indicator.
 *
 * The basic idea is that every sub task has its own Progress Handler
 * that knows about its own sub tasks. If a super task next() is called
 * its Sub Tasks is finished automatically.
 *
 * This means that the Progress Handler does not work like a stack.
 * Therefore it is not necessary to wrap every step into a try-finally
 * block.
 *
 * Created by schaefa on 3/14/16.
 */
public interface ProgressHandler {

    /**
     * Creates a new set of sub tasks. Keep in mind
     * that this will not start a sub task so for the
     * first subtask a next() is required.
     *
     * @param Steps Number of steps. 0 or less means that
     *              the indicator is indeterminate
     * @param title Title of the Sub Tasks
     */
    public ProgressHandler startSubTasks(int Steps, String title);

    /**
     * Creates a new set of sub tasks. Keep in mind
     * that this will not start a sub task so for the
     * first subtask a next() is required.
     *
     * @param Steps Number of steps. 0 or less means that
     *              the indicator is indeterminate
     * @param title Title of the Sub Tasks
     * @param params List of Parameters added to the Task
     */
    public ProgressHandler startSubTasks(int Steps, String title, String...params);

    /**
     * Starts the next sub task. If this call goes beyond
     * the last step it will keep on setting the task name
     * but not progress the indicator bar.
     * If there is a sub task then it needs to be finished
     * first (in IntelliJ pop from stack).
     *
     * @param task Name of the Task
     */
    public void next(String task);

    /**
     * Starts the next sub task. If this call goes beyond
     * the last step it will keep on setting the task name
     * but not progress the indicator bar.
     * If there is a sub task then it needs to be finished
     * first (in IntelliJ pop from stack).
     *
     * @param task Name of the Task
     * @param params List of Parameters added to the Task
     */
    public void next(String task, String...params);

    public String getTitle();

    /**
     * Marks the progress to be cancelled at the next
     * possibility.
     */
    public void markAsCancelled();

    /** @return True if the Progress Handler is marked as cancelled */
    public boolean isMarkedAsCancelled();

    /**
     * Marks this and any child Progress Handler as not cancelable or not
     *
     * @param notCancelable True marks this progress handler as not cancelable
     *                      and False removes that.
     */
    public void setNotCancelable(boolean notCancelable);

    /** @return True if the Progress Handler is set a not cancelable */
    public boolean isNotCancelable();

    public ProgressHandler getParent();

    /** @return True if it has to run asynchronous, false if not and null if it is not forced **/
    public Boolean forceAsynchronous();
}
