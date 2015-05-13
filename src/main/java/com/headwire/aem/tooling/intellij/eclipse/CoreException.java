package com.headwire.aem.tooling.intellij.eclipse;

/**
 * Created by schaefa on 5/12/15.
 */
public class CoreException
    extends Exception
{
    public CoreException(String s) {
        super(s);
    }

    public CoreException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
