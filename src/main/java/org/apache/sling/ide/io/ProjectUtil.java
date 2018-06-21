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

/**
 * Created by Andreas Schaefer (Headwire.com) on 11/9/15.
 */
@Deprecated
public interface ProjectUtil {

    public Filter loadFilter(SlingProject project);

    /**
     * Obtains the Sling Resource representing the Sync Directory
     * @param resource Any resource part of the a Sync Directory
     * @return The resources that is represents the Sync Directory
     */
    public SlingResource getSyncDirectory(SlingResource resource);

    /**
     * Creates a Sling Resource from a Relative Local Path to the Sync Directory
     * @param resourcePath Local (IDE) path that points to a resource. The path can be OS dependent, even with mixed
     *                     folder separators.
     * @param syncDirectory Resource Path is a resource path relative to this Sync Directory
     * @return A local sling resource
     */
    public SlingResource getResourceFromPath(String resourcePath, SlingResource syncDirectory);

    /**
     * Creates a Sling Resource from an Absolute Local Path
     * @param resourcePath Local (IDE) path that points to a resource. The path can be OS dependent, even with mixed
     *                     folder separators.
     * @param project Sling Project
     * @return A local sling resource
     */
    public SlingResource getResourceFromPath(String resourcePath, SlingProject project);
}
