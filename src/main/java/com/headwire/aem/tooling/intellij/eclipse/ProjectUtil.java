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

package com.headwire.aem.tooling.intellij.eclipse;

import com.headwire.aem.tooling.intellij.config.UnifiedModule;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IFile;
import com.headwire.aem.tooling.intellij.eclipse.stub.IFolder;
import com.headwire.aem.tooling.intellij.eclipse.stub.IPath;
import com.headwire.aem.tooling.intellij.eclipse.stub.IProject;
import com.headwire.aem.tooling.intellij.eclipse.stub.IResource;
import com.headwire.aem.tooling.intellij.eclipse.stub.IStatus;
import com.headwire.aem.tooling.intellij.eclipse.stub.Status;
import com.headwire.aem.tooling.intellij.io.wrapper.ResourcesPlugin;
import com.headwire.aem.tooling.intellij.util.Constants;
import com.headwire.aem.tooling.intellij.util.ServiceProvider;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.filter.FilterLocator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;
import static com.headwire.aem.tooling.intellij.util.Constants.META_INF_FOLDER_NAME;
import static com.headwire.aem.tooling.intellij.util.Constants.VAULT_FILTER_FILE_NAME;
//import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_PATH_INDICATOR;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/13/15.
 */
@Deprecated
public class ProjectUtil {

    public static IPath getSyncDirectoryFullPath(IProject project) {
        return getSyncDirectoryValue(project);
    }

    public static IFolder getSyncDirectory(IProject project) {
        if (project==null) {
            return null;
        }
//AS TODO: Fix that later
//        if (!project.isOpen()) {
//            return null;
//        } else if (!ProjectHelper.isContentProject(project)) {
//            return null;
//        }

        IPath syncDirectoryValue = ProjectUtil.getSyncDirectoryValue(project);
        if (syncDirectoryValue==null || syncDirectoryValue.isEmpty()) {
            return null;
        }
        IResource syncDir = project.findMember(syncDirectoryValue);
        if (syncDir==null || !(syncDir instanceof IFolder)) {
            return null;
        }
        return (IFolder) syncDir;
    }

//    public static Filter loadFilter(IProject project) {
//        return null;
//    }

