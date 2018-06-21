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
package org.apache.sling.ide.io;

import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.sync.content.WorkspaceProject;

/**
 * Created by Andreas Schaefer (Headwire.com) on 11/9/15.
 */
public interface SlingProject
    extends WorkspaceProject
{

    /**
     * Find a File by the absolute Path
     * @param path Absolute Path to a File
     * @return Sling Resource if found otherwise null
     */
    public SlingResource findFileByPath(String path);

//    /** @return Sling Resource of the Sync Directory **/
//    public SlingResource getSyncDirectory();

    /** Loads the META-INF/filter.xml file **/
    public Filter loadFilter() throws ConnectorException;
}
