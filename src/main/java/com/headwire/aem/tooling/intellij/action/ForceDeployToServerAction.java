package com.headwire.aem.tooling.intellij.action;

/**
 * Created by schaefa on 6/13/15.
 */
public class ForceDeployToServerAction
    extends DeployToServerAction
{
    public ForceDeployToServerAction() {
        super("force.deploy.configuration.action");
    }

    protected boolean isForced() {
        return true;
    }
}
