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

import org.apache.sling.ide.log.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/15/15.
 */
public class Activator2 {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.apache.sling.ide.impl-vlt"; //$NON-NLS-1$

    private static Activator2 instance = new Activator2();

    public static Activator2 getDefault() {
        return instance;
    }

    public Logger getPluginLogger() {
        Logger answer = null;
        // Use reflection to avoid runtime dependencies
        try {
            Class applicationManagerClass = Thread.currentThread().getContextClassLoader().loadClass("com.intellij.openapi.application.ApplicationManager");
            Method applicationMethod = applicationManagerClass.getDeclaredMethod("getApplication", null);
            Object application = applicationMethod.invoke(null, null);
            if(application != null) {
                Method componentMethod = application.getClass().getDeclaredMethod("getComponent", Class.class);
                answer = (Logger) componentMethod.invoke(application, Logger.class);
            }
        } catch(ClassNotFoundException e) {
            System.err.println("Could find Application Manager class");
            e.printStackTrace();
        } catch(NoSuchMethodException e) {
            System.err.println("Could find 'getApplication' or 'getComponent' method");
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            System.err.println("Could not access 'getApplication' or 'getComponent' method");
            e.printStackTrace();
        } catch(InvocationTargetException e) {
            System.err.println("Could not invoke 'getApplication' or 'getComponent' method");
            e.printStackTrace();
        }
        return answer;
    }
}
