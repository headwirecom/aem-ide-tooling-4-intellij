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

package com.headwire.aem.tooling.intellij.ui;

import com.headwire.aem.tooling.intellij.util.Util;

import javax.swing.*;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/22/15.
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
