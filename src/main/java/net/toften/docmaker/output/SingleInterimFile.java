package net.toften.docmaker.output;

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
import java.util.logging.Logger;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.postprocessors.ApplyKeyValue;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.ElementsSection;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

public class SingleInterimFile implements InterimFileHandler {
	private static Logger lw = Logger.getLogger(SingleInterimFile.class.getName());	

	private File outputFile;
	private OutputStreamWriter htmlFile;
	
	@Override
	public void init(final File interimFileDir, final String filename, final String encodingString) throws IOException {
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
	
	protected File buildInterimFile(File interimFileDir, String filename, String encoding, TOC t) throws IOException, URISyntaxException {
		init(interimFileDir, filename, encoding);
		
		Properties 							metaData 		= t.getMetaData();
		Map<String, Map<String, String>> 	htmlMeta 		= t.getHtmlMeta();
		List<GeneratedSection> 				headerSections 	= t.getHeaderSections();
		List<Section> 						sections 		= t.getSections();
		
		writeToOutputFile(DocPart.DOCUMENT.preElement());
		
		writeToOutputFile(DocPart.HEADER.preElement());
		writeToOutputFile("<title>" + ApplyKeyValue.resolve(metaData, t.getDocumentTitle()) + "</title>\n");
		lw.fine("Writing " + htmlMeta.size() + " keys of metadata");
		for (String htmlHeadKey : htmlMeta.keySet()) {
			lw.fine("Writing key: " + htmlHeadKey);
			writeToOutputFile("<" + htmlHeadKey);
			for (Entry<String, String> metaAttr : htmlMeta.get(htmlHeadKey).entrySet()) {
				// Apply any potential property value to the metadata
				String value = ApplyKeyValue.resolve(t.getMetaData(), metaAttr.getValue());
				writeToOutputFile(" " + metaAttr.getKey() + "=\"" + value + "\"");
			}
			writeToOutputFile(" />\n");
		}
		
		// Embed stylesheets
		if (t.getStyleSheets() != null) {
			for (String cssFile : t.getStyleSheets()) {
				URI cssURI = new URI(cssFile);
				if (!cssURI.isAbsolute()) {
					cssURI = t.getBaseURI().resolve(cssURI);
				}
				
				lw.fine("Writing CSS file: " + cssURI.toString());
				
				InputStream is = cssURI.toURL().openStream();
				String text = new Scanner(is, encoding).useDelimiter("\\A").next();
				// Apply any potential property value to the metadata
				text = ApplyKeyValue.resolve(t.getMetaData(), text);
				writeToOutputFile("<style>\n");
				writeToOutputFile(text + "\n");
				writeToOutputFile("</style>\n");
			}
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
		lw.fine("Writing " + metaData.size() + " metadata records.");
		writeToOutputFile(DocPart.PROPERTIES.preElement());
		for (Map.Entry<Object, Object> m : metaData.entrySet()) {
			// Apply any potential property value to the metadata
			String key = m.getKey().toString();
			String value = ApplyKeyValue.resolve(t.getMetaData(), m.getValue().toString());
			lw.fine("Writing metadata: " + key + " = " + value + " (" + m.getValue().toString() + ")");
			writeToOutputFile(DocPart.PROPERTY.preElement(new String[][]{{ "key", key}}, true));
			writeToOutputFile(value + "\n");
			writeToOutputFile(DocPart.PROPERTY.postElement());
		}
		writeToOutputFile(DocPart.PROPERTIES.postElement());

		for (Section section : sections) {
			lw.fine("Writing " + section.getDocPart().name() + " " + section.getName() + " (" + section.getIdAttr(t) + ")");
			writeToOutputFile(section.getDocPart().preElement());
			writeToOutputFile(section.getDivOpenTag(t));
			switch (section.getDocPart()) {
			case SECTION:
				writeContentSection((ChapterSection)section, metaData, t);
				break;

			case METASECTION:
				writeMetaElements((ElementsSection)section, metaData, t);
				break;
				
			case PSECTION:
				writePseudoSection((GeneratedSection)section, t);
				break;
			}
			writeToOutputFile(section.getDivCloseTag());
			writeToOutputFile(section.getDocPart().postElement());
		}
		writeToOutputFile(DocPart.SECTIONS.postElement());
		
		writeToOutputFile(DocPart.DOCUMENT.postElement());
		
		close();
		
		return outputFile;
	}

	private void writeContentSection(ChapterSection section, Properties metaData, TOC t) throws IOException, URISyntaxException {
		writeToOutputFile(DocPart.CHAPTERS.preElement());
		for (Chapter c : section.getChapters()) {
			lw.fine("Writing CHAPTER: " + c.getFragmentURI().toString() + " (" + c.getIdAttr(t) + ")");
			writeToOutputFile(DocPart.CHAPTER.preElement());
			
			writeToOutputFile(c.getDivOpenTag(t));
			writeToOutputFile(c.getAsHtml(t));
			writeToOutputFile(c.getDivCloseTag());
			
			writeToOutputFile(DocPart.CHAPTER.postElement());
		}
		writeToOutputFile(DocPart.CHAPTERS.postElement());
		// A contents section might also contain elements
		writeMetaElements(section, metaData, t);
	}

	private void writeMetaElements(ElementsSection section, Properties metaData, TOC t) throws IOException {
		for (String[] e : section.getElements()) {
			writeToOutputFile(DocPart.ELEMENT.preElement());
			writeToOutputFile("<div key=\"" + e[0] + "\">" + ApplyKeyValue.resolve(metaData, e[1]) + "</div>\n");
			writeToOutputFile(DocPart.ELEMENT.postElement());
		}
	}
	
	private void writePseudoSection(GeneratedSection section, TOC t) throws IOException {
		writeToOutputFile(section.getAsHtml(t));
	}
}
