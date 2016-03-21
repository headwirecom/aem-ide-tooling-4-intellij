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

/**
 * This interface is implemented by objects that visit resource trees. The fast
 * visitor is an optimized mechanism for tree traversal that creates a minimal
 * number of objects. The visitor is provided with a callback interface,
 * instead of a resource. Through the callback, the visitor can request
 * information about the resource being visited.
 * <p>
 * Usage:
 * <pre>
 * class Visitor implements IResourceProxyVisitor {
 * public boolean visit (IResourceProxy proxy) {
 * // your code here
 * return true;
 * }
 * }
 * ResourcesPlugin.getWorkspace().getRoot().accept(new Visitor(), IResource.NONE);
 * </pre>
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IResource#accept(IResourceVisitor)
 * @since 2.1
 */
@Deprecated
public interface IResourceProxyVisitor {
    /**
     * Visits the given resource.
     *
     * @param proxy for requesting information about the resource being visited;
     *              this object is only valid for the duration of the invocation of this
     *              method, and must not be used after this method has completed
     * @return <code>true</code> if the resource's members should
     * be visited; <code>false</code> if they should be skipped
     * @throws CoreException if the visit fails for some reason.
     */
//    public boolean visit(IResourceProxy proxy) throws CoreException;
}
