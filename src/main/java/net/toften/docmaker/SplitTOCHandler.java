package net.toften.docmaker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SplitTOCHandler extends AssemblyAndProcessHandler {
	
	public class Chapter {
		private String fragmentName;
		private int chapterLevelOffset;
		private String fragmentAsHtml;

		public Chapter(String fragmentName, int chapterLevelOffset,
				String fragmentAsHtml) {
			this.fragmentName = fragmentName;
			this.chapterLevelOffset = chapterLevelOffset;
			this.fragmentAsHtml = fragmentAsHtml;
		}
	}

	public class Section {
		private LinkedList<Chapter> chapters = new LinkedList<Chapter>();
		private String sectionName;
		private Integer sectionLevel;

		public Section(String sectionName, Integer sectionLevel) {
			sections.add(this);
			
			this.sectionName = sectionName;
			this.sectionLevel = sectionLevel;
		}

		public void addChapter(String fragmentName, int chapterLevelOffset, String fragmentAsHtml) {
			chapters.add(new Chapter(fragmentName, chapterLevelOffset, fragmentAsHtml));
		}
	}

	private LinkedList<Section> sections = new LinkedList<Section>();
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
				if (s.sectionLevel == null) {
					// Meta section
					writeMetaSectionDivOpenTag(s.sectionName);
				} else {
					writeStandardSectionDivOpenTag(s.sectionName);
					for (Chapter c : s.chapters) {
						writeToOutputFile(DocPart.CHAPTER.preElement());
						writeChapterDivOpenTag(s.sectionName, c.fragmentName);
						writeToOutputFile(AbstractAssemblyHandler.incrementHTag(c.fragmentAsHtml, c.chapterLevelOffset));
						writeDivCloseTag();
						writeToOutputFile(DocPart.CHAPTER.postElement());
					}
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
