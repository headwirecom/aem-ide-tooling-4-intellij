/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.ide.io;

/**
 * Created by Andreas Schaefer (Headwire.com) on 11/8/15.
 */
public class ConnectorException
    extends Exception
{
    public static final int UNKNOWN = 0;

    private int id = UNKNOWN;

    public ConnectorException(int id) {
        this.id = id;
    }

    public ConnectorException(String message) {
        super(message);
    }

    public ConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectorException(String message, ConnectorException cause) {
        super(message, cause);
        id = cause.getId();
    }

    public ConnectorException(int id, String message) {
        super(message);
        this.id = id;
    }

    public ConnectorException(int id, Throwable cause) {
        super(cause);
        this.id = id;
    }

    public ConnectorException(int id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }

    /** @return The Error Id if set or UNKNOWN. The convention is that anything below 0 is an error and above a warning **/
    public int getId() {
        return id;
    }
}
