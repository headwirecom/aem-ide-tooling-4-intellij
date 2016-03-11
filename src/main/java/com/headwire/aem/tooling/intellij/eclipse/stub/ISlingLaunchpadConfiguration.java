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
 * Created by schaefa on 5/14/15.
 */
@Deprecated
public class ISlingLaunchpadConfiguration {

    private ServerConfiguration serverConfiguration;

    public ISlingLaunchpadConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public int getPort() {
        return serverConfiguration.getConnectionPort();
    }

    public String getContextPath() {
        return serverConfiguration.getContextPath();
    }

    public String getUsername() {
        return serverConfiguration.getUserName();
    }

    public String getPassword() {
        return new String(serverConfiguration.getPassword());
    }
}
