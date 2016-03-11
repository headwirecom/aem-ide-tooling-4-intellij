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
 * Created by schaefa on 5/15/15.
 */
@Deprecated
public class ServerBehaviourDelegate {
    /**
     * Publish kind constant (value 0) for no change.
     */
    public static final int NO_CHANGE = 0;

    /**
     * Publish kind constant (value 1) for added resources.
     */
    public static final int ADDED = 1;

    /**
     * Publish kind constant (value 2) for changed resources.
     */
    public static final int CHANGED = 2;

    /**
     * Publish kind constant (value 3) for removed resources.
     */
    public static final int REMOVED = 3;


}
