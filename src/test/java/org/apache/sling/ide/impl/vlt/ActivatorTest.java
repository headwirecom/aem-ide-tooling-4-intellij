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
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * This test here is to verify that the "eclipse-fix" is working in IntelliJ using the
 *
 * Created by schaefa on 4/28/15.
 */
public class ActivatorTest {

    @Test
    public void testActivator() throws Exception {
        Activator activator = new Activator();

//        // Check if that works with just a null bundle context
//        BundleContext bundleContext = new MyBundleContext();
//        activator.start(bundleContext);
        // Obtain the Default instance
        Activator test = Activator.getDefault();
        // Obtain the Plugin Logger
        Logger pluginLogger = test.getPluginLogger();

        assertNotNull("Logger was not specified", pluginLogger);
    }
}
