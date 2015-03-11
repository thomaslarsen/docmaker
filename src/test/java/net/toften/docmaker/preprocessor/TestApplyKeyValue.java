package net.toften.docmaker.preprocessor;

import static org.junit.Assert.*;

import java.util.Properties;

import net.toften.docmaker.LogWrapper;
import net.toften.docmaker.postprocessors.ApplyKeyValue;

import org.junit.Test;

public class TestApplyKeyValue {
	private LogWrapper lw = new LogWrapper() {
		
		@Override
		public void warn(String message) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(String message) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(String message) {
			// TODO Auto-generated method stub
			
		}
	};

	@Test
    public void testSingleValueSingleLine() {
        String text = "Hello ${key}";
        Properties p = new Properties();
        p.put("key", "World");

        ApplyKeyValue akv = new ApplyKeyValue(p);

        assertEquals("Hello World", akv.resolve(p, text, lw));
    }

    @Test
    public void testSingleValueMultiLine() {
        String text = "Hello ${key}\nI live in ${home}";
        Properties p = new Properties();
        p.put("key", "World");
        p.put("home", "London");

        ApplyKeyValue akv = new ApplyKeyValue(p);

        assertEquals("Hello World\nI live in London", akv.resolve(p, text, lw));
    }

    @Test
    public void testSameValueMultiLine() {
        String text = "Hello ${home}\nI live in ${home}";
        Properties p = new Properties();
        p.put("key", "World");
        p.put("home", "London");

        ApplyKeyValue akv = new ApplyKeyValue(p);

        assertEquals("Hello London\nI live in London", akv.resolve(p, text, lw));
    }

    @Test
    public void testSingleKeyInKeySingleLine() {
        String text = "Hello ${key}";
        Properties p = new Properties();
        p.put("home", "London");
        p.put("key", "${home} City");

        ApplyKeyValue akv = new ApplyKeyValue(p);

        assertEquals("Hello London City", akv.resolve(p, text, lw));
    }
}
