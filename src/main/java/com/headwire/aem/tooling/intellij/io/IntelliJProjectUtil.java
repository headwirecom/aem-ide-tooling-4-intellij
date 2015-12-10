package com.headwire.aem.tooling.intellij.io;

import org.apache.sling.ide.filter.Filter;
import org.apache.sling.ide.io.ConnectorException;
import org.apache.sling.ide.io.ProjectUtil;
import org.apache.sling.ide.io.SlingProject;
import org.apache.sling.ide.io.SlingResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by schaefa on 11/16/15.
 */
@Deprecated
public class IntelliJProjectUtil
    implements ProjectUtil
{
    @Override
    public Filter loadFilter(SlingProject project) {
        try {
            return ((SlingProject4IntelliJ) project).loadFilter();
        } catch (ConnectorException e) {
            //AS Log this
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SlingResource getSyncDirectory(SlingResource resource) {
        return resource.getProject().getSyncDirectory();
    }

    @Override
    public SlingResource getResourceFromPath(String resourcePath, SlingResource syncDirectory) {
        //AS TODO: Need to handle OS Specific Paths
        String check = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        return syncDirectory.findChildByPath(check);
    }

    @Override
    public SlingResource getResourceFromPath(String resourcePath, SlingProject project) {
        return project.findFileByPath(resourcePath);
    }

}
