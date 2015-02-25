package net.toften.docmaker.handler.standard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.toften.docmaker.handler.AssemblyHandlerAdapter;
import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.headersections.HeaderSection;
import net.toften.docmaker.pseudosections.PseudoSection;
import net.toften.docmaker.toc.Chapter;
import net.toften.docmaker.toc.ChapterSection;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.SectionType;

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
		postProcessors.add(new net.toften.docmaker.postprocessors.AdjustImageHrefPostProcessor());
		postProcessors.add(new net.toften.docmaker.postprocessors.ApplyKeyValue());
	}
	
	@Override
	public void endDocument() throws SAXException {
		for (Section s : getSections()) {
			if (s.getSectionType() == SectionType.CONTENTS_SECTION) {
				for (Chapter c : ((ChapterSection)s).getChapters()) {
					c.runPostProcessors(postProcessors, this, true);
				}
			}
		}
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
		getCurrentMetaSection().addElement(key, (String)getMetaData().get(key));
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
}
