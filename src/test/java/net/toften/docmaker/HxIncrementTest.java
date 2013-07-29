package net.toften.docmaker;

import org.junit.Test;

import static org.junit.Assert.*;

public class HxIncrementTest {
	@Test
	public void testSimpleHxLine() {
		String line = "<h1>Header 2</h1>";
		
		assertEquals(AssemblyHandler.incrementHTag(line, 0), "<h1>Header 2</h1>");
		assertEquals(AssemblyHandler.incrementHTag(line, 1), "<h2>Header 2</h2>");
		assertEquals(AssemblyHandler.incrementHTag(line, 2), "<h3>Header 2</h3>");
		assertEquals(AssemblyHandler.incrementHTag(line, 3), "<h4>Header 2</h4>");
		assertEquals(AssemblyHandler.incrementHTag(line, 4), "<h5>Header 2</h5>");
		assertEquals(AssemblyHandler.incrementHTag(line, 5), "<h6>Header 2</h6>");

		assertEquals(AssemblyHandler.incrementHTag(line, -1), "<h0>Header 2</h0>");
	}

	@Test
	public void testMixedLine() {
		String line = "<h1>Header 1</h1><p>Body</p><h2>Header 2</h2>";
		
		assertEquals(AssemblyHandler.incrementHTag(line, 1), "<h2>Header 1</h2><p>Body</p><h3>Header 2</h3>");
	}

	public void testMixedIlligalLine() {
		String line = "<h1>Header 1</h2><p>Body</p><h4>Header 2</h2>";
		
		assertEquals(AssemblyHandler.incrementHTag(line, 2), "<h3>Header 1</h4><p>Body</p><h6>Header 2</h4>");
	}
}
