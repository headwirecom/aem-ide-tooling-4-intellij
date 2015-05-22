package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by schaefa on 5/14/15.
 */
public class Status implements IStatus {

    private String message;
    private Throwable cause;

    public Status(int status, int componentId, String message) {
        this(status, componentId, message, null);
    }

    public Status(int status, int componentId, String message, Throwable t) {
        this(status, componentId, 0, message, t);
    }

    public Status(int status, int componentId, int unknown, String message, Throwable t) {
        this.message = message;
        this.cause = t;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
