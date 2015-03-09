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
import net.toften.docmaker.LogWrapper;
import net.toften.docmaker.output.InterimFileHandler;
import net.toften.docmaker.postprocessors.ApplyKeyValue;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.ElementsSection;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

public class SingleInterimFile implements InterimFileHandler {
	private File outputFile;
	private OutputStreamWriter htmlFile;
	private LogWrapper lw;
	
	@Override
	public void init(final File interimFileDir, final String filename, final String encodingString, LogWrapper lw) throws IOException {
		this.lw = lw;
		this.outputFile = new File(interimFileDir, filename + "." + getFileExtension());
		this.htmlFile = new OutputStreamWriter(new FileOutputStream(outputFile), Charset.forName(encodingString).newEncoder());

		lw.info("Initialised interim file: " + outputFile.getCanonicalPath() + " using encoding: " + encodingString);
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
	
	protected File buildInterimFile(File interimFileDir, String filename, String encoding, TOC t, LogWrapper lw) throws IOException, URISyntaxException {
		init(interimFileDir, filename, encoding, lw);
		
		Properties 							metaData 		= t.getMetaData();
		Map<String, Map<String, String>> 	htmlMeta 		= t.getHtmlMeta();
		List<GeneratedSection> 				headerSections 	= t.getHeaderSections();
		List<Section> 						sections 		= t.getSections();
		
		writeToOutputFile(DocPart.DOCUMENT.preElement());
		
		writeToOutputFile(DocPart.HEADER.preElement());
		writeToOutputFile("<title>" + ApplyKeyValue.processFragment(metaData, t.getDocumentTitle()) + "</title>\n");
		lw.debug("Writing " + htmlMeta.size() + " keys of metadata");
		for (String htmlHeadKey : htmlMeta.keySet()) {
			lw.debug("Writing key: " + htmlHeadKey);
			writeToOutputFile("<" + htmlHeadKey);
			for (Entry<String, String> metaAttr : htmlMeta.get(htmlHeadKey).entrySet()) {
				// Apply any potential property value to the metadata
				String value = ApplyKeyValue.processFragment(t.getMetaData(), metaAttr.getValue());
				writeToOutputFile(" " + metaAttr.getKey() + "=\"" + value + "\"");
			}
			writeToOutputFile(" />\n");
		}
		
		// Embed stylesheets
		for (String cssFile : t.getStyleSheets()) {
			URI cssURI = new URI(cssFile);
			if (!cssURI.isAbsolute()) {
				cssURI = t.getBaseURI().resolve(cssURI);
			}
			
			lw.debug("Writing CSS file: " + cssURI.toString());
			
			InputStream is = cssURI.toURL().openStream();
			String text = new Scanner(is, encoding).useDelimiter("\\A").next();
			// Apply any potential property value to the metadata
			text = ApplyKeyValue.processFragment(t.getMetaData(), text);
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
		lw.debug("Writing " + metaData.size() + " metadata records.");
		writeToOutputFile(DocPart.PROPERTIES.preElement());
		for (Map.Entry<Object, Object> m : metaData.entrySet()) {
			// Apply any potential property value to the metadata
			String key = m.getKey().toString();
			String value = ApplyKeyValue.processFragment(t.getMetaData(), m.getValue().toString());
			lw.debug("Writing metadata: " + key + " = " + value + " (" + m.getValue().toString() + ")");
			writeToOutputFile(DocPart.PROPERTY.preElement(new String[][]{{ "key", key}}, true));
			writeToOutputFile(value + "\n");
			writeToOutputFile(DocPart.PROPERTY.postElement());
		}
		writeToOutputFile(DocPart.PROPERTIES.postElement());

		for (Section section : sections) {
			lw.debug("Writing " + section.getDocPart().name() + " " + section.getSectionName() + " (" + section.getIdAttr(t) + ")");
			switch (section.getDocPart()) {
			case SECTION:
				writeToOutputFile(DocPart.SECTION.preElement());
				writeContentSection((ChapterSection)section, metaData, t);
				writeToOutputFile(DocPart.SECTION.postElement());
				break;

			case METASECTION:
				writeToOutputFile(DocPart.METASECTION.preElement());
				writeToOutputFile(section.getDivOpenTag(t));
				writeMetaElements((ElementsSection)section, metaData, t);
				writeToOutputFile(DocPart.METASECTION.postElement());
				writeToOutputFile(section.getDivCloseTag());
				break;
				
			case PSECTION:
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

	private void writeContentSection(ChapterSection section, Properties metaData, TOC t) throws IOException, URISyntaxException {
		writeToOutputFile(section.getDivOpenTag(t));
		writeToOutputFile(DocPart.CHAPTERS.preElement());
		for (Chapter c : section.getChapters()) {
			writeToOutputFile(DocPart.CHAPTER.preElement());
			lw.debug("Writing CHAPTER: " + c.getFragmentURI().toString() + " (" + c.getIdAttr(t) + ")");
			writeChapter(c, t);
			writeToOutputFile(DocPart.CHAPTER.postElement());
		}
		writeToOutputFile(DocPart.CHAPTERS.postElement());
		// A contents section might also contain elements
		writeMetaElements(section, metaData, t);
		
		writeToOutputFile(section.getDivCloseTag());
	}
	
	private void writeChapter(Chapter c, TOC t) throws IOException {
		writeToOutputFile(c.getDivOpenTag(t));
		String htmlFragment = c.getAsHtml();

		writeToOutputFile(htmlFragment);
		writeToOutputFile(c.getDivCloseTag());
	}

	private void writeMetaElements(ElementsSection section, Properties metaData, TOC t) throws IOException {
		for (String[] e : section.getElements()) {
			writeToOutputFile(DocPart.ELEMENT.preElement());
			writeToOutputFile("<div key=\"" + e[0] + "\">" + ApplyKeyValue.processFragment(metaData, e[1]) + "</div>\n");
			writeToOutputFile(DocPart.ELEMENT.postElement());
		}
	}
	
	private void writePseudoSection(GeneratedSection section, TOC t) throws IOException {
		writeToOutputFile(section.getDivOpenTag(t));
		writeToOutputFile(section.getAsHtml(t));
		writeToOutputFile(section.getDivCloseTag());
	}
}
