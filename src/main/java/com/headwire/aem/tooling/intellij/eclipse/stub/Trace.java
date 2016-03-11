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

import java.util.Date;

/**
 * Created by schaefa on 5/18/15.
 */
@Deprecated
public class Trace {
    // tracing enablement flags
    public static boolean CONFIG = false;
    public static boolean INFO = false;
    public static boolean WARNING = false;
    public static boolean SEVERE = false;
    public static boolean FINER = false;
    public static boolean FINEST = false;
    public static boolean RESOURCES = false;
    public static boolean EXTENSION_POINT = false;
    public static boolean LISTENERS = false;
    public static boolean RUNTIME_TARGET = false;
    public static boolean PERFORMANCE = false;
    public static boolean PUBLISHING = false;

    // tracing levels. One most exist for each debug option
    public final static String STRING_CONFIG = "/config"; //$NON-NLS-1$
    public final static String STRING_INFO = "/info"; //$NON-NLS-1$
    public final static String STRING_WARNING = "/warning"; //$NON-NLS-1$
    public final static String STRING_SEVERE = "/severe"; //$NON-NLS-1$
    public final static String STRING_FINER = "/finer"; //$NON-NLS-1$
    public final static String STRING_FINEST = "/finest"; //$NON-NLS-1$
    public final static String STRING_RESOURCES = "/resources"; //$NON-NLS-1$
    public final static String STRING_EXTENSION_POINT = "/extension_point"; //$NON-NLS-1$
    public final static String STRING_LISTENERS = "/listeners"; //$NON-NLS-1$
    public final static String STRING_RUNTIME_TARGET = "/runtime_target"; //$NON-NLS-1$
    public final static String STRING_PERFORMANCE = "/performance"; //$NON-NLS-1$
    public final static String STRING_PUBLISHING = "/publishing"; //$NON-NLS-1$

    /**
     * Trace the given message.
     *
     * @param level
     *            The tracing level.
     * @param s
     *            The message to trace
     */
    public static void trace(final String level, String s) {
        Trace.trace(level, s, null);
    }

    /**
     * Trace the given message and exception.
     *
     * @param level
     *            The tracing level.
     * @param s
     *            The message to trace
     * @param t
     *            A {@link Throwable} to trace
     */
    public static void trace(final String level, String s, Throwable t) {
//        if (s == null) {
//            return;
//        }
//        if (Trace.STRING_SEVERE.equals(level)) {
//            if (!logged.contains(s)) {
//                ServerPlugin.log(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, s, t));
//                logged.add(s);
//            }
//        }
//        if (ServerPlugin.getInstance().isDebugging()) {
//            final StringBuffer sb = new StringBuffer(ServerPlugin.PLUGIN_ID);
//            sb.append(" "); //$NON-NLS-1$
//            sb.append(level);
//            sb.append(" "); //$NON-NLS-1$
//            sb.append(sdf.format(new Date()));
//            sb.append(" "); //$NON-NLS-1$
//            sb.append(s);
//            System.out.println(sb.toString());
//            if (t != null) {
//                t.printStackTrace();
//            }
//        }
    }

}
