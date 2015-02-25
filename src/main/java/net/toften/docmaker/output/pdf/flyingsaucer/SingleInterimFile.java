package net.toften.docmaker.output.pdf.flyingsaucer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.handler.AssemblyHandlerAdapter;
import net.toften.docmaker.handler.InterimFileHandler;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.ElementsSection;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

public class SingleInterimFile implements InterimFileHandler {
	private File outputFile;
	private OutputStreamWriter htmlFile;
	
	@Override
	public void init(final File interimFileDir, final String filename, final String encodingString) throws IOException {
		this.outputFile = new File(interimFileDir, filename + "." + getFileExtension());
		this.htmlFile = new OutputStreamWriter(new FileOutputStream(outputFile), Charset.forName(encodingString).newEncoder());
	}

	@Override
	public void close() throws IOException {
		try {
			htmlFile.flush();
		} finally {
			htmlFile.close();
		}
	}

	@Override
	public void writeToOutputFile(String text) throws IOException {
		if (text != null)
			htmlFile.write(text);
	}

	@Override
	public String getFileExtension() {
		return "html";
	}
	
	protected File buildInterimFile(File interimFileDir, String filename, String encoding, TOC t) throws IOException, URISyntaxException {
		init(interimFileDir, filename, encoding);
		
		Properties metaData = t.getMetaData();
		Map<String, Map<String, String>> htmlMeta = t.getHtmlMeta();
		List<GeneratedSection> headerSections = t.getHeaderSections();
		List<Section> sections = t.getSections();
		
		writeToOutputFile(DocPart.DOCUMENT.preElement());
		
		writeToOutputFile(DocPart.HEADER.preElement());
		writeToOutputFile("<title>" + metaData.get(AssemblyHandlerAdapter.HEADER_TITLE) + "</title>\n");
		for (String htmlHeadKey : htmlMeta.keySet()) {
			writeToOutputFile("<" + htmlHeadKey);
			for (Entry<String, String> metaAttr : htmlMeta.get(htmlHeadKey).entrySet()) {
				writeToOutputFile(" " + metaAttr.getKey() + "=\"" + metaAttr.getValue() + "\"");
			}
			writeToOutputFile(" />\n");
		}
		
		// Embed stylesheets
		for (String cssFile : t.getStyleSheets()) {
			URI cssURI = new URI(cssFile);
			if (!cssURI.isAbsolute()) {
				cssURI = t.getBaseURI().resolve(cssURI);
			}
			
			InputStream is = cssURI.toURL().openStream();
			String text = new Scanner(is, encoding).useDelimiter("\\A").next();
			writeToOutputFile("<style>\n");
			writeToOutputFile(text + "\n");
			writeToOutputFile("</style>\n");
		}
		
		for (GeneratedSection section : headerSections) {
			writeToOutputFile(DocPart.HSECTION.preElement());
			writeToOutputFile(section.getDivOpenTag(t));
			writeToOutputFile(section.getAsHtml(t));
			writeToOutputFile(section.getDivCloseTag());
			writeToOutputFile(DocPart.HSECTION.postElement());
		}
		writeToOutputFile(DocPart.HEADER.postElement());
		
		writeToOutputFile(DocPart.SECTIONS.preElement());
		
		// Write document metadata
		writeToOutputFile("<div class=\"metadata\">\n");
		for (Map.Entry<Object, Object> m : metaData.entrySet()) {
			writeToOutputFile("<div class=\"meta\" key=\"" + m.getKey().toString() + "\">" + m.getValue().toString() + "</div>\n");
		}
		writeToOutputFile("</div>\n");

		for (Section section : sections) {
			switch (section.getSectionType()) {
			case CONTENTS_SECTION:
				writeToOutputFile(DocPart.SECTION.preElement());
				writeContentSection((ChapterSection)section, t);
				writeToOutputFile(DocPart.SECTION.postElement());
				break;

			case META_SECTION:
				writeToOutputFile(DocPart.METASECTION.preElement());
				writeToOutputFile(section.getDivOpenTag(t));
				writeMetaElements((ElementsSection)section, t);
				writeToOutputFile(DocPart.METASECTION.postElement());
				writeToOutputFile(section.getDivCloseTag());
				break;
				
			case PSEUDO_SECTION:
				writeToOutputFile(DocPart.PSECTION.preElement());
				writePseudoSection((GeneratedSection)section, t);
				writeToOutputFile(DocPart.PSECTION.postElement());
				break;
			}
		}
		writeToOutputFile(DocPart.SECTIONS.postElement());
		
		writeToOutputFile(DocPart.DOCUMENT.postElement());
		
		close();
		
		return outputFile;
	}

	private void writeContentSection(ChapterSection section, TOC t) throws IOException {
		writeToOutputFile(section.getDivOpenTag(t));
		writeToOutputFile(DocPart.CHAPTERS.preElement());
		for (Chapter c : section.getChapters()) {
			writeToOutputFile(DocPart.CHAPTER.preElement());
			writeChapter(c, t);
			writeToOutputFile(DocPart.CHAPTER.postElement());
		}
		writeToOutputFile(DocPart.CHAPTERS.postElement());
		// A contents section might also contain elements
		writeMetaElements(section, t);
		
		writeToOutputFile(section.getDivCloseTag());
	}
	
	private void writeChapter(Chapter c, TOC t) throws IOException {
		writeToOutputFile(c.getDivOpenTag(t));
		String htmlFragment = c.getAsHtml();

		writeToOutputFile(htmlFragment);
		writeToOutputFile(c.getDivCloseTag());
	}

	private void writeMetaElements(ElementsSection section, TOC t) throws IOException {
		for (String[] e : section.getElements()) {
			writeToOutputFile(DocPart.ELEMENT.preElement());
			writeToOutputFile("<div key=\"" + e[0] + "\">" + e[1] + "</div>\n");
			writeToOutputFile(DocPart.ELEMENT.postElement());
		}
	}
	
	private void writePseudoSection(GeneratedSection section, TOC t) throws IOException {
		writeToOutputFile(section.getDivOpenTag(t));
		writeToOutputFile(section.getAsHtml(t));
		writeToOutputFile(section.getDivCloseTag());
	}

}
