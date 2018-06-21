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

import com.headwire.aem.tooling.intellij.config.UnifiedModule;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.util.ServiceProvider;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.filter.FilterLocator;
import org.apache.sling.ide.io.ConnectorException;
import org.apache.sling.ide.io.SlingDirectory;
import org.apache.sling.ide.io.SlingProject;
import org.apache.sling.ide.io.SlingResource;
import org.apache.sling.ide.sync.content.WorkspaceDirectory;
import org.apache.sling.ide.sync.content.WorkspacePath;
import org.apache.sling.ide.sync.content.WorkspaceProject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;
import static com.headwire.aem.tooling.intellij.util.Constants.META_INF_FOLDER_NAME;
import static com.headwire.aem.tooling.intellij.util.Constants.VAULT_FILTER_FILE_NAME;

/**
 * Created by Andreas Schaefer (Headwire.com) on 11/16/15.
 */
public class SlingProject4IntelliJ
    implements SlingProject
{
    private Logger logger = Logger.getInstance(this.getClass());

    private ServerConfiguration.Module module;
    private SlingDirectory syncDirectory;

    public SlingProject4IntelliJ(ServerConfiguration.Module module) {
        logger.debug("Getting Started, Module: " + module);
        this.module = module;
        Project project = module.getProject();
        VirtualFileSystem vfs = project.getBaseDir().getFileSystem();
        for(String path: module.getUnifiedModule().getContentDirectoryPaths()) {
            if(Util.pathEndsWithFolder(path, JCR_ROOT_FOLDER_NAME)) {
                File folder = new File(path);
                if(folder.exists() && folder.isDirectory()) {
                    syncDirectory = new SlingDirectory4IntelliJ(this, vfs.findFileByPath(path));
                    break;
                }
            }
        }
    }

    public ServerConfiguration.Module getModule() {
        return module;
    }

    @Override
    public SlingResource findFileByPath(String path) {
        SlingResource ret = null;
        VirtualFile file = module.getProject().getBaseDir().getFileSystem().findFileByPath(path);
        if(file != null) {
            ret = new SlingResource4IntelliJ(module.getSlingProject(), file);
        }
        return ret;
    }

    @Override
    public WorkspaceDirectory getSyncDirectory() {
        return syncDirectory;
    }

    @Override
    public Filter getFilter() throws IOException {
        try {
            return loadFilter();
        } catch (ConnectorException e) {
            return null;
        }
    }

    @Override
    public WorkspaceDirectory getDirectory(WorkspacePath path) {
        return new SlingDirectory4IntelliJ((SlingProject) getProject(), path.asPortableString());
    }

    public Filter loadFilter() throws ConnectorException {
        // First check if the filter file is cached and if it isn't outdated. If it is found and not outdated
        // then we just return this one. If the filter is outdated then we just reload if the cache file
        // and if there is not file then we search for it. At the end we place both the file and filter in the cache.
        Filter filter = module.getFilter();
        VirtualFile filterFile = module.getFilterFile();
        if(filter != null) {
            if(Util.isOutdated(module.getFilterFile())) {
                filter = null;
            }
        }
        if(filter == null && filterFile == null) {
            // First we check if the META-INF folder was already found
            VirtualFile metaInfFolder = module.getMetaInfFolder();
            if(metaInfFolder == null) {
                // Now go through the Maven Resource folder and check
                UnifiedModule unifiedModule = module.getUnifiedModule();
                for(String contentPath: unifiedModule.getContentDirectoryPaths()) {
                    if(contentPath.endsWith("/" + META_INF_FOLDER_NAME)) {
                        metaInfFolder = module.getProject().getBaseDir().getFileSystem().findFileByPath(contentPath);
                        module.setMetaInfFolder(metaInfFolder);
                    }
                }
            }
            logger.debug("Stored Meta-Inf Folder: '" + metaInfFolder + "'");
            if(metaInfFolder != null && !metaInfFolder.exists()) {
                logger.debug("Non-Existing Meta-Inf Folder: '" + metaInfFolder + "' -> reset");
                metaInfFolder = null;
            }
            if(metaInfFolder == null) {
                // Lastly we check if we can find the folder somewhere in the maven project file system
                UnifiedModule unifiedModule = module.getUnifiedModule();
                VirtualFile test = module.getProject().getBaseDir().getFileSystem().findFileByPath(unifiedModule.getModuleDirectory());
                metaInfFolder = findFileOrFolder(test, META_INF_FOLDER_NAME, true);
                logger.debug("Module FS File: '" + test + "', META-INF folder: '" + metaInfFolder + "'");
                module.setMetaInfFolder(metaInfFolder);
            }
            if(metaInfFolder != null) {
                // Found META-INF folder
                // Find filter.xml file
                filterFile = findFileOrFolder(metaInfFolder, VAULT_FILTER_FILE_NAME, false);
                logger.debug("Filter File: '" + filterFile + "'");
                module.setFilterFile(filterFile);
                Util.setModificationStamp(filterFile);
            }
        }
        if(filter == null && filterFile != null) {
//            FilterLocator filterLocator = Activator.getDefault().getFilterLocator();
            FilterLocator filterLocator = ServiceProvider.getService(FilterLocator.class);
            InputStream contents = null;
            try {
                contents = filterFile.getInputStream();
                logger.debug("Filter File Content: '" + contents + "'");
                filter = filterLocator.loadFilter(contents);
                module.setFilter(filter);
                Util.setModificationStamp(filterFile);
            } catch (IOException e) {
                logger.debug("Reading Filter File Failed", e);
                throw new ConnectorException(
                    "Failed loading filter file for module " + module
                        + " from location " + filterFile,
                    e
                );
            } finally {
                IOUtils.closeQuietly(contents);
            }
        }
        return filter;
    }

    private VirtualFile findFileOrFolder(VirtualFile rootFile, String name, boolean isFolder) {
        VirtualFile ret = null;
        for(VirtualFile child: rootFile.getChildren()) {
            String childName = child.getName();
            if("jcr_root".equals(childName)) {
                continue;
            }
            if(child.isDirectory()) {
                if(isFolder) {
                    if(childName.equals(name)) {
                        return child;
                    }
                }
                ret = findFileOrFolder(child, name, isFolder);
                if(ret != null) { break; }
            } else {
                if(childName.equals(name)) {
                    ret = child;
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean isIgnored() {
        return false;
    }

    @Override
    public WorkspacePath getLocalPath() {
        return null;
    }

    @Override
    public Path getOSPath() {
        return null;
    }

    @Override
    public WorkspaceProject getProject() {
        return null;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public Object getTransientProperty(String propertyName) {
        return null;
    }
}
