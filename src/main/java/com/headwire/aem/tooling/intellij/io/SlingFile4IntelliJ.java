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

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.io.SlingFile;
import org.apache.sling.ide.io.SlingProject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Andreas Schaefer (Headwire.com) on 11/15/15.
 */
public class SlingFile4IntelliJ
    extends SlingResource4IntelliJ
    implements SlingFile
{

    public SlingFile4IntelliJ(SlingProject project, VirtualFile file) {
        super(project, file);
    }

    public SlingFile4IntelliJ(SlingProject project, String resourcePath) {
        super(project, resourcePath);
    }

    @Override
    public InputStream getContents() throws IOException {
        return getContentStream();
    }
}
