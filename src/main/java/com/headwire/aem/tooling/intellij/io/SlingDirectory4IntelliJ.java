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

package com.headwire.aem.tooling.intellij.io;

import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.io.*;
import org.apache.sling.ide.sync.content.WorkspaceDirectory;
import org.apache.sling.ide.sync.content.WorkspaceFile;
import org.apache.sling.ide.sync.content.WorkspacePath;
import org.apache.sling.ide.sync.content.WorkspaceResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;

/**
 * Created by Andreas Schaefer (Headwire.com) on 11/15/15.
 */
public class SlingDirectory4IntelliJ
    extends SlingResource4IntelliJ
    implements SlingDirectory
{

    public SlingDirectory4IntelliJ(SlingProject project, VirtualFile file) {
        super(project, file);
    }

    public SlingDirectory4IntelliJ(SlingProject project, String resourcePath) {
        super(project, resourcePath);
    }

    @Override
    public WorkspaceFile getFile(WorkspacePath relativePath) {
        return (SlingFile) getResourceFromPath(getResourcePath() + "/" + relativePath);
    }

    @Override
    public WorkspaceDirectory getDirectory(WorkspacePath relativePath) {
        return (SlingDirectory) getResourceFromPath(getResourcePath() + "/" + relativePath);
    }

    @Override
    public List<WorkspaceResource> getChildren() {
        return new ArrayList<>(getLocalChildren());
    }
}
