package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by schaefa on 5/14/15.
 */
public class Status {

    public static final int ERROR = 1;

    private String message;
    private Throwable cause;

    public Status(int status, int componentId, String message, Throwable t) {
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
