package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by schaefa on 5/14/15.
 */
public interface IStatus {
    public static final int ERROR = 1;
    public static final int WARNING = 2;
    public static final IStatus OK_STATUS = new Status(0, 0, "Ok");

//    public static final int ERROR = 1;
}
