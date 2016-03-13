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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/13/15.
 */
@Deprecated
public class IPath {

    private IPath base;
    private String relativePath;
    private File file;
    private List<String> segments;

    public IPath(@NotNull File file) {
        this.file = file;
    }

    public IPath(@NotNull String filePath) {
        filePath = filePath.replace('\\', '/');
        if(
            filePath.contains(":/") || filePath.startsWith("/")
        ) {
            this.file = new File(filePath);
        } else {
            this.relativePath = filePath;
        }
    }

    public IPath(@NotNull IPath base, @NotNull String relativePath) {
//        this.file = new File(relativePath);
        relativePath = relativePath.replace('\\', '/');
        this.relativePath = relativePath;
        this.base = base;
    }

    public File toFile() {
        File ret = null;
        if(file != null) {
            ret = file;
        } else if(base != null) {
            ret = new File(base.toFile(), relativePath);
        } else {
            ret = new File(relativePath);
        }
        return ret;
    }

    public IPath makeRelativeTo(IPath otherPath) {
        IPath ret = null;
        File parent = otherPath.toFile();
        if(parent != null && parent.exists() && parent.isDirectory()) {
            IPath absolute = this.makeAbsolute();
            if(absolute.file.getAbsolutePath().startsWith(parent.getAbsolutePath())) {
                // Cut of the beginning
                String relativePath = absolute.file.getAbsolutePath().substring(parent.getAbsolutePath().length());
                relativePath = relativePath.replace("\\", "/");
                if(relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                ret = new IPath(otherPath, relativePath);
            }
        } else if(relativePath != null && relativePath.startsWith(otherPath.relativePath)) {
            String temp = relativePath.substring(otherPath.relativePath.length());
            temp = temp.replace("\\", "/");
            if(temp.startsWith("/")) {
                temp = temp.substring(1);
            }
            ret = new IPath(otherPath, temp);
        }
        return ret;
    }

    public String toPortableString() {
        String ret = file != null ? file.getPath() : relativePath;
        if(ret.contains("\\")) {
            ret = ret.replace('\\', '/');
        }
        // Remove drive letter if windows
//        if(ret.indexOf(":/") == 1) {
//            ret = ret.substring(2);
//        }
        return ret;
    }

    public String toOSString() {
        return file != null ? file.getPath() : relativePath;
    }

    public IPath makeAbsolute() {
        IPath ret = this;
        if(base != null) {
            ret = new IPath(new File(base.toFile(), relativePath));
        } else if(file == null) {
            ret = new IPath(new File(relativePath));
        }
        return ret;
    }

    public int segmentCount() {
        return getSegments().size();
    }

    public String segment(int i) {
        return getSegments().get(i);
    }

    private List<String> getSegments() {
        if(segments == null) {
            //AS TODO: Handle Windows Paths
            String path = relativePath;
            path = path == null ? file.getPath() : path;
            // If Windows then remove Drive letter
            if(path.contains(":/")) {
                path = path.substring(2);
            }
            path = path.replace("\\", "/");
            String[] tokens = path.split("/");
            List<String> ret = new ArrayList<String>(Arrays.asList(tokens));
            Iterator<String> i = ret.iterator();
            while(i.hasNext()) {
                String value = i.next();
                if(value == null || value.length() == 0) {
                    i.remove();
                }
            }
            segments = ret;
        }
        return segments;
    }

    public IPath removeLastSegments(int i) {
        IPath ret;
        if(file != null) {
            File parent = file.getParentFile();
            ret = new IPath(parent);
        } else {
            // If Windows then remove Drive letter
            String path = relativePath;
            if(path != null && relativePath.contains(":/")) {
                path = path.substring(3);
                if(!path.startsWith("/")) {
                    path = "/" + path;
                }
            }
            String cleanPath = path.endsWith("/") ?
                (path.length() > 1 ? path.substring(0, path.length() - 1) : "") :
                path;
            int index = cleanPath.lastIndexOf("/");
            String newPath = index >= 0 ?
                (index == 0 ? "" : cleanPath.substring(0, index)) :
                "";
            ret = new IPath(base, newPath);
        }
        return ret;
    }

    public IPath append(String osPath) {
        IPath ret = makeAbsolute();
        ret = new IPath(new File(ret.toFile(), osPath));
        return ret;
    }

    public boolean isEmpty() {
        return makeAbsolute().toFile().listFiles().length == 0;
    }

}
