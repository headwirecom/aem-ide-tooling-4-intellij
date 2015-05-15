package com.headwire.aem.tooling.intellij.eclipse.wrapper;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationAdapter;
import com.intellij.openapi.components.ApplicationComponent;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Created by schaefa on 5/14/15.
 */
public class EventAdminWrapper
    extends ApplicationComponent.Adapter
    implements EventAdmin
{

    public EventAdminWrapper(Application application) {
    }

    @Override
    public void postEvent(Event event) {

    }

    @Override
    public void sendEvent(Event event) {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }
}
