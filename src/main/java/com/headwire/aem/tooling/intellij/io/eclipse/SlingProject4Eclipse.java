package com.headwire.aem.tooling.intellij.io.eclipse;

import com.headwire.aem.tooling.intellij.eclipse.Path;
import com.headwire.aem.tooling.intellij.eclipse.ProjectUtil;
import com.headwire.aem.tooling.intellij.eclipse.stub.IFile;
import com.headwire.aem.tooling.intellij.eclipse.stub.IFolder;
import com.headwire.aem.tooling.intellij.eclipse.stub.IPath;
import com.headwire.aem.tooling.intellij.eclipse.stub.IProject;
import com.headwire.aem.tooling.intellij.eclipse.stub.IResource;
import com.headwire.aem.tooling.intellij.eclipse.wrapper.ResourcesPlugin;
import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.vfs.VirtualFile;
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

/**
 * Created by schaefa on 11/16/15.
 */
public class SlingProject4Eclipse
    implements SlingProject
{
//    private ServerConfiguration.Module module;
    private IProject project;
    private SlingResource syncDirectory;

    public SlingProject4Eclipse(IProject project) {
        this.project = project;
        for(String path: project.getSourceFolderList()) {
            if(Util.pathEndsWithFolder(path, JCR_ROOT_FOLDER_NAME)) {
                File folder = new File(path);
                if(folder.exists() && folder.isDirectory()) {
                    syncDirectory = new SlingResource4Eclipse(this, project.findMember(new IPath(path)));
                    break;
                }
            }
        }
    }

//    public ServerConfiguration.Module getModule() {
//        return module;
//    }

    @Override
    public SlingResource findFileByPath(String path) {
        SlingResource ret = null;
        IResource resource = project.findMember(new IPath(path));
        if(resource != null) {
            ret = new SlingResource4Eclipse(this, resource);
        }
        return ret;
    }

    @Override
    public SlingResource getSyncDirectory() {
        return syncDirectory;
    }

    public Filter loadFilter() throws ConnectorException {
        FilterLocator filterLocator = Activator.getDefault().getFilterLocator();

        IPath filterPath = findFilterPath(project);
        if (filterPath == null) {
            return null;
        }

        //AS TODO: My wrapper code is still old so the 'null' can be removed actually
        IFile filterFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(null, filterPath);
        Filter filter = null;
        if (filterFile != null && filterFile.exists()) {
            InputStream contents = null;
            try {
                contents = filterFile.getContents();
                filter = filterLocator.loadFilter(contents);
            } catch (IOException e) {
                throw new ConnectorException(
//                    new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    "Failed loading filter file for project " + project
                        + " from location " + filterFile, e);
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

        FilterLocator filterLocator = Activator.getDefault().getFilterLocator();

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
