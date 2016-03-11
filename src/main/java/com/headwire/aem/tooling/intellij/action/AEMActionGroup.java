/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.action;

import com.headwire.aem.tooling.intellij.explorer.SlingServerTreeSelectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

/**
 * Created by schaefa on 6/18/15.
 */
public class AEMActionGroup extends DefaultActionGroup implements DumbAware {
    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        boolean available = isAvailable(e);
        e.getPresentation().setEnabled(available);
        e.getPresentation().setVisible(available);
    }

    protected boolean isAvailable(AnActionEvent e) {
        boolean ret = false;
        Project project = e.getProject();
        if(project != null) {
            final SlingServerTreeSelectionHandler selectionHandler = project.getComponent(SlingServerTreeSelectionHandler.class);
            ret = selectionHandler.getCurrentConfiguration() != null;
        }
        return ret;
    }
}
