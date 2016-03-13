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

package com.headwire.aem.tooling.intellij.eclipse.stub;

import com.headwire.aem.tooling.intellij.util.Util;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.headwire.aem.tooling.intellij.config.ServerConfiguration.Module;

/**
 * This is a wrapper class for the Eclipse IResource class used in some methods.
 * It should map the IntelliJ properties to the Eclipse IResource to avoid having
 * to rewrite certain classes.
 *
 * Created by Andreas Schaefer (Headwire.com) on 5/13/15.
 */
@Deprecated
public class IResource {
    public static final int FILE = 1;
    public static final int FOLDER = 2;
    public static final int CHECK_ANCESTORS = 3;
    public static final int DEPTH_INFINITE = 1;
    public static final int KEEP_HISTORY = 10;

    protected VirtualFile virtualFile;
    protected File file;
    protected Module module;
    protected IProject project;
    protected IPath location;

    public IResource(@NotNull Module module, @NotNull VirtualFile virtualFile) {
        this.module = module;
        this.virtualFile = virtualFile;
        project = new IProject(module);
        location = new IPath(virtualFile.getPath());
    }

    public IResource(@NotNull Module module, @NotNull String filePath) {
        this.module = module;
        virtualFile = module.getProject().getProjectFile().getFileSystem().findFileByPath(filePath);
        if(virtualFile == null) {
            this.file = new File(filePath);
        }
        project = new IProject(module);
        location = new IPath(file.getPath());
    }

    public IResource(@NotNull Module module, @NotNull File file) {
        this.module = module;
        virtualFile = module.getProject().getProjectFile().getFileSystem().findFileByPath(file.getPath());
        if(virtualFile == null) {
            this.file = file;
        }
        project = new IProject(module);
        location = new IPath(file.getPath());
    }

    public IResource() {
    }

    public String getName() {
        return virtualFile == null ? file.getName() : virtualFile.getName();
    }

    public IProject getProject() {
        return project;
    }

    public IPath getLocation() {
        return location;
    }

    public int getType() {
        if(virtualFile != null) {
            if(virtualFile.isDirectory()) {
                return FOLDER;
            }
        } else {
            if(file.isDirectory()) {
                return FOLDER;
            }
        }
        return FILE;
    }

    public boolean exists() {
        return virtualFile == null ? file.exists() : virtualFile.exists();
    }

    public IPath getFullPath() {
        if(virtualFile == null) {
            return new IPath(file.getPath());
        } else {
            return new IPath(virtualFile.getPath());
        }
    }

    public IResource getParent() {
        //AS TODO: Maybe we should create this once but lazy
        if(virtualFile == null) {
            File parent = file.getParentFile();
            return parent == null ? null : new IFolder(module, parent);
        } else {
            VirtualFile parent = virtualFile.getParent();
            return parent == null ? null : new IFolder(module, parent);
        }
    }

    public List<IResource> members() {
        List<IResource> ret = new ArrayList<IResource>();
        if(virtualFile == null) {
            for(File child : file.listFiles()) {
                ret.add(new IResource(module, child.getPath()));
            }
        } else {
            for(VirtualFile child : virtualFile.getChildren()) {
                ret.add(new IResource(module, child));
            }
        }
        return ret;
    }

    public IFile getFile(IPath parentSerializationFilePath) {
        String path = parentSerializationFilePath.toPortableString();
        IFile ret;
        if(virtualFile == null) {
            File aFile = new File(file, path);
            ret = new IFile(module, aFile);
        } else {
            VirtualFile aFile = virtualFile.findFileByRelativePath(path);
            if(aFile == null) {
                File bFile = new File(virtualFile.getPath(), path);
                ret = new IFile(module, bFile);
            } else {
                ret = new IFile(module, aFile);
            }
        }
        return ret;
    }

    public boolean isTeamPrivateMember(int checkAncestors) {
//AS TODO: Is Ream Private supported in IntelliJ? -> Check later
//        throw new UnsupportedOperationException("Not yet implemented");
        return false;
    }

