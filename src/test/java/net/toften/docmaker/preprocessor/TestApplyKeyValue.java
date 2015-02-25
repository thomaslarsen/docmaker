package net.toften.docmaker.preprocessor;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import net.toften.docmaker.postprocessors.ApplyKeyValue;

import org.junit.Test;

public class TestApplyKeyValue {
    @Test
    public void testSingleValueSingleLine() {
        String text = "Hello ${key}";
        Properties p = new Properties();
        p.put("key", "World");

        ApplyKeyValue akv = new ApplyKeyValue(p);

        assertEquals("Hello World", akv.processFragment(text));
    }

    @Test
    public void testSingleValueMultiLine() {
        String text = "Hello ${key}\nI live in ${home}";
        Properties p = new Properties();
        p.put("key", "World");
        p.put("home", "London");

        ApplyKeyValue akv = new ApplyKeyValue(p);

        assertEquals("Hello World\nI live in London", akv.processFragment(text));
    }

    @Test
    public void testSameValueMultiLine() {
        String text = "Hello ${home}\nI live in ${home}";
        Properties p = new Properties();
        p.put("key", "World");
        p.put("home", "London");

        ApplyKeyValue akv = new ApplyKeyValue(p);

        assertEquals("Hello London\nI live in London", akv.processFragment(text));
    }

    @Test
    public void testSingleKeyInKeySingleLine() {
        String text = "Hello ${key}";
        Properties p = new Properties();
        p.put("home", "London");
        p.put("key", "${home} City");

        ApplyKeyValue akv = new ApplyKeyValue(p);

        assertEquals("Hello London City", akv.processFragment(text));
    }
}
