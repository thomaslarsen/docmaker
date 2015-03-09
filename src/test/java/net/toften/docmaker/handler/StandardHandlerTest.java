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

import net.toften.docmaker.handler.standard.StandardHandler;
import net.toften.docmaker.markup.MarkupProcessor;
import net.toften.docmaker.markup.markdown.pegdown.PegdownProcessor;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.SectionType;
import net.toften.docmaker.toc.TOC;

import org.junit.Test;

public class StandardHandlerTest {
	String 	tocName = "test", 
			defaultExtension = "md";
	URI		baseURI = new File(".").toURI(); 
	Map<String, MarkupProcessor> markupProcessor = null; 
	Properties baseProperties = null; 
	List<String> cssFiles = null;

	@Test
	public void testSectionNoChapters() throws Exception {
		AssemblyHandler handler = new StandardHandler();
		
		String toc =
				"<document>"
				+ "<section title=\"S1\" level=\"2\">"
				+ "</section>"
				+ "</document>";
		InputStream is = new ByteArrayInputStream(toc.getBytes(StandardCharsets.UTF_8));
		
		TOC t = handler.parse(is, tocName, defaultExtension, baseURI, markupProcessor, baseProperties, cssFiles);
		
		assertEquals(1, t.getSections().size());
		Section s = t.getSections().get(0);
		assertEquals("S1", s.getSectionName());
		assertEquals(SectionType.CONTENTS_SECTION, s.getSectionType());
		assertFalse(s.isRotated());
		
		ChapterSection cs = (ChapterSection)s;
		assertEquals(0, cs.getChapters().size());
		assertEquals(2, cs.getSectionLevel().intValue());
	}

	@Test
	public void testSectionOneChapter() throws Exception {
		AssemblyHandler handler = new StandardHandler();
		
		markupProcessor = new HashMap<String, MarkupProcessor>();
		MarkupProcessor mdProcessor = new PegdownProcessor();
		mdProcessor.setEncoding("UTF-8");
		markupProcessor.put("md", mdProcessor);
		
		String toc =
				"<document>"
				+ "<repos>"
				+ "<repo id=\"common\" uri=\"src/test/resources/sample/sections/common/\" />"
				+ "</repos>"
				+ "<section title=\"S1\" level=\"2\">"
				+ "<chapters>"
				+ "<chapter repo=\"common\" fragment=\"intro\" level=\"1\" />"
				+ "</chapters>"
				+ "</section>"
				+ "</document>";
		InputStream is = new ByteArrayInputStream(toc.getBytes(StandardCharsets.UTF_8));
		
		TOC t = handler.parse(is, tocName, defaultExtension, baseURI, markupProcessor, baseProperties, cssFiles);
		
		assertEquals(1, t.getSections().size());
		Section s = t.getSections().get(0);
		assertEquals("S1", s.getSectionName());
		assertEquals(SectionType.CONTENTS_SECTION, s.getSectionType());
		assertFalse(s.isRotated());
		
		ChapterSection cs = (ChapterSection)s;
		assertEquals(1, cs.getChapters().size());
		assertEquals(2, cs.getSectionLevel().intValue());
		
		Chapter c = cs.getChapters().get(0);
		assertEquals("intro", c.getFragmentName());
		assertEquals(1, c.getChapterLevelOffset());
		assertEquals(1, c.calcEffectiveLevel());
		assertFalse(c.isRotated());
	}

	@Test
	public void testSectionMultipleChapters() throws Exception {
		AssemblyHandler handler = new StandardHandler();
		
		markupProcessor = new HashMap<String, MarkupProcessor>();
		MarkupProcessor mdProcessor = new PegdownProcessor();
		mdProcessor.setEncoding("UTF-8");
		markupProcessor.put("md", mdProcessor);
		
		String toc =
				"<document>"
				+ "<header title=\"Doc\">"
				+ "</header>"
				+ "<repos>"
				+ "<repo id=\"common\" uri=\"src/test/resources/sample/sections/common/\" />"
				+ "</repos>"
				+ "<section title=\"S1\" level=\"2\">"
				+ "<chapters>"
				+ "<chapter repo=\"common\" fragment=\"intro\" level=\"1\" />"
				+ "<chapter repo=\"common\" fragment=\"copyright\" level=\"2\" rotate=\"true\" />"
				+ "</chapters>"
				+ "</section>"
				+ "</document>";
		InputStream is = new ByteArrayInputStream(toc.getBytes(StandardCharsets.UTF_8));
		
		TOC t = handler.parse(is, tocName, defaultExtension, baseURI, markupProcessor, baseProperties, cssFiles);
		assertEquals("Doc", t.getDocumentTitle());
		assertEquals(tocName, t.getTocFileName());
		assertEquals(0, t.getHeaderSections().size());
		assertEquals(0, t.getHtmlMeta().size());
		assertEquals(1, t.getMetaData().size());
		assertEquals("Doc", t.getMetaData().getProperty(AssemblyHandlerAdapter.HEADER_TITLE));
		assertNull(t.getStyleSheets());
		
		assertEquals(1, t.getSections().size());
		Section s = t.getSections().get(0);
		assertEquals("S1", s.getSectionName());
		assertEquals(SectionType.CONTENTS_SECTION, s.getSectionType());
		assertFalse(s.isRotated());
		assertEquals("<div class=\"section-header\" id=\"test-s1\" title=\"S1\">\n", s.getDivOpenTag(t));

		ChapterSection cs = (ChapterSection)s;
		assertEquals(2, cs.getChapters().size());
		assertEquals(2, cs.getSectionLevel().intValue());
		
		Chapter c = cs.getChapters().get(0);
		assertEquals("intro", c.getFragmentName());
		assertEquals(1, c.getChapterLevelOffset());
		assertEquals(1, c.calcEffectiveLevel());
		assertFalse(c.isRotated());
		assertEquals("<div class=\"chapter\" id=\"test-s1-intro\" title=\"intro\">\n", c.getDivOpenTag(t));
		
		c = cs.getChapters().get(1);
		assertEquals("copyright", c.getFragmentName());
		assertEquals(2, c.getChapterLevelOffset());
		assertEquals(2, c.calcEffectiveLevel());
		assertTrue(c.isRotated());
		assertEquals("<div class=\"chapter rotate\" id=\"test-s1-copyright\" title=\"copyright\">\n", c.getDivOpenTag(t));
	}
}
