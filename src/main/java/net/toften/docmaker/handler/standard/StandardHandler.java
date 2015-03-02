package net.toften.docmaker.handler.standard;

import java.util.LinkedList;
import java.util.List;

import net.toften.docmaker.handler.AssemblyHandlerAdapter;
import net.toften.docmaker.handler.Repo;
import net.toften.docmaker.headersections.HeaderSection;
import net.toften.docmaker.pseudosections.PseudoSection;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.Section;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class StandardHandler extends AssemblyHandlerAdapter {
	private List<Section> sections = new LinkedList<Section>();
	private List<GeneratedSection> headerSections = new LinkedList<GeneratedSection>();

	private String currentFragmentName;
	private Repo currentChapterRepo;

	public StandardHandler() {
		super();
		
		getPostProcessors().add(new net.toften.docmaker.postprocessors.HeaderIncrementPostProcessor());
		getPostProcessors().add(new net.toften.docmaker.postprocessors.InjectHeaderIdPostProcessor());
		getPostProcessors().add(new net.toften.docmaker.postprocessors.AdjustImageHrefPostProcessor());
		getPostProcessors().add(new net.toften.docmaker.postprocessors.ApplyKeyValue());
	}
	
	@Override
	public void endDocument() throws SAXException {
		/*
		 * Run all the postprocessors for the document
		 * 
		 * Each postprocessor will be run over each chapter.
		 * This is done in the order of the chapters
		 */
		runPostProcessors(true);
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
		if (attributes.getValue(CHAPTER_FRAGMENT) == null)
			throw new SAXException("Chapter fragment attribute not specified");

		if (attributes.getValue(CHAPTER_REPO) == null)
			throw new SAXException("Chapter repo attribute not specified");

		currentFragmentName = attributes.getValue(CHAPTER_FRAGMENT);
		currentChapterRepo = getRepos().get(attributes.getValue(CHAPTER_REPO));

		int chapterLevelOffset = attributes.getValue(CHAPTER_LEVEL) == null ? 0 : Integer.valueOf(attributes.getValue(CHAPTER_LEVEL));
		String chapterConfig = attributes.getValue(CHAPTER_CONFIG);
		boolean chapterRotate = attributes.getValue(CHAPTER_ROTATE) != null;
		
		getCurrentContentSection().addChapter(currentFragmentName, chapterConfig, this, currentChapterRepo, chapterLevelOffset, chapterRotate);
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
