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

package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/14/15.
 */
@Deprecated
public class Status implements IStatus {

    private int status;
    private int componentId;
    private int actionId;
    private String message;
    private Throwable cause;

    public Status(int status, int componentId, String message) {
        this(status, componentId, message, null);
    }

    public Status(int status, int componentId, String message, Throwable t) {
        this(status, componentId, 0, message, t);
    }

    public Status(int status, int componentId, int actionId, String message, Throwable t) {
        this.status = status;
        this.componentId = componentId;
        this.actionId = actionId;
        this.message = message;
        this.cause = t;
    }

    public int getStatus() {
        return status;
    }

    public int getComponentId() {
        return componentId;
    }

    public int getActionId() {
        return actionId;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
