package com.headwire.aem.tooling.intellij.eclipse;

import com.headwire.aem.tooling.intellij.eclipse.stub.IPath;

/**
 * Created by schaefa on 5/13/15.
 */
public class Path {
    public static IPath fromOSString(String serializationFilePath) {
        return new IPath(serializationFilePath);
    }
}
