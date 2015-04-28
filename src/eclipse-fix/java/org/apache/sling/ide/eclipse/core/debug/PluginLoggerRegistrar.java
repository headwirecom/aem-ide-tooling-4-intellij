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
 */
package org.apache.sling.ide.eclipse.core.debug;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.sling.ide.eclipse.core.debug.impl.Tracer;
import org.apache.sling.ide.log.Logger;
//import org.eclipse.osgi.service.debug.DebugOptions;
//import org.eclipse.osgi.service.debug.DebugOptionsListener;
//import org.osgi.framework.BundleContext;
import org.eclipse.core.runtime.MyBundleContext;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;

/**
 * The <tt>PluginLoggerRegistrar</tt> registers {@link Logger} implementations for use for specific plugins
 *
 */
public class PluginLoggerRegistrar {

    /**
     * Registers a new tracer for the specified plugin
     *
     * @param plugin the plugin to register for
     * @return the service registration
     */
    public static ServiceRegistration<?> register(final Plugin plugin) {

//        Dictionary<String, Object> props = new Hashtable<String, Object>();
//        props.put(DebugOptions.LISTENER_SYMBOLICNAME, plugin.getBundle().getSymbolicName());
//        BundleContext ctx = plugin.getBundle().getBundleContext();
//        return ctx.registerService(new String[] { DebugOptionsListener.class.getName(), Logger.class.getName() },
//            new Tracer(plugin), props);
//        return new ServiceRegistration(new Tracer(plugin));
        final Tracer tracer = new Tracer(plugin);

        final Bundle bundle = new Bundle() {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void start(int options) throws BundleException {

            }

            @Override
            public void start() throws BundleException {

            }

            @Override
            public void stop(int options) throws BundleException {

            }

            @Override
            public void stop() throws BundleException {

            }

            @Override
            public void update(InputStream input) throws BundleException {

            }

            @Override
            public void update() throws BundleException {

            }

            @Override
            public void uninstall() throws BundleException {

            }

            @Override
            public Dictionary<String, String> getHeaders() {
                return null;
            }

            @Override
            public long getBundleId() {
                return 0;
            }

            @Override
            public String getLocation() {
                return null;
            }

            @Override
            public ServiceReference<?>[] getRegisteredServices() {
                return new ServiceReference<?>[0];
            }

            @Override
            public ServiceReference<?>[] getServicesInUse() {
                return new ServiceReference<?>[0];
            }

            @Override
            public boolean hasPermission(Object permission) {
                return false;
            }

            @Override
            public URL getResource(String name) {
                return null;
            }

            @Override
            public Dictionary<String, String> getHeaders(String locale) {
                return null;
            }

            @Override
            public String getSymbolicName() {
                return null;
            }

            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return null;
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                return null;
            }

            @Override
            public Enumeration<String> getEntryPaths(String path) {
                return null;
            }

            @Override
            public URL getEntry(String path) {
                return null;
            }

            @Override
            public long getLastModified() {
                return 0;
            }

            @Override
            public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
                return null;
            }

            @Override
            public BundleContext getBundleContext() {
                return null;
            }

            @Override
            public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
                return null;
            }

            @Override
            public Version getVersion() {
                return null;
            }

            @Override
            public <A> A adapt(Class<A> type) {
                return null;
            }

            @Override
            public File getDataFile(String filename) {
                return null;
            }

            @Override
            public int compareTo(Bundle bundle) {
                return 0;
            }
        };

        MyBundleContext bundleContext = (MyBundleContext) plugin.getBundleContext();
        bundleContext.setBundle(bundle);

        final ServiceReference<Object> reference = new ServiceReference<Object>() {
            @Override
            public Object getProperty(String key) {
                if(Constants.SERVICE_ID.equals(key)) {
                    return plugin.getClass().getName();
                }
                return null;
            }

            @Override
            public String[] getPropertyKeys() {
                return new String[0];
            }

            @Override
            public Bundle getBundle() {
                return bundle;
            }

            @Override
            public Bundle[] getUsingBundles() {
                return new Bundle[] { bundle };
            }

            @Override
            public boolean isAssignableTo(Bundle bundle, String className) {
                return false;
            }

            @Override
            public int compareTo(Object reference) {
                return 0;
            }
        };

        bundleContext.addServiceReference(reference, tracer);

        return new ServiceRegistration<Object>() {
            @Override
            public ServiceReference<Object> getReference() {
                return reference;
            }

            @Override
            public void setProperties(Dictionary<String, ?> properties) {
            }

            @Override
            public void unregister() {
            }
        };
    }
}
