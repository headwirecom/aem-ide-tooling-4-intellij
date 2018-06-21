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

package com.headwire.aem.tooling.intellij.io.wrapper;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.components.ApplicationComponent;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/14/15.
 */
public class EventAdminWrapper
//    extends ApplicationComponent.Adapter
    implements EventAdmin, ApplicationComponent
{

//    public EventAdminWrapper(Application application) {
//    }
//
    @Override
    public void postEvent(Event event) {

    }

    @Override
    public void sendEvent(Event event) {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }
}
