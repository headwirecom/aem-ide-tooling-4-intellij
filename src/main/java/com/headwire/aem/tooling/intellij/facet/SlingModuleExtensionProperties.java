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
}
