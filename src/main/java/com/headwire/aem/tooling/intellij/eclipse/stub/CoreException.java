package com.headwire.aem.tooling.intellij.eclipse.stub;

import org.jetbrains.annotations.NotNull;

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

    public CoreException(@NotNull Status status) {
        super(status.getMessage(), status.getCause());
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
