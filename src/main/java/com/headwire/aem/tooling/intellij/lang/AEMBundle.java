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

package com.headwire.aem.tooling.intellij.lang;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/1/15.
 */
public class AEMBundle {
    public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params) {
        String message = CommonBundle.messageOrDefault(getBundle(), key, "", params);
        return message;
    }

//    public static Properties loadProperties(String path) {
//        Reference<ResourceBundle> referenceBundle = propertiesBundleMap.get(path);
//        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(referenceBundle);
//        if(bundle == null) {
//            bundle = ResourceBundle.getBundle(path);
//            referenceBundle = new SoftReference<ResourceBundle>(bundle);
//            propertiesBundleMap.put(path, referenceBundle);
//        }
//        Properties ret = new Properties();
//        if(bundle != null) {
//            for(String key: bundle.keySet()) {
//                ret.setProperty(key, bundle.getString(key));
//            }
//        }
//        return ret;
//    }
//
//    private static Map<String, Reference<ResourceBundle>> propertiesBundleMap = new HashMap<String, Reference<ResourceBundle>>();

    private static Reference<ResourceBundle> ourBundle;
    @NonNls
    protected static final String PATH_TO_BUNDLE = "messages.AEMBundle";

    private AEMBundle() {
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
        if(bundle == null) {
            bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE);
            ourBundle = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }
}
