package net.toften.docmaker;

import org.junit.Test;
import static org.junit.Assert.*;

public class InjectHeaderIdTest {
		
	private String tocFileName = "toc",
				repoName = "repo",
				sectionName = "test",
				fragmentName = "intro";
	@Test
	public void testInjectSingle() {
		String htmlFragment = "<h1>Header</h1>";
		
		String i = DefaultAssemblyHandler.injectHeaderIdAttributes(htmlFragment, tocFileName, repoName, sectionName, fragmentName);
		
		assertEquals("<h1 id=\"toc-test-intro-header\">Header</h1>", i);
	}
	
	@Test
	public void testInjectMultiple() {
		String htmlFragment = "<h1>Header</h1><h2>Header 2</h2>";
		
		String i = DefaultAssemblyHandler.injectHeaderIdAttributes(htmlFragment, tocFileName, repoName, sectionName, fragmentName);
		
		assertEquals("<h1 id=\"toc-test-intro-header\">Header</h1><h2 id=\"toc-test-intro-header-2\">Header 2</h2>", i);
	}
}
