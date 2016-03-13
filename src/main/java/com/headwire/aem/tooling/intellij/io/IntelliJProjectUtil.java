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

package com.headwire.aem.tooling.intellij.io;

import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.io.ConnectorException;
import org.apache.sling.ide.io.ProjectUtil;
import org.apache.sling.ide.io.SlingProject;
import org.apache.sling.ide.io.SlingResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Andreas Schaefer (Headwire.com) on 11/16/15.
 */
@Deprecated
public class IntelliJProjectUtil
    implements ProjectUtil
{
    @Override
    public Filter loadFilter(SlingProject project) {
        try {
            return ((SlingProject4IntelliJ) project).loadFilter();
        } catch (ConnectorException e) {
            //AS Log this
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SlingResource getSyncDirectory(SlingResource resource) {
        return resource.getProject().getSyncDirectory();
    }

    @Override
    public SlingResource getResourceFromPath(String resourcePath, SlingResource syncDirectory) {
        //AS TODO: Need to handle OS Specific Paths
        String check = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        return syncDirectory.findChildByPath(check);
    }

    @Override
    public SlingResource getResourceFromPath(String resourcePath, SlingProject project) {
        return project.findFileByPath(resourcePath);
    }

}
