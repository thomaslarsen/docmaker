package net.toften.docmaker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import net.toften.docmaker.maven.DocMakerMojo;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SplitTOCHandler extends AbstractAssemblyHandler {
	
	private List<MetaSection> sections = new LinkedList<MetaSection>();
	private MetaSection currentSection;
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
	protected void handleMetaSectionElement(Attributes attributes)
			throws IOException {
		super.handleMetaSectionElement(attributes);
		
		currentSection = new MetaSection(getCurrentSectionName());
		sections.add(currentSection);
	}
	
	@Override
	protected void handleUnknownElement(DocPart dp, Attributes attributes) {
		if (dp == DocPart.PSECTION) {
			handlePseudoSectionElement(attributes);
		}
	}
	
	protected void handlePseudoSectionElement(Attributes attributes) {
		String pSectionHandlerClassname = attributes.getValue("classname");
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
		if (currentSection instanceof Section) {
			String fragmentAsHtml = super.getFragmentAsHTML(repoURI, fragmentName, 0);
			
			((Section)currentSection).addChapter(fragmentName, chapterLevelOffset, fragmentAsHtml);
		} else
			throw new IllegalStateException("Current section: " + currentSection.getSectionName() + " is not a standard section");
		
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
			for (MetaSection s : sections) {
				writeToOutputFile(DocPart.SECTION.preElement());
				if (s instanceof MetaSection) {
					// Meta section
					writeMetaSectionDivOpenTag(s.getSectionName());
					for (String[] e : s.getElements()) {
						writeElement(e[0], e[1]);
					}
				} else if (s instanceof Section) {
					writeStandardSectionDivOpenTag(s.getSectionName());
					writeToOutputFile(DocPart.CHAPTERS.preElement());
					for (Chapter c : ((Section)s).getChapters()) {
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
