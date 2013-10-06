package net.toften.docmaker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SplitTOCHandler extends AssemblyAndProcessHandler {
	
	private List<Section> sections = new LinkedList<Section>();
	private Section currentSection;
	private boolean writeToOutput = false;

	@Override
	public void close() throws IOException {
		// Trap this, so we don't prematurely close the file
		if (writeToOutput)
			super.close();
	}
	
	@Override
	public void writeToOutputFile(String text) throws IOException {
		if (writeToOutput && text != null)
			super.writeToOutputFile(text);
	}
	
	@Override
	protected void handleSectionElement(Attributes attributes) throws IOException {
		super.handleSectionElement(attributes);
		
		currentSection = new Section(getCurrentSectionName(), getCurrentSectionLevel());
		sections.add(currentSection);
	}
	
	@Override
	protected void handleElementElement(Attributes attributes)
			throws IOException {
		super.handleElementElement(attributes);
		
		String key = attributes.getValue("key");
		if (metaData.containsKey(key)) {
			currentSection.addElement(key, metaData.get(key));
		}
	}
	
	@Override
	protected String getFragmentAsHTML(URI repoURI, String fragmentName, int chapterLevelOffset) throws IOException, URISyntaxException {
		String fragmentAsHtml = super.getFragmentAsHTML(repoURI, fragmentName, 0);
		
		currentSection.addChapter(fragmentName, chapterLevelOffset, fragmentAsHtml);
		
		// We return null as we don't want to write anything to the file as yet
		return null;
	}
	
	@Override
	public void endDocument() throws SAXException {
		/*
		 * We trap this method as an indication the TOC has now been fully processed.
		 */
		/*
		 * Now we assemble and write the output HTML doc
		 */
		writeToOutput = true;
		try {
			// Start the document
			writeToOutputFile(DocPart.DOCUMENT.preElement());
			
			// Header section
			writeToOutputFile(DocPart.HEADER.preElement());
			writeTitleElement();
			writeCSSElement();
			writeToOutputFile(DocPart.HEADER.postElement());

			// Metadata
			writeToOutputFile(DocPart.SECTIONS.preElement());
			writeMetadataElement();
			
			// Sections
			for (Section s : sections) {
				writeToOutputFile(DocPart.SECTION.preElement());
				if (s.getSectionLevel() == null) {
					// Meta section
					writeMetaSectionDivOpenTag(s.getSectionName());
					for (String[] e : s.getElements()) {
						writeElement(e[0], e[1]);
					}
				} else {
					writeStandardSectionDivOpenTag(s.getSectionName());
					writeToOutputFile(DocPart.CHAPTERS.preElement());
					for (Chapter c : s.getChapters()) {
						writeToOutputFile(DocPart.CHAPTER.preElement());
						writeChapterDivOpenTag(s.getSectionName(), c.getFragmentName());
						writeToOutputFile(AbstractAssemblyHandler.incrementHTag(c.getFragmentAsHtml(), c.getChapterLevelOffset()));
						writeDivCloseTag();
						writeToOutputFile(DocPart.CHAPTER.postElement());
					}
					writeDivCloseTag();
				}
				writeDivCloseTag();
				writeToOutputFile(DocPart.SECTION.postElement());
			}
			
			// All the post tags
			writeToOutputFile(DocPart.SECTIONS.postElement());
			writeToOutputFile(DocPart.DOCUMENT.postElement());
			
		} catch (IOException e) {
			throw new SAXException("Can not write output file", e);
		} finally {
			super.endDocument();
		}
	}
}
