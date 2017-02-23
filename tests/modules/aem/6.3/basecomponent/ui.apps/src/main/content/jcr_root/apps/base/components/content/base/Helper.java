package apps.base.components.content.base;

import javax.jcr.Node;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;
import com.adobe.cq.sightly.WCMUsePojo;

import com.headwire.aem.base.models.ICheckEmpty;

public class Helper extends WCMUsePojo {

	private Object model = null;

    public String getComponentName() {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, StringUtils.substringAfterLast(getResource().getResourceType(), "/"));
    }

    public boolean isEmpty() {
        if(model instanceof ICheckEmpty) {
            return ((ICheckEmpty) model).isEmpty();
        }
        return false;
    }

    public Object getModel() {
        return model;
    }

    public void activate() {
        model = getSlingScriptHelper().getService(ModelFactory.class).getModelFromResource(getResource()); 
    }
}