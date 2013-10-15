package net.toften.docmaker;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class TOCSectionTest {
	private DefaultAssemblyHandler ah;

	@Before
	public void setupAssemblyHandler() {
		ah = new DefaultAssemblyHandler() {
			@Override
			protected String getCurrentRepoName() {
				return "repo";
			}
			
			@Override
			protected String getTocFileName() {
				return "TOC";
			}
		};
	}
	
	@Test
	public void testSection() throws IOException {
		ah.setCurrentFileHandler(new DefaultAssemblyHandler.GenericFileHandler() {
			@Override
			public void writeToOutputFile(String text) throws IOException {
				System.out.println(text);
			}
		});
		
		Attributes attributes = new AttributesImpl() {
			@Override
			public String getValue(String qName) {
				if (qName.equals("title"))
					return "test";
				
				if (qName.equals("level"))
					return "2";
				
				return null;
			}
		};
		
		ah.handleSectionElement(attributes);
	}
}
