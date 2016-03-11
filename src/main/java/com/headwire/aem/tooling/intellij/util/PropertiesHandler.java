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

package com.headwire.aem.tooling.intellij.util;

import com.intellij.openapi.util.text.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by schaefa on 7/24/15.
 */
public class PropertiesHandler {

//AS TODO: Not sure if that is worthwhile
//    /**
//     *
//     * @param properties
//     * @param pattern The Pattern on how to parse the Properties. There are a few rules for the format:
//     *                - The levels (tokens) are separated by '|'
//     *                - Any leading filter tokens are removed from the properties structure
//     *                - Filter Tokens can be used to create sub properties
//     *                - Filter Tokens are wrapped into <>
//     *                - Indexes are numbers and the value indicates the starting index. It is wrapped into {}
//     *                - Property names list are separated by colons ':'. [any] means any value
//     * @return
//     */
//    public static Container parseProperties(Properties properties, String pattern) {
//        Container ret = null;
//        if(StringUtil.isNotEmpty(pattern)) {
//            List<String> tokenList = StringUtil.split(pattern, "|");
//
//        }
//    }
//
//    public static class Container {
//        private Properties properties;
//        private Map<Integer, Properties> propertiesMap;
//
//        public Container(Properties properties) {
//            this.properties = properties;
//            propertiesMap = null;
//        }
//
//        public Container(Map<Integer, Properties> propertiesMap) {
//            this.propertiesMap = propertiesMap;
//            properties = null;
//        }
//
//        public boolean isMap() {
//            return propertiesMap != null;
//        }
//
//        public Properties getProperties() {
//            return properties;
//        }
//
//        public Map<Integer, Properties> getPropertiesMap() {
//            return propertiesMap;
//        }
//    }

    /**
     * Filters the given Properties with the given Prefix Token. Keep in mind you cannot
     * filter the last token.
     * Any property name that leads to an empty string is ignored
     * @param properties Properties to be filter
     * @param prefix Starting Token(s) that are filtered out. Keep in mind that these must
     *               be full tokens as this method will add an dot at the end if missing
     * @return Filtered Properties or empty Properties if any parameters are null
     */
    public static Properties filterProperties(Properties properties, String prefix) {
        Properties ret = new Properties();
        if(properties != null && prefix != null) {
            // Make sure the prefix ends with a dot
            prefix = prefix.endsWith(".") ? prefix : prefix + ".";
            for (String name : properties.stringPropertyNames()) {
                String newName = removeLeadingDots(name);
                if (newName.startsWith(prefix)) {
                    newName = newName.length() > prefix.length() ? newName.substring(prefix.length()) : "";
                    newName = removeLeadingDots(newName);
                    if (newName.length() > 0) {
                        ret.setProperty(newName, properties.getProperty(name));
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Collects Properties by the leading index. This means that the name of the properties must start
     * with an Integer or else they are dropped.
     *
     * @param properties Source Properties
     * @return Map of the index together with its Properties instance. Empty if the given properties are null.
     */
    public static Map<Integer, Properties> collectProperties(Properties properties) {
        Map<Integer, Properties> ret = new HashMap<Integer, Properties>();
        if(properties != null) {
            for (String name : properties.stringPropertyNames()) {
                int index = getIndex(name);
                if(index >= 0) {
                    Properties props = ret.get(index);
                    if (props == null) {
                        props = new Properties();
                        ret.put(index, props);
                    }
                    String newName = dropToken(name);
                    if (newName.length() > 0) {
                        props.setProperty(newName, properties.getProperty(name));
                    }
                }
            }
        }
        return ret;
    }

    public static String dropToken(String name) {
        String ret = "";
        name = name == null ? "" : removeLeadingDots(name);
        int index = name.indexOf('.');
        if(index >= 0 && index < (name.length() - 1)) {
            name = name.substring(index + 1);
            ret = removeLeadingDots(name);
        }
        return ret;
    }

    public static int getIndex(String name) {
        int ret = -1;
        String token = getToken(name);
        if(token.length() > 0) {
            try {
                ret = Integer.parseInt(token);
            } catch(NumberFormatException e) {
                // Ignore it
            }
        }
        return ret;
    }

    public static String getToken(String name) {
        String ret = name == null ? "" : removeLeadingDots(name);
        int index = ret.indexOf('.');
        if(index >= 0 && index < ret.length()) {
            ret = ret.substring(0, index);
        }
        return ret;
    }

    public static String removeLeadingDots(String name) {
        while(name.length() > 0 && name.charAt(0) == '.') {
            name = name.substring(1);
        }
        return name;
    }
}
