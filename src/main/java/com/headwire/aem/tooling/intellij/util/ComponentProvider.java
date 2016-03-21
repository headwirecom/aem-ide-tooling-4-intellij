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
import com.intellij.openapi.project.Project;

/**
 * This class hides the way IntelliJ obtains components / services.
 * The reason is that there is a shift between IntelliJ 14 and 15+
 *
 * Created by schaefa on 3/18/16.
 */
public class ComponentProvider {

    public static <T> T getComponent(Project project, Class<T> clazz) {
        T ret = null;
        if(project != null) {
            // IntelliJ 15+ should find all components / servies here
            ret = project.getComponent(clazz);
        }
        if(ret == null) {
            // If not found then we try IntelliJ 14's way through the Application Manager
            // which only works for Application Components but worth a shot
            ret = ApplicationManager.getApplication().getComponent(clazz);
        }
        //AS TODO: We should report an error here and maybe throw an exception to make sure the plugin is proceeding
        return ret;
    }
}
