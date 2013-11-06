package net.toften.docmaker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SplitTOCHandler extends DefaultAssemblyHandler {
	
	private List<BaseSection> sections = new LinkedList<BaseSection>();
	private BaseSection currentSection;
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
	protected void handleUnknownElement(DocPart dp, Attributes attributes) throws Exception {
		if (dp == DocPart.PSECTION) {
			handlePseudoSectionElement(attributes);
		}
	}
	
	protected void handlePseudoSectionElement(Attributes attributes) throws Exception {
		setCurrentSectionName(attributes.getValue("title"));
		String pSectionHandlerClassname = attributes.getValue("classname");
		
		currentSection = new PseudoSection(getCurrentSectionName(), pSectionHandlerClassname, attributes);
		sections.add(currentSection);
	}

	@Override
	protected void handleElementElement(Attributes attributes)
			throws IOException {
		if (currentSection instanceof MetaSection) {
			super.handleElementElement(attributes);
			
			String key = attributes.getValue("key");
			if (metaData.containsKey(key)) {
				((MetaSection)currentSection).addElement(key, metaData.get(key));
			}
		}
	}
	
	@Override
	protected String getFragmentAsHTML(String repoName, String fragmentName, int chapterLevelOffset) throws IOException, URISyntaxException {
		if (currentSection instanceof Section) {
			String fragmentAsHtml = super.getFragmentAsHTML(repoName, fragmentName, 0);
			
			((Section)currentSection).addChapter(fragmentName, repoName, chapterLevelOffset, fragmentAsHtml);
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
			for (BaseSection s : sections) {
				writeToOutputFile(DocPart.SECTION.preElement());
				
				if (s instanceof Section) {
					writeStandardSectionDivOpenTag(s.getSectionName());
					writeToOutputFile(DocPart.CHAPTERS.preElement());
					
					for (Chapter c : ((Section)s).getChapters()) {
						writeToOutputFile(DocPart.CHAPTER.preElement());
						
						writeChapterDivOpenTag(s.getSectionName(), c.getFragmentName(), c.getRepoName());
						String htmlFragment = c.getFragmentAsHtml();
						htmlFragment = DefaultAssemblyHandler.incrementHTag(htmlFragment, calcEffectiveLevel(((Section) s).getSectionLevel(), c.getChapterLevelOffset()));
						htmlFragment = DefaultAssemblyHandler.injectHeaderIdAttributes(htmlFragment, getTocFileName(), c.getRepoName(), s.getSectionName(), c.getFragmentName());
						writeToOutputFile(htmlFragment);
						writeDivCloseTag();
						
						writeToOutputFile(DocPart.CHAPTER.postElement());
					}
					
					writeToOutputFile(DocPart.CHAPTERS.postElement());
					writeDivCloseTag();
				} else if (s instanceof PseudoSection) {
					writePseudoSectionDivOpenTag(s.getSectionName());
					writeToOutputFile(((PseudoSection)s).getSectionHandler().getSectionAsHtml(sections, this));
					writeDivCloseTag();
				} else if (s instanceof MetaSection) {
					// Meta section
					writeMetaSectionDivOpenTag(s.getSectionName());
					for (String[] e : ((MetaSection)s).getElements()) {
						writeElement(e[0], e[1]);
					}
					writeDivCloseTag();
				}
				
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

	private void writePseudoSectionDivOpenTag(String sectionName) throws IOException {
		writeDivOpenTag("pseudo-section", (getTocFileName() + "-" + sectionName).toLowerCase().replace(' ', '-'), sectionName);
	}
}
