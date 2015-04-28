package com.headwire.aem.tooling.intellij;

import com.headwire.aem.tooling.intellij.explorer.SlingServerExplorer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
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
//        BytecodeOutline outline = BytecodeOutline.getInstance(project);
//        BytecodeASMified asmified = BytecodeASMified.getInstance(project);
//        GroovifiedView groovified = GroovifiedView.getInstance(project);
//        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(outline, "Servers", false));
//        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(asmified, "ASMified", false));
//        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(groovified, "Groovified", false));
    }
}
