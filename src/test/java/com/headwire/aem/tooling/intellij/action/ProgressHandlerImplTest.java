package com.headwire.aem.tooling.intellij.action;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by schaefa on 3/17/16.
 */
public class ProgressHandlerImplTest {

    ProgressHandlerImpl root;
    ProgressHandlerImpl subTasks;
    ProgressHandlerImpl subSubTasks;

    @Before
    public void setUp() throws Exception {
        root = new ProgressHandlerImpl("root");
        subTasks = (ProgressHandlerImpl) root.startSubTasks(2, "Sub Tasks 1");
        subSubTasks = (ProgressHandlerImpl) subTasks.startSubTasks(3, "Sub Tasks 2");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testMarkAsCancelled() throws Exception {
        subSubTasks.markAsCancelled();
        // Check that all Progress Handlers are marked as cancelled
        assertTrue("Root was not marked as cancelled", root.isMarkedAsCancelled());
        assertTrue("Sub Tasks 1 was not marked as cancelled", subTasks.isMarkedAsCancelled());
        assertTrue("Sub Tasks 2 was not marked as cancelled", subSubTasks.isMarkedAsCancelled());
    }

    @Test
    public void testSetNotCancelable() throws Exception {
        subTasks.setNotCancelable(true);
        // Check only Sub Tasks 1 and its children are not cancelable
        assertFalse("Root was not cancelable but should", root.isNotCancelable());
        assertTrue("Sub Tasks 1 was cancelable but should not", subTasks.isNotCancelable());
        assertTrue("Sub Tasks 2 was cancelable but should not", subSubTasks.isNotCancelable());
        subSubTasks.setNotCancelable(false);
        assertFalse("Root was not cancelable but should", root.isNotCancelable());
        assertTrue("Sub Tasks 1 was cancelable but should not", subTasks.isNotCancelable());
        assertFalse("Sub Tasks 2 was not cancelable but should", subSubTasks.isNotCancelable());
        subTasks.setNotCancelable(true);
        // Re-Check only Sub Tasks 1 and its children are not cancelable
        assertFalse("Root was not cancelable but should", root.isNotCancelable());
        assertTrue("Sub Tasks 1 was cancelable but should not", subTasks.isNotCancelable());
        assertTrue("Sub Tasks 2 was cancelable but should not", subSubTasks.isNotCancelable());
        subTasks.setNotCancelable(false);
        // Cleared the Not Cancelable. Make sure all are cancelable now
        assertFalse("Root was not cancelable but should", root.isNotCancelable());
        assertFalse("Sub Tasks 1 was not cancelable but should", subTasks.isNotCancelable());
        assertFalse("Sub Tasks 2 was not cancelable but should", subSubTasks.isNotCancelable());
    }

    @Test
    public void testGetTrace() {
        String trace = subSubTasks.getTrace();
        assertEquals("Trace did not match expectation", "root -> Sub Tasks 1 -> Sub Tasks 2", trace);
    }
}