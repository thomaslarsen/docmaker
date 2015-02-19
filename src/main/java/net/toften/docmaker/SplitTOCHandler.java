package net.toften.docmaker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.handler.standard.BaseSection;
import net.toften.docmaker.handler.standard.ContentSection;
import net.toften.docmaker.handler.standard.MetaSection;
import net.toften.docmaker.handler.standard.TOCChapter;
import net.toften.docmaker.headersections.HeaderSection;
import net.toften.docmaker.maven.DocMakerMojo;
import net.toften.docmaker.postprocessors.HeaderIncrementPostProcessor;
import net.toften.docmaker.postprocessors.InjectHeaderIdPostProcessor;
import net.toften.docmaker.postprocessors.PostProcessor;
import net.toften.docmaker.pseudosections.PseudoSection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SplitTOCHandler extends DefaultAssemblyHandler {
	
	private List<BaseSection> sections = new LinkedList<BaseSection>();
	private List<HeaderSection> headerSections = new LinkedList<HeaderSection>();
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
			currentSection = new ContentSection(getCurrentSectionName(), getCurrentSectionLevel(), isCurrentSectionRotated());
			sections.add(currentSection);
		}
	}
	
	@Override
	protected void handleMetaSectionElement(Attributes attributes)
			throws IOException, SAXException {
		super.handleMetaSectionElement(attributes);
		
		currentSection = new MetaSection(getCurrentSectionName(), isCurrentSectionRotated());
		sections.add(currentSection);
	}
	
	@Override
	protected void handleUnknownElement(DocPart dp, Attributes attributes) throws Exception {
		if (dp == DocPart.PSECTION) {
			handlePseudoSectionElement(attributes);
		} else if (dp == DocPart.POSTPROC) {
			handlePostProcessorElement(attributes);
		} else if (dp == DocPart.HSECTION) {
			handleHeaderSectionElement(attributes);
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

	protected void handleHeaderSectionElement(Attributes attributes) throws Exception {
		String hSectionHandlerClassname = attributes.getValue("classname");
		
		headerSections.add(new HeaderSection(hSectionHandlerClassname, attributes));
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
	protected String getFragmentAsHTML(Repo repo, String fragmentName, int chapterLevelOffset, String config) throws Exception {
		if (currentSection instanceof ContentSection) {
			// Section section, String fragmentName, String config, AssemblyHandler handler, Repo repo, int chapterLevelOffset, boolean isRotated
			((ContentSection)currentSection).addChapter(fragmentName, config, this, repo, chapterLevelOffset, isRotateCurrentChapter());
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
			
			/*
			 * Write header sections
			 */
			for (HeaderSection hSection : headerSections) {
				writeToOutputFile(hSection.getSectionHandler().getSectionAsHtml(sections, this));
			}
			
			writeToOutputFile(DocPart.HEADER.postElement());

			// Metadata
			writeToOutputFile(DocPart.SECTIONS.preElement());
			writeMetadataElement();
			
			// Sections
			for (BaseSection s : sections) {
				writeToOutputFile(DocPart.SECTION.preElement());
				writeToOutputFile(s.getDivOpenTag(this));
				
				if (s instanceof ContentSection) {
					writeToOutputFile(DocPart.CHAPTERS.preElement());
					
					for (TOCChapter c : ((ContentSection)s).getChapters()) {
						writeToOutputFile(DocPart.CHAPTER.preElement());
//						writeToOutputFile(c.getDivOpenTag(this));
						
						String htmlFragment = c.getAsHtml();
						for (PostProcessor pp : postProcessors) {
							StringBuffer out = new StringBuffer();
							pp.processFragment(c, htmlFragment, out, this);
							
							htmlFragment = out.toString();
						}
						
						writeToOutputFile(htmlFragment);
//						writeDivCloseTag();
						
						writeToOutputFile(DocPart.CHAPTER.postElement());
					}
					
					writeToOutputFile(DocPart.CHAPTERS.postElement());
				} else if (s instanceof PseudoSection) {
					writeToOutputFile(((PseudoSection)s).getSectionHandler().getSectionAsHtml(sections, this));
				} else if (s instanceof MetaSection) {
					for (String[] e : ((MetaSection)s).getElements()) {
						writeElement(e[0], e[1]);
					}
				}
				
				// Only write the close tag, if we wrote the open tag
				if (s.getDivOpenTag(this) != null)
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
