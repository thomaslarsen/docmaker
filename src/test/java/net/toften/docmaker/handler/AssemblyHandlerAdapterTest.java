package net.toften.docmaker.handler;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

import org.junit.Test;
import org.xml.sax.Attributes;

public class AssemblyHandlerAdapterTest {
	String 	tocName = "test", 
			defaultExtension = "md";
	URI		baseURI = new File("").toURI(); 
	Map<String, MarkupProcessor> markupProcessor = new HashMap<String, MarkupProcessor>(); 
	Properties baseProperties = null; 
	List<String> cssFiles = null;
	
	private class TestHandler extends AssemblyHandlerAdapter {
		
		@Override
		public List<Section> getSections() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<GeneratedSection> getHeaderSections() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected void handlePseudoSection(Attributes attributes) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		protected void handleMetaSectionElement(Attributes attributes)
				throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		protected void handleElementElement(Attributes attributes) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		protected void handleContentSectionElement(Attributes attributes)
				throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		protected void handleChapterElement(Attributes attributes) throws Exception {
			// TODO Auto-generated method stub
			
		}
	}
	
	@Test
	public void testProperty() throws Exception {
		AssemblyHandler handler = new TestHandler();
		
		String toc =
				"<document>"
				+ "<properties>"
				+ "<property key=\"key\" value=\"value\" />"
				+ "</properties>"
				+ "</document>";
		InputStream is = new ByteArrayInputStream(toc.getBytes(StandardCharsets.UTF_8));
		
		TOC t = handler.parse(is, tocName, defaultExtension, baseURI, markupProcessor, baseProperties, cssFiles);
		
		assertEquals(1, t.getMetaData().size());
		assertTrue(t.getMetaData().containsKey("key"));
		assertEquals("value", t.getMetaData().getProperty("key"));
	}
	
	@Test
	public void testPropertyFile() throws Exception {
		AssemblyHandler handler = new TestHandler();
		
		String toc =
				"<document>"
				+ "<properties>"
				+ "<property src=\"./src/test/resources/proptest.properties\" />"
				+ "</properties>"
				+ "</document>";
		InputStream is = new ByteArrayInputStream(toc.getBytes(StandardCharsets.UTF_8));
		
		TOC t = handler.parse(is, tocName, defaultExtension, baseURI, markupProcessor, baseProperties, cssFiles);
		
		assertEquals(1, t.getMetaData().size());
		assertTrue(t.getMetaData().containsKey("key"));
		assertEquals("value", t.getMetaData().getProperty("key"));
	}
	
	@Test
	public void testPropertyFileAndElement() throws Exception {
		AssemblyHandler handler = new TestHandler();
		
		String toc =
				"<document>"
				+ "<properties>"
				+ "<property src=\"./src/test/resources/proptest.properties\" />"
				+ "<property key=\"key2\" value=\"value2\" />"
				+ "</properties>"
				+ "</document>";
		InputStream is = new ByteArrayInputStream(toc.getBytes(StandardCharsets.UTF_8));
		
		TOC t = handler.parse(is, tocName, defaultExtension, baseURI, markupProcessor, baseProperties, cssFiles);
		
		assertEquals(2, t.getMetaData().size());
		assertTrue(t.getMetaData().containsKey("key"));
		assertEquals("value", t.getMetaData().getProperty("key"));
		assertTrue(t.getMetaData().containsKey("key2"));
		assertEquals("value2", t.getMetaData().getProperty("key2"));
	}
	
	@Test
	public void testSection() throws Exception {
		AssemblyHandler handler = new TestHandler() {
			@Override
			protected void handleContentSectionElement(Attributes attributes)
					throws Exception {
				super.handleContentSectionElement(attributes);
				
				assertNotNull(attributes.getValue("title"));
				assertEquals("test", attributes.getValue("title"));
			}
		};
		
		String toc =
				"<document>"
				+ "<section title=\"test\">"
				+ "</section>"
				+ "</document>";
		InputStream is = new ByteArrayInputStream(toc.getBytes(StandardCharsets.UTF_8));
		
		TOC t = handler.parse(is, tocName, defaultExtension, baseURI, markupProcessor, baseProperties, cssFiles);
	}
}
