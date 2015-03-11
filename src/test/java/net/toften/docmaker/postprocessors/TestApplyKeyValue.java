package net.toften.docmaker.postprocessors;

import static org.junit.Assert.*;

import java.util.Properties;

import net.toften.docmaker.postprocessors.ApplyKeyValue;

import org.junit.Test;

public class TestApplyKeyValue {
	@Test
    public void testSingleValueSingleLine() {
        String text = "Hello ${key}";
        Properties p = new Properties();
        p.put("key", "World");

        assertEquals("Hello World", ApplyKeyValue.resolve(p, text));
    }

    @Test
    public void testSingleValueMultiLine() {
        String text = "Hello ${key}\nI live in ${home}";
        Properties p = new Properties();
        p.put("key", "World");
        p.put("home", "London");

        assertEquals("Hello World\nI live in London", ApplyKeyValue.resolve(p, text));
    }

    @Test
    public void testSameValueMultiLine() {
        String text = "Hello ${home}\nI live in ${home}";
        Properties p = new Properties();
        p.put("key", "World");
        p.put("home", "London");

        assertEquals("Hello London\nI live in London", ApplyKeyValue.resolve(p, text));
    }

    @Test
    public void testSingleKeyInKeySingleLine() {
        String text = "Hello ${key}";
        Properties p = new Properties();
        p.put("home", "London");
        p.put("key", "${home} City");

        assertEquals("Hello London City", ApplyKeyValue.resolve(p, text));
    }
}
