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

/**
 * Created by Andreas Schaefer (Headwire.com) on 6/18/15.
 * <p/>
 * This interface is implemented by objects that visit resource trees.
 * <p>
 * Usage:
 * <pre>
 * class Visitor implements IResourceVisitor {
 * public boolean visit(IResource res) {
 * // your code here
 * return true;
 * }
 * }
 * IResource root = ...;
 * root.accept(new Visitor());
 * </pre>
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IResource#accept(IResourceVisitor)
 */
@Deprecated
public interface IResourceVisitor {
    /**
     * Visits the given resource.
     *
     * @param resource the resource to visit
     * @return <code>true</code> if the resource's members should
     * be visited; <code>false</code> if they should be skipped
     * @throws CoreException if the visit fails for some reason.
     */
    public boolean visit(IResource resource) throws CoreException;
}
