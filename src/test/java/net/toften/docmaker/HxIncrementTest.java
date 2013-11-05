package net.toften.docmaker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.junit.Assert.*;

public class HxIncrementTest {
	

	@Test
	public void testSimpleHxLine() {
		String line = "<h1>Header 2</h1>";
		
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 0), "<h1>Header 2</h1>");
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 1), "<h2>Header 2</h2>");
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 2), "<h3>Header 2</h3>");
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 3), "<h4>Header 2</h4>");
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 4), "<h5>Header 2</h5>");
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 5), "<h6>Header 2</h6>");

		assertEquals(DefaultAssemblyHandler.incrementHTag(line, -1), "<h0>Header 2</h0>");
	}

	@Test
	public void testMixedLine() {
		String line = "<h1>Header 1</h1><p>Body</p><h2>Header 2</h2>";
		
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 1), "<h2>Header 1</h2><p>Body</p><h3>Header 2</h3>");
	}

	public void testMixedIlligalLine() {
		String line = "<h1>Header 1</h2><p>Body</p><h4>Header 2</h2>";
		
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 2), "<h3>Header 1</h4><p>Body</p><h6>Header 2</h4>");
	}
	
//	@Test
	public void testWithId() {
		String line = "<h1 id=\"header2\">Header 2</h1>";
		
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 0), "<h1 id=\"header2\">Header 2</h1>");
		assertEquals(DefaultAssemblyHandler.incrementHTag(line, 1), "<h2 id=\"header2\">Header 2</h2>");
	}
	
	@Test
	public void testPattern() {
		Pattern p = Pattern.compile(DefaultAssemblyHandler.headerRegex);
		
		String line = "<h1>Header 2</h1>";
		Matcher m = p.matcher(line);
		
		assertTrue(m.find());
		assertEquals("<h1>", m.group(0));
		assertEquals(1, Integer.parseInt(m.group(2)));
		assertEquals(0, m.start());
		assertEquals(4, m.end());
		int start = m.end();
		
		assertTrue(m.find());
		assertEquals("</h1>", m.group(0));
		assertEquals('/', m.group().charAt(1));
		assertEquals(12, m.start());
		assertEquals(17, m.end());
		int end = m.start();
		
		assertEquals("Header 2", line.substring(start, end));
	}
}
