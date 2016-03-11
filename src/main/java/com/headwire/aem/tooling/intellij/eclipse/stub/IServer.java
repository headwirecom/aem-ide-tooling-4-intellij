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

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/14/15.
 */
@Deprecated
public class IServer {

    public static final int PUBLISH_INCREMENTAL = 1;
    public static final int STATE_STOPPED = 2;
    public static final int PUBLISH_STATE_NONE = 3;
    public static final int PUBLISH_AUTO = 4;
    public static final int PUBLISH_FULL = 5;
    public static final int PUBLISH_CLEAN = 6;
    public static final int STATE_STARTED = 7;
    public static final int STATE_STARTING = 8;
    public static final int STATE_STOPPING = 9;
    public static final int PUBLISH_STATE_FULL = 10;

    private ServerConfiguration serverConfiguration;
    private IServerType serverType;

    protected IFolder configuration;

    public IServer(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public Object loadAdapter(Class clazz, IProgressMonitor monitor) {
        if(clazz == SlingLaunchpadServer.class) {
            return new ISlingLaunchpadServer(serverConfiguration);
        }
        throw new UnsupportedOperationException("Not yet implemented");
//        return null;
    }

    public String getHost() {
        return serverConfiguration.getHost();
    }

    public <T> T getAttribute(String propAutoPublishSetting, T autoPublishResource) {
        throw new UnsupportedOperationException("Not yet implemented");
//        return 0;
    }

    public IServerType getServerType() {
        return serverType;
    }

    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    //    /*
//     * Publish to the server using the progress monitor. The result of the
//     * publish operation is returned as an IStatus.
//     */
//    public IStatus publish(int kind, IProgressMonitor monitor) {
////AS TODO: Find a solution for this
////        if (getServerType() == null)
////            return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorMissingAdapter, null);
////
////        // check what is out of sync and publish
////        if (getServerType().hasServerConfiguration() && configuration == null)
////            return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorNoConfiguration, null);
////
////        // make sure that the delegate is loaded and the server state is correct
////        loadAdapter(ServerBehaviourDelegate.class, monitor);
////
////        if (((ServerType)getServerType()).startBeforePublish() && (getServerState() == IServer.STATE_STOPPED)) {
////            IStatus status = startImpl(ILaunchManager.RUN_MODE, monitor);
////            if (status != null && status.getSeverity() == IStatus.ERROR)
////                return status;
////        }
//
//        return publishImpl(kind, null, null, monitor);
//    }

//    protected IStatus publishImpl(int kind, List<IModule[]> modules4, IAdaptable info, IProgressMonitor monitor) {
////        if (Trace.FINEST) {
////            Trace.trace(Trace.STRING_FINEST, "-->-- Publishing to server: " + Server.this.toString() + " -->--");
////        }
////        if (Trace.FINEST) {
////            Trace.trace(Trace.STRING_FINEST, "Server.publishImpl(): kind=<" + getPublishKindString(kind) + "> modules="
////                + modules4);
////        }
//
//        stopAutoPublish();
//
//        try {
//            long time = System.currentTimeMillis();
//            firePublishStarted();
//
////AS TODO: Find a solution for handling Server Publish Info
////            getServerPublishInfo().startCaching();
//            IStatus status = Status.OK_STATUS;
//            try {
////                getBehaviourDelegate(monitor).publish(kind, modules4, monitor, info);
//                new SlingPublisher().publishModule(kind, -2, modules4.toArray(new IModule[] {}), monitor);
//            } catch (CoreException ce) {
//                if (Trace.WARNING) {
//                    Trace.trace(Trace.STRING_WARNING, "Error during publishing", ce);
//                }
////                status = ce.getStatus();
//            }
//
////AS TODO: Find a solution for handling Server Publish Info
////            final List<IModule[]> modules2 = new ArrayList<IModule[]>();
////            visit(new IModuleVisitor() {
////                public boolean visit(IModule[] module) {
////                    if (getModulePublishState(module) == IServer.PUBLISH_STATE_NONE)
////                        getServerPublishInfo().fill(module);
////
////                    modules2.add(module);
////                    return true;
////                }
////            }, monitor);
//
////AS TODO: Find a solution for handling Server Publish Info
////            getServerPublishInfo().removeDeletedModulePublishInfo(this, modules2);
////            getServerPublishInfo().clearCache();
////            getServerPublishInfo().save();
//
//            firePublishFinished(Status.OK_STATUS);
//            if (Trace.PERFORMANCE) {
////                Trace.trace(Trace.STRING_PERFORMANCE, "Server.publishImpl(): <" + (System.currentTimeMillis() - time)
////                    + "> " + getServerType().getId());
//            }
//            return status;
//        } catch (Exception e) {
//            if (Trace.SEVERE) {
////                Trace.trace(Trace.STRING_SEVERE, "Error calling delegate publish() " + Server.this.toString(), e);
//            }
//            return new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorPublishing, e);
//        }
//    }

//    public ServerPublishInfo getServerPublishInfo() {
//        if (publishInfo == null) {
//            publishInfo = PublishInfo.getInstance().getServerPublishInfo(this);
//        }
//        return publishInfo;
//    }

    /**
     * Fire a publish start event.
     */
    protected void firePublishStarted() {
//AS TODO: Do we want, need, support this
//        if (Trace.FINEST) {
//            Trace.trace(Trace.STRING_FINEST, "->- Firing publish started event ->-");
//        }
//
//        if (publishListeners == null || publishListeners.isEmpty())
//            return;
//
//        int size = publishListeners.size();
//        IPublishListener[] srl = new IPublishListener[size];
//        publishListeners.toArray(srl);
//
//        for (int i = 0; i < size; i++) {
//            if (Trace.FINEST) {
//                Trace.trace(Trace.STRING_FINEST, "  Firing publish started event to " + srl[i]);
//            }
//            try {
//                srl[i].publishStarted(this);
//            } catch (Exception e) {
//                if (Trace.SEVERE) {
//                    Trace.trace(Trace.STRING_SEVERE, "  Error firing publish started event to " + srl[i], e);
//                }
//            }
//        }
//
//        if (Trace.FINEST) {
//            Trace.trace(Trace.STRING_FINEST, "-<- Done firing publish started event -<-");
//        }
    }

    /**
     * Fire a publish stop event.
     *
     * @param status publishing status
     */
    protected void firePublishFinished(IStatus status) {
//AS TODO: Do we want, need, support this
//        if (Trace.FINEST) {
//            Trace.trace(Trace.STRING_FINEST, "->- Firing publishing finished event: " + status + " ->-");
//        }
//
//        if (publishListeners == null || publishListeners.isEmpty())
//            return;
//
//        int size = publishListeners.size();
//        IPublishListener[] srl = new IPublishListener[size];
//        publishListeners.toArray(srl);
//
//        for (int i = 0; i < size; i++) {
//            if (Trace.FINEST) {
//                Trace.trace(Trace.STRING_FINEST, "  Firing publishing finished event to " + srl[i]);
//            }
//            try {
//                srl[i].publishFinished(this, status);
//            } catch (Exception e) {
//                if (Trace.SEVERE) {
//                    Trace.trace(Trace.STRING_SEVERE, "  Error firing publishing finished event to " + srl[i], e);
//                }
//            }
//        }
//
//        if (Trace.FINEST) {
//            Trace.trace(Trace.STRING_FINEST, "-<- Done firing publishing finished event -<-");
//        }
    }

    protected void stopAutoPublish() {
//AS TODO: Do we want, need, support this
//        if (autoPublishThread == null)
//            return;
//
//        autoPublishThread.stop = true;
//        autoPublishThread.interrupt();
//        autoPublishThread = null;
    }

    public IModule[] getModules() {
        throw new UnsupportedOperationException("Not yet implemented");
//        return new IModule[0];
    }

    public int getServerState() {
        switch(serverConfiguration.getServerStatus()) {
            case connected:
            case running:
//            case upToDate:
                return STATE_STARTED;
            default:
                return STATE_STARTING;
        }
    }

    public String getMode() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
