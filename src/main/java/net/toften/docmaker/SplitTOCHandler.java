package net.toften.docmaker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.toften.docmaker.maven.DocMakerMojo;
import net.toften.docmaker.pseudosections.HeaderIncrementPostProcessor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SplitTOCHandler extends DefaultAssemblyHandler {
	
	private List<BaseSection> sections = new LinkedList<BaseSection>();
	private List<PostProcessor> postProcessors = new LinkedList<PostProcessor>();
	private BaseSection currentSection;
	private boolean writeToOutput = false;
	private List<Map<String, String>> htmlMeta = new LinkedList<Map<String, String>>();

	public SplitTOCHandler() {
		postProcessors.add(new HeaderIncrementPostProcessor());
		postProcessors.add(new InjectHeaderIdPostProcessor());
	}
	
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
	protected void handleSectionElement(Attributes attributes) throws IOException, SAXException {
		super.handleSectionElement(attributes);
		
		// if the current section level is null, then this is a meta section and
		// should then *not* be added as a "normal" section
		if (getCurrentSectionLevel() != null) {
			currentSection = new Section(getCurrentSectionName(), getCurrentSectionLevel(), isRotateCurrentSection());
			sections.add(currentSection);
		}
	}
	
	@Override
	protected void handleMetaSectionElement(Attributes attributes)
			throws IOException, SAXException {
		super.handleMetaSectionElement(attributes);
		
		currentSection = new MetaSection(getCurrentSectionName(), isRotateCurrentSection());
		sections.add(currentSection);
	}
	
	@Override
	protected void handleUnknownElement(DocPart dp, Attributes attributes) throws Exception {
		if (dp == DocPart.PSECTION) {
			handlePseudoSectionElement(attributes);
		} else if (dp == DocPart.PPROCESSOR) {
			handlePostProcessorElement(attributes);
		}
	}
	
	private void handlePostProcessorElement(Attributes attributes) throws Exception {
		String postProcessorHandlerClassname = attributes.getValue("classname");
		
		postProcessors.add(DocMakerMojo.newInstance(PostProcessor.class, postProcessorHandlerClassname));
	}

	protected void handlePseudoSectionElement(Attributes attributes) throws Exception {
		setCurrentSectionName(attributes.getValue("title"));
		String pSectionHandlerClassname = attributes.getValue("classname");
		boolean rotateCurrentSection = attributes.getValue("rotate") != null;
		
		currentSection = new PseudoSection(getCurrentSectionName(), pSectionHandlerClassname, attributes, rotateCurrentSection);
		sections.add(currentSection);
	}

	@Override
	protected void handleElementElement(Attributes attributes) throws IOException, SAXException {
		if (currentSection instanceof MetaSection) {
			super.handleElementElement(attributes);
			
			String key = attributes.getValue("key");
			if (metaData.containsKey(key)) {
				((MetaSection)currentSection).addElement(key, metaData.get(key));
			}
		}
	}
	
	@Override
	protected void handleMetaElement(String metaName, Attributes attributes) throws IOException, SAXException {
		Map<String, String> meta = new HashMap<String, String>();
		
		for (int i = 0; i < attributes.getLength(); i++) {
			meta.put(attributes.getQName(i), attributes.getValue(i));
		}
		
		htmlMeta.add(meta);
	}
	
	@Override
	protected String getFragmentAsHTML(String repoName, String fragmentName, int chapterLevelOffset) throws IOException, URISyntaxException {
		if (currentSection instanceof Section) {
			// Deliberately using '0' as the offset
			String fragmentAsHtml = super.getFragmentAsHTML(repoName, fragmentName, 0);
			
			((Section)currentSection).addChapter(fragmentName, repoName, chapterLevelOffset, fragmentAsHtml, isRotateCurrentChapter());
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
			
			/*
			 * Write HTML meta tags
			 */
			for (Map<String, String> meta : htmlMeta) {
				writeToOutputFile("<meta ");

				for (String key : meta.keySet()) {
					writeToOutputFile(" " + key + "=\"" + meta.get(key) + "\"");
				}

				writeToOutputFile("/>");
			}
			
			writeToOutputFile(DocPart.HEADER.postElement());

			// Metadata
			writeToOutputFile(DocPart.SECTIONS.preElement());
			writeMetadataElement();
			
			// Sections
			for (BaseSection s : sections) {
				writeToOutputFile(DocPart.SECTION.preElement());
				writeToOutputFile(s.getDivOpenTag(this));
				
				if (s instanceof Section) {
					writeToOutputFile(DocPart.CHAPTERS.preElement());
					
					for (Chapter c : ((Section)s).getChapters()) {
						writeToOutputFile(DocPart.CHAPTER.preElement());
						writeToOutputFile(c.getDivOpenTag(this));
						
						String htmlFragment = c.getFragmentAsHtml();
						for (PostProcessor pp : postProcessors) {
							StringBuffer out = new StringBuffer();
							pp.processFragment(c, htmlFragment, out, this);
							
							htmlFragment = out.toString();
						}
						
						writeToOutputFile(htmlFragment);
						writeDivCloseTag();
						
						writeToOutputFile(DocPart.CHAPTER.postElement());
					}
					
					writeToOutputFile(DocPart.CHAPTERS.postElement());
					writeDivCloseTag();
				} else if (s instanceof PseudoSection) {
					writeToOutputFile(((PseudoSection)s).getSectionHandler().getSectionAsHtml(sections, this));
					writeDivCloseTag();
				} else if (s instanceof MetaSection) {
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
}
