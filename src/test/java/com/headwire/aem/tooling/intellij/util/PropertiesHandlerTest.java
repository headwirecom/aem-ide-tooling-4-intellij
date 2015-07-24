package com.headwire.aem.tooling.intellij.util;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by schaefa on 7/24/15.
 */
public class PropertiesHandlerTest {

    @Test
    public void testRemoveLeadingDots() {
        String result = "one..";
        String test = "one..";
        assertEquals("Did not handle no leading dots", result, PropertiesHandler.removeLeadingDots(test));
        test = ".one..";
        assertEquals("Did not remove single leading dots", result, PropertiesHandler.removeLeadingDots(test));
        test = "....one..";
        assertEquals("Did not remove multiple leading dots", result, PropertiesHandler.removeLeadingDots(test));
    }

    @Test
    public void testGetToken() {
        String result = "one";
        String test = "one";
        assertEquals("Did not deliver just the token", result, PropertiesHandler.getToken(test));
        test = ".one.two";
        assertEquals("Did not deliver token from 2 values", result, PropertiesHandler.getToken(test));
        test = "....one..two.three";
        assertEquals("Did not deliver token from 3 values", result, PropertiesHandler.getToken(test));
        test = "";
        assertEquals("Did not handle empty token", "", PropertiesHandler.getToken(test));
        test = null;
        assertEquals("Did not handle null token", "", PropertiesHandler.getToken(test));
    }

    @Test
    public void testGetIndex() {
        int result = 1;
        String test = "1";
        assertEquals("Did not deliver just the index", result, PropertiesHandler.getIndex(test));
        test = "1.two";
        assertEquals("Did not deliver index from 2 values", result, PropertiesHandler.getIndex(test));
        test = "....1..two.three";
        assertEquals("Did not deliver token from 3 values", result, PropertiesHandler.getIndex(test));
        test = "";
        assertEquals("Did not handle index with empty token", -1, PropertiesHandler.getIndex(test));
        test = null;
        assertEquals("Did not handle index with null token", -1, PropertiesHandler.getIndex(test));
    }

    @Test
    public void testDropToken() {
        String result = "two";
        String test = "two";
        assertEquals("Did not drop single value", "", PropertiesHandler.dropToken(test));
        test = "1.two";
        assertEquals("Did not drop token from 2 values", result, PropertiesHandler.dropToken(test));
        result = "two.three";
        test = "....1.two.three";
        assertEquals("Did not drop token from 3 values", result, PropertiesHandler.dropToken(test));
        result = "two..three";
        test = "....1..two..three";
        assertEquals("Did not drop token from 3 values with dots", result, PropertiesHandler.dropToken(test));
        test = "";
        assertEquals("Did not handle drop token with empty token", "", PropertiesHandler.dropToken(test));
        test = null;
        assertEquals("Did not handle drop token with null token", "", PropertiesHandler.dropToken(test));
    }

    private String propertyContent =
        "test.1.a=value\n" +
        ".test.1..b=value2\n" +
        "..test.1...c=value3\n" +
        "...test.2.a=value4\n" +
        "test..2..b=value5\n" +
        ".test..2.c=value6\n" +
        "..test..3=value7\n" +
        "another.10.a=value\n" +
        "another.10.b=value"
        ;

    @Test
    public void testFilterProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(propertyContent));
        Properties filteredProperties = PropertiesHandler.filterProperties(properties, "test");
        assertEquals("Wrong Filtering Result Count", properties.size() - 2, filteredProperties.size());
        for(String name: filteredProperties.stringPropertyNames()) {
            if(!name.startsWith("1") && !name.startsWith("2") && !name.startsWith("3")) {
                fail("Wrong Filtering Result Token: '" + name + "'");
            }
        }
    }

    private List<Integer> indexes = Arrays.asList(1, 2, 3);
    private String propertyResult1 =
            "a=value\n" +
            "b=value2\n" +
            "c=value3";

    private String propertyResult2 =
            "a=value4\n" +
            "b=value5\n" +
            "c=value6";

    @Test
    public void testCollectProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(propertyContent));
        Properties filteredProperties = PropertiesHandler.filterProperties(properties, "test");
        assertEquals("Wrong Filtering Result Count", properties.size() - 2, filteredProperties.size());
        Map<Integer, Properties> collectedProperties = PropertiesHandler.collectProperties(filteredProperties);
        assertEquals("Wrong Number of Indexes", 3, collectedProperties.keySet().size());

        Properties propertiesResults1 = new Properties();
        propertiesResults1.load(new StringReader(propertyResult1));
        Properties propertiesResults2 = new Properties();
        propertiesResults2.load(new StringReader(propertyResult2));
        Properties propertiesResults3 = new Properties();
        for(int index: collectedProperties.keySet()) {
            assertTrue("Unknown Collected Index: " + index, indexes.contains(index));
            switch(index) {
                case 1:
                    assertEquals("Wrong Properties for Index 1", propertiesResults1, collectedProperties.get(index));
                    break;
                case 2:
                    assertEquals("Wrong Properties for Index 2", propertiesResults2, collectedProperties.get(index));
                    break;
                case 3:
                    assertEquals("Wrong Properties for Index 3", propertiesResults3, collectedProperties.get(index));
                    break;
                default:
                    fail("Unknown Collected Index: " + index);
            }
        }
    }
}
