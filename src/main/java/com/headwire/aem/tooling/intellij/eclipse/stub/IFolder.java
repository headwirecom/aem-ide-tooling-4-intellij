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

import com.headwire.aem.tooling.intellij.util.Constants;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/13/15.
 */
@Deprecated
public class IFolder extends IResource {

    public IFolder() {}

    public IFolder(@NotNull Module module, @NotNull VirtualFile file) {
        super(module, file);
    }

    public IFolder(@NotNull Module module, @NotNull File file) {
        super(module, file);
    }

    public IResource findMember(String member) {
        IResource ret = null;
        // IntelliJ is expected forward slashes
        final String adjustedMember = member.replace("\\", "/");
        if(virtualFile == null) {
            File[] memberFiles = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    //AS TODO: Check if that also works for files in child folders
                    return name.equals(adjustedMember);
                }
            });
            if(memberFiles.length > 0) {
                File memberFile = memberFiles[0];
                ret = memberFile.isDirectory() ? new IFolder(module, memberFile) : new IFile(module, memberFile);
            }
        } else {
            VirtualFile memberFile = virtualFile.findChild(member);
            if(memberFile == null) {
                memberFile = virtualFile.findFileByRelativePath(member);
            }
            if(memberFile != null) {
                ret = memberFile.isDirectory() ? new IFolder(module, memberFile) : new IFile(module, memberFile);
            }
        }
        return ret;
    }

    public IResource findMember(IPath member) {
        return findMember(member.toOSString());
    }

    public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
        VirtualFile parentFolder;
        String folderName;
        if(virtualFile == null) {
            File parentFile = file.getParentFile();
            parentFolder = module.getProject().getProjectFile().getFileSystem().findFileByPath(parentFile.getPath());
            folderName = file.getName();
        } else {
            parentFolder = virtualFile.getParent();
            folderName = virtualFile.getName();
        }
        if(parentFolder != null) {
            try {
                parentFolder.createChildDirectory(module, folderName);
                if(virtualFile == null) {
                    virtualFile = parentFolder.findChild(folderName);
                }
            } catch(IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Failed to create file: " + file, e));
            }
        } else {
            throw new CoreException(new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Failed to create folder: " + file + " because parent could not be found"));
        }
    }
}
