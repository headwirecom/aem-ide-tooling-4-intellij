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

package com.headwire.aem.tooling.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Andreas Schaefer
 */
public class SlingModuleFacet extends Facet<SlingModuleFacetConfiguration> {
    public static final FacetTypeId<SlingModuleFacet> ID = new FacetTypeId<SlingModuleFacet>(SlingModuleFacetType.STRING_ID);

    public SlingModuleFacet(@NotNull FacetType facetType, @NotNull Module module, @NotNull String name, @NotNull SlingModuleFacetConfiguration configuration) {
        super(facetType, module, name, configuration, null);
    }

    public static FacetType<SlingModuleFacet, SlingModuleFacetConfiguration> getFacetType() {
        return FacetTypeRegistry.getInstance().findFacetType(ID);
    }

    @Nullable
    public static SlingModuleFacet getFacetByModule(@Nullable Module module) {
        if(module == null) return null;
        return FacetManager.getInstance(module).getFacetByType(ID);
    }

    //  @NotNull
    //  public AppEngineSdk getSdk() {
    //    return AppEngineSdkManager.getInstance().findSdk(getConfiguration().getSdkHomePath());
    //  }
    //
    //  @Nullable
    //  public static AppEngineWebApp getDescriptorRoot(@Nullable VirtualFile descriptorFile, @NotNull final Project project) {
    //    if (descriptorFile == null) return null;
    //
    //    Module module = ModuleUtilCore.findModuleForFile(descriptorFile, project);
    //    if (module == null) return null;
    //
    //    PsiFile psiFile = PsiManager.getInstance(project).findFile(descriptorFile);
    //    if (psiFile == null) return null;
    //
    //    return getRootElement(psiFile, AppEngineWebApp.class, module);
    //  }

    //todo[nik] copied from JamCommonUtil
    @Nullable
    private static <T> T getRootElement(final PsiFile file, final Class<T> domClass, final Module module) {
        if(!(file instanceof XmlFile)) return null;
        final DomManager domManager = DomManager.getDomManager(file.getProject());
        final DomFileElement<DomElement> element = domManager.getFileElement((XmlFile) file, DomElement.class);
        if(element == null) return null;
        final DomElement root = element.getRootElement();
        if(!ReflectionUtil.isAssignable(domClass, root.getClass())) return null;
        return (T) root;
    }


    public boolean shouldRunEnhancerFor(@NotNull VirtualFile file) {
        //    for (String path : getConfiguration().getFilesToEnhance()) {
        //      final VirtualFile toEnhance = LocalFileSystem.getInstance().findFileByPath(path);
        //      if (toEnhance != null && VfsUtilCore.isAncestor(toEnhance, file, false)) {
        //        return true;
        //      }
        //    }
        return false;
    }
}
