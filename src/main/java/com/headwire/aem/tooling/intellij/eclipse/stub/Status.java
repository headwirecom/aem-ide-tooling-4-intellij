package com.headwire.aem.tooling.intellij.eclipse.stub;

/**
 * Created by schaefa on 5/14/15.
 */
@Deprecated
public class Status implements IStatus {

    private int status;
    private int componentId;
    private int actionId;
    private String message;
    private Throwable cause;

    public Status(int status, int componentId, String message) {
        this(status, componentId, message, null);
    }

    public Status(int status, int componentId, String message, Throwable t) {
        this(status, componentId, 0, message, t);
    }

    public Status(int status, int componentId, int actionId, String message, Throwable t) {
        this.status = status;
        this.componentId = componentId;
        this.actionId = actionId;
        this.message = message;
        this.cause = t;
    }

    public int getStatus() {
        return status;
    }

    public int getComponentId() {
        return componentId;
    }

    public int getActionId() {
        return actionId;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
