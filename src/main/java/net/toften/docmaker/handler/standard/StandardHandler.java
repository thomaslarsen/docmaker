package net.toften.docmaker.handler.standard;

import java.util.LinkedList;
import java.util.List;

import net.toften.docmaker.handler.AssemblyHandler;
import net.toften.docmaker.handler.AssemblyHandlerAdapter;
import net.toften.docmaker.toc.GeneratedSection;
import net.toften.docmaker.toc.Section;
import net.toften.docmaker.toc.TOC;

import org.xml.sax.Attributes;

/**
 * This is the standard implementation of the {@link AssemblyHandler}.
 * <p>
 * It will parse the TOC XML and implement the {@link TOC} interface.
 * <p>
 * After the TOC has been processed, it will run the following {@link net.toften.docmaker.postprocessors.PostProcessor}s:
 * <ul>
 * <li>{@link net.toften.docmaker.postprocessors.HeaderIncrementPostProcessor}</li>
 * <li>{@link net.toften.docmaker.postprocessors.InjectHeaderIdPostProcessor}</li>
 * <li>{@link net.toften.docmaker.postprocessors.AdjustImageHrefPostProcessor}</li>
 * <li>{@link net.toften.docmaker.postprocessors.ApplyKeyValue}</li>
 * </ul>
 * 
 * @author thomaslarsen
 *
 */
public class StandardHandler extends AssemblyHandlerAdapter implements TOC, AssemblyHandler {
	private List<Section> sections = new LinkedList<Section>();
	private List<GeneratedSection> headerSections = new LinkedList<GeneratedSection>();

	public StandardHandler() {
		super();
		
		getPostProcessors().add(new net.toften.docmaker.postprocessors.HeaderIncrementPostProcessor());
		getPostProcessors().add(new net.toften.docmaker.postprocessors.InjectHeaderIdPostProcessor());
		getPostProcessors().add(new net.toften.docmaker.postprocessors.AdjustImageHrefPostProcessor());
		getPostProcessors().add(new net.toften.docmaker.postprocessors.ApplyKeyValue());
	}
	
	@Override
	public void endDocument() {
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
		sections.add(new ContentSection(getCurrentSectionName(), getCurrentSectionLevel(), isCurrentSectionRotated()));
	}
	
	@Override
	protected void handlePseudoSection(Attributes attributes) throws Exception {
		sections.add(new PseudoSection(getCurrentSectionName(), attributes.getValue(SECTION_CLASSNAME), attributes, isCurrentSectionRotated()));
	}
	
	@Override
	protected void handleHeaderSection(Attributes attributes) throws Exception {
		headerSections.add(new HeaderSection(attributes.getValue(SECTION_CLASSNAME), attributes));
	}
	
	@Override
	protected void handleElementElement(Attributes attributes) {
		String key = attributes.getValue(ELEMENT_KEY);
		getCurrentMetaSection().addElement(key, (String)getMetaData().get(key));
	}
	
	@Override
	protected void handleChapterElement(Attributes attributes) throws Exception {
		String currentFragmentName	= attributes.getValue(CHAPTER_FRAGMENT);
		String fragmentRepo			= attributes.getValue(CHAPTER_REPO);
		String fragmentLevel		= attributes.getValue(CHAPTER_LEVEL);
		String chapterConfig		= attributes.getValue(CHAPTER_CONFIG);
		boolean chapterRotate		= attributes.getValue(CHAPTER_ROTATE) != null;
		
		if (currentFragmentName == null)
			throw new IllegalArgumentException("Chapter fragment attribute not specified");

		if (fragmentRepo == null)
			throw new IllegalArgumentException("Chapter repo attribute not specified");

		if (!getRepos().containsKey(fragmentRepo))
			throw new IllegalArgumentException("Chapter repo " + fragmentRepo + " does not exist");
		
		int chapterLevelOffset = fragmentLevel == null ? 0 : Integer.valueOf(fragmentLevel);
		
		getCurrentContentSection().addChapter(currentFragmentName, chapterConfig, this, getRepos().get(fragmentRepo), chapterLevelOffset, chapterRotate);
	}

	@Override
	public List<GeneratedSection> getHeaderSections() {
		return headerSections;
	}

	@Override
	public List<Section> getSections() {
		return sections;
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
