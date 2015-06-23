package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.util.Util;

import javax.swing.*;

/**
 * Created by schaefa on 6/22/15.
 */
public class UIUtil {

    public static int obtainInteger(JTextField textField, int defaultValue) {
        int ret = defaultValue;
        if(textField != null) {
            ret = Util.convertToInt(textField.getText(), defaultValue);
        }
        return ret;
    }

    public static int obtainInteger(JSpinner spinner, int defaultValue) {
        int ret = defaultValue;
        if(spinner != null) {
            ret = Util.convertToInt(spinner.getValue() + "", defaultValue);
        }
        return ret;
    }

}
