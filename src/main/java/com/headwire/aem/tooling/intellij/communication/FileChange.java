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

package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/23/15.
 */
public class FileChange {
    private VirtualFile file;
    private ServerConnectionManager.FileChangeType fileChangeType;
    private ServerConfiguration.Module module;
    private String resourcePath;

    public FileChange(VirtualFile file, ServerConnectionManager.FileChangeType fileChangeType) {
        this.file = file;
        this.fileChangeType = fileChangeType;
    }

    public VirtualFile getFile() {
        return file;
    }

    public ServerConnectionManager.FileChangeType getFileChangeType() {
        return fileChangeType;
    }

    public ServerConfiguration.Module getModule() {
        return module;
    }

    public void setModule(ServerConfiguration.Module module) {
        this.module = module;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        FileChange fileChange = (FileChange) o;

        if(!file.equals(fileChange.file)) {
            return false;
        }
        if(fileChangeType != fileChange.fileChangeType) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = file.hashCode();
        result = 31 * result + fileChangeType.hashCode();
        return result;
    }
}
