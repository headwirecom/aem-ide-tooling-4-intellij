package com.headwire.aem.tooling.intellij.eclipse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IModule;
import com.headwire.aem.tooling.intellij.eclipse.stub.IProgressMonitor;
import com.headwire.aem.tooling.intellij.eclipse.stub.IProject;
import com.headwire.aem.tooling.intellij.eclipse.stub.IResource;
import com.headwire.aem.tooling.intellij.eclipse.stub.IServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.ISlingLaunchpadConfiguration;
import com.headwire.aem.tooling.intellij.eclipse.stub.ISlingLaunchpadServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.NullProgressMonitor;
import com.headwire.aem.tooling.intellij.eclipse.stub.ServerCore;
import com.headwire.aem.tooling.intellij.eclipse.stub.SlingLaunchpadServer;
import com.headwire.aem.tooling.intellij.eclipse.stub.Status;
import com.headwire.aem.tooling.intellij.util.ServerException;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.transport.Repository;
import org.apache.sling.ide.transport.RepositoryException;
import org.apache.sling.ide.transport.RepositoryFactory;
import org.apache.sling.ide.transport.RepositoryInfo;

//AS TOOD: Copy from the Eclipse Project -> Clean up
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

    public static Repository getDefaultRepository(IProject project) {
        IServer server = getDefaultServer(project);
        if (server == null) {
            return null;
        }
        try {
            RepositoryFactory repository = Activator.getDefault().getRepositoryFactory();
            try {
                RepositoryInfo repositoryInfo = getRepositoryInfo(server, new NullProgressMonitor());
                return repository.getRepository(repositoryInfo, true);
            } catch (URISyntaxException e) {
                throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
            } catch (RuntimeException e) {
                throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
            } catch (RepositoryException e) {
                throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
            }
        } catch (CoreException e) {
            Activator.getDefault().getPluginLogger().warn("Failed getting a repository for " + project, e);
            return null;
        }
    }

    private static IServer getDefaultServer(IProject project) {
//        IModule module = org.eclipse.wst.server.core.ServerUtil.getModule(project);
        IModule module = ServerUtil.getModule(project);
        if (module==null) {
            // if there's no module for a project then there's no IServer for sure - which
            // is what we need to create a RepositoryInfo
            return null;
        }
        IServer server = ServerCore.getDefaultServer(module);
        if (server!=null) {
            return server;
        }
        // then we cannot create a repository
        IServer[] allServers = ServerCore.getServers();
        out: for (int i = 0; i < allServers.length; i++) {
            IServer aServer = allServers[i];
            IModule[] allModules = aServer.getModules();
            for (int j = 0; j < allModules.length; j++) {
                IModule aMoudle = allModules[j];
                if (aMoudle.equals(module)) {
                    server = aServer;
                    break out;
                }
            }
        }
        return server;
    }

    private static Set<IServer> getAllServers(IProject project) {
//        IModule module = org.eclipse.wst.serxver.core.ServerUtil.getModule(project);
        IModule module = ServerUtil.getModule(project);
        if (module==null) {
            // if there's no module for a project then there's no IServer for sure - which
            // is what we need to create a RepositoryInfo
            return null;
        }
        Set<IServer> result = new HashSet<IServer>();
        IServer defaultServer = ServerCore.getDefaultServer(module);
        if (defaultServer!=null) {
            result.add(defaultServer);
        }

        IServer[] allServers = ServerCore.getServers();
        for (int i = 0; i < allServers.length; i++) {
            IServer aServer = allServers[i];
            IModule[] allModules = aServer.getModules();
            for (int j = 0; j < allModules.length; j++) {
                IModule aMoudle = allModules[j];
                if (aMoudle.equals(module)) {
                    result.add(aServer);
                    break;
                }
            }
        }
        return result;
    }

    /* Is original inside the org.eclipse.wst.serxver.core.ServerUtil class */
    private static IModule getModule(IProject project) {
        throw new UnsupportedOperationException("Not yet implemented");
//        return null;
    }

    public static Repository getConnectedRepository(IServer server, IProgressMonitor monitor) throws CoreException {
        if (server==null) {
            throw new CoreException(new Status(Status.WARNING, Activator.PLUGIN_ID, "No server available/selected."));
        }
        if (server.getServerState()!=IServer.STATE_STARTED) {
            throw new CoreException(new Status(Status.WARNING, Activator.PLUGIN_ID, "Server not started, please start server first."));
        }
        RepositoryFactory repository = Activator.getDefault().getRepositoryFactory();
        try {
            RepositoryInfo repositoryInfo = getRepositoryInfo(server, monitor);
            return repository.getRepository(repositoryInfo, false);
        } catch (URISyntaxException e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        } catch (RuntimeException e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        } catch (RepositoryException e) {
            throw new CoreException(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }
    }

//    public static Repository getConnectedRepository(
////        ServerConfiguration serverConfiguration
//        IServer server, IProgressMonitor monitor
//    ) throws ServerException {
////        if (server==null) {
////            throw new CoreException(new Status(Status.WARNING, Activator.PLUGIN_ID, "No server available/selected."));
////        }
////        if (server.getServerState()!=IServer.STATE_STARTED) {
////            throw new CoreException(new Status(Status.WARNING, Activator.PLUGIN_ID, "Server not started, please start server first."));
////        }
////        RepositoryFactory repository = new RepositoryFactoryImpl();
////        RepositoryFactory repository = OSGiFactory.getRepositoryFactory();
//        RepositoryFactory repository = Activator.getDefault().getRepositoryFactory();
//        try {
//            RepositoryInfo repositoryInfo = getRepositoryInfo(
////                serverConfiguration
//                server, monitor
//            );
//            return repository.getRepository(repositoryInfo, false);
//        } catch (URISyntaxException e) {
//            throw new ServerException("URI Exception: " + e.getMessage(), e);
//        } catch (RuntimeException e) {
//            throw new ServerException("Unexpected Exception", e);
//        } catch (RepositoryException e) {
//            throw new ServerException("Repository Exception", e);
//        }
//    }

//    public static Repository connectRepository(ServerConfiguration serverConfiguration)
//        throws ServerException
//    {
////        RepositoryFactory repository = new RepositoryFactoryImpl();
////        RepositoryFactory repository = OSGiFactory.getRepositoryFactory();
//        RepositoryFactory repository = Activator.getDefault().getRepositoryFactory();
//        try {
//            RepositoryInfo repositoryInfo = getRepositoryInfo(serverConfiguration);
//            return repository.connectRepository(repositoryInfo);
//        } catch (URISyntaxException e) {
//            throw new ServerException("URI Exception: " + e.getMessage(), e);
//        } catch (RuntimeException e) {
//            throw new ServerException("Unexpected Exception", e);
//        } catch (RepositoryException e) {
//            throw new ServerException("Repository Exception", e);
//        }
//    }

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

//    public static Repository connectRepository(ServerConfiguration server) {
////        RepositoryFactory repository = Activator.getDefault().getRepositoryFactory();
//        RepositoryFactory repository = new RepositoryFactoryImpl();
//        try {
//            RepositoryInfo repositoryInfo = new RepositoryInfo(
//                    "admin",
//                    "admin",
//                    "http://" + server.getHostName() + ":4502" + "/"
//            );
////            return repository.connectRepository(repositoryInfo);
//            Repository myRepository = repository.connectRepository(repositoryInfo);
//
//            Command<ResourceProxy> command = myRepository.newListChildrenNodeCommand("/");
//            Result<ResourceProxy> result = command.execute();
//            boolean success = result.isSuccess();
//            LOGGER.debug("Repository: '{}', children: '{}'", repositoryInfo, result.get());
//
//            return myRepository;
//        } catch (RuntimeException e) {
//            throw new RuntimeException("Failed to connect to server", e);
//        } catch (RepositoryException e) {
//            throw new RuntimeException("Failed to connect to server", e);
//        }
//    }

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
    
    
//    public static RepositoryInfo getRepositoryInfo(
//        ServerConfiguration serverConfiguration
//    )
//        throws URISyntaxException
//    {
//        // TODO configurable scheme?
//        URI uri = new URI("http", null, serverConfiguration.getHost(), serverConfiguration.getConnectionPort(), serverConfiguration.getContextPath(),
//                null, null);
//        return new RepositoryInfo(
//            serverConfiguration.getUserName(),
//            new String(serverConfiguration.getPassword()),
//            uri.toString());
//    }

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

    public static void triggerIncrementalBuild(IResource anyResourceInThatProject, IProgressMonitor monitorOrNull) {
        if (anyResourceInThatProject==null) {
            throw new IllegalArgumentException("anyResourceInThatProject must not be null");
        }
        IProject proj = anyResourceInThatProject.getProject();
        if (proj==null) {
            throw new IllegalStateException("no project found for "+anyResourceInThatProject);
        }
        Set<IServer> servers = getAllServers(proj);

        if (servers!=null) {
            if (monitorOrNull==null) {
                monitorOrNull = new NullProgressMonitor();
            }
            for (Iterator it = servers.iterator(); it.hasNext();) {
                IServer aServer = (IServer) it.next();
                if (aServer!=null) {
                    int autoPublishSetting = aServer.getAttribute(PROP_AUTO_PUBLISH_SETTING, AUTO_PUBLISH_RESOURCE);
                    int autoPublishTime = aServer.getAttribute(PROP_AUTO_PUBLISH_TIME, 15);
                    if (autoPublishSetting==AUTO_PUBLISH_RESOURCE) {
                        //TODO: ignoring the autoPublishTime - SLING-3587
                        aServer.publish(IServer.PUBLISH_INCREMENTAL, monitorOrNull);
                    }
                }
            }
        }
    }
}
