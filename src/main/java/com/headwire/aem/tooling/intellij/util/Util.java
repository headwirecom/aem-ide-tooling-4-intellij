package com.headwire.aem.tooling.intellij.util;

import com.intellij.openapi.util.Key;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Created by schaefa on 4/30/15.
 */
public class Util {

    public static final Key<Long> MODIFICATION_DATE_KEY = new Key("modification date");


    public static int convertToInt(String value, int defaultValue) {
        int ret = defaultValue;
        if(StringUtils.isNotBlank(value)) {
            try {
                ret = Integer.parseInt(value);
            } catch(NumberFormatException e) {
                // Ignore
            }
        }
        return ret;
    }

    public static <T extends Enum> T convertToEnum(String name, T defaultValue) {
        T ret = defaultValue;
        if(defaultValue == null) {
            throw new IllegalArgumentException("Default Value for Enumeration must be provided");
        }
        if(name != null) {
            try {
                ret = (T) T.valueOf(defaultValue.getClass(), name);
            } catch(IllegalArgumentException e) {
                // Enum was not found so use the default value instead
            }
        }
        return ret;
    }
}
