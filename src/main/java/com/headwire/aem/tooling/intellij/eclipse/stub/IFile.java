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

package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.headwire.aem.tooling.intellij.util.Constants;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/13/15.
 */
@Deprecated
public class IFile extends IResource {

    public IFile() {}

    public IFile(@NotNull ServerConfiguration.Module module, @NotNull VirtualFile file) {
        super(module, file);
    }

    public IFile(@NotNull ServerConfiguration.Module module, @NotNull File file) {
        super(module, file);
    }

    public InputStream getContents() throws IOException {
        if(virtualFile != null) {
            return virtualFile.getInputStream();
        } else {
            return null;
        }
    }

    public Long getLocalTimeStamp() {
        if(virtualFile == null) {
            return file.lastModified();
        } else {
            return virtualFile.getTimeStamp();
        }
    }

    public void create(InputStream content, boolean force, IProgressMonitor monitor) throws CoreException {
        File newFile = file;
        if(virtualFile != null && !virtualFile.exists()) {
            newFile = new File(virtualFile.getPath());
        }
        if(!newFile.exists()) {
            String parentPath = newFile.getParent();
            VirtualFile parent = module.getProject().getProjectFile().getFileSystem().findFileByPath(parentPath);
            if(parent != null) {
                VirtualFile child = null;
                try {
                    child = parent.createChildData(this, newFile.getName());
                    this.virtualFile = child;
                    setContents(content, 0, null);
                } catch(IOException e) {
                    throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Failed to create file: " + virtualFile, e));
                }
            } else {
                throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Parent: " + parentPath + " (" + newFile.getPath() + ") could not be found"));
            }
        }
    }

    public void setContents(InputStream content, int updateFlags, IProgressMonitor monitor) throws CoreException {
        if(virtualFile != null && virtualFile.exists()) {
            byte[] bytes;
            try {
                bytes = IOUtils.toByteArray(content);
                virtualFile.setBinaryContent(bytes);
            } catch(IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Failed to write bytes to: " + virtualFile, e));
            }
        }
    }
}
