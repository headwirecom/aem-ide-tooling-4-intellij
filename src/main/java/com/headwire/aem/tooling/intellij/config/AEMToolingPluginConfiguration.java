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

package com.headwire.aem.tooling.intellij.config;

import javax.swing.*;
import java.awt.*;

/**
 * Created by schaefa on 3/19/15.
 */
public class AEMToolingPluginConfiguration {

//AS TODO: Ecluded the Groovy Style Combobox to build

    protected static final String COMPONENT_NAME = "AEMToolingPluginConfiguration";
    private JPanel contentPane;
    private JCheckBox skipDebugCheckBox;
    private JCheckBox skipFramesCheckBox;
    private JCheckBox skipCodeCheckBox;
    private JCheckBox expandFramesCheckBox;
    private JComboBox groovyCodeStyleComboBox;

    public AEMToolingPluginConfiguration() {
    }

    public JComponent getRootPane() {
        return contentPane;
    }

    public void setData(AEMToolingPluginComponent data) {
        skipDebugCheckBox.setSelected(data.isSkipDebug());
        skipFramesCheckBox.setSelected(data.isSkipFrames());
        skipCodeCheckBox.setSelected(data.isSkipCode());
        expandFramesCheckBox.setSelected(data.isExpandFrames());
//        groovyCodeStyleComboBox.setSelectedItem(data.getCodeStyle());
    }

    public void getData(AEMToolingPluginComponent data) {
        data.setSkipDebug(skipDebugCheckBox.isSelected());
        data.setSkipFrames(skipFramesCheckBox.isSelected());
        data.setSkipCode(skipCodeCheckBox.isSelected());
        data.setExpandFrames(expandFramesCheckBox.isSelected());
//        data.setCodeStyle((GroovyCodeStyle) groovyCodeStyleComboBox.getSelectedItem());
    }

    public boolean isModified(AEMToolingPluginComponent data) {
        if (skipDebugCheckBox.isSelected() != data.isSkipDebug()) return true;
        if (skipFramesCheckBox.isSelected() != data.isSkipFrames()) return true;
        if (skipCodeCheckBox.isSelected() != data.isSkipCode()) return true;
        if (expandFramesCheckBox.isSelected() != data.isExpandFrames()) return true;
//        if (!groovyCodeStyleComboBox.getSelectedItem().equals(data.getCodeStyle())) return true;
        return false;
    }

    private void createUIComponents() {
//        ComboBoxModel model = new EnumComboBoxModel<GroovyCodeStyle>(GroovyCodeStyle.class);
//        groovyCodeStyleComboBox = new ComboBox(model);
//        groovyCodeStyleComboBox.setRenderer(new GroovyCodeStyleCellRenderer());
    }

    private static class GroovyCodeStyleCellRenderer implements ListCellRenderer {
//        private EnumMap<GroovyCodeStyle, JLabel> labels;

        private GroovyCodeStyleCellRenderer() {
//            labels = new EnumMap<GroovyCodeStyle, JLabel>(GroovyCodeStyle.class);
//            for (GroovyCodeStyle codeStyle : GroovyCodeStyle.values()) {
//                labels.put(codeStyle, new JLabel(codeStyle.label));
//            }
        }

        public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
//            return labels.get(value);
            return null;
        }
    }

}
