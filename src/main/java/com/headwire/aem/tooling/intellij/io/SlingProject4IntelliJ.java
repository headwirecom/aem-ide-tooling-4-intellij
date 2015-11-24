package com.headwire.aem.tooling.intellij.io;

import com.headwire.aem.tooling.intellij.config.ModuleProject;
import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.filter.FilterLocator;
import org.apache.sling.ide.io.ConnectorException;
import org.apache.sling.ide.io.SlingProject;
import org.apache.sling.ide.io.SlingResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;
import static com.headwire.aem.tooling.intellij.util.Constants.META_INF_FOLDER_NAME;
import static com.headwire.aem.tooling.intellij.util.Constants.VAULT_FILTER_FILE_NAME;

/**
 * Created by schaefa on 11/16/15.
 */
public class SlingProject4IntelliJ
    implements SlingProject
{
    private ServerConfiguration.Module module;
    private SlingResource syncDirectory;

    public SlingProject4IntelliJ(ServerConfiguration.Module module) {
        this.module = module;
        Project project = module.getProject();
        VirtualFileSystem vfs = project.getBaseDir().getFileSystem();
        for(String path: module.getModuleProject().getContentDirectoryPaths()) {
            if(Util.pathEndsWithFolder(path, JCR_ROOT_FOLDER_NAME)) {
                File folder = new File(path);
                if(folder.exists() && folder.isDirectory()) {
                    syncDirectory = new SlingResource4IntelliJ(this, vfs.findFileByPath(path));
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
    public SlingResource getSyncDirectory() {
        return syncDirectory;
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
        if(filterFile == null) {
            // First we check if the META-INF folder was already found
            VirtualFile metaInfFolder = module.getMetaInfFolder();
            if(metaInfFolder == null) {
                // Now go through the Maven Resource folder and check
                ModuleProject moduleProject = module.getModuleProject();
                for(String contentPath: moduleProject.getContentDirectoryPaths()) {
                    if(contentPath.endsWith("/" + META_INF_FOLDER_NAME)) {
                        metaInfFolder = module.getProject().getBaseDir().getFileSystem().findFileByPath(contentPath);
                        module.setMetaInfFolder(metaInfFolder);
                    }
                }
            }
            if(metaInfFolder == null) {
                // Lastly we check if we can find the folder somewhere in the maven project file system
                ModuleProject moduleProject = module.getModuleProject();
                VirtualFile test = module.getProject().getBaseDir().getFileSystem().findFileByPath(moduleProject.getModuleDirectory());
                metaInfFolder = findFileOrFolder(test, META_INF_FOLDER_NAME, true);
                module.setMetaInfFolder(metaInfFolder);
            }
            if(metaInfFolder != null) {
                // Found META-INF folder
                // Find filter.xml file
                filterFile = findFileOrFolder(metaInfFolder, VAULT_FILTER_FILE_NAME, false);
                module.setFilterFile(filterFile);
                Util.setModificationStamp(filterFile);
            }
        }
        if(filter == null && filterFile != null) {
            FilterLocator filterLocator = Activator.getDefault().getFilterLocator();
            InputStream contents = null;
            try {
                contents = filterFile.getInputStream();
                filter = filterLocator.loadFilter(contents);
                module.setFilter(filter);
                Util.setModificationStamp(filterFile);
            } catch (IOException e) {
                throw new ConnectorException(
//                    "Failed loading filter file for project " + project.getName()
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
}
