package com.headwire.aem.tooling.intellij.util;

/**
 * Created by schaefa on 5/5/15.
 */
public class ServerException
    extends Exception
{
    public ServerException(String s) {
        super(s);
    }

    public ServerException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
