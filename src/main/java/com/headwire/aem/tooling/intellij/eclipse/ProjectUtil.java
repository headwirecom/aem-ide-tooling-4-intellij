package com.headwire.aem.tooling.intellij.eclipse;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.eclipse.stub.CoreException;
import com.headwire.aem.tooling.intellij.eclipse.stub.IFile;
import com.headwire.aem.tooling.intellij.eclipse.stub.IFolder;
import com.headwire.aem.tooling.intellij.eclipse.stub.IPath;
import com.headwire.aem.tooling.intellij.eclipse.stub.IProject;
import com.headwire.aem.tooling.intellij.eclipse.stub.IResource;
import com.headwire.aem.tooling.intellij.eclipse.stub.IStatus;
import com.headwire.aem.tooling.intellij.eclipse.stub.Status;
import com.headwire.aem.tooling.intellij.eclipse.wrapper.ResourcesPlugin;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.sling.ide.eclipse.core.internal.Activator;
import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.filter.FilterLocator;
import org.jetbrains.idea.maven.model.MavenResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by schaefa on 5/13/15.
 */
public class ProjectUtil {

    public static final String CONTENT_SOURCE_TO_ROOT_PATH = "/content/jcr_root";

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
        Filter filter = null;

        FilterLocator filterLocator = Activator.getDefault().getFilterLocator();
        for(MavenResource mavenResource: module.getMavenProject().getResources()) {
            String path = mavenResource.getDirectory();
            if(path.indexOf("/META-INF") > 0) {
                // Found META-INF folder
                // Find filter.xml file
                VirtualFile rootFile = module.getMavenProject().getDirectoryFile();
                VirtualFile moduleRootDirectory = rootFile.getFileSystem().findFileByPath(path);
                VirtualFile filterFile = findFilterFile(moduleRootDirectory);
                InputStream contents = null;
                try {
                    if(filterFile != null) {
                        contents = filterFile.getInputStream();
                        filter = filterLocator.loadFilter(contents);
                    }
                } catch (IOException e) {
                    throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
//                    "Failed loading filter file for project " + project.getName()
                        "Failed loading filter file for module " + module
                            + " from location " + filterFile, e));
                } finally {
                    IOUtils.closeQuietly(contents);
                }
            }
        }
        return filter;
    }

    private static VirtualFile findFilterFile(VirtualFile rootFile) {
        VirtualFile ret = null;
        for(VirtualFile child: rootFile.getChildren()) {
            if(child.isDirectory()) {
                ret = findFilterFile(child);
                if(ret != null) { break; }
            } else {
                if(child.getName().equals("filter.xml")) {
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

        FilterLocator filterLocator = Activator.getDefault().getFilterLocator();

        IPath filterPath = findFilterPath(project);
        if (filterPath == null) {
            return null;
        }

        IFile filterFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filterPath);
        Filter filter = null;
        if (filterFile != null && filterFile.exists()) {
            InputStream contents = null;
            try {
                contents = filterFile.getContents();
                filter = filterLocator.loadFilter(contents);
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
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

    public static IPath getSyncDirectoryValue(IProject project) {
//        String value = null;
//        try {
//            value = project.getPersistentProperty(new QualifiedName(Activator.PLUGIN_ID, PROPERTY_SYNC_ROOT));
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
            if(sourceFolder.endsWith(CONTENT_SOURCE_TO_ROOT_PATH)) {
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
