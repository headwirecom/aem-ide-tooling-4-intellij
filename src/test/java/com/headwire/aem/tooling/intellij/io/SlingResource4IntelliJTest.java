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

import com.headwire.aem.tooling.intellij.config.ModuleContext;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.mock.MockModuleContext;
import com.headwire.aem.tooling.intellij.mock.MockProject;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.sling.ide.io.SlingProject;
import org.apache.sling.ide.io.SlingResource;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by Andreas Schaefer (Headwire.com) on 12/28/15.
 */
public class SlingResource4IntelliJTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlingResource4IntelliJTest.class);

    private ServerConfiguration.Module module;

    @Before
    public void setup() {
        String artifactId = "myArtifact";
        String groupId = "myGroupId";
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        module = new ServerConfiguration.Module( serverConfiguration, groupId + "." + artifactId, true, 0 );
        VirtualFile syncFolder = new MockVirtualFile(true, "baseDir");
        Project mockProject = new MockProject()
            .setBaseDir(syncFolder);
        ModuleContext mockModuleContext = new MockModuleContext()
            .setSymbolicName(groupId + "." + artifactId)
            .setContentDirectoryPaths(Arrays.asList("/baseDir/" + JCR_ROOT_FOLDER_NAME));
        module.rebind(mockProject, mockModuleContext);
    }

    @Test
    public void testBasics() {
        SlingProject slingProject = new SlingProject4IntelliJ(module);
        VirtualFile virtualFile = new MockVirtualFile("test");
        SlingResource slingResource = new SlingResource4IntelliJ(slingProject, virtualFile);

        assertThat("Sling Resource Name is wrong", slingResource.getName(), equalTo("test"));
        assertThat("Sling Resource Project is undefined", slingResource.getProject(), notNullValue());
        assertThat("No Modification Stored expected", Util.getModificationStamp(virtualFile), equalTo(-1L));
        assertThat("Resource isn't marked as changed", slingResource.isModified(), is(Boolean.TRUE));
        assertThat("Resource isn't a file", slingResource.isFile(), is(Boolean.TRUE));
        assertThat("Resource is a folder but should be file", slingResource.isFolder(), is(Boolean.FALSE));
        LOGGER.info("Resource Path: '{}'", slingResource.getLocalPath());
        LOGGER.info("Sync Directory: '{}'", slingProject.getSyncDirectory());
        SlingResource slingResourceChild = new SlingResource4IntelliJ(slingProject, "/a/b/c.txt");
        assertThat("Obtained Resource doesn't match", slingResource.getResourceFromPath("/a/b/c.txt"), equalTo(slingResourceChild));
        //AS TODO: Test Load Filter (should be the same as for Sling Project
        SlingResource slingResourceEqual = new SlingResource4IntelliJ(slingProject, virtualFile);
        assertThat("Sling Resource must be equal even if not identical", slingResource, equalTo(slingResourceEqual));
    }

}
