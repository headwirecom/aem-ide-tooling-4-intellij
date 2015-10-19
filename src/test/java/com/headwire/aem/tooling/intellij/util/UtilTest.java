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
