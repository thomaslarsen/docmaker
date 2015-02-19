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

        StringBuffer out = new StringBuffer();

        ApplyKeyValue akv = new ApplyKeyValue(p);

        akv.processFragment(text, out);
        assertEquals("Hello World", out.toString());
    }

    @Test
    public void testSingleValueMultiLine() {
        String text = "Hello ${key}\nI live in ${home}";
        Properties p = new Properties();
        p.put("key", "World");
        p.put("home", "London");

        StringBuffer out = new StringBuffer();

        ApplyKeyValue akv = new ApplyKeyValue(p);

        akv.processFragment(text, out);
        assertEquals("Hello World\nI live in London", out.toString());
    }

    @Test
    public void testSameValueMultiLine() {
        String text = "Hello ${home}\nI live in ${home}";
        Properties p = new Properties();
        p.put("key", "World");
        p.put("home", "London");

        StringBuffer out = new StringBuffer();

        ApplyKeyValue akv = new ApplyKeyValue(p);

        akv.processFragment(text, out);
        assertEquals("Hello London\nI live in London", out.toString());
    }

    @Test
    public void testSingleKeyInKeySingleLine() {
        String text = "Hello ${key}";
        Properties p = new Properties();
        p.put("home", "London");
        p.put("key", "${home} City");

        StringBuffer out = new StringBuffer();

        ApplyKeyValue akv = new ApplyKeyValue(p);

        akv.processFragment(text, out);
        assertEquals("Hello London City", out.toString());
    }

}
