package com.headwire.aem.base.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Exporter;

import javax.inject.Inject;
import javax.inject.Named;

@Model(
        adaptables = Resource.class,
        resourceType = "base/components/content/text",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = "jackson", extensions = "json")
public class TextModel implements ICheckEmpty {

    @Inject
    private String text;

    @Inject @Named("textIsRich")
    private boolean isRichText;

    public String getText() {
        return text == null ? "" : text;
    }

    public boolean isRichText() {
        return isRichText;
    }

    @Override
    public boolean isEmpty() {
        return text == null;
    }
}