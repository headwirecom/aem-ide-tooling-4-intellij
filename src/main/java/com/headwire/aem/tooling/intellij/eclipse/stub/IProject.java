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

import com.headwire.aem.tooling.intellij.util.ComponentProvider;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/13/15.
 */
@Deprecated
public class IProject {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Project project;
    private Module module;

    public IProject(@NotNull Module module) {
        this.project = module.getProject();
        this.module = module;
    }

    public IFolder getFolder(IPath path) {
        com.headwire.aem.tooling.intellij.communication.MessageManager messageManager = ServiceManager.getService(project,
            com.headwire.aem.tooling.intellij.communication.MessageManager.class
        );
        messageManager.sendDebugNotification("debug.folder.path", path);

        String filePath = path.toOSString();

        messageManager.sendDebugNotification("debug.folder.os.path", filePath);

        VirtualFileSystem vfs = module.getProject().getBaseDir().getFileSystem();
        VirtualFile file = vfs.findFileByPath(path.toOSString());
        if(file != null) {
            return new IFolder(module, file);
        } else {
            return new IFolder(module, new File(path.toOSString()));
        }
    }

    /* IntelliJ Specific Methods */

    public List<String> getSourceFolderList() {
        List<String> ret = new ArrayList<String>();
        for(String path: module.getUnifiedModule().getContentDirectoryPaths()) {
            ret.add(path);
        }
        return ret;
    }

    public IResource findMember(IPath path) {
        String filePath = path.toOSString();
        VirtualFile file;
        //AS TODO: What is the proper handling here (for Windows that is)?
        if(filePath.startsWith("/") || filePath.contains(":\\")) {
            file = module.getProject().getBaseDir().getFileSystem().findFileByPath(filePath);
        } else {
            file = module.getProject().getBaseDir().findFileByRelativePath(filePath);
        }
        return file.isDirectory() ? new IFolder(module, file) : new IFile(module, file);
    }

    public Workspace getWorkspace() {
        return new Workspace();
    }

    public class Workspace {

        public Root getRoot() {
            return new Root();
        }
    }

    public class Root {

        public IResource findMember(IPath childPath) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    public Module getModule() {
        return module;
    }

    public IFile getFile(String path) {
        IFile ret = null;
        boolean isAbsolute = false;
        if(path != null) {
            if (path.charAt(0) == '/') {
                isAbsolute = true;
            } else {
                int index = path.indexOf(":\\");
                int index2 = path.indexOf('\\');
                if (index < 0) {
                    index = path.indexOf(":/");
                    index2 = path.indexOf('/');
                }
                if (index > 0 && index < index2) {
                    isAbsolute = true;
                }
            }
        }
        if(isAbsolute) {
            VirtualFile virtualFile = project.getProjectFile().getFileSystem().findFileByPath(path);
            if(virtualFile != null) {
                ret = new IFile(module, virtualFile);
            } else {
                ret = new IFile(module, new File(path));
            }
        } else {
            VirtualFile virtualFile = project.getProjectFile().findFileByRelativePath(path);
            if(virtualFile != null) {
                ret = new IFile(module, virtualFile);
            } else {
                ret = new IFile(module, new File(project.getProjectFile().getPath(), path));
            }
        }
        return ret;
    }
}
