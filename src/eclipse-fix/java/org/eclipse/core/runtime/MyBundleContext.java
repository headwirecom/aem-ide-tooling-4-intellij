package org.eclipse.core.runtime;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by schaefa on 4/28/15.
 */
public class MyBundleContext
    implements BundleContext
{
    Bundle bundle;
    Map<ServiceReference, Object> serviceReferenceObjectMap = new HashMap<ServiceReference, Object>();

    public void addServiceReference(ServiceReference serviceReference, Object service) {
        serviceReferenceObjectMap.put(serviceReference, service);
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public Bundle getBundle() {
        return null;
    }

    @Override
    public Bundle installBundle(String location, InputStream input) throws BundleException {
        return null;
    }

    @Override
    public Bundle installBundle(String location) throws BundleException {
        return null;
    }

    @Override
    public Bundle getBundle(long id) {
        return bundle;
    }

    @Override
    public Bundle[] getBundles() {
        return new Bundle[] {bundle};
    }

    @Override
    public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {

    }

    @Override
    public void addServiceListener(ServiceListener listener) {

    }

    @Override
    public void removeServiceListener(ServiceListener listener) {

    }

    @Override
    public void addBundleListener(BundleListener listener) {

    }

    @Override
    public void removeBundleListener(BundleListener listener) {

    }

    @Override
    public void addFrameworkListener(FrameworkListener listener) {

    }

    @Override
    public void removeFrameworkListener(FrameworkListener listener) {

    }

    @Override
    public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
        return null;
    }

    @Override
    public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
        return null;
    }

    @Override
    public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
        return null;
    }

    @Override
    public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        return new ServiceReference<?>[0];
    }

    @Override
    public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
        return new ServiceReference<?>[0];
    }

    @Override
    public ServiceReference<?> getServiceReference(String clazz) {
        return null;
    }

    @Override
    public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
        return null;
    }

    @Override
    public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) throws InvalidSyntaxException {
        return null;
    }

    @Override
    public <S> S getService(ServiceReference<S> reference) {
        return (S) serviceReferenceObjectMap.get(reference);
    }

    @Override
    public boolean ungetService(ServiceReference<?> reference) {
        return false;
    }

    @Override
    public File getDataFile(String filename) {
        return null;
    }

    @Override
    public Filter createFilter(String filter) throws InvalidSyntaxException {
        return null;
    }

    @Override
    public Bundle getBundle(String location) {
        return null;
    }
}
