package com.headwire.aem.tooling.intellij.util;

import java.lang.reflect.Field;

public class AccessUtil {

    public static void setPrivateFieldValue(Class instanceClass, Object instance, String fieldName, Object fieldValue)
        throws IllegalAccessException, NoSuchFieldException
    {
        Field field = getPrivateField(instanceClass, fieldName);
        field.set(instance, fieldValue);
    }

    public static <T> T getPrivateFieldValue(Class instanceClass, Class<T> returnClass, Object instance, String fieldName)
        throws NoSuchFieldException, IllegalAccessException
    {
        Field field = getPrivateField(instanceClass, fieldName);
        return (T) field.get(instance);
    }

    public static Field getPrivateField(Class instanceClass, String fieldName)
        throws NoSuchFieldException
    {
        Field field = instanceClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
