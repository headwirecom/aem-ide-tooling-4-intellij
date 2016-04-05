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

package com.headwire.aem.tooling.intellij.facet;

import com.intellij.util.xmlb.annotations.Tag;

/**
 * @author Andreas Schaefer
 */
public class SlingModuleExtensionProperties {

  public enum ModuleType { content, bundle, excluded };

  @Tag("source-root-path")
  public String sourceRootPath = "";

  @Tag("meta-inf-path")
  public String metaInfPath = "";

  @Tag("module-type")
  public ModuleType moduleType = ModuleType.excluded;

  @Tag("ignore-maven")
  public boolean ignoreMaven = false;

  @Tag("osgi-symblic-name")
  public String osgiSymbolicName = "";

  @Tag("osgi-version")
  public String osgiVersion = "";

  @Tag("osgi-jar-file-name")
  public String osgiJarFileName = "";
}
