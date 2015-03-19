package com.headwire.aem.tooling.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;

/**
 * Created by schaefa on 3/19/15.
 */
public class SlingServersToolWindowFactory
    implements ToolWindowFactory
{
    public void createToolWindowContent(final Project project, final ToolWindow toolWindow) {
        BytecodeOutline outline = BytecodeOutline.getInstance(project);
//        BytecodeASMified asmified = BytecodeASMified.getInstance(project);
//        GroovifiedView groovified = GroovifiedView.getInstance(project);
        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(outline, "Servers", false));
//        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(asmified, "ASMified", false));
//        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(groovified, "Groovified", false));
    }
}
