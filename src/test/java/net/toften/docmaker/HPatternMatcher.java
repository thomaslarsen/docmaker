package net.toften.docmaker;

import static org.junit.Assert.*;

import org.junit.Test;

public class HPatternMatcher {

	@Test
	public void test() {
		String line = "<h2>This is a heading";
		
		String newLine = AssemblyHandler.replaceHTag(line, 2);
		assertEquals("<h4>This is a heading", newLine);
	}

	@Test
	public void test2() {
		String line = "<h2>This is a heading</h2>";
		
		String newLine = AssemblyHandler.replaceHTag(line, 2);
		assertEquals("<h4>This is a heading</h4>", newLine);
	}

	@Test
	public void test3() {
		String line = "</h2>This is a heading";
		
		String newLine = AssemblyHandler.replaceHTag(line, 2);
		assertEquals("</h4>This is a heading", newLine);
	}
}