    public static Filter loadFilter(ServerConfiguration.Module module) throws CoreException {
        // First check if the filter file is cached and if it isn't outdated. If it is found and not outdated
        // then we just return this one. If the filter is outdated then we just reload if the cache file
        // and if there is not file then we search for it. At the end we place both the file and filter in the cache.
        Filter filter = module.getFilter();
        VirtualFile filterFile = module.getFilterFile();
        if(filter != null) {
            if(Util.isOutdated(module.getFilterFile())) {
                filter = null;
                filterFile = null;
            }
        }
        if(filterFile == null) {
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
            if(metaInfFolder == null) {
                // Lastly we check if we can find the folder somewhere in the maven project file system
                UnifiedModule unifiedModule = module.getUnifiedModule();
                VirtualFile test = module.getProject().getBaseDir().getFileSystem().findFileByPath(unifiedModule.getModuleDirectory());
                metaInfFolder = findFileOrFolder(test, META_INF_FOLDER_NAME, true);
                module.setMetaInfFolder(metaInfFolder);
            }
            if(metaInfFolder != null) {
                // Found META-INF folder
                // Find filter.xml file
                filterFile = findFileOrFolder(metaInfFolder, VAULT_FILTER_FILE_NAME, false);
                if(filterFile != null) {
                    module.setFilterFile(filterFile);
                    Util.setModificationStamp(filterFile);
                }
            }
        }
        if(filter == null && filterFile != null) {
//            FilterLocator filterLocator = Activator.getDefault().getFilterLocator();
            FilterLocator filterLocator = ServiceProvider.getService(FilterLocator.class);
            InputStream contents = null;
            try {
                contents = filterFile.getInputStream();
                filter = filterLocator.loadFilter(contents);
                module.setFilter(filter);
                Util.setModificationStamp(filterFile);
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID,
//                    "Failed loading filter file for project " + project.getName()
                    "Failed loading filter file for module " + module
                        + " from location " + filterFile, e));
            } finally {
                IOUtils.closeQuietly(contents);
            }
        }
        return filter;
    }

    //AS TODO: This should be move to a better place
    public static VirtualFile findFileOrFolder(VirtualFile rootFile, String name, boolean isFolder) {
        VirtualFile ret = null;
        for(VirtualFile child: rootFile.getChildren()) {
            if(child.isDirectory()) {
                if(isFolder) {
                    if(child.getName().equals(name)) {
                        return child;
                    }
                }
                ret = findFileOrFolder(child, name, isFolder);
                if(ret != null) { break; }
            } else {
                if(child.getName().equals(name)) {
                    ret = child;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Loads a filter for the specified project
     *
     * @param project the project to find a filter for
     * @return the found filter or null
     * @throws CoreException
     */
    public static Filter loadFilter(final IProject project) throws CoreException {

//        FilterLocator filterLocator = Activator.getDefault().getFilterLocator();
        FilterLocator filterLocator = ServiceProvider.getService(FilterLocator.class);

        IPath filterPath = findFilterPath(project);
        if (filterPath == null) {
            return null;
        }

        IFile filterFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(project, filterPath);
        Filter filter = null;
        if (filterFile != null && filterFile.exists()) {
            InputStream contents = null;
            try {
                contents = filterFile.getContents();
                filter = filterLocator.loadFilter(contents);
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID,
//                    "Failed loading filter file for project " + project.getName()
                    "Failed loading filter file for project " + project
                        + " from location " + filterFile, e));
            } finally {
                IOUtils.closeQuietly(contents);
            }
        }
        return filter;
    }

    /**
     * Finds the path to a filter defined for the project
     *
     * @param project the project
     * @return the path to the filter defined in the project, or null if no filter is found
     */
    public static IPath findFilterPath(final IProject project) {

//        FilterLocator filterLocator = Activator.getDefault().getFilterLocator();
        FilterLocator filterLocator = ServiceProvider.getService(FilterLocator.class);

        IFolder syncFolder = ProjectUtil.getSyncDirectory(project);
        if (syncFolder == null) {
            return null;
        }
        File filterLocation = filterLocator.findFilterLocation(syncFolder.getLocation().toFile());
        if (filterLocation == null) {
            return null;
        }
        return Path.fromOSString(filterLocation.getAbsolutePath());
    }

    public static IPath getSyncDirectoryValue(IProject project) {
//        String value = null;
//        try {
//            value = project.getPersistentProperty(new QualifiedName(Constants.PLUGIN_ID, PROPERTY_SYNC_ROOT));
//        } catch (CoreException e) {
////AS TODO: Handle Logging
////            Activator.getDefault().getPluginLogger().error(e.getMessage(), e);
//        }
//
//        // TODO central place for defaults
//        if (value == null) {
//            return Path.fromOSString(PROPERTY_SYNC_ROOT_DEFAULT_VALUE);
//        } else {
//            return Path.fromPortableString(value);
//        }
        // Look for Content Folder
        File contentFolder = null;
        for(String sourceFolder : project.getSourceFolderList()) {
            if(Util.pathEndsWithFolder(sourceFolder, JCR_ROOT_FOLDER_NAME)) {
//            if(sourceFolder.endsWith(JCR_ROOT_PATH_INDICATOR)) {
                File folder = new File(sourceFolder);
                if(folder.exists() && folder.isDirectory()) {
                    contentFolder = folder;
                    break;
                }
            }
        }
        IPath ret = null;
        if(contentFolder != null) {
            ret = new IPath(contentFolder);
        }
        return ret;
    }

    public static File getSyncDirectoryFile(IProject project) {
        return getSyncDirectoryValue(project).toFile();
    }
}
