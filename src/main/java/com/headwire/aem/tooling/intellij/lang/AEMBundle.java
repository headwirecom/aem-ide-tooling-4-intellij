package com.headwire.aem.tooling.intellij.lang;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * Created by schaefa on 5/1/15.
 */
public class AEMBundle {
    public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params) {
        String message = CommonBundle.messageOrDefault(getBundle(), key, "", params);
        return message;
    }

    private static Reference<ResourceBundle> ourBundle;
    @NonNls
    protected static final String PATH_TO_BUNDLE = "messages.AEMBundle";

    private AEMBundle() {
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
        if(bundle == null) {
            bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE);
            ourBundle = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }
}
