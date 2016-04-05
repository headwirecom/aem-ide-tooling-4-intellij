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

package com.headwire.aem.tooling.intellij.eclipse;

import java.net.URI;
import java.net.URISyntaxException;

import com.headwire.aem.tooling.intellij.communication.MessageManager;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IModule;
import com.headwire.aem.tooling.intellij.eclipse.stub.IProgressMonitor;
import com.headwire.aem.tooling.intellij.eclipse.stub.IProject;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.ISlingLaunchpadConfiguration;
import com.headwire.aem.tooling.intellij.eclipse.stub.ISlingLaunchpadServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.eclipse.stub.SlingLaunchpadServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.Status;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.transport.Repository;
import org.apache.sling.ide.transport.RepositoryException;
import org.apache.sling.ide.transport.RepositoryFactory;
import org.apache.sling.ide.transport.RepositoryInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//AS TOOD: Copy from the Eclipse Project -> Clean up
@Deprecated
public abstract class ServerUtil {

    private static final Logger LOGGER = Logger.getInstance(ServerUtil.class);

    //TODO: SLING-3587 - following constants are from wst.Server - an internal class
    // we should replace this with proper API - but afaik this information is not
    // accessible via any API ..
    private static final int AUTO_PUBLISH_DISABLE = 1;
    private static final int AUTO_PUBLISH_RESOURCE = 2;
    private static final int AUTO_PUBLISH_BUILD = 3;
    private static final String PROP_AUTO_PUBLISH_SETTING = "auto-publish-setting";
    private static final String PROP_AUTO_PUBLISH_TIME = "auto-publish-time";


    /* Is original inside the org.eclipse.wst.serxver.core.ServerUtil class */
    private static IModule getModule(IProject project) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    public static Repository getConnectedRepository(@NotNull IServer server, @NotNull IProgressMonitor monitor, @NotNull MessageManager messageManager) {
        Repository ret = null;
        if (server.getServerState()!=IServer.STATE_STARTED) {
            messageManager.showAlertWithArguments(NotificationType.ERROR, "deploy.connection.not.stared");
        } else {
            RepositoryFactory repository = Activator.getDefault().getRepositoryFactory();
            try {
                RepositoryInfo repositoryInfo = getRepositoryInfo(server, monitor);
                ret = repository.getRepository(repositoryInfo, false);
            } catch(URISyntaxException e) {
                messageManager.showAlertWithArguments(NotificationType.ERROR, "deploy.connection.configuration.bad.url", server.getServerConfiguration().getName());
            } catch(RuntimeException e) {
                messageManager.showAlertWithArguments(NotificationType.ERROR, "deploy.connection.unexpected.problem", server.getServerConfiguration().getName(), e.getMessage());
            } catch(RepositoryException e) {
                messageManager.showAlertWithArguments(NotificationType.ERROR, "deploy.connection.repository.problem", server.getServerConfiguration().getName(), e.getMessage());
            }
        }
        return ret;
    }

    public static Repository connectRepository(IServer server, IProgressMonitor monitor) throws CoreException {
        RepositoryFactory repository = Activator.getDefault().getRepositoryFactory();
        try {
            RepositoryInfo repositoryInfo = getRepositoryInfo(server, monitor);
            return repository.connectRepository(repositoryInfo);
        } catch (URISyntaxException e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        } catch (RuntimeException e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        } catch (RepositoryException e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }
    }

    public static void stopRepository(IServer server, IProgressMonitor monitor) throws CoreException {
        RepositoryFactory repository = Activator.getDefault().getRepositoryFactory();
        try {
            RepositoryInfo repositoryInfo = getRepositoryInfo(server, monitor);
            repository.disconnectRepository(repositoryInfo);
        } catch (URISyntaxException e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        } catch (RuntimeException e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }
    }


    public static RepositoryInfo getRepositoryInfo(IServer server, IProgressMonitor monitor) throws URISyntaxException {

        ISlingLaunchpadServer launchpadServer = (ISlingLaunchpadServer) server.loadAdapter(SlingLaunchpadServer.class,
            monitor);

        ISlingLaunchpadConfiguration configuration = launchpadServer.getConfiguration();

        // TODO configurable scheme?
        URI uri = new URI("http", null, server.getHost(), configuration.getPort(), configuration.getContextPath(),
            null, null);
        return new RepositoryInfo(configuration.getUsername(),
            configuration.getPassword(), uri.toString());
    }

    private ServerUtil() {

    }
}
