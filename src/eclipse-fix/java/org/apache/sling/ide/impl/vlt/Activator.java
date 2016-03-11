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

package org.apache.sling.ide.impl.vlt;

import com.intellij.openapi.components.ServiceManager;
import org.apache.sling.ide.log.Logger;

/**
 * Created by schaefa on 5/15/15.
 */
public class Activator {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.apache.sling.ide.impl-vlt"; //$NON-NLS-1$

    private static Activator instance = new Activator();

    public static Activator getDefault() {
        return instance;
    }

    public Logger getPluginLogger() {
        return ServiceManager.getService(Logger.class);
    }
}
