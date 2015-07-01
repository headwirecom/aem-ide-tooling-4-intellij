package com.headwire.aem.tooling.intellij.console;

import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;

public abstract class ConsoleLogCategory {
    public static final String CONSOLE_LOG_CATEGORY = "com.headwire.aem.tooling.intellij.console.consoleLogCategory";
    public static final ExtensionPointName<ConsoleLogCategory> EP_NAME = ExtensionPointName.create(CONSOLE_LOG_CATEGORY);

    private final String myDisplayName;

    protected ConsoleLogCategory(@NotNull String displayName) {
        myDisplayName = displayName;
    }

    @NotNull
    public final String getDisplayName() {
        return myDisplayName;
    }

    public abstract boolean acceptsNotification(@NotNull String groupId);
}
