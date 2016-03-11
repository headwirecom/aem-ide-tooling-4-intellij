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

package com.headwire.aem.tooling.intellij.util;

import com.headwire.aem.tooling.intellij.TestUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by schaefa on 10/15/15.
 */
public class UtilTest {

    @Test
    public void testPathEndsWithFolderUnixStyle() {
        Util util = new Util();
        TestUtils.setPrivateVariable(
            util,
            "folderSeparator",
            '/'
        );
        boolean check = Util.pathEndsWithFolder(
            "/a/b/c/d",
            "d"
        );
        assertTrue("Folder ends with 'd' but did not", check);
        check = Util.pathEndsWithFolder(
            "/a/b/c/d/",
            "d"
        );
        assertFalse("Folder ends with 'd/' but was reported as ended with", check);
        check = Util.pathEndsWithFolder(
            "/a/b/c/ed",
            "d"
        );
        assertFalse("Folder ends with 'ed' but was reported as ended with 'd'", check);
        check = Util.pathEndsWithFolder(
            "/a/b/c/d/e",
            "d"
        );
        assertFalse("Folder ends with '/e' but was reported as ended with 'd'", check);
    }

    @Test
    public void testPathEndsWithFolderWindowsStyle() {
        Util util = new Util();
        TestUtils.setPrivateVariable(
            util,
            "folderSeparator",
            '\\'
        );
        boolean check = Util.pathEndsWithFolder(
            "\\a\\b\\c\\d",
            "d"
        );
        assertTrue("Folder ends with 'd' but did not", check);
        check = Util.pathEndsWithFolder(
            "\\a\\b\\c\\d\\",
            "d"
        );
        assertFalse("Folder ends with 'd\\' but was reported as ended with", check);
        check = Util.pathEndsWithFolder(
            "\\a\\b\\c\\ed",
            "d"
        );
        assertFalse("Folder ends with 'ed' but was reported as ended with 'd'", check);
        check = Util.pathEndsWithFolder(
            "\\a\\b\\c\\d\\e",
            "d"
        );
        assertFalse("Folder ends with '\\e' but was reported as ended with 'd'", check);
    }
}
