package com.headwire.aem.tooling.intellij.util;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.FileAttribute;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import com.intellij.util.io.IOUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by schaefa on 4/30/15.
 */
public class Util {

    public static final Key<Long> MODIFICATION_DATE_KEY = Key.create("modification date");


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

    public static long convertToLong(String value, long defaultValue) {
        long ret = defaultValue;
        if(StringUtils.isNotBlank(value)) {
            try {
                ret = Long.parseLong(value);
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

    private static final FileAttribute MODIFICATION_STAMP_FILE_ATTRIBUTE = new FileAttribute("modificationStampFileAttribute", 1, true);

    @Nullable
    public static long getModificationStamp(VirtualFile file) {
        long ret = -1;
        Long temporary = file.getUserData(Util.MODIFICATION_DATE_KEY);
        if(temporary == null) {
            if(file instanceof NewVirtualFile) {
                final DataInputStream is = MODIFICATION_STAMP_FILE_ATTRIBUTE.readAttribute(file);
                if(is != null) {
                    try {
                        try {
                            if(is.available() > 0) {
                                String value = IOUtil.readString(is);
                                ret = convertToLong(value, ret);
                                if(ret > 0) {
                                    file.putUserData(Util.MODIFICATION_DATE_KEY, ret);
                                }
                            }
                        } finally {
                            is.close();
                        }
                    } catch(IOException e) {
                        // Ignore it but we might need to throw an exception
                        String message = e.getMessage();
                    }
                }
            }
        }
        return ret;
    }

    public static void setModificationStamp(VirtualFile file) {
        // Store it in memory first
        file.putUserData(Util.MODIFICATION_DATE_KEY, file.getModificationStamp());
        if(file instanceof NewVirtualFile) {
            final DataOutputStream os = MODIFICATION_STAMP_FILE_ATTRIBUTE.writeAttribute(file);
            try {
                try {
                    IOUtil.writeString(StringUtil.notNullize(file.getTimeStamp() + ""), os);
                }
                finally {
                    os.close();
                }
            }
            catch (IOException e) {
                // Ignore it but we might need to throw an exception
                String message = e.getMessage();
            }
        }
    }
}
