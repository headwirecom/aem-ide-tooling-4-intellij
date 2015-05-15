package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by schaefa on 5/12/15.
 */
public class CoreException
    extends Exception
{
    private Status status;

    public CoreException(String s) {
        super(s);
    }

    public CoreException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CoreException(Status status) {
        super(status.getMessage(), status.getCause());
    }
}
