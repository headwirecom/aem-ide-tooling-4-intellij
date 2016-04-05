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

package com.headwire.aem.tooling.intellij.eclipse.wrapper;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import org.apache.sling.ide.impl.vlt.VltRepositoryFactory;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.event.EventAdmin;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/14/15.
 */
@Deprecated
public class VltRepositoryFactoryWrapper
    extends VltRepositoryFactory
    implements ApplicationComponent
{

    public VltRepositoryFactoryWrapper() {
        EventAdmin eventAdmin = ServiceManager.getService(EventAdmin.class);
        bindEventAdmin(eventAdmin);
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "VLT Repository Factory";
    }
}
