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

package org.apache.sling.ide.eclipse.core.internal;

import com.intellij.openapi.components.ServiceManager;
import org.apache.sling.ide.artifacts.EmbeddedArtifactLocator;
import org.apache.sling.ide.filter.FilterLocator;
import org.apache.sling.ide.osgi.OsgiClientFactory;
import org.apache.sling.ide.serialization.SerializationManager;
import org.apache.sling.ide.transport.RepositoryFactory;
import org.osgi.service.event.EventAdmin;
import org.apache.sling.ide.log.Logger;

/**
 * This class is a Stub of the class used in the Eclipse Core Module of the Sling Eclipse IDE and
 * is just there to make the integration easier until there is a solution for separating the UI
 * from the Handling Code (support IntelliJ w/o OSGi).
 *
 * Created by schaefa on 5/14/15.
 */
public class Activator {

    public static final int PLUGIN_ID = 1;

    private static Activator instance = new Activator();

    public static Activator getDefault() {
        return instance;
    }

    public RepositoryFactory getRepositoryFactory() {
        return ServiceManager.getService(RepositoryFactory.class);
    }

    public SerializationManager getSerializationManager() {
        return ServiceManager.getService(SerializationManager.class);
    }

    public FilterLocator getFilterLocator() {
        return ServiceManager.getService(FilterLocator.class);
    }

    public OsgiClientFactory getOsgiClientFactory() {
        return ServiceManager.getService(OsgiClientFactory.class);
    }

    public EmbeddedArtifactLocator getArtifactLocator() {
        return ServiceManager.getService(EmbeddedArtifactLocator.class);
    }

    public Logger getPluginLogger() {
        return ServiceManager.getService(Logger.class);
    }

    public EventAdmin getEventAdmin() {
        return ServiceManager.getService(EventAdmin.class);
    }

    public void issueConsoleLog(String installBundle, String s, String s1) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
