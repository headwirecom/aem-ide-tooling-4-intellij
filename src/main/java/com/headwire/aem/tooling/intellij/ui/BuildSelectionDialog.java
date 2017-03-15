/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

public class BuildSelectionDialog extends DialogWrapper {
    private JPanel contentPane;
    private JButton Add;
    private JButton Remove;
    private JList excludeList;
    private JList includedList;

    private ModuleListModel excludedModuleListModel;
    private ModuleListModel includeModuleListModel;

    public BuildSelectionDialog(@NotNull Project project, @NotNull ServerConfiguration serverConfiguration) {
        super(project);

        setTitle(AEMBundle.message("dialog.build.configuration.title"));
        setModal(true);
        setUpDialog(serverConfiguration);
        init();
    }

    private void setUpDialog(ServerConfiguration serverConfiguration) {
        Add.setEnabled(false);
        Remove.setEnabled(false);
        includedList.setCellRenderer(new ModuleListCellRenderer());
        excludeList.setCellRenderer(new ModuleListCellRenderer());
        includedList.addListSelectionListener(
            new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                    if(!listSelectionEvent.getValueIsAdjusting()) {
                        int firstIndex = listSelectionEvent.getFirstIndex();
                        // No Selection -> disable remove button otherwise enable it
                        Remove.setEnabled(includeModuleListModel.getSize() > 0);
                    }
                }
            }
        );

        excludeList.addListSelectionListener(
            new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                    if(!listSelectionEvent.getValueIsAdjusting()) {
                        int firstIndex = listSelectionEvent.getFirstIndex();
                        // No Selection -> disable add button otherwise enable it
                        Add.setEnabled(excludedModuleListModel.getSize() > 0);
                    }
                }
            }
        );

        Add.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // Move all selected entries in the Excluded Module to the Included List
                    //AS TODO: This is deprecated in 1.8 but not in 1.6
                    List<Object> selection = Arrays.asList(excludeList.getSelectedValues());
                    for(Object item: selection) {
                        if(item instanceof Module) {
                            Module module = (Module) item;
                            includeModuleListModel.addModule(module);
                            excludedModuleListModel.removeModule(module);
                            excludeList.setSelectedIndex(0);
                            if(includedList.getSelectedIndices().length == 0) { includedList.setSelectedIndex(0); }
                        }
                    }
                }
            }
        );

        Remove.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    // Move all selected entries in the Excluded Module to the Included List
                    //AS TODO: This is deprecated in 1.8 but not in 1.6
                    List<Object> selection = Arrays.asList(includedList.getSelectedValues());
                    for(Object item: selection) {
                        if(item instanceof Module) {
                            Module module = (Module) item;
                            excludedModuleListModel.addModule(module);
                            includeModuleListModel.removeModule(module);
                            includedList.setSelectedIndex(0);
                            if(excludeList.getSelectedIndices().length == 0) { excludeList.setSelectedIndex(0); }
                        }
                    }
                }
            }
        );

        fillIn(serverConfiguration);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void fillIn(ServerConfiguration serverConfiguration) {
        clearList(includedList);
        clearList(excludeList);
        List<Module> included = new ArrayList<Module>();
        List<Module> excluded = new ArrayList<Module>();
        for(Module module: serverConfiguration.getModuleList()) {
            if(module.isPartOfBuild()) {
                included.add(module);
            } else {
                excluded.add(module);
            }
        }
        includeModuleListModel = new ModuleListModel(included);
        includedList.setModel(includeModuleListModel);
        excludedModuleListModel = new ModuleListModel(excluded);
        excludeList.setModel(excludedModuleListModel);
        // Try to select the first entry
        includedList.setSelectedIndex(0);
        excludeList.setSelectedIndex(0);
    }

    private void clearList(JList list) {
        if(includeModuleListModel != null) { includeModuleListModel.clear(); }
        if(excludedModuleListModel != null) { excludedModuleListModel.clear(); }
    }

    protected void doOKAction() {
        // Apply the Part of Build Flag accordingly
        for(Module module: includeModuleListModel.getData()) {
            module.setPartOfBuild(true);
        }
        for(Module module: excludedModuleListModel.getData()) {
            module.setPartOfBuild(false);
        }
        super.doOKAction();
    }

    public static class ModuleListModel extends AbstractListModel {
        private List<Module> data;

        public ModuleListModel(List<Module> modules) {
            this.data = modules;
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int i) {
            return data.get(i);
        }

        public List<Module> getData() {
            return data;
        }

        public boolean addModule(Module module) {
            boolean ret = false;
            int index = data.indexOf(module);
            if(index < 0) {
                data.add(module);
                index = data.indexOf(module);
                fireIntervalAdded(module, index, index);
                ret = true;
            }
            return ret;
        }

        public boolean removeModule(Module module) {
            boolean ret = false;
            int index = data.indexOf(module);
            if(index >= 0) {
                data.remove(index);
                fireIntervalRemoved(module, index, index);
                ret = true;
            }
            return ret;
        }

        public void clear() {
            data.clear();
        }
    }

    public static class ModuleListCellRenderer extends ColoredListCellRenderer {
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            Module module = (Module) value;
            append(
                module.getName(),
                SimpleTextAttributes.REGULAR_ATTRIBUTES
            );
//            //AS TODO: This should refer to the module name in the display of the Server Configuration
//            append(module.getSymbolicName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }
}
