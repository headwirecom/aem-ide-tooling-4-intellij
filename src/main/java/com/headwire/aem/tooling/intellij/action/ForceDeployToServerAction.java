package com.headwire.aem.tooling.intellij.action;

/**
 * Created by schaefa on 6/13/15.
 */
public class ForceDeployToServerAction
    extends DeployToServerAction
{
    protected boolean isForced() {
        return true;
    }
}
