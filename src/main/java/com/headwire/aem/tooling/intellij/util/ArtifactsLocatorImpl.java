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

package com.headwire.aem.tooling.intellij.util;

import com.intellij.openapi.components.ApplicationComponent;
import org.apache.sling.ide.artifacts.EmbeddedArtifact;
import org.apache.sling.ide.artifacts.EmbeddedArtifactLocator;

import java.net.URL;

/**
 * Created by Andreas Schaefer (Headwire.com) on 5/5/15.
 */
public class ArtifactsLocatorImpl
    extends ApplicationComponent.Adapter
    implements EmbeddedArtifactLocator
{
    private static final String ARTIFACTS_LOCATION = "artifacts";

    @Override
    public EmbeddedArtifact loadToolingSupportBundle() {

        String version = "1.0.2"; // TODO - remove version hardcoding
        String artifactId = "org.apache.sling.tooling.support.install";
        String extension = "jar";

        String fileName = artifactId
            + (version != null || version.trim().length() > 0 ? "-" + version : "")
            + "." + extension;
        URL jarUrl = loadResource(
            ARTIFACTS_LOCATION + "/sling-tooling-support-install/" + fileName
        );

        return new EmbeddedArtifact(fileName, version, jarUrl);
    }

    @Override
    public EmbeddedArtifact loadSourceSupportBundle() {
        String version = "1.0.0"; // TODO - remove version hardcoding
        String artifactId = "org.apache.sling.tooling.support.source";
        String extension = "jar";

        String fileName = artifactId
            + (version != null || version.trim().length() > 0 ? "-" + version : "")
            + "." + extension;

        URL jarUrl = loadResource(
            ARTIFACTS_LOCATION + "/sling-tooling-support-source/" + fileName
        );

        return new EmbeddedArtifact(fileName, version, jarUrl);
    }

    private URL loadResource(
        String resourceLocation
    ) {
        URL resourceUrl = getClass().getClassLoader().getResource(resourceLocation);
        if(resourceUrl == null) {
            throw new RuntimeException("Unable to locate bundle resource " + resourceLocation);
        }
        return resourceUrl;
    }
}
