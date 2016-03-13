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

package com.headwire.aem.tooling.intellij.eclipse.wrapper;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.components.ApplicationComponent;
import org.apache.sling.ide.log.Logger;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/14/15.
 */
@Deprecated
public class PluginLoggerWrapper
    extends ApplicationComponent.Adapter
    implements Logger
{
    private com.intellij.openapi.diagnostic.Logger delegate;

    public PluginLoggerWrapper() {
        delegate = PluginManager.getLogger();
    }
    @Override
    public void error(String s) {
        delegate.error(s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        delegate.error(s, throwable);
    }

    @Override
    public void warn(String s) {
        delegate.warn(s);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        delegate.warn(s, throwable);
    }

    @Override
    public void trace(String s, Object... objects) {
        delegate.debug(s, objects);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        delegate.debug(s, throwable);
    }

    @Override
    public void tracePerformance(String s, long l, Object... objects) {
        delegate.debug(s, objects);
    }
}
