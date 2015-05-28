package com.headwire.aem.tooling.intellij.config;

import java.util.EventListener;

/**
 * Created by schaefa on 3/20/15.
 */
public interface ConfigurationListener extends EventListener {
    public void configurationLoaded();
}
