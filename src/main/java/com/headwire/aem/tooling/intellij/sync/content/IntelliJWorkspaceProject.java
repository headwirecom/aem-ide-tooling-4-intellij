package com.headwire.aem.tooling.intellij.sync.content;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.config.UnifiedModule;
import com.headwire.aem.tooling.intellij.io.SlingResource4IntelliJ;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.filter.FilterLocator;
import org.apache.sling.ide.sync.content.WorkspaceDirectory;
import org.apache.sling.ide.sync.content.WorkspacePath;
import org.apache.sling.ide.sync.content.WorkspaceProject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static com.headwire.aem.tooling.intellij.util.Constants.JCR_ROOT_FOLDER_NAME;
import static com.headwire.aem.tooling.intellij.util.Constants.META_INF_FOLDER_NAME;
import static com.headwire.aem.tooling.intellij.util.Constants.VAULT_FILTER_FILE_NAME;
import static com.headwire.aem.tooling.intellij.util.Util.findFileOrFolder;

public class IntelliJWorkspaceProject
    extends IntelliJWorkspaceResource
    implements WorkspaceProject
{
    private Logger logger = Logger.getInstance(this.getClass());

    private ServerConfiguration.Module module;
    private WorkspaceDirectory syncDirectory;

    public IntelliJWorkspaceProject(ServerConfiguration.Module module, Set<String> ignoredFileNames) {
        super(ignoredFileNames);
        logger.debug("Getting Started, Module: " + module);
        this.module = module;
        Project project = module.getProject();
        VirtualFileSystem vfs = project.getBaseDir().getFileSystem();
        for(String path: module.getUnifiedModule().getContentDirectoryPaths()) {
            if(Util.pathEndsWithFolder(path, JCR_ROOT_FOLDER_NAME)) {
                File folder = new File(path);
                if(folder.exists() && folder.isDirectory()) {
                    syncDirectory = new IntelliJWorkspaceDirectory(this, vfs.findFileByPath(path), ignoredFileNames);
                    break;
                }
            }
        }
    }

    @Override
    /** This method is overrideen because we have to set the project to null in the constructure **/
    public WorkspaceProject getProject() {
        return this;
    }

    @Override
    public WorkspaceDirectory getSyncDirectory() {
        return syncDirectory;
    }

    @Override
    public Filter getFilter() throws IOException {
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
            FilterLocator filterLocator = ApplicationManager.getApplication().getComponent(FilterLocator.class);
            InputStream contents = null;
            try {
                contents = filterFile.getInputStream();
                logger.debug("Filter File Content: '" + contents + "'");
                filter = filterLocator.loadFilter(contents);
                module.setFilter(filter);
                Util.setModificationStamp(filterFile);
            } catch (IOException e) {
                logger.debug("Reading Filter File Failed", e);
                throw new IOException(
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

    @Override
    public WorkspaceDirectory getDirectory(WorkspacePath path) {
        return new IntelliJWorkspaceDirectory(getProject(), getResource().findFileByRelativePath(path.asPortableString()), getIgnoredFileNames());
    }
}
