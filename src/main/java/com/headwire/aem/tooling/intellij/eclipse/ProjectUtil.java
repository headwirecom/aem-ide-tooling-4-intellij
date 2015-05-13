package com.headwire.aem.tooling.intellij.eclipse;

import org.apache.sling.ide.filter.Filter;

import java.io.File;

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

    public static Filter loadFilter(IProject project) {
        return null;
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
