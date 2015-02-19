package net.toften.docmaker.handler.standard;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.toften.docmaker.DocPart;
import net.toften.docmaker.handler.AssemblyHandlerAdapter;
import net.toften.docmaker.handler.InterimFileHandler;
import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.headersections.HeaderSection;
import net.toften.docmaker.pseudosections.PseudoSection;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.ElementsSection;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.SectionType;
import net.toften.docmaker.toc.TOC;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class StandardHandler extends AssemblyHandlerAdapter {
	private List<Section> sections = new LinkedList<Section>();
	private List<GeneratedSection> headerSections = new LinkedList<GeneratedSection>();

	private String currentFragmentName;
	private Repo currentChapterRepo;

	public StandardHandler() {
		super();
		
		postProcessors.add(new net.toften.docmaker.postprocessors.HeaderIncrementPostProcessor());
		postProcessors.add(new net.toften.docmaker.postprocessors.InjectHeaderIdPostProcessor());
	}
	
	@Override
	public void endDocument() throws SAXException {
//		try {
			// Run postprocessors
			for (Section s : getSections()) {
				if (s.getSectionType() == SectionType.CONTENTS_SECTION) {
					for (Chapter c : ((ChapterSection)s).getChapters()) {
						c.runPostProcessors(postProcessors, this, true);
					}
				}
			}

			//writeInterimFile(this, this);
			
//			close();
//		} catch (IOException e) {
//			throw new SAXException("Outfile could not be closed", e);
//		}
	}
	
	@Override
	protected void handleMetaSectionElement(Attributes attributes) {
		sections.add(new MetaSection(getCurrentSectionName(), isCurrentSectionRotated()));
	}
	
	@Override
	protected void handleContentSectionElement(Attributes attributes) throws Exception {
		if (attributes.getValue(SECTION_LEVEL) == null)
			throw new SAXException("Section level attribute not specified");
		
		sections.add(new ContentSection(getCurrentSectionName(), getCurrentSectionLevel(), isCurrentSectionRotated()));
	}
	
	@Override
	protected void handlePseudoSection(Attributes attributes) throws Exception {
		String pseudoSectionClassname = attributes.getValue(SECTION_CLASSNAME);
		
		sections.add(new PseudoSection(getCurrentSectionName(), pseudoSectionClassname, attributes, isCurrentSectionRotated()));
	}
	
	@Override
	protected void handleHeaderSection(Attributes attributes) throws Exception {
		String pseudoSectionClassname = attributes.getValue(SECTION_CLASSNAME);
		
		headerSections.add(new HeaderSection(pseudoSectionClassname, attributes));
	}
	
	@Override
	protected void handleElementElement(Attributes attributes) {
		String key = attributes.getValue(ELEMENT_KEY);
		getCurrentMetaSection().addElement(key, metaData.get(key));
	}
	
	@Override
	protected void handleChapterElement(Attributes attributes) throws Exception {		
		if (attributes.getValue("fragment") == null)
			throw new SAXException("Chapter fragment attribute not specified");

		if (attributes.getValue("repo") == null)
			throw new SAXException("Chapter repo attribute not specified");

		currentFragmentName = attributes.getValue("fragment");
		currentChapterRepo = repos.get(attributes.getValue("repo"));

		int chapterLevelOffset = attributes.getValue("level") == null ? 0 : Integer.valueOf(attributes.getValue("level"));
		
		getCurrentContentSection().addChapter(currentFragmentName, attributes.getValue("config"), this, currentChapterRepo, chapterLevelOffset, attributes.getValue("rotate") != null);
	}

	@Override
	public List<GeneratedSection> getHeaderSections() {
		return headerSections;
	}

	@Override
	public List<Section> getSections() {
		return sections;
	}

	@Override
	public String getCurrentFragmentName() {
		return currentFragmentName;
	}

	@Override
	public Repo getCurrentRepo() {
		return currentChapterRepo;
	}
	
	protected ContentSection getCurrentContentSection() {
		if (sections.get(sections.size() - 1) instanceof ContentSection)
			return (ContentSection) sections.get(sections.size() - 1);
		else
			return null;
	}
	
	protected MetaSection getCurrentMetaSection() {
		if (sections.get(sections.size() - 1) instanceof MetaSection)
			return (MetaSection) sections.get(sections.size() - 1);
		else
			return null;
	}
	
	protected PseudoSection getCurrentPseudoSection() {
		if (sections.get(sections.size() - 1) instanceof PseudoSection)
			return (PseudoSection) sections.get(sections.size() - 1);
		else
			return null;
	}

	private static void writeInterimFile(TOC t, InterimFileHandler ifh) throws IOException {
		Map<String, String> metaData = t.getMetaData();
		Map<String, Map<String, String>> htmlMeta = t.getHtmlMeta();
		List<GeneratedSection> headerSections = t.getHeaderSections();
		List<Section> sections = t.getSections();
		
		ifh.writeToOutputFile(DocPart.DOCUMENT.preElement());
		
		ifh.writeToOutputFile(DocPart.HEADER.preElement());
		ifh.writeToOutputFile("<title>" + metaData.get(HEADER_TITLE) + "</title>\n");
		for (String htmlHeadKey : htmlMeta.keySet()) {
			ifh.writeToOutputFile("<" + htmlHeadKey);
			for (Entry<String, String> metaAttr : htmlMeta.get(htmlHeadKey).entrySet()) {
				ifh.writeToOutputFile(" " + metaAttr.getKey() + "=\"" + metaAttr.getValue() + "\"");
			}
			ifh.writeToOutputFile(" />\n");
		}
		
		for (GeneratedSection section : headerSections) {
			ifh.writeToOutputFile(DocPart.HSECTION.preElement());
			ifh.writeToOutputFile(section.getDivOpenTag(t));
			ifh.writeToOutputFile(section.getAsHtml(t));
			ifh.writeToOutputFile(section.getDivCloseTag());
			ifh.writeToOutputFile(DocPart.HSECTION.postElement());
		}
		ifh.writeToOutputFile(DocPart.HEADER.postElement());
		
		ifh.writeToOutputFile(DocPart.SECTIONS.preElement());
		
		// Write document metadata
		ifh.writeToOutputFile("<div class=\"metadata\">\n");
		for (Map.Entry<String, String> m : metaData.entrySet()) {
			ifh.writeToOutputFile("<div class=\"meta\" key=\"" + m.getKey() + "\">" + m.getValue() + "</div>\n");
		}
		ifh.writeToOutputFile("</div>\n");

		for (Section section : sections) {
			switch (section.getSectionType()) {
			case CONTENTS_SECTION:
				ifh.writeToOutputFile(DocPart.SECTION.preElement());
				writeContentSection((ChapterSection)section, t, ifh);
				ifh.writeToOutputFile(DocPart.SECTION.postElement());
				break;

			case META_SECTION:
				ifh.writeToOutputFile(DocPart.METASECTION.preElement());
				ifh.writeToOutputFile(section.getDivOpenTag(t));
				writeMetaElements((ElementsSection)section, t, ifh);
				ifh.writeToOutputFile(DocPart.METASECTION.postElement());
				ifh.writeToOutputFile(section.getDivCloseTag());
				break;
				
			case PSEUDO_SECTION:
				ifh.writeToOutputFile(DocPart.PSECTION.preElement());
				writePseudoSection((GeneratedSection)section, t, ifh);
				ifh.writeToOutputFile(DocPart.PSECTION.postElement());
				break;
			}
		}
		ifh.writeToOutputFile(DocPart.SECTIONS.postElement());
		
		ifh.writeToOutputFile(DocPart.DOCUMENT.postElement());
	}

	private static void writeContentSection(ChapterSection section, TOC t, InterimFileHandler ifh) throws IOException {
		ifh.writeToOutputFile(section.getDivOpenTag(t));
		ifh.writeToOutputFile(DocPart.CHAPTERS.preElement());
		for (Chapter c : section.getChapters()) {
			ifh.writeToOutputFile(DocPart.CHAPTER.preElement());
			writeChapter(c, t, ifh);
			ifh.writeToOutputFile(DocPart.CHAPTER.postElement());
		}
		ifh.writeToOutputFile(DocPart.CHAPTERS.postElement());
		// A contents section might also contain elements
		writeMetaElements(section, t, ifh);
		
		ifh.writeToOutputFile(section.getDivCloseTag());
	}
	
	private static void writeChapter(Chapter c, TOC t, InterimFileHandler ifh) throws IOException {
		ifh.writeToOutputFile(c.getDivOpenTag(t));
		String htmlFragment = c.getAsHtml();

		ifh.writeToOutputFile(htmlFragment);
		ifh.writeToOutputFile(c.getDivCloseTag());
	}

	private static void writeMetaElements(ElementsSection section, TOC t, InterimFileHandler ifh) throws IOException {
		for (String[] e : section.getElements()) {
			ifh.writeToOutputFile(DocPart.ELEMENT.preElement());
			ifh.writeToOutputFile("<div key=\"" + e[0] + "\">" + e[1] + "</div>\n");
			ifh.writeToOutputFile(DocPart.ELEMENT.postElement());
		}
	}
	
	private static void writePseudoSection(GeneratedSection section, TOC t, InterimFileHandler ifh) throws IOException {
		ifh.writeToOutputFile(section.getDivOpenTag(t));
		ifh.writeToOutputFile(section.getAsHtml(t));
		ifh.writeToOutputFile(section.getDivCloseTag());
	}
}
