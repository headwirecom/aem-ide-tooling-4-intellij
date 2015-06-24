package com.headwire.aem.tooling.intellij.communication;

import com.headwire.aem.tooling.intellij.config.ServerConfiguration;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by schaefa on 6/23/15.
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
