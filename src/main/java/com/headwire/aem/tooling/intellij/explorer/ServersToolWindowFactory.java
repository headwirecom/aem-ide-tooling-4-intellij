package com.headwire.aem.tooling.intellij.explorer;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

/**
 * Created by schaefa on 3/19/15.
 */
public class ServersToolWindowFactory
    implements ToolWindowFactory
{
    private static final Logger LOGGER = Logger.getInstance(ServersToolWindowFactory.class);

    public void createToolWindowContent(final Project project, final ToolWindow toolWindow) {
        SlingServerExplorer explorer = new SlingServerExplorer(project);
        LOGGER.debug("CTWC: Explorer: '" + explorer + "'");
        final ContentManager contentManager = toolWindow.getContentManager();
        final Content content = contentManager.getFactory().createContent(explorer, null, false);
        contentManager.addContent(content);
        Disposer.register(project, explorer);
    }
}
