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

package com.headwire.aem.tooling.intellij;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by schaefa on 8/24/15.
 */
public class TestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

    public static void setPrivateVariable(Object target, String variableName, Object value) {
        Field field;
        try {
            field = target.getClass().getDeclaredField(variableName);
            field.setAccessible(true);
            field.set(target, value);
        } catch(NoSuchFieldException e) {
            fail("Field Not Found: '" + variableName + "'");
        } catch(IllegalAccessException e) {
            fail("Field Could not be: '" + variableName + "', value: '" + value + "'");
        }
    }

    public static void callRestrictedMethod(Object target, String methodName, Object value) {
        Method method = null;
        try {
            method = target.getClass().getDeclaredMethod(methodName, value.getClass());
        } catch (NoSuchMethodException e) {
            // Try to go with Interface
            Class[] interfaces = value.getClass().getInterfaces();
            if (interfaces.length > 0) {
                try {
                    method = target.getClass().getDeclaredMethod(methodName, interfaces[0]);
                } catch (NoSuchMethodException e1) {
                    LOGGER.debug("Method could not be found", e);
                    fail("Method Not Found: '" + methodName + "'");
                }
            }
        }
        if(method != null) {
            try {
                method.setAccessible(true);
                method.invoke(target, value);
            } catch (InvocationTargetException e) {
                LOGGER.debug("Method could not be invoked", e);
                fail("Method Invocation Failed: '" + methodName + "', value: '" + value + "'");
            } catch (IllegalAccessException e) {
                LOGGER.debug("Method could not be accessed", e);
                fail("Illegal Access to Method: '" + methodName + "'");
            }
        }
    }

    public static void callRestrictedMethod(Object target, String methodName, Object...typeValueList) {
        Method method = null;
        Object[] parameterValueList = null;
        try {
            if(typeValueList == null || typeValueList.length == 0) {
                method = target.getClass().getMethod(methodName);
            } else if(typeValueList.length % 2 == 0) {
                Class[] parameterTypeList = new Class[typeValueList.length / 2];
                parameterValueList = new Object[typeValueList.length / 2];
                for(int i = 0; i < typeValueList.length; i++) {
                    if(i % 2 == 0) {
                        parameterTypeList[i / 2] =  (Class) typeValueList[i];
                    } else {
                        parameterValueList[i / 2] = (typeValueList[i]);
                    }
                }
                method = target.getClass().getDeclaredMethod(methodName, parameterTypeList);
            }
        } catch (NoSuchMethodException e) {
            fail("Method Not Found: '" + methodName + "'");
        }
        if(method != null) {
            try {
                method.setAccessible(true);
                method.invoke(target, parameterValueList);
            } catch (InvocationTargetException e) {
                fail("Method Invocation Failed: '" + methodName + "', value: '" + parameterValueList + "'");
            } catch (IllegalAccessException e) {
                fail("Illegal Access to Method: '" + methodName + "'");
            }
        }
    }
}
