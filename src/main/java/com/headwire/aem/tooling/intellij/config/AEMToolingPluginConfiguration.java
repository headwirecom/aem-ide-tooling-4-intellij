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
