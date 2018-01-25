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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.sling.ide.osgi.OsgiClient;
import org.apache.sling.ide.osgi.OsgiClientException;
import org.apache.sling.ide.osgi.impl.HttpOsgiClient;
import org.apache.sling.ide.osgi.impl.TracingOsgiClient;
import org.apache.sling.ide.transport.RepositoryInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides the data of a deployed bundle. It is written in a way
 * so that it can be easily copied into HttpOsgiClient
 *
 * TODO: Migrate this into the OsgiClient of Sling
 *
 * Created by Andreas Schaefer
 */
public class BundleDataUtil {

    public static Map<String, String> getData(OsgiClient osgiClient, String bundleSymbolicName) throws OsgiClientException {
        Map<String, String> answer = new HashMap<>();
        RepositoryInfo repositoryInfo = getRepositoryInfo(osgiClient);
        GetMethod method = new GetMethod(repositoryInfo.appendPath("system/console/bundles/" + bundleSymbolicName + ".json"));
        HttpClient client = getHttpClient(osgiClient);

        try {
            int result = client.executeMethod(method);
            if (result != HttpStatus.SC_OK) {
                throw new HttpException("Got status code " + result + " for call to " + method.getURI());
            }
            try ( InputStream input = method.getResponseBodyAsStream() ) {
                JSONObject object = new JSONObject(new JSONTokener(new InputStreamReader(input)));
                JSONArray bundleData = object.getJSONArray("data");
                if(bundleData.length() > 1) {
                    throw new OsgiClientException("More than one Bundle found");
                } else {
                    JSONObject bundle = bundleData.getJSONObject(0);
                    for(Object item: bundle.keySet()) {
                        String name = item + "";
                        String value = bundle.get(name) + "";
                        answer.put(name, value);
                    }
                }
            }
        } catch (IOException | JSONException e) {
            throw new OsgiClientException(e);
        } finally {
            method.releaseConnection();
        }
        return answer;
    }

    private static RepositoryInfo getRepositoryInfo(OsgiClient osgiClient)
        throws OsgiClientException
    {
        Field repositoryInfoField = null;
        try {
            if(osgiClient instanceof TracingOsgiClient) {
                try {
                    osgiClient = getPrivateField(TracingOsgiClient.class, HttpOsgiClient.class, osgiClient, "osgiClient");
                } catch(NoSuchFieldException e) {
                    throw new OsgiClientException("Could not Access Server", e);
                } catch(IllegalAccessException e) {
                    throw new OsgiClientException("Could not Access Server", e);
                }
            }
            return getPrivateField(HttpOsgiClient.class, RepositoryInfo.class, osgiClient, "repositoryInfo");
        } catch(NoSuchFieldException e) {
            throw new OsgiClientException("Failed to Obtain Repository Info", e);
        } catch(IllegalAccessException e) {
            throw new OsgiClientException("Failed to Obtain Repository Info", e);
        }
    }

    private static <T> T getPrivateField(Class instanceClass, Class<T> returnClass, Object instance, String name)
        throws NoSuchFieldException, IllegalAccessException
    {
        Field field = instanceClass.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(instance);
    }

    private static HttpClient getHttpClient(OsgiClient osgiClient)
        throws OsgiClientException
    {
        Method httpClientMethod = null;
        try {
            if(osgiClient instanceof TracingOsgiClient) {
                try {
                    osgiClient = getPrivateField(TracingOsgiClient.class, HttpOsgiClient.class, osgiClient, "osgiClient");
                } catch(NoSuchFieldException e) {
                    throw new OsgiClientException("Could not Access Server", e);
                } catch(IllegalAccessException e) {
                    throw new OsgiClientException("Could not Access Server", e);
                }
            }
            httpClientMethod = HttpOsgiClient.class.getDeclaredMethod("getHttpClient");
            httpClientMethod.setAccessible(true);
            Object temp = httpClientMethod.invoke(osgiClient);
            if(temp instanceof HttpClient) {
                return (HttpClient) temp;
            } else {
                throw new OsgiClientException("Failed to Http Client Info");
            }
        } catch(IllegalAccessException e) {
            throw new OsgiClientException("Failed to Http Client Info", e);
        } catch(NoSuchMethodException e) {
            throw new OsgiClientException("Failed to Http Client Info", e);
        } catch(InvocationTargetException e) {
            throw new OsgiClientException("Failed to Http Client Info", e);
        }
    }
}
