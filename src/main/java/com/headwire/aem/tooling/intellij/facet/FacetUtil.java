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

package com.headwire.aem.tooling.intellij.facet;

import com.headwire.aem.tooling.intellij.lang.AEMBundle;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by schaefa on 3/30/16.
 */
public class FacetUtil {

    public enum Result { fileEmpty, fileNotFound, notDirectory, notFile, ok }

    public static Result checkFile(Module module, String filePath, boolean directory) {
        Result ret = Result.ok;
        if(filePath == null || filePath.length() == 0) {
            ret = Result.fileEmpty;
        } else {
            VirtualFile moduleFile = module.getModuleFile();
            VirtualFile file = moduleFile.getFileSystem().findFileByPath(filePath);
            if(file == null) {
                ret = Result.fileNotFound;
            } else if(directory) {
                if(!file.isDirectory()) {
                    ret = Result.notDirectory;
                }
            } else {
                if(!file.isDirectory()) {
                    ret = Result.notFile;
                }
            }
        }
        return ret;
    }

    public static ValidationResult createValidatorResult(String textId, String...params) {
        return new ValidationResult(AEMBundle.message(textId, params), null);
    }

    public static ConfigurationException createConfigurationException(String textId, String...params) {
        return new ConfigurationException(AEMBundle.message(textId, params));
    }
}