    public void delete(boolean isWhat, IProgressMonitor monitor) {
        if(virtualFile == null) {
            if(file.exists()) {
                file.delete();
            }
        } else {
            if(virtualFile.exists()) {
                try {
                    virtualFile.delete(this);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * Returns a relative path of this resource with respect to its project.
     * Returns the empty path for projects and the workspace root.
     * <p>
     * This is a resource handle operation; the resource need not exist.
     * If this resource does exist, its path can be safely assumed to be valid.
     * </p>
     * <p>
     * A resource's project-relative path indicates the route from the project
     * to the resource. Within a workspace, there is exactly one such path
     * for any given resource. The returned path never has a trailing slash.
     * </p>
     * <p>
     * Project-relative paths are recommended over absolute paths, since
     * the former are not affected if the project is renamed.
     * </p>
     *
     * @return the relative path of this resource with respect to its project
     * @see #getFullPath()
     * @see #getProject()
     */
    public IPath getProjectRelativePath() {
        IPath ret = null;
        String projectBasePath = module.getModuleContext().getModuleDirectory();
        String filePath = virtualFile == null ? file.getPath() : virtualFile.getPath();
        if(filePath.startsWith(projectBasePath)) {
            String relativePath = filePath.substring(projectBasePath.length());
            if(relativePath.startsWith("/")) { relativePath = relativePath.substring(1); }
            ret = new IPath(new IPath(projectBasePath), relativePath);
        }
        return ret;
    }

    public Object getSessionProperty(Object type) {
        Object ret = null;
        if(type == ResourceUtil.QN_IMPORT_MODIFICATION_TIMESTAMP) {
            if(virtualFile != null) {
                ret = Util.getModificationStamp(virtualFile);
            }
        }
        return ret;
    }

    public void setSessionProperty(Object type, Object value) {
        if(type == ResourceUtil.QN_IMPORT_MODIFICATION_TIMESTAMP) {
            if(virtualFile != null) {
                Util.setModificationStamp(virtualFile);
            }
        }
    }

    public Long getModificationStamp() {
        Long ret = virtualFile == null ? file.lastModified() : virtualFile.getTimeStamp();
        return ret;
    }

    public Module getModule() {
        return module;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    /* (non-Javadoc)
     * @see IResource#accept(IResourceVisitor)
     */
    public void accept(IResourceVisitor visitor) throws CoreException {
        accept(visitor, IResource.DEPTH_INFINITE, 0);
    }

    private boolean handleVisitor(final IResourceVisitor visitor) throws CoreException {
        boolean ret = visitor.visit(this);
        if(virtualFile == null) {
            for(File child : file.listFiles()) {
                IResource childResource = new IResource(module, child.getPath());
                boolean doChildren = childResource.handleVisitor(visitor);
                if(doChildren) {
                    childResource.handleVisitor(visitor);
                }
            }
        } else {
            for(VirtualFile child : virtualFile.getChildren()) {
                IResource childResource = child.isDirectory() ?
                    new IFolder(module, child) :
                    new IFile(module, child);
                boolean doChildren = childResource.handleVisitor(visitor);
                if(doChildren) {
                    childResource.handleVisitor(visitor);
                }
            }
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see IResource#accept(IResourceVisitor, int, int)
     */
    public void accept(final IResourceVisitor visitor, int depth, int memberFlags) throws CoreException {
        //use the fast visitor if visiting to infinite depth
        if(depth == IResource.DEPTH_INFINITE) {
            handleVisitor(visitor);
//            accept(new IResourceProxyVisitor() {
//                public boolean visit(IResourceProxy proxy) throws CoreException {
//                    return visitor.visit(proxy.requestResource());
//                }
//            }, memberFlags);
            return;
        }
//        // it is invalid to call accept on a phantom when INCLUDE_PHANTOMS is not specified
//        final boolean includePhantoms = (memberFlags & IContainer.INCLUDE_PHANTOMS) != 0;
//        ResourceInfo info = getResourceInfo(includePhantoms, false);
//        int flags = getFlags(info);
//        checkAccessible(flags);
//
//        //check that this resource matches the member flags
//        if(!isMember(flags, memberFlags)) {
//            return;
//        }
//        // visit this resource
//        if(!visitor.visit(this) || depth == DEPTH_ZERO) {
//            return;
//        }
//        // get the info again because it might have been changed by the visitor
//        info = getResourceInfo(includePhantoms, false);
//        if(info == null) {
//            return;
//        }
//        // thread safety: (cache the type to avoid changes -- we might not be inside an operation)
//        int type = info.getType();
//        if(type == FILE) {
//            return;
//        }
//        // if we had a gender change we need to fix up the resource before asking for its members
//        IContainer resource = getType() != type ? (IContainer) workspace.newResource(getFullPath(), type) : (IContainer) this;
//        IResource[] members = resource.members(memberFlags);
//        for(int i = 0; i < members.length; i++)
//            members[i].accept(visitor, DEPTH_ZERO, memberFlags);
    }
}
