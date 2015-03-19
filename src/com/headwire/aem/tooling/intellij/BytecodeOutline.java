package com.headwire.aem.tooling.intellij;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentContainer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 07/01/11
 * Time: 17:07
 */

/**
 * Bytecode view.
 */
public class BytecodeOutline extends ACodeView {

    public BytecodeOutline(final Project project, KeymapManager keymapManager, final ToolWindowManager toolWindowManager) {
        super(toolWindowManager, keymapManager, project);
    }

    public static BytecodeOutline getInstance(Project project) {
        return ServiceManager.getService(project, BytecodeOutline.class);
    }
}
