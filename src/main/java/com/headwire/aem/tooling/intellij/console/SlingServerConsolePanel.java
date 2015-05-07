package com.headwire.aem.tooling.intellij.console;

import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NonNls;

/**
 * Created by schaefa on 5/6/15.
 *
 * @deprecated This is not used so far -> delete it later
 */
public class SlingServerConsolePanel
    extends SimpleToolWindowPanel
{
    public SlingServerConsolePanel(boolean vertical) {
        super(vertical);
    }

    public SlingServerConsolePanel(boolean vertical, boolean borderless) {
        super(vertical, borderless);
    }

    @Override
    public Object getData(@NonNls String dataId) {
        return PlatformDataKeys.HELP_ID.is(dataId) ?
            ConsoleLog.HELP_ID :
            super.getData(dataId);

    }

}
